package com.campus.market.admin.service;

import com.campus.market.common.PageResult;
import com.campus.market.entity.ErrandOrder;

/**
 * 管理端：跑腿订单监控
 */
public interface AdminErrandService {

    /**
     * 跑腿订单列表：item 附 publisherNickname + runnerNickname + abnormal
     * abnormal = 申诉中(status=6)，或 expect_time 已过且状态仍在 待接单/已接单/配送中
     */
    PageResult<ErrandOrder> list(Integer status, Integer type, String keyword, int page, int size);
}
