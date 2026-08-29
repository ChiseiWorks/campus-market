package com.campus.market.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天消息表（数据库设计文档 3.11，P1 轮询方案）
 */
@Data
@TableName("chat_message")
public class ChatMessage {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发送方 */
    private Long fromUserId;

    /** 接收方 */
    private Long toUserId;

    /** 关联商品（从商品页发起会话时记录） */
    private Long goodsId;

    /** 1文本 2图片 */
    private Integer type;

    /** 内容 */
    private String content;

    /** 0未读 1已读 */
    private Integer isRead;

    /** 发送时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
