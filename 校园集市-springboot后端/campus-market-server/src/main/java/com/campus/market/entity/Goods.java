package com.campus.market.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 闲置商品表（数据库设计文档 3.4）
 */
@Data
@TableName(value = "goods", autoResultMap = true)
public class Goods {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 卖家 */
    private Long userId;

    /** 商品标题 */
    private String title;

    /** 分类 */
    private Integer categoryId;

    /** 售价（DECIMAL，禁止浮点） */
    private BigDecimal price;

    /** 原价（展示几折用） */
    private BigDecimal originalPrice;

    /** 新旧：1全新 2九成新 3八成新 4有明显使用痕迹 */
    private Integer quality;

    /** 商品描述 */
    private String description;

    /** 图片URL数组，最多9张（JSON 存储） */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> images;

    /** 期望面交地点 → school_location */
    private Integer locationId;

    /** 0审核中 1在售 2已售出 3卖家下架 4违规下架 */
    private Integer status;

    /** 违规下架原因（v1.1 新增列，管理端违规下架时写入） */
    private String violationReason;

    /** 浏览数 */
    private Integer viewCount;

    /** 收藏数 */
    private Integer favCount;

    /** "我想要"次数 */
    private Integer wantCount;

    /** 逻辑删除：0正常 1已删 */
    @TableLogic
    private Integer isDeleted;

    /** 发布时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // ---------- 以下为展示用非数据库字段 ----------

    /** 卖家昵称（列表/详情展示） */
    @TableField(exist = false)
    private String sellerNickname;

    /** 卖家头像 */
    @TableField(exist = false)
    private String sellerAvatar;

    /** 面交地点名 */
    @TableField(exist = false)
    private String locationName;
}
