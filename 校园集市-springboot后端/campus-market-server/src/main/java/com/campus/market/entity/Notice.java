package com.campus.market.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统公告表（数据库设计文档 3.13）
 */
@Data
@TableName("notice")
public class Notice {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 1发布 0下线 */
    private Integer status;

    /** 发布时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
