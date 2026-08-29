package com.campus.market.admin.controller;

import com.campus.market.admin.dto.GoodsTakedownDTO;
import com.campus.market.admin.service.AdminGoodsService;
import com.campus.market.common.PageResult;
import com.campus.market.common.Result;
import com.campus.market.entity.Goods;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端：商品监管
 */
@RestController
@RequestMapping("/api/admin/goods")
@RequiredArgsConstructor
public class AdminGoodsController {

    private final AdminGoodsService adminGoodsService;

    /** 商品列表：keyword 模糊匹配标题，status 过滤，item 附 sellerNickname */
    @GetMapping("/list")
    public Result<PageResult<Goods>> list(@RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) Integer status,
                                          @RequestParam(defaultValue = "1") Integer page,
                                          @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(adminGoodsService.list(keyword, status, page, size));
    }

    /** 违规下架：{id, reason} → status=4，原因写入 goods.violation_reason */
    @PostMapping("/takedown")
    public Result<Void> takedown(@Valid @RequestBody GoodsTakedownDTO dto) {
        adminGoodsService.takedown(dto.getId(), dto.getReason());
        return Result.ok();
    }
}
