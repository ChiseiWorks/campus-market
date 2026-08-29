package com.campus.market.admin.service;

import java.util.List;
import java.util.Map;

/**
 * 管理端：数据看板
 */
public interface AdminDashboardService {

    /** 汇总卡片：今日新增/总量/待办 */
    Map<String, Object> summary();

    /** 近 N 天趋势：{dates, newUsers, newGoods, newOrders}（缺天补 0） */
    Map<String, Object> trend(int days);

    /** 在售商品按分类计数：[{name, value}] */
    List<Map<String, Object>> categoryStat();

    /** 跑腿单按小时分布：[{hour, count}]（0-23 缺时补 0） */
    List<Map<String, Object>> errandPeak();
}
