package com.campus.market.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.market.admin.service.AdminDashboardService;
import com.campus.market.entity.Complaint;
import com.campus.market.entity.ErrandOrder;
import com.campus.market.entity.Goods;
import com.campus.market.entity.GoodsOrder;
import com.campus.market.entity.User;
import com.campus.market.entity.UserAuth;
import com.campus.market.mapper.ComplaintMapper;
import com.campus.market.mapper.ErrandOrderMapper;
import com.campus.market.mapper.GoodsMapper;
import com.campus.market.mapper.GoodsOrderMapper;
import com.campus.market.mapper.UserAuthMapper;
import com.campus.market.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理端：数据看板
 */
@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserMapper userMapper;
    private final GoodsMapper goodsMapper;
    private final GoodsOrderMapper goodsOrderMapper;
    private final ErrandOrderMapper errandOrderMapper;
    private final UserAuthMapper userAuthMapper;
    private final ComplaintMapper complaintMapper;

    @Override
    public Map<String, Object> summary() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime tomorrowStart = todayStart.plusDays(1);

        // 今日交易额 = 今日完成的闲置订单金额 + 今日完成的跑腿赏金（金额快照口径）
        BigDecimal goodsAmount = goodsOrderMapper.sumFinishedBetween(todayStart, tomorrowStart);
        BigDecimal errandAmount = errandOrderMapper.sumFinishedBetween(todayStart, tomorrowStart);

        Map<String, Object> data = new HashMap<>();
        data.put("todayNewUsers", userMapper.countNewSince(todayStart));
        data.put("todayNewGoods", goodsMapper.countNewSince(todayStart));
        data.put("todayNewGoodsOrders", goodsOrderMapper.countNewSince(todayStart));
        data.put("todayNewErrandOrders", errandOrderMapper.countNewSince(todayStart));
        data.put("todayTradeAmount", goodsAmount.add(errandAmount));
        // 总量（逻辑删除表由 MyBatis-Plus 自动过滤 is_deleted=0）
        data.put("totalUsers", userMapper.selectCount(new QueryWrapper<User>()));
        data.put("totalGoods", goodsMapper.selectCount(new QueryWrapper<Goods>()));
        data.put("totalGoodsOrders", goodsOrderMapper.selectCount(new QueryWrapper<GoodsOrder>()));
        data.put("totalErrandOrders", errandOrderMapper.selectCount(new QueryWrapper<ErrandOrder>()));
        // 待办
        data.put("pendingAuthCount", userAuthMapper.selectCount(
                new QueryWrapper<UserAuth>().eq("audit_status", 0)));
        data.put("pendingComplaintCount", complaintMapper.selectCount(
                new QueryWrapper<Complaint>().eq("status", 0)));
        return data;
    }

    /**
     * 近 N 天趋势：按 create_time 按天分组，缺的天补 0
     * newOrders = 当日新增闲置订单 + 跑腿订单
     */
    @Override
    public Map<String, Object> trend(int days) {
        if (days <= 0) {
            days = 7;
        }
        LocalDate startDate = LocalDate.now().minusDays(days - 1L);
        LocalDateTime start = startDate.atStartOfDay();

        Map<LocalDate, Long> userMap = toDayMap(userMapper.countByDaySince(start));
        Map<LocalDate, Long> goodsMap = toDayMap(goodsMapper.countByDaySince(start));
        Map<LocalDate, Long> goodsOrderMap = toDayMap(goodsOrderMapper.countByDaySince(start));
        Map<LocalDate, Long> errandOrderMap = toDayMap(errandOrderMapper.countByDaySince(start));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        List<String> dates = new ArrayList<>();
        List<Long> newUsers = new ArrayList<>();
        List<Long> newGoods = new ArrayList<>();
        List<Long> newOrders = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate d = startDate.plusDays(i);
            dates.add(d.format(fmt));
            newUsers.add(userMap.getOrDefault(d, 0L));
            newGoods.add(goodsMap.getOrDefault(d, 0L));
            newOrders.add(goodsOrderMap.getOrDefault(d, 0L) + errandOrderMap.getOrDefault(d, 0L));
        }
        Map<String, Object> data = new HashMap<>();
        data.put("dates", dates);
        data.put("newUsers", newUsers);
        data.put("newGoods", newGoods);
        data.put("newOrders", newOrders);
        return data;
    }

    @Override
    public List<Map<String, Object>> categoryStat() {
        return goodsMapper.countOnSaleByCategory();
    }

    @Override
    public List<Map<String, Object>> errandPeak() {
        List<Map<String, Object>> rows = errandOrderMapper.countByHour();
        Map<Integer, Long> hourMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Number hour = (Number) row.get("hour");
            Number count = (Number) row.get("count");
            if (hour != null && count != null) {
                hourMap.put(hour.intValue(), count.longValue());
            }
        }
        // 0-23 小时缺时补 0
        List<Map<String, Object>> result = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            Map<String, Object> item = new HashMap<>();
            item.put("hour", h);
            item.put("count", hourMap.getOrDefault(h, 0L));
            result.add(item);
        }
        return result;
    }

    /** DATE(create_time) 分组结果 → Map<LocalDate, Long>（JDBC 返回 java.sql.Date 或字符串，两种都兼容） */
    private Map<LocalDate, Long> toDayMap(List<Map<String, Object>> rows) {
        Map<LocalDate, Long> map = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Object d = row.get("d");
            if (d == null) {
                continue;
            }
            LocalDate date = d instanceof Date ? ((Date) d).toLocalDate() : LocalDate.parse(String.valueOf(d));
            Number c = (Number) row.get("c");
            map.put(date, c == null ? 0L : c.longValue());
        }
        return map;
    }
}
