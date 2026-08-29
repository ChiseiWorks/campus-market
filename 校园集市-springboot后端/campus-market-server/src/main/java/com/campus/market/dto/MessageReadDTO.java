package com.campus.market.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 标记会话已读请求：将与某人的会话全部标记已读
 */
@Data
public class MessageReadDTO {

    @NotNull(message = "对方用户ID不能为空")
    private Long userId;
}
