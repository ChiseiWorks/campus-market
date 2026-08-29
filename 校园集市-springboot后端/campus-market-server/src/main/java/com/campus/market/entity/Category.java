package com.campus.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 分类表（数据库设计文档 3.3）
 * type=1 闲置分类 / 2 跑腿类型，平台预置
 */
@Data
@TableName("category")
public class Category {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 分类名（教材/数码/取快递/代买餐…） */
    private String name;

    /** 图标 URL */
    private String icon;

    /** 1闲置分类 2跑腿类型 */
    private Integer type;

    /** 排序，越小越靠前 */
    private Integer sort;

    /** 1启用 0停用 */
    private Integer status;
}
