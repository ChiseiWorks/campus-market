package com.campus.market.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评价表（数据库设计文档 3.7）
 * uk_order_from(order_type, order_id, from_user_id) 唯一索引：每单每人只能评一次
 */
@Data
@TableName(value = "evaluation", autoResultMap = true)
public class Evaluation {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 1闲置订单 2跑腿订单 */
    private Integer orderType;

    /** 关联订单 */
    private Long orderId;

    /** 评价人 */
    private Long fromUserId;

    /** 被评价人 */
    private Long toUserId;

    /** 评分 1~5 */
    private Integer score;

    /** 标签数组：["准时","描述相符","态度好"] */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

    /** 文字评价 */
    private String content;

    /** 评价时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
