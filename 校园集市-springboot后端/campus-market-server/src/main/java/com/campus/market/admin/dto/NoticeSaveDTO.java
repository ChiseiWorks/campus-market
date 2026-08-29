package com.campus.market.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 公告发布请求
 */
@Data
public class NoticeSaveDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 64, message = "标题最长 64 个字符")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;
}
