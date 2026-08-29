package com.campus.market.controller;

import com.campus.market.common.PageResult;
import com.campus.market.common.Result;
import com.campus.market.dto.OrderCreateDTO;
import com.campus.market.dto.ReasonDTO;
import com.campus.market.entity.GoodsOrder;
import com.campus.market.service.GoodsOrderService;
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

/**
 * 闲置交易订单接口（接口文档 5.2）
 */
@RestController
@RequestMapping("/api/goods/order")
@RequiredArgsConstructor
public class GoodsOrderController {

    private final GoodsOrderService goodsOrderService;

    /** 立即交易：校验商品在售 → 生成订单（金额快照）→ 锁定商品，同一事务 */
    @PostMapping("/create")
    public Result<GoodsOrder> create(@RequestAttribute Long userId, @Valid @RequestBody OrderCreateDTO dto) {
        return Result.ok(goodsOrderService.create(userId, dto));
    }

    /** 我的闲置订单：role=buyer|seller */
    @GetMapping("/my")
    public Result<PageResult<GoodsOrder>> my(@RequestAttribute Long userId,
                                             @RequestParam(defaultValue = "buyer") String role,
                                             @RequestParam(required = false) Integer status,
                                             @RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(goodsOrderService.my(userId, role, status, page, size));
    }

    /** 确认完成面交 */
    @PostMapping("/{id}/finish")
    public Result<Void> finish(@RequestAttribute Long userId, @PathVariable Long id) {
        goodsOrderService.finish(userId, id);
        return Result.ok();
    }

    /** 取消订单（body 带原因） */
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@RequestAttribute Long userId, @PathVariable Long id,
                               @RequestBody(required = false) ReasonDTO dto) {
        goodsOrderService.cancel(userId, id, dto != null ? dto.getReason() : null);
        return Result.ok();
    }
}
