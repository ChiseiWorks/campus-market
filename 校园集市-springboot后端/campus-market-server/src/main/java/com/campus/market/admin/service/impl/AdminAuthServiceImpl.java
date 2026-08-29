package com.campus.market.admin.service.impl;

import com.campus.market.admin.dto.AdminLoginDTO;
import com.campus.market.admin.service.AdminAuthService;
import com.campus.market.common.BizException;
import com.campus.market.common.JwtUtil;
import com.campus.market.common.PageResult;
import com.campus.market.entity.User;
import com.campus.market.entity.UserAuth;
import com.campus.market.mapper.UserAuthMapper;
import com.campus.market.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理端：登录（配置账号）+ 校园认证审核
 */
@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {

    private final UserAuthMapper userAuthMapper;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    /** 管理员账号配置（不走数据库） */
    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Override
    public Map<String, Object> login(AdminLoginDTO dto) {
        if (!adminUsername.equals(dto.getUsername()) || !adminPassword.equals(dto.getPassword())) {
            throw new BizException("账号或密码错误");
        }
        Map<String, Object> adminInfo = new HashMap<>();
        adminInfo.put("username", adminUsername);
        adminInfo.put("nickname", "平台管理员");
        Map<String, Object> data = new HashMap<>();
        data.put("token", jwtUtil.generateAdmin(adminUsername));
        data.put("adminInfo", adminInfo);
        return data;
    }

    @Override
    public PageResult<UserAuth> authList(Integer status, int page, int size) {
        long offset = (long) (page - 1) * size;
        List<UserAuth> list = userAuthMapper.selectPageWithUser(status, offset, size);
        long total = userAuthMapper.countWithStatus(status);
        return PageResult.of(list, total);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id) {
        UserAuth auth = requirePending(id);
        auth.setAuditStatus(1); // 通过
        auth.setAuditTime(LocalDateTime.now());
        userAuthMapper.updateById(auth);
        User user = userMapper.selectById(auth.getUserId());
        if (user != null) {
            user.setAuthStatus(2); // 已认证
            // 跑男认证(type=2)通过 → 同步开通跑男资格
            if (auth.getType() != null && auth.getType() == 2) {
                user.setIsRunner(1);
            }
            userMapper.updateById(user);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id, String remark) {
        UserAuth auth = requirePending(id);
        auth.setAuditStatus(2); // 驳回
        auth.setAuditRemark(remark);
        auth.setAuditTime(LocalDateTime.now());
        userAuthMapper.updateById(auth);
        User user = userMapper.selectById(auth.getUserId());
        if (user != null) {
            user.setAuthStatus(3); // 已驳回
            userMapper.updateById(user);
        }
    }

    /** 取认证申请并校验仍处于待审核（防重复处理） */
    private UserAuth requirePending(Long id) {
        UserAuth auth = userAuthMapper.selectById(id);
        if (auth == null) {
            throw new BizException("认证申请不存在");
        }
        if (auth.getAuditStatus() != null && auth.getAuditStatus() != 0) {
            throw new BizException("该申请已处理，请勿重复操作");
        }
        return auth;
    }
}
