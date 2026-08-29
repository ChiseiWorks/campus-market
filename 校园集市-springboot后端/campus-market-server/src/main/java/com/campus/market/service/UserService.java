package com.campus.market.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.market.common.PageResult;
import com.campus.market.dto.LoginDTO;
import com.campus.market.dto.RegisterDTO;
import com.campus.market.dto.UserAuthDTO;
import com.campus.market.entity.CreditLog;
import com.campus.market.entity.User;

import java.util.Map;

/**
 * 用户与认证服务
 */
public interface UserService extends IService<User> {

    /** 手机号密码登录，返回 {token, userInfo} */
    Map<String, Object> login(LoginDTO dto);

    /** 注册（校验短信验证码），成功后直接返回 {token, userInfo} 自动登录 */
    Map<String, Object> register(RegisterDTO dto);

    /** 发送短信验证码（骨架阶段：内存存储 + 日志模拟，TODO 接真实短信服务） */
    void sendSms(String phone);

    /** 当前用户信息 */
    User info(Long userId);

    /** 提交校园认证（type=1 普通认证 / 2 跑男认证） */
    void submitAuth(Long userId, UserAuthDTO dto);

    /** 信用分流水（分页） */
    PageResult<CreditLog> creditLogs(Long userId, int page, int size);
}
