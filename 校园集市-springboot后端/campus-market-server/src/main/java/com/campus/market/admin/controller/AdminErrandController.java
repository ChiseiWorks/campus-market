package com.campus.market.admin.controller;

import com.campus.market.admin.service.AdminErrandService;
import com.campus.market.common.PageResult;
import com.campus.market.common.Result;
import com.campus.market.entity.ErrandOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端：跑腿订单监控
 */
@RestController
@RequestMapping("/api/admin/errand")
@RequiredArgsConstructor
public class AdminErrandController {

    private final AdminErrandService adminErrandService;

    /** 跑腿订单列表：status / type 过滤，keyword 匹配标题或订单号，item 附双方昵称 + abnormal 异常标记 */
    @GetMapping("/list")
    public Result<PageResult<ErrandOrder>> list(@RequestParam(required = false) Integer status,
                                                @RequestParam(required = false) Integer type,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(adminErrandService.list(status, type, keyword, page, size));
    }
}
