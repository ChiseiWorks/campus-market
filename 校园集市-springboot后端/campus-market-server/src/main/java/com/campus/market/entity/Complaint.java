package com.campus.market.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 投诉表（数据库设计文档 3.9）
 */
@Data
@TableName(value = "complaint", autoResultMap = true)
public class Complaint {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 1闲置 2跑腿 */
    private Integer orderType;

    /** 关联订单 */
    private Long orderId;

    /** 投诉人（原告） */
    private Long plaintiffId;

    /** 被投诉人（被告） */
    private Long defendantId;

    /** 1爽约 2商品与描述不符 3物品损坏 4态度恶劣 5其他 */
    private Integer type;

    /** 投诉描述 */
    private String content;

    /** 证据图片数组 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private java.util.List<String> evidence;

    /** 0待处理 1处理中 2已办结 */
    private Integer status;

    /** 处理结果 */
    private String result;

    /** 处理管理员 */
    private Long handlerId;

    /** 处理时间 */
    private LocalDateTime handleTime;

    /** 投诉时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    // ---------- 以下为展示用非数据库字段（管理端列表填充） ----------

    /** 投诉人昵称 */
    @TableField(exist = false)
    private String plaintiffNickname;

    /** 被投诉人昵称 */
    @TableField(exist = false)
    private String defendantNickname;
}
