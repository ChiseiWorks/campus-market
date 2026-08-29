package com.campus.market.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 校园认证表（数据库设计文档 3.2）
 * 与 user 表分离：用户可多次提交认证（驳回后重提），历史材料留痕
 */
@Data
@TableName("user_auth")
public class UserAuth {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联用户 */
    private Long userId;

    /** 认证类型：1普通认证 2跑男认证（通过后 user.is_runner=1） */
    private Integer type;

    /** 学号（唯一索引，一个学号只能认证一个账号） */
    private String studentNo;

    /** 真实姓名 */
    private String realName;

    /** 学院/系 */
    private String college;

    /** 年级班级 */
    private String grade;

    /** 宿舍楼（用于同楼推荐） */
    private String dormBuilding;

    /** 认证材料图片（校园卡/教务截图） */
    private String materialUrl;

    /** 0待审核 1通过 2驳回 */
    private Integer auditStatus;

    /** 驳回原因 */
    private String auditRemark;

    /** 审核管理员 */
    private Long auditorId;

    /** 审核时间 */
    private LocalDateTime auditTime;

    /** 提交时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // ---------- 以下为展示用非数据库字段（管理端审核列表联查填充） ----------

    /** 用户昵称 */
    @TableField(exist = false)
    private String nickname;

    /** 用户手机号 */
    @TableField(exist = false)
    private String phone;
}
