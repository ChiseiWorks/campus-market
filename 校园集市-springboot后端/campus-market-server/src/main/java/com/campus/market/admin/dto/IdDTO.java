package com.campus.market.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 通用 ID 请求（管理端审核通过/封禁/解封/地点切换/公告下线等共用）
 */
@Data
public class IdDTO {

    @NotNull(message = "id 不能为空")
    private Long id;
}
