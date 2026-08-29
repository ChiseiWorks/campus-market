package com.campus.market.admin.service;

import com.campus.market.admin.dto.AdminLoginDTO;
import com.campus.market.common.PageResult;
import com.campus.market.entity.UserAuth;

import java.util.Map;

/**
 * 管理端：登录 + 校园认证审核
 */
public interface AdminAuthService {

    /** 管理员登录（配置账号），返回 {token, adminInfo:{username,nickname}} */
    Map<String, Object> login(AdminLoginDTO dto);

    /** 认证申请列表（item = user_auth 全字段 + nickname + phone） */
    PageResult<UserAuth> authList(Integer status, int page, int size);

    /** 审核通过：audit_status=1；user.auth_status=2；跑男认证(type=2)同步 is_runner=1 */
    void approve(Long id);

    /** 驳回：audit_status=2 + 原因；user.auth_status=3 */
    void reject(Long id, String remark);
}
