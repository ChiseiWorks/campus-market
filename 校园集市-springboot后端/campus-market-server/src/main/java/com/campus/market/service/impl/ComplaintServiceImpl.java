package com.campus.market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.market.common.BizException;
import com.campus.market.common.PageResult;
import com.campus.market.dto.ComplaintSubmitDTO;
import com.campus.market.entity.Complaint;
import com.campus.market.entity.ErrandOrder;
import com.campus.market.entity.GoodsOrder;
import com.campus.market.mapper.ComplaintMapper;
import com.campus.market.mapper.ErrandOrderMapper;
import com.campus.market.mapper.GoodsOrderMapper;
import com.campus.market.service.ComplaintService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 投诉服务实现
 */
@Service
@RequiredArgsConstructor
public class ComplaintServiceImpl extends ServiceImpl<ComplaintMapper, Complaint> implements ComplaintService {

    private final GoodsOrderMapper goodsOrderMapper;
    private final ErrandOrderMapper errandOrderMapper;

    @Override
    public void submit(Long userId, ComplaintSubmitDTO dto) {
        if (dto.getDefendantId().equals(userId)) {
            throw new BizException("不能投诉自己");
        }
        // 校验订单存在且当前用户是交易当事人
        if (dto.getOrderType() == 1) {
            GoodsOrder order = goodsOrderMapper.selectById(dto.getOrderId());
            if (order == null) {
                throw new BizException("订单不存在");
            }
            if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
                throw new BizException("只有交易双方可以发起投诉");
            }
        } else if (dto.getOrderType() == 2) {
            ErrandOrder order = errandOrderMapper.selectById(dto.getOrderId());
            if (order == null) {
                throw new BizException("订单不存在");
            }
            boolean isParty = order.getPublisherId().equals(userId)
                    || userId.equals(order.getRunnerId());
            if (!isParty) {
                throw new BizException("只有交易双方可以发起投诉");
            }
        } else {
            throw new BizException("订单类型不正确");
        }
        Complaint complaint = new Complaint();
        complaint.setOrderType(dto.getOrderType());
        complaint.setOrderId(dto.getOrderId());
        complaint.setPlaintiffId(userId);
        complaint.setDefendantId(dto.getDefendantId());
        complaint.setType(dto.getType());
        complaint.setContent(dto.getContent());
        complaint.setEvidence(dto.getEvidence());
        complaint.setStatus(0); // 待处理
        save(complaint);
    }

    @Override
    public PageResult<Complaint> my(Long userId, int page, int size) {
        // 我发起的 + 我被投诉的
        Page<Complaint> p = page(new Page<>(page, size),
                new QueryWrapper<Complaint>()
                        .and(w -> w.eq("plaintiff_id", userId).or().eq("defendant_id", userId))
                        .orderByDesc("create_time"));
        return PageResult.of(p);
    }

    @Override
    public Complaint detail(Long id, Long userId) {
        Complaint complaint = getById(id);
        if (complaint == null) {
            throw new BizException("投诉记录不存在");
        }
        if (!complaint.getPlaintiffId().equals(userId) && !complaint.getDefendantId().equals(userId)) {
            throw new BizException("只有投诉双方可以查看处理进度");
        }
        return complaint;
    }

    /**
     * 跑腿申诉自动生成投诉单（状态机 DISPUTED 副作用调用，同一事务）
     */
    @Override
    public void createFromDispute(ErrandOrder order, Long operatorId, String reason) {
        Complaint complaint = new Complaint();
        complaint.setOrderType(2); // 跑腿
        complaint.setOrderId(order.getId());
        complaint.setPlaintiffId(operatorId);        // 申诉人（发单人）
        complaint.setDefendantId(order.getRunnerId()); // 被申诉人（跑男）
        complaint.setType(5); // 其他：申诉自动生成，具体类型由管理员仲裁时归类
        complaint.setContent("【跑腿订单申诉】订单号 " + order.getOrderNo()
                + (reason != null && !reason.isEmpty() ? ("，原因：" + reason) : ""));
        complaint.setStatus(0); // 待处理，进入仲裁队列
        save(complaint);
    }
}
