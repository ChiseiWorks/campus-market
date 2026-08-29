package com.campus.market.admin.service;

import com.campus.market.common.PageResult;
import com.campus.market.entity.CreditLog;
import com.campus.market.entity.User;

/**
 * 管理端：用户管理
 */
public interface AdminUserService {

    /** 用户列表：keyword 模糊匹配 nickname/phone，status 按账号状态过滤 */
    PageResult<User> userList(String keyword, Integer status, int page, int size);

    /** 封禁：user.status=1 */
    void ban(Long id);

    /** 解封：user.status=0 */
    void unban(Long id);

    /** 指定用户信用分流水 */
    PageResult<CreditLog> creditLogs(Long userId, int page, int size);
}
