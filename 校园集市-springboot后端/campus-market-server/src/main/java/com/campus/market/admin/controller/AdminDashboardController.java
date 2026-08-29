package com.campus.market.admin.controller;

import com.campus.market.admin.service.AdminDashboardService;
import com.campus.market.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 管理端：数据看板
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    /** 汇总卡片：今日新增 / 总量 / 待办 */
    @GetMapping("/summary")
    public Result<Map<String, Object>> summary() {
        return Result.ok(adminDashboardService.summary());
    }

    /** 近 N 天趋势（默认 7 天） */
    @GetMapping("/trend")
    public Result<Map<String, Object>> trend(@RequestParam(defaultValue = "7") Integer days) {
        return Result.ok(adminDashboardService.trend(days));
    }

    /** 在售商品按分类计数 */
    @GetMapping("/category")
    public Result<List<Map<String, Object>>> category() {
        return Result.ok(adminDashboardService.categoryStat());
    }

    /** 跑腿单按小时分布（0-23 缺时补 0） */
    @GetMapping("/errand-peak")
    public Result<List<Map<String, Object>>> errandPeak() {
        return Result.ok(adminDashboardService.errandPeak());
    }
}
