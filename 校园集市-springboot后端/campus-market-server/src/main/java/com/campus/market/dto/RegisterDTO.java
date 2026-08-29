package com.campus.market.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求：phone / smsCode / password / nickname
 */
@Data
public class RegisterDTO {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "短信验证码不能为空")
    private String smsCode;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度需为 6~20 位")
    private String password;

    @Size(max = 32, message = "昵称最长 32 个字符")
    private String nickname;
}
