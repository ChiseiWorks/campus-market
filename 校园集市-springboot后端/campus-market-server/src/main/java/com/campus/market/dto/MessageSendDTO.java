package com.campus.market.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发送聊天消息请求
 */
@Data
public class MessageSendDTO {

    @NotNull(message = "接收人不能为空")
    private Long toUserId;

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 1000, message = "消息最长 1000 个字符")
    private String content;

    /** 1文本 2图片，默认 1 */
    private Integer type;

    /** 从商品页发起会话时带上 */
    private Long goodsId;
}
