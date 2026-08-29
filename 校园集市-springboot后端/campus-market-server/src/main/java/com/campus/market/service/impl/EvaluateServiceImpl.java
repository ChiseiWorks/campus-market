package com.campus.market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.market.common.BizException;
import com.campus.market.common.PageResult;
import com.campus.market.dto.EvaluateSubmitDTO;
import com.campus.market.entity.ErrandOrder;
import com.campus.market.entity.Evaluation;
import com.campus.market.entity.GoodsOrder;
import com.campus.market.mapper.ErrandOrderMapper;
import com.campus.market.mapper.EvaluationMapper;
import com.campus.market.mapper.GoodsOrderMapper;
import com.campus.market.service.EvaluateService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * 评价服务实现
 * 每单每人限一次：uk_order_from(order_type, order_id, from_user_id) 唯一约束兜底，
 * 重复提交按幂等返回成功（文档 5.5）
 */
@Service
@RequiredArgsConstructor
public class EvaluateServiceImpl extends ServiceImpl<EvaluationMapper, Evaluation> implements EvaluateService {

    private final GoodsOrderMapper goodsOrderMapper;
    private final ErrandOrderMapper errandOrderMapper;

    @Override
    public void submit(Long userId, EvaluateSubmitDTO dto) {
        if (dto.getToUserId().equals(userId)) {
            throw new BizException("不能评价自己");
        }
        // 校验订单存在、已完成、且双方都是交易当事人
        if (dto.getOrderType() == 1) {
            GoodsOrder order = goodsOrderMapper.selectById(dto.getOrderId());
            if (order == null) {
                throw new BizException("订单不存在");
            }
            if (order.getStatus() != 2) {
                throw new BizException("订单完成后才能评价");
            }
            checkParty(userId, dto.getToUserId(), order.getBuyerId(), order.getSellerId());
        } else if (dto.getOrderType() == 2) {
            ErrandOrder order = errandOrderMapper.selectById(dto.getOrderId());
            if (order == null) {
                throw new BizException("订单不存在");
            }
            if (order.getStatus() != 4) {
                throw new BizException("订单完成后才能评价");
            }
            checkParty(userId, dto.getToUserId(), order.getPublisherId(), order.getRunnerId());
        } else {
            throw new BizException("订单类型不正确");
        }
        Evaluation evaluation = new Evaluation();
        evaluation.setOrderType(dto.getOrderType());
        evaluation.setOrderId(dto.getOrderId());
        evaluation.setFromUserId(userId);
        evaluation.setToUserId(dto.getToUserId());
        evaluation.setScore(dto.getScore());
        evaluation.setTags(dto.getTags());
        evaluation.setContent(dto.getContent());
        try {
            save(evaluation);
        } catch (DuplicateKeyException e) {
            // 唯一约束兜底：已评价过，重复提交按幂等成功处理
        }
    }

    @Override
    public PageResult<Evaluation> ofUser(Long userId, int page, int size) {
        Page<Evaluation> p = page(new Page<>(page, size),
                new QueryWrapper<Evaluation>()
                        .eq("to_user_id", userId)
                        .orderByDesc("create_time"));
        return PageResult.of(p);
    }

    @Override
    public PageResult<Evaluation> my(Long userId, int page, int size) {
        Page<Evaluation> p = page(new Page<>(page, size),
                new QueryWrapper<Evaluation>()
                        .eq("from_user_id", userId)
                        .orderByDesc("create_time"));
        return PageResult.of(p);
    }

    /** 校验评价人与被评价人正好是订单双方 */
    private void checkParty(Long fromUserId, Long toUserId, Long partyA, Long partyB) {
        boolean fromIsParty = fromUserId.equals(partyA) || fromUserId.equals(partyB);
        boolean toIsParty = toUserId.equals(partyA) || toUserId.equals(partyB);
        if (!fromIsParty) {
            throw new BizException("只有交易双方可以评价");
        }
        if (!toIsParty) {
            throw new BizException("评价对象不是本单交易对方");
        }
    }
}
