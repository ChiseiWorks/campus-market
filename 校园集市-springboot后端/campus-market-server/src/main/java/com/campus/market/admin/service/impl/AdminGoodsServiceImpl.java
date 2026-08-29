package com.campus.market.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.market.admin.service.AdminGoodsService;
import com.campus.market.common.BizException;
import com.campus.market.common.PageResult;
import com.campus.market.entity.Goods;
import com.campus.market.entity.User;
import com.campus.market.mapper.GoodsMapper;
import com.campus.market.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 管理端：商品监管
 */
@Service
@RequiredArgsConstructor
public class AdminGoodsServiceImpl implements AdminGoodsService {

    private final GoodsMapper goodsMapper;
    private final UserMapper userMapper;

    @Override
    public PageResult<Goods> list(String keyword, Integer status, int page, int size) {
        QueryWrapper<Goods> qw = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            qw.like("title", keyword);
        }
        if (status != null) {
            qw.eq("status", status);
        }
        qw.orderByDesc("create_time");
        Page<Goods> p = goodsMapper.selectPage(new Page<>(page, size), qw);
        fillSellerNickname(p.getRecords());
        return PageResult.of(p);
    }

    @Override
    public void takedown(Long id, String reason) {
        Goods goods = goodsMapper.selectById(id);
        if (goods == null) {
            throw new BizException("商品不存在");
        }
        goods.setStatus(4); // 违规下架
        goods.setViolationReason(reason);
        goodsMapper.updateById(goods);
    }

    /** 批量填充卖家昵称 */
    private void fillSellerNickname(List<Goods> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        List<Long> userIds = list.stream().map(Goods::getUserId).distinct().collect(Collectors.toList());
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        for (Goods g : list) {
            User u = userMap.get(g.getUserId());
            if (u != null) {
                g.setSellerNickname(u.getNickname());
            }
        }
    }
}
