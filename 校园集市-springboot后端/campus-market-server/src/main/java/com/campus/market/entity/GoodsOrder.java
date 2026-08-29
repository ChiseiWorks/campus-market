package com.campus.market.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 闲置交易订单表（数据库设计文档 3.5）
 * 约束：同一商品同一时刻只允许一条进行中订单（status IN 0,1），
 * 由"下单时校验商品状态 + CAS 锁定商品状态"在同一事务内保证
 */
@Data
@TableName("goods_order")
public class GoodsOrder {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单号（日期+用户id+随机数），唯一索引 */
    private String orderNo;

    /** 商品 */
    private Long goodsId;

    /** 买家 */
    private Long buyerId;

    /** 卖家 */
    private Long sellerId;

    /** 成交价（下单时快照，防商品改价） */
    private BigDecimal dealPrice;

    /** 0待卖家确认 1交易中 2已完成 3已取消 4申诉中 */
    private Integer status;

    /** 买家留言 */
    private String remark;

    /** 卖家确认时间 */
    private LocalDateTime confirmTime;

    /** 完成时间（双方确认面交完成） */
    private LocalDateTime finishTime;

    /** 取消原因 */
    private String cancelReason;

    /** 取消发起方 */
    private Long cancelBy;

    /** 逻辑删除 */
    @TableLogic
    private Integer isDeleted;

    /** 下单时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
