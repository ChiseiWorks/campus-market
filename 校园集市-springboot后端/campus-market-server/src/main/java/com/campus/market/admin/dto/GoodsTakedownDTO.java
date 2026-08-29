package com.campus.market.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 商品违规下架请求（body 带 id + 原因，原因写入 goods.violation_reason）
 */
@Data
public class GoodsTakedownDTO {

    @NotNull(message = "id 不能为空")
    private Long id;

    @NotBlank(message = "违规原因不能为空")
    @Size(max = 255, message = "违规原因最长 255 个字符")
    private String reason;
}
