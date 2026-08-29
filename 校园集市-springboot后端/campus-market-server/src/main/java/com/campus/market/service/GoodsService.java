package com.campus.market.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.market.common.PageResult;
import com.campus.market.dto.GoodsPublishDTO;
import com.campus.market.entity.Goods;

import java.util.Map;

/**
 * 闲置商品服务
 */
public interface GoodsService extends IService<Goods> {

    /** 商品列表：分类筛选 + 关键词搜索，只在售 */
    PageResult<Goods> list(Integer categoryId, String keyword, int page, int size);

    /** 商品详情（服务端累计 view_count，附卖家信息） */
    Goods detail(Long id);

    /** 发布商品（信用分 <60 限制发布） */
    Goods publish(Long userId, GoodsPublishDTO dto);

    /** 我的发布 */
    PageResult<Goods> my(Long userId, Integer status, int page, int size);

    /** 下架（仅卖家本人，在售→下架） */
    void offShelf(Long userId, Long id);

    /** 重新上架（仅卖家本人，下架→在售） */
    void onShelf(Long userId, Long id);

    /** 收藏/取消收藏（切换式），返回 {favorited: true/false} */
    Map<String, Object> toggleFavorite(Long userId, Long id);

    /** 我的收藏 */
    PageResult<Goods> favoriteMy(Long userId, int page, int size);
}
