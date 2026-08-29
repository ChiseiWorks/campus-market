package com.campus.market.controller;

import com.campus.market.common.PageResult;
import com.campus.market.common.Result;
import com.campus.market.dto.GoodsPublishDTO;
import com.campus.market.entity.Goods;
import com.campus.market.service.GoodsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 闲置商品接口（接口文档 5.2）
 */
@RestController
@RequestMapping("/api/goods")
@RequiredArgsConstructor
public class GoodsController {

    private final GoodsService goodsService;

    /** 商品列表：categoryId / keyword / page */
    @GetMapping("/list")
    public Result<PageResult<Goods>> list(@RequestParam(required = false) Integer categoryId,
                                          @RequestParam(required = false) String keyword,
                                          @RequestParam(defaultValue = "1") Integer page,
                                          @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(goodsService.list(categoryId, keyword, page, size));
    }

    /** 我的发布 */
    @GetMapping("/my")
    public Result<PageResult<Goods>> my(@RequestAttribute Long userId,
                                        @RequestParam(required = false) Integer status,
                                        @RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(goodsService.my(userId, status, page, size));
    }

    /** 我的收藏 */
    @GetMapping("/favorite/my")
    public Result<PageResult<Goods>> favoriteMy(@RequestAttribute Long userId,
                                                @RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(goodsService.favoriteMy(userId, page, size));
    }

    /** 商品详情（服务端累计 view_count） */
    @GetMapping("/{id}")
    public Result<Goods> detail(@PathVariable Long id) {
        return Result.ok(goodsService.detail(id));
    }

    /** 发布商品 */
    @PostMapping("/publish")
    public Result<Goods> publish(@RequestAttribute Long userId, @Valid @RequestBody GoodsPublishDTO dto) {
        return Result.ok(goodsService.publish(userId, dto));
    }

    /** 下架 */
    @PostMapping("/{id}/offshelf")
    public Result<Void> offShelf(@RequestAttribute Long userId, @PathVariable Long id) {
        goodsService.offShelf(userId, id);
        return Result.ok();
    }

    /** 重新上架 */
    @PostMapping("/{id}/onshelf")
    public Result<Void> onShelf(@RequestAttribute Long userId, @PathVariable Long id) {
        goodsService.onShelf(userId, id);
        return Result.ok();
    }

    /** 收藏 / 取消收藏（切换式，返回最新状态） */
    @PostMapping("/{id}/favorite")
    public Result<Map<String, Object>> favorite(@RequestAttribute Long userId, @PathVariable Long id) {
        return Result.ok(goodsService.toggleFavorite(userId, id));
    }
}
