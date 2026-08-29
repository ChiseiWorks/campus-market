package com.campus.market.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 信用分流水表（数据库设计文档 3.8）
 * 设计要点：信用分只通过本表累加更新，不允许直接 UPDATE user.credit_score，保证可追溯
 */
@Data
@TableName("credit_log")
public class CreditLog {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 变动用户 */
    private Long userId;

    /** 变动值（正加负减） */
    private Integer changeValue;

    /** 变动后余额分 */
    private Integer balance;

    /** 变动原因（如"跑腿单爽约""交易好评"） */
    private String reason;

    /** 关联订单类型：1闲置 2跑腿 */
    private Integer orderType;

    /** 关联订单 */
    private Long orderId;

    /** 操作人，NULL=系统自动 */
    private Long operatorId;

    /** 时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
