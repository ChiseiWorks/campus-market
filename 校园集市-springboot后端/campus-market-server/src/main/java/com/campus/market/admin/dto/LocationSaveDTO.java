package com.campus.market.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 校内地点保存请求：有 id 改、无 id 增
 */
@Data
public class LocationSaveDTO {

    /** 有 id 为编辑，无 id 为新增 */
    private Long id;

    @NotBlank(message = "地点名不能为空")
    @Size(max = 32, message = "地点名最长 32 个字符")
    private String name;

    /** 1宿舍楼 2教学楼 3快递点 4食堂 5其他 */
    @NotNull(message = "地点类型不能为空")
    private Integer type;

    /** 排序，越小越靠前 */
    private Integer sort;
}
