package com.campus.market.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.market.admin.service.AdminUserService;
import com.campus.market.common.BizException;
import com.campus.market.common.PageResult;
import com.campus.market.entity.CreditLog;
import com.campus.market.entity.User;
import com.campus.market.mapper.CreditLogMapper;
import com.campus.market.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 管理端：用户管理
 */
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserMapper userMapper;
    private final CreditLogMapper creditLogMapper;

    @Override
    public PageResult<User> userList(String keyword, Integer status, int page, int size) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w.like("nickname", keyword).or().like("phone", keyword));
        }
        if (status != null) {
            qw.eq("status", status);
        }
        qw.orderByDesc("create_time");
        Page<User> p = userMapper.selectPage(new Page<>(page, size), qw);
        // password 字段有 @JsonIgnore，返回安全
        return PageResult.of(p);
    }

    @Override
    public void ban(Long id) {
        User user = requireUser(id);
        user.setStatus(1); // 封禁
        userMapper.updateById(user);
    }

    @Override
    public void unban(Long id) {
        User user = requireUser(id);
        user.setStatus(0); // 正常
        userMapper.updateById(user);
    }

    @Override
    public PageResult<CreditLog> creditLogs(Long userId, int page, int size) {
        requireUser(userId);
        Page<CreditLog> p = creditLogMapper.selectPage(new Page<>(page, size),
                new QueryWrapper<CreditLog>()
                        .eq("user_id", userId)
                        .orderByDesc("create_time")
                        .orderByDesc("id"));
        return PageResult.of(p);
    }

    private User requireUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        return user;
    }
}
