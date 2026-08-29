package com.campus.market.controller;

import com.campus.market.common.PageResult;
import com.campus.market.common.Result;
import com.campus.market.dto.LoginDTO;
import com.campus.market.dto.RegisterDTO;
import com.campus.market.dto.SmsDTO;
import com.campus.market.dto.UserAuthDTO;
import com.campus.market.entity.CreditLog;
import com.campus.market.entity.User;
import com.campus.market.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 用户与认证接口（接口文档 5.1）
 * 开放路径：/login /register /sms（拦截器放行），其余需登录
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 手机号密码登录，返回 {token, userInfo} */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        return Result.ok(userService.login(dto));
    }

    /** 注册（phone/smsCode/password/nickname），成功后直接返回 token */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterDTO dto) {
        return Result.ok(userService.register(dto));
    }

    /** 发送短信验证码（骨架阶段日志模拟） */
    @PostMapping("/sms")
    public Result<Void> sms(@Valid @RequestBody SmsDTO dto) {
        userService.sendSms(dto.getPhone());
        return Result.ok();
    }

    /** 当前用户信息 */
    @GetMapping("/info")
    public Result<User> info(@RequestAttribute Long userId) {
        return Result.ok(userService.info(userId));
    }

    /** 提交校园认证（type=1 普通认证 / 2 跑男认证） */
    @PostMapping("/auth")
    public Result<Void> auth(@RequestAttribute Long userId, @Valid @RequestBody UserAuthDTO dto) {
        userService.submitAuth(userId, dto);
        return Result.ok();
    }

    /** 信用分流水（分页） */
    @GetMapping("/credit/logs")
    public Result<PageResult<CreditLog>> creditLogs(@RequestAttribute Long userId,
                                                    @RequestParam(defaultValue = "1") Integer page,
                                                    @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(userService.creditLogs(userId, page, size));
    }
}
