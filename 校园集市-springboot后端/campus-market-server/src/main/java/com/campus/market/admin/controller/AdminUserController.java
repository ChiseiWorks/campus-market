package com.campus.market.admin.controller;

import com.campus.market.admin.dto.IdDTO;
import com.campus.market.admin.service.AdminUserService;
import com.campus.market.common.PageResult;
import com.campus.market.common.Result;
import com.campus.market.entity.CreditLog;
import com.campus.market.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端：用户管理
 */
@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    /** 用户列表：keyword 模糊匹配昵称/手机号，status 按账号状态过滤 */
    @GetMapping("/list")
    public Result<PageResult<User>> list(@RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) Integer status,
                                         @RequestParam(defaultValue = "1") Integer page,
                                         @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(adminUserService.userList(keyword, status, page, size));
    }

    /** 封禁：{id} → user.status=1 */
    @PostMapping("/ban")
    public Result<Void> ban(@Valid @RequestBody IdDTO dto) {
        adminUserService.ban(dto.getId());
        return Result.ok();
    }

    /** 解封：{id} → user.status=0 */
    @PostMapping("/unban")
    public Result<Void> unban(@Valid @RequestBody IdDTO dto) {
        adminUserService.unban(dto.getId());
        return Result.ok();
    }

    /** 指定用户信用分流水：?userId=&page=&size= */
    @GetMapping("/credit-logs")
    public Result<PageResult<CreditLog>> creditLogs(@RequestParam Long userId,
                                                    @RequestParam(defaultValue = "1") Integer page,
                                                    @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(adminUserService.creditLogs(userId, page, size));
    }
}
