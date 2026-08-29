package com.campus.market.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏表（数据库设计文档 3.10）
 * uk_user_goods(user_id, goods_id) 唯一索引，防重复收藏
 */
@Data
@TableName("favorite")
public class Favorite {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 */
    private Long userId;

    /** 商品 */
    private Long goodsId;

    /** 收藏时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
