package com.campus.market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.market.common.BizException;
import com.campus.market.common.OrderNoUtil;
import com.campus.market.common.PageResult;
import com.campus.market.dto.OrderCreateDTO;
import com.campus.market.entity.Goods;
import com.campus.market.entity.GoodsOrder;
import com.campus.market.mapper.GoodsMapper;
import com.campus.market.mapper.GoodsOrderMapper;
import com.campus.market.service.GoodsOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 闲置交易订单服务实现
 *
 * 并发约束（文档 3.5）：同一商品同一时刻只允许一条进行中订单，
 * 由"下单时 CAS 锁定商品状态（1在售→2已售出）"在同一事务内保证。
 */
@Service
@RequiredArgsConstructor
public class GoodsOrderServiceImpl extends ServiceImpl<GoodsOrderMapper, GoodsOrder> implements GoodsOrderService {

    private final GoodsMapper goodsMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GoodsOrder create(Long buyerId, OrderCreateDTO dto) {
        Goods goods = goodsMapper.selectById(dto.getGoodsId());
        if (goods == null) {
            throw new BizException("商品不存在或已删除");
        }
        if (goods.getStatus() != 1) {
            throw new BizException("商品当前不在售");
        }
        if (goods.getUserId().equals(buyerId)) {
            throw new BizException("不能购买自己发布的商品");
        }
        // CAS 锁定商品：UPDATE ... WHERE status=1，rows=0 说明刚被别人买走（禁止先查后改）
        int rows = goodsMapper.lockForOrder(goods.getId());
        if (rows == 0) {
            throw new BizException("手慢了，商品刚被其他同学买走");
        }
        // 生成订单：成交价做快照，防商品改价影响已生成订单
        GoodsOrder order = new GoodsOrder();
        order.setOrderNo(OrderNoUtil.generate(buyerId));
        order.setGoodsId(goods.getId());
        order.setBuyerId(buyerId);
        order.setSellerId(goods.getUserId());
        order.setDealPrice(goods.getPrice());
        order.setRemark(dto.getRemark());
        order.setStatus(0); // 待卖家确认
        save(order);
        return order;
    }

    @Override
    public PageResult<GoodsOrder> my(Long userId, String role, Integer status, int page, int size) {
        QueryWrapper<GoodsOrder> qw = new QueryWrapper<>();
        if ("seller".equals(role)) {
            qw.eq("seller_id", userId);
        } else {
            qw.eq("buyer_id", userId);
        }
        if (status != null) {
            qw.eq("status", status);
        }
        qw.orderByDesc("create_time");
        return PageResult.of(page(new Page<>(page, size), qw));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finish(Long userId, Long orderId) {
        GoodsOrder order = requireParticipant(userId, orderId);
        if (order.getStatus() != 0 && order.getStatus() != 1) {
            throw new BizException("当前状态不能确认完成");
        }
        order.setStatus(2); // 已完成
        LocalDateTime now = LocalDateTime.now();
        order.setFinishTime(now);
        if (order.getConfirmTime() == null) {
            order.setConfirmTime(now);
        }
        updateById(order);
        // TODO: 可选——完成后双方信用分 +2（跑腿单已实现，闲置单是否加分待产品确认）
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long userId, Long orderId, String reason) {
        GoodsOrder order = requireParticipant(userId, orderId);
        if (order.getStatus() != 0 && order.getStatus() != 1) {
            throw new BizException("当前状态不能取消");
        }
        order.setStatus(3); // 已取消
        order.setCancelBy(userId);
        order.setCancelReason(reason);
        updateById(order);
        // 恢复商品为在售（仅当商品仍处于锁定状态）
        goodsMapper.restoreOnSale(order.getGoodsId());
    }

    /** 取订单并校验当前用户是买卖双方之一 */
    private GoodsOrder requireParticipant(Long userId, Long orderId) {
        GoodsOrder order = getById(orderId);
        if (order == null) {
            throw new BizException("订单不存在");
        }
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BizException("只有订单双方可以操作");
        }
        return order;
    }
}
