package com.campus.market.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 投诉处理请求（body 带 id + 处理结果 + 可选信用分调整）
 */
@Data
public class ComplaintHandleDTO {

    @NotNull(message = "id 不能为空")
    private Long id;

    @NotBlank(message = "处理结果不能为空")
    @Size(max = 255, message = "处理结果最长 255 个字符")
    private String result;

    /** 被投诉人信用分变动（正加负减，可空/0 表示不动分） */
    private Integer defendantCreditDelta;
}
