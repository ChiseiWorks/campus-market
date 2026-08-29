package com.campus.market.admin.controller;

import com.campus.market.admin.dto.AdminLoginDTO;
import com.campus.market.admin.dto.AuthRejectDTO;
import com.campus.market.admin.dto.IdDTO;
import com.campus.market.admin.service.AdminAuthService;
import com.campus.market.common.PageResult;
import com.campus.market.common.Result;
import com.campus.market.entity.UserAuth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 管理端：登录 + 校园认证审核
 * /api/admin/login 在 WebMvcConfig 中放行，其余 /api/admin/** 由 AdminInterceptor 校验
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    /** 管理员登录（账号配置在 application.yml，不走数据库）→ {token, adminInfo} */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody AdminLoginDTO dto) {
        return Result.ok(adminAuthService.login(dto));
    }

    /** 认证申请列表：status=0待审核 1通过 2驳回（空查全部） */
    @GetMapping("/auth/list")
    public Result<PageResult<UserAuth>> authList(@RequestParam(required = false) Integer status,
                                                 @RequestParam(defaultValue = "1") Integer page,
                                                 @RequestParam(defaultValue = "10") Integer size) {
        return Result.ok(adminAuthService.authList(status, page, size));
    }

    /** 审核通过：{id}（跑男认证 type=2 同步开通跑男资格） */
    @PostMapping("/auth/approve")
    public Result<Void> approve(@Valid @RequestBody IdDTO dto) {
        adminAuthService.approve(dto.getId());
        return Result.ok();
    }

    /** 驳回：{id, remark}（必须填原因） */
    @PostMapping("/auth/reject")
    public Result<Void> reject(@Valid @RequestBody AuthRejectDTO dto) {
        adminAuthService.reject(dto.getId(), dto.getRemark());
        return Result.ok();
    }
}
