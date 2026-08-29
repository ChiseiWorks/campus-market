package com.campus.market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.market.common.BizException;
import com.campus.market.common.OrderNoUtil;
import com.campus.market.common.PageResult;
import com.campus.market.dto.ErrandPublishDTO;
import com.campus.market.entity.ErrandOrder;
import com.campus.market.entity.SchoolLocation;
import com.campus.market.entity.User;
import com.campus.market.enums.ErrandStatusEnum;
import com.campus.market.mapper.ErrandOrderMapper;
import com.campus.market.mapper.SchoolLocationMapper;
import com.campus.market.mapper.UserMapper;
import com.campus.market.service.ErrandService;
import com.campus.market.service.ErrandStateMachine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 跑腿订单服务实现
 * 状态推进统一走 ErrandStateMachine；抢单走原子 CAS（accept）
 */
@Service
@RequiredArgsConstructor
public class ErrandServiceImpl extends ServiceImpl<ErrandOrderMapper, ErrandOrder> implements ErrandService {

    private final ErrandStateMachine stateMachine;
    private final UserMapper userMapper;
    private final SchoolLocationMapper schoolLocationMapper;

    @Override
    public ErrandOrder publish(Long userId, ErrandPublishDTO dto) {
        User user = requireUser(userId);
        // 发布校验（文档 1.3 流转规则表）：已认证；信用分 <60 限制发布
        if (user.getAuthStatus() == null || user.getAuthStatus() != 2) {
            throw new BizException("请先完成校园认证，再发布跑腿单");
        }
        if (user.getCreditScore() != null && user.getCreditScore() < 60) {
            throw new BizException("信用分不足 60，暂时不能发布跑腿单");
        }
        ErrandOrder order = new ErrandOrder();
        order.setOrderNo(OrderNoUtil.generate(userId));
        order.setPublisherId(userId);
        order.setType(dto.getType());
        order.setTitle(dto.getTitle());
        order.setPickupLocationId(dto.getPickupLocationId());
        order.setDeliveryLocationId(dto.getDeliveryLocationId());
        order.setPickupDetail(dto.getPickupDetail());
        // TODO: 取件码应加密存储（数据库设计文档敏感字段要求），骨架阶段明文存储 + 接口层脱敏
        order.setPickupCode(dto.getPickupCode());
        order.setGoodsDesc(dto.getGoodsDesc());
        order.setReward(dto.getReward());
        order.setExpectTime(dto.getExpectTime());
        order.setStatus(ErrandStatusEnum.PENDING.getCode());
        order.setVersion(0);
        save(order);
        return order;
    }

    @Override
    public PageResult<ErrandOrder> hall(Integer type, String sort, int page, int size) {
        QueryWrapper<ErrandOrder> qw = new QueryWrapper<>();
        qw.eq("status", ErrandStatusEnum.PENDING.getCode());
        if (type != null) {
            qw.eq("type", type);
        }
        // sort=reward 按悬赏金额排序，否则按发布时间最新
        if ("reward".equals(sort)) {
            qw.orderByDesc("reward");
        } else {
            qw.orderByDesc("create_time");
        }
        Page<ErrandOrder> p = page(new Page<>(page, size), qw);
        // 接单大厅是"路人视角"：取件码一律不返回（脱敏规则）
        p.getRecords().forEach(o -> o.setPickupCode(null));
        // 补充地点名展示
        p.getRecords().forEach(this::fillLocationNames);
        return PageResult.of(p);
    }

    /**
     * 订单详情：按查看者角色 + 订单状态在服务端脱敏（不靠前端隐藏）
     *
     * 脱敏规则（文档第 2 节）：
     * - 取件码：发单人订单未结束时可见；本单跑男仅 ACCEPTED/DELIVERING 可见；
     *           其他人不可见；订单结束（完成/取消）后任何人都不返回
     * - 联系方式：订单进行中，发单人可见跑男手机号、跑男可见发单人手机号；
     *           路人不可见；订单结束后双方都不返回
     */
    @Override
    public ErrandOrder detail(Long id, Long viewerId) {
        ErrandOrder order = getById(id);
        if (order == null) {
            throw new BizException("订单不存在");
        }
        boolean isPublisher = order.getPublisherId().equals(viewerId);
        boolean isRunner = viewerId != null && viewerId.equals(order.getRunnerId());
        int status = order.getStatus();
        boolean ended = status == ErrandStatusEnum.FINISHED.getCode()
                || status == ErrandStatusEnum.CANCELLED.getCode();

        // 填充双方昵称/头像（始终可见，方便展示）
        User publisher = userMapper.selectById(order.getPublisherId());
        if (publisher != null) {
            order.setPublisherNickname(publisher.getNickname());
            order.setPublisherAvatar(publisher.getAvatar());
            // 手机号：仅本单跑男在订单进行中可见
            if (isRunner && !ended) {
                order.setPublisherPhone(publisher.getPhone());
            }
        }
        if (order.getRunnerId() != null) {
            User runner = userMapper.selectById(order.getRunnerId());
            if (runner != null) {
                order.setRunnerNickname(runner.getNickname());
                order.setRunnerAvatar(runner.getAvatar());
                // 手机号：仅发单人在订单进行中可见
                if (isPublisher && !ended) {
                    order.setRunnerPhone(runner.getPhone());
                }
            }
        }
        // 取件码脱敏
        boolean pickupVisible = !ended && (isPublisher
                || (isRunner && (status == ErrandStatusEnum.ACCEPTED.getCode()
                || status == ErrandStatusEnum.DELIVERING.getCode())));
        if (!pickupVisible) {
            order.setPickupCode(null);
        }
        fillLocationNames(order);
        return order;
    }

