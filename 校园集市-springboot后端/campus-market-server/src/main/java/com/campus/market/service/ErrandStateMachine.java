package com.campus.market.service;

import com.campus.market.common.BizException;
import com.campus.market.entity.ErrandOrder;
import com.campus.market.enums.ErrandStatusEnum;
import com.campus.market.mapper.ErrandOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 跑腿单状态机（对应《跑腿状态机与接口设计》3.2 ~ 3.4，答辩核心考点）
 *
 * 所有状态变更必须走本类 transit() 统一入口：
 *   1. 校验流转合法性（显式流转表，杜绝"野流转"）
 *   2. 校验操作人身份
 *   3. 乐观锁 CAS 更新（WHERE status=旧状态 AND version=旧版本）
 *   4. 执行副作用（时间戳 / 信用分 / 投诉单 / 通知）
 *
 * 幂等设计：current == target 直接返回成功，重复点击不报错、不重复扣分。
 * 注意：抢单（PENDING→ACCEPTED）不走这里，走 ErrandService.accept 的原子 CAS SQL。
 */
@Service
@RequiredArgsConstructor
public class ErrandStateMachine {

    private final ErrandOrderMapper orderMapper;
    private final CreditService creditService;
    private final ComplaintService complaintService;
    private final MessageService messageService;

    /**
     * 统一状态推进方法
     *
     * @param orderId    订单ID
     * @param target     目标状态
     * @param operatorId 操作人
     * @param reason     原因（取消/申诉时必填）
     */
    @Transactional(rollbackFor = Exception.class)
    public void transit(Long orderId, ErrandStatusEnum target, Long operatorId, String reason) {
        ErrandOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException("订单不存在");
        }
        ErrandStatusEnum current = ErrandStatusEnum.of(order.getStatus());
        // 幂等：重复请求直接返回，视为成功
        if (current == target) {
            return;
        }
        if (!current.canTransitTo(target)) {
            throw new BizException("当前状态不允许该操作：" + current.getDesc());
        }
        // 身份校验（谁能在什么状态干什么事）
        checkPermission(order, target, operatorId);
        // 副作用（信用分 / 投诉单）：先于 CAS 执行，失败则整个事务回滚
        applySideEffectBefore(order, target, operatorId, reason);

        // 乐观锁更新：WHERE status=旧状态 AND version=旧版本，防并发脏写
        int rows = orderMapper.updateStatusWithLock(
                orderId, current.getCode(), target.getCode(), order.getVersion());
        if (rows == 0) {
            throw new BizException("手慢了，订单状态已变化，请刷新");
        }
        // CAS 成功后在同一事务内落副作用字段（时间戳 / 取消信息）
        ErrandOrder patch = buildPatch(orderId, order, target);
        if (patch != null) {
            orderMapper.updateById(patch);
        }
        // 推送状态变更通知（骨架阶段为日志实现）
        messageService.pushStatusChange(order, target);
    }

    /**
     * 权限校验（对应文档 3.3）
     */
    private void checkPermission(ErrandOrder order, ErrandStatusEnum target, Long operatorId) {
        boolean isPublisher = order.getPublisherId().equals(operatorId);
        boolean isRunner = operatorId.equals(order.getRunnerId());
        switch (target) {
            case ACCEPTED:
                // 抢单：跑男资格在 Service 层已校验，这里防"自己接自己的单"
                if (isPublisher) {
                    throw new BizException("不能接自己发布的单");
                }
                break;
            case DELIVERING:
            case ARRIVED:
                if (!isRunner) {
                    throw new BizException("只有本单接单人可以操作");
                }
                break;
            case FINISHED:
            case DISPUTED:
                if (!isPublisher) {
                    throw new BizException("只有本单发单人可以操作");
                }
                break;
            case CANCELLED:
                if (!isPublisher && !isRunner) {
                    throw new BizException("只有订单双方可以取消");
                }
                // 设计红线：配送中（DELIVERING）之后禁止单方面取消，
                // 由流转表保证到不了这里（DELIVERING 只能去 ARRIVED）
                break;
            default:
        }
    }

    /**
     * 副作用（对应文档 3.4）：信用分 + 敏感信息控制
     * 时间戳字段先写到 order 对象上，CAS 成功后由 buildPatch 落库
     */
    private void applySideEffectBefore(ErrandOrder order, ErrandStatusEnum target,
                                       Long operatorId, String reason) {
        LocalDateTime now = LocalDateTime.now();
        switch (target) {
            case ACCEPTED:
                // 防囤单：校验该跑男进行中的单不超过 3 单
                // （抢单实际走 acceptOrder CAS，此处为防御性保留）
                int doing = orderMapper.countDoingByRunner(operatorId);
                if (doing >= 3) {
                    throw new BizException("你手头的单太多了，先完成再接");
                }
                order.setRunnerId(operatorId);
                order.setAcceptTime(now);
                break;
            case DELIVERING:
                order.setDeliverTime(now);
                break;
            case ARRIVED:
                order.setArriveTime(now);
                break;
            case FINISHED:
                order.setFinishTime(now);
                // 完成订单双方信用分 +2（走流水，可追溯）
                creditService.change(order.getPublisherId(), 2, "跑腿订单顺利完成", 2, order.getId());
                creditService.change(order.getRunnerId(), 2, "跑腿订单顺利完成", 2, order.getId());
                break;
            case CANCELLED:
                order.setCancelBy(operatorId);
                order.setCancelReason(reason);
                // 已接单后跑男主动取消 = 爽约，扣 10 分
                if (operatorId.equals(order.getRunnerId())) {
                    creditService.change(order.getRunnerId(), -10, "接单后爽约", 2, order.getId());
                }
                break;
            case DISPUTED:
                // 申诉自动生成投诉单，进入仲裁队列
                complaintService.createFromDispute(order, operatorId, reason);
                break;
            default:
        }
    }

    /**
     * 构造副作用字段的增量更新（只更新非空字段，避免覆盖并发写入）
     */
    private ErrandOrder buildPatch(Long orderId, ErrandOrder order, ErrandStatusEnum target) {
        ErrandOrder patch = null;
        switch (target) {
            case DELIVERING:
                patch = new ErrandOrder();
                patch.setDeliverTime(order.getDeliverTime());
                break;
            case ARRIVED:
                patch = new ErrandOrder();
                patch.setArriveTime(order.getArriveTime());
                break;
            case FINISHED:
                patch = new ErrandOrder();
                patch.setFinishTime(order.getFinishTime());
                break;
            case CANCELLED:
                patch = new ErrandOrder();
                patch.setCancelBy(order.getCancelBy());
                patch.setCancelReason(order.getCancelReason());
                break;
            default:
        }
        if (patch != null) {
            patch.setId(orderId);
        }
        return patch;
    }
}
