package com.campus.market.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 认证驳回请求（body 带 id + 驳回原因）
 */
@Data
public class AuthRejectDTO {

    @NotNull(message = "id 不能为空")
    private Long id;

    @NotBlank(message = "驳回原因不能为空")
    @Size(max = 255, message = "驳回原因最长 255 个字符")
    private String remark;
}
