package com.campus.market.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.market.common.PageResult;
import com.campus.market.dto.OrderCreateDTO;
import com.campus.market.entity.GoodsOrder;

/**
 * 闲置交易订单服务
 */
public interface GoodsOrderService extends IService<GoodsOrder> {

    /** 立即交易：校验商品在售 → CAS 锁定商品 → 生成订单（金额快照），同一事务 */
    GoodsOrder create(Long buyerId, OrderCreateDTO dto);

    /** 我的闲置订单：role=buyer|seller */
    PageResult<GoodsOrder> my(Long userId, String role, Integer status, int page, int size);

    /** 确认完成面交（买卖双方均可操作） */
    void finish(Long userId, Long orderId);

    /** 取消订单（买卖双方均可操作，取消后恢复商品在售） */
    void cancel(Long userId, Long orderId, String reason);
}
