package com.campus.market.admin.service;

import com.campus.market.common.PageResult;
import com.campus.market.entity.Goods;

/**
 * 管理端：商品监管
 */
public interface AdminGoodsService {

    /** 商品列表：keyword 模糊匹配标题，status 过滤，item 附 sellerNickname */
    PageResult<Goods> list(String keyword, Integer status, int page, int size);

    /** 违规下架：status=4，原因存 goods.violation_reason */
    void takedown(Long id, String reason);
}
