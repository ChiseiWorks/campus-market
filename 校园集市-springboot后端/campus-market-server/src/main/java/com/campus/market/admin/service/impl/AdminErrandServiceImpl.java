package com.campus.market.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.market.admin.service.AdminErrandService;
import com.campus.market.common.PageResult;
import com.campus.market.entity.ErrandOrder;
import com.campus.market.entity.User;
import com.campus.market.mapper.ErrandOrderMapper;
import com.campus.market.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 管理端：跑腿订单监控
 */
@Service
@RequiredArgsConstructor
public class AdminErrandServiceImpl implements AdminErrandService {

    private final ErrandOrderMapper errandOrderMapper;
    private final UserMapper userMapper;

    @Override
    public PageResult<ErrandOrder> list(Integer status, Integer type, String keyword, int page, int size) {
        QueryWrapper<ErrandOrder> qw = new QueryWrapper<>();
        if (status != null) {
            qw.eq("status", status);
        }
        if (type != null) {
            qw.eq("type", type);
        }
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w.like("title", keyword).or().like("order_no", keyword));
        }
        qw.orderByDesc("create_time");
        Page<ErrandOrder> p = errandOrderMapper.selectPage(new Page<>(page, size), qw);
        fillNicknamesAndAbnormal(p.getRecords());
        return PageResult.of(p);
    }

    /** 批量填充发单人/跑男昵称 + 计算异常标记 */
    private void fillNicknamesAndAbnormal(List<ErrandOrder> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        List<Long> userIds = new ArrayList<>();
        for (ErrandOrder o : list) {
            if (o.getPublisherId() != null) {
                userIds.add(o.getPublisherId());
            }
            if (o.getRunnerId() != null) {
                userIds.add(o.getRunnerId());
            }
        }
        Map<Long, User> userMap = userIds.isEmpty() ? Map.of() :
                userMapper.selectBatchIds(userIds.stream().distinct().collect(Collectors.toList()))
                        .stream().collect(Collectors.toMap(User::getId, Function.identity()));
        LocalDateTime now = LocalDateTime.now();
        for (ErrandOrder o : list) {
            User publisher = userMap.get(o.getPublisherId());
            if (publisher != null) {
                o.setPublisherNickname(publisher.getNickname());
            }
            User runner = userMap.get(o.getRunnerId());
            if (runner != null) {
                o.setRunnerNickname(runner.getNickname());
            }
            // 异常：申诉中(status=6)，或期望时间已过仍卡在 待接单/已接单/配送中
            boolean overdue = o.getExpectTime() != null && o.getExpectTime().isBefore(now)
                    && o.getStatus() != null && o.getStatus() <= 2;
            o.setAbnormal((o.getStatus() != null && o.getStatus() == 6) || overdue);
        }
    }
}
