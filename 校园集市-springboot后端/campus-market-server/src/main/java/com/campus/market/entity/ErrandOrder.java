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
 * 跑腿订单表（核心表，数据库设计文档 3.6）
 * version 乐观锁：UPDATE ... WHERE status=旧状态 AND version=旧版本，防并发抢单
 * pickup_code 取件码：按角色 + 状态在服务端脱敏返回（见 ErrandService.detail）
 */
@Data
@TableName("errand_order")
public class ErrandOrder {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单号，唯一索引 */
    private String orderNo;

    /** 发单人 */
    private Long publisherId;

    /** 接单人（跑男） */
    private Long runnerId;

    /** 1取快递 2代买餐 3代送物品 4其他 */
    private Integer type;

    /** 需求标题 */
    private String title;

    /** 取货地点 → school_location */
    private Integer pickupLocationId;

    /** 送达地点 → school_location */
    private Integer deliveryLocationId;

    /** 取货补充说明（如快递柜编号） */
    private String pickupDetail;

    /** 取件码（TODO: 加密存储，骨架阶段明文 + 接口层脱敏） */
    private String pickupCode;

    /** 物品描述（大小/重量/是否易碎） */
    private String goodsDesc;

    /** 悬赏金额 */
    private BigDecimal reward;

    /** 期望完成时间 */
    private LocalDateTime expectTime;

    /** 0待接单 1已接单 2配送中 3送达待确认 4已完成 5已取消 6申诉中 */
    private Integer status;

    /** 乐观锁版本号，防并发抢单（手动维护，不用 @Version，由自定义 CAS SQL 控制） */
    private Integer version;

    /** 接单时间 */
    private LocalDateTime acceptTime;

    /** 开始配送时间 */
    private LocalDateTime deliverTime;

    /** 送达时间 */
    private LocalDateTime arriveTime;

    /** 完成时间（发单人确认） */
    private LocalDateTime finishTime;

    /** 取消发起方 */
    private Long cancelBy;

    /** 取消原因 */
    private String cancelReason;

    /** 逻辑删除 */
    @TableLogic
    private Integer isDeleted;

    /** 发布时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // ---------- 以下为展示用非数据库字段（详情接口按角色脱敏填充） ----------

    /** 发单人昵称 */
    @TableField(exist = false)
    private String publisherNickname;

    /** 发单人头像 */
    @TableField(exist = false)
    private String publisherAvatar;

    /** 发单人手机号（仅本单跑男在订单进行中可见，其余情况为 null） */
    @TableField(exist = false)
    private String publisherPhone;

    /** 跑男昵称 */
    @TableField(exist = false)
    private String runnerNickname;

    /** 跑男头像 */
    @TableField(exist = false)
    private String runnerAvatar;

    /** 跑男手机号（仅发单人在订单进行中可见，其余情况为 null） */
    @TableField(exist = false)
    private String runnerPhone;

    /** 取货地点名 */
    @TableField(exist = false)
    private String pickupLocationName;

    /** 送达地点名 */
    @TableField(exist = false)
    private String deliveryLocationName;

    /** 是否异常单（管理端列表计算：申诉中，或已超时仍未完结） */
    @TableField(exist = false)
    private Boolean abnormal;
}
