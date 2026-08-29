package com.campus.market.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求：手机号 + 密码
 */
@Data
public class LoginDTO {

    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotBlank(message = "密码不能为空")
    private String password;
}
