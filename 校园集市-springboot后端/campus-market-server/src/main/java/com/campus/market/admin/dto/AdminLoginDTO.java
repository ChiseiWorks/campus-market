package com.campus.market.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理员登录请求（账号配置在 application.yml，不走数据库）
 */
@Data
public class AdminLoginDTO {

    @NotBlank(message = "账号不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