    /**
     * 抢单（对应文档 3.5，并发安全核心考点）
     * 为什么不能"先查后改"：先 SELECT 判断 status=0 再 UPDATE，中间的时间窗口
     * 会造成一单两人接。UPDATE ... WHERE status=0 是单条原子语句，
     * MySQL 行锁保证并发下只有一个赢家。
     */
    @Override
    public void accept(Long id, Long userId) {
        User user = requireUser(userId);
        // 前置校验：已认证 + 跑男资格 + 信用分
        if (user.getAuthStatus() == null || user.getAuthStatus() != 2) {
            throw new BizException("请先完成校园认证");
        }
        if (user.getIsRunner() == null || user.getIsRunner() != 1) {
            throw new BizException("你还未认证为跑男，暂时不能接单");
        }
        if (user.getCreditScore() != null && user.getCreditScore() < 60) {
            throw new BizException("信用分不足 60，暂时不能接单");
        }
        ErrandOrder order = getById(id);
        if (order == null) {
            throw new BizException("订单不存在");
        }
        if (order.getPublisherId().equals(userId)) {
            throw new BizException("不能接自己发布的单");
        }
        // 防囤单：进行中的单最多 3 单
        int doing = baseMapper.countDoingByRunner(userId);
        if (doing >= 3) {
            throw new BizException("你手头的单太多了，先完成再接");
        }
        // 原子抢单：只有 status 仍是 0 才能更新成功
        int rows = baseMapper.acceptOrder(id, userId);
        if (rows == 0) {
            throw new BizException("手慢了，这单刚被抢走");
        }
    }

    @Override
    public void deliver(Long id, Long userId) {
        stateMachine.transit(id, ErrandStatusEnum.DELIVERING, userId, null);
    }

    @Override
    public void arrive(Long id, Long userId) {
        stateMachine.transit(id, ErrandStatusEnum.ARRIVED, userId, null);
    }

    @Override
    public void confirm(Long id, Long userId) {
        stateMachine.transit(id, ErrandStatusEnum.FINISHED, userId, null);
    }

    @Override
    public void cancel(Long id, Long userId, String reason) {
        stateMachine.transit(id, ErrandStatusEnum.CANCELLED, userId, reason);
    }

    @Override
    public void dispute(Long id, Long userId, String reason) {
        stateMachine.transit(id, ErrandStatusEnum.DISPUTED, userId, reason);
    }

    @Override
    public PageResult<ErrandOrder> myPublish(Long userId, Integer status, int page, int size) {
        QueryWrapper<ErrandOrder> qw = new QueryWrapper<>();
        qw.eq("publisher_id", userId);
        if (status != null) {
            qw.eq("status", status);
        }
        qw.orderByDesc("create_time");
        Page<ErrandOrder> p = page(new Page<>(page, size), qw);
        // 发单人视角：订单结束后不再返回取件码（与详情脱敏规则一致）
        p.getRecords().forEach(o -> {
            if (o.getStatus() == ErrandStatusEnum.FINISHED.getCode()
                    || o.getStatus() == ErrandStatusEnum.CANCELLED.getCode()) {
                o.setPickupCode(null);
            }
            fillLocationNames(o);
        });
        return PageResult.of(p);
    }

    @Override
    public PageResult<ErrandOrder> myAccept(Long userId, Integer status, int page, int size) {
        QueryWrapper<ErrandOrder> qw = new QueryWrapper<>();
        qw.eq("runner_id", userId);
        if (status != null) {
            qw.eq("status", status);
        }
        qw.orderByDesc("accept_time");
        Page<ErrandOrder> p = page(new Page<>(page, size), qw);
        // 跑男视角：仅 ACCEPTED/DELIVERING 状态可见取件码
        p.getRecords().forEach(o -> {
            if (o.getStatus() != ErrandStatusEnum.ACCEPTED.getCode()
                    && o.getStatus() != ErrandStatusEnum.DELIVERING.getCode()) {
                o.setPickupCode(null);
            }
            fillLocationNames(o);
        });
        return PageResult.of(p);
    }

    private User requireUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        return user;
    }

    /** 填充取/送地点名（展示用） */
    private void fillLocationNames(ErrandOrder order) {
        if (order.getPickupLocationId() != null) {
            SchoolLocation loc = schoolLocationMapper.selectById(order.getPickupLocationId());
            if (loc != null) {
                order.setPickupLocationName(loc.getName());
            }
        }
        if (order.getDeliveryLocationId() != null) {
            SchoolLocation loc = schoolLocationMapper.selectById(order.getDeliveryLocationId());
            if (loc != null) {
                order.setDeliveryLocationName(loc.getName());
            }
        }
    }
}
