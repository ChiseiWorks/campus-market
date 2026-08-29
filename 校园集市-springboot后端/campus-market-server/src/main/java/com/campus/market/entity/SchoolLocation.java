package com.campus.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 校内地点表（数据库设计文档 3.12）
 * 地点由平台预置，用户只能选不能填——保证数据结构化，支撑"同楼优先推荐"
 */
@Data
@TableName("school_location")
public class SchoolLocation {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 地点名（如"1号宿舍楼""菜鸟驿站"） */
    private String name;

    /** 1宿舍楼 2教学楼 3快递点 4食堂 5其他 */
    private Integer type;

    /** 排序 */
    private Integer sort;

    /** 1启用 0停用 */
    private Integer status;
}
