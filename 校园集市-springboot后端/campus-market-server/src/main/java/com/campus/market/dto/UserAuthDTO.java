package com.campus.market.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 校园认证提交请求
 * type=1 普通认证 / 2 跑男认证（跑男需二次认证，审核通过后置 user.is_runner=1）
 */
@Data
public class UserAuthDTO {

    /** 1普通认证 2跑男认证 */
    @NotNull(message = "认证类型不能为空")
    private Integer type;

    @NotBlank(message = "学号不能为空")
    private String studentNo;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    /** 学院/系 */
    private String college;

    /** 年级班级 */
    private String grade;

    /** 宿舍楼（用于同楼推荐） */
    private String dormBuilding;

    /** 认证材料图片（校园卡/教务截图 URL，先走 /file/upload 上传） */
    @NotBlank(message = "请上传认证材料图片")
    private String materialUrl;
}
