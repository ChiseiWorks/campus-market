package com.campus.market.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表（数据库设计文档 3.1）
 */
@Data
@TableName("user")
public class User {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 手机号，唯一索引 */
    private String phone;

    /** BCrypt 加密后的密码（永不返回给前端） */
    @JsonIgnore
    private String password;

    /** 昵称 */
    private String nickname;

    /** 头像 URL */
    private String avatar;

    /** 0未知 1男 2女 */
    private Integer gender;

    /** 认证状态：0未认证 1审核中 2已认证 3已驳回 */
    private Integer authStatus;

    /** 是否跑男：0否 1是（需二次认证） */
    private Integer isRunner;

    /** 信用分，满分100，低于60限制发布/接单 */
    private Integer creditScore;

    /** 账号状态：0正常 1封禁 */
    private Integer status;

    /** 最近登录时间 */
    private LocalDateTime lastLoginTime;

    /** 注册时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
