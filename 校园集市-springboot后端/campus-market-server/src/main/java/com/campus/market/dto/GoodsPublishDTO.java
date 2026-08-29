package com.campus.market.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 发布闲置商品请求
 */
@Data
public class GoodsPublishDTO {

    @NotBlank(message = "商品标题不能为空")
    @Size(max = 64, message = "标题最长 64 个字符")
    private String title;

    @NotNull(message = "请选择分类")
    private Integer categoryId;

    @NotNull(message = "售价不能为空")
    @DecimalMin(value = "0.01", message = "售价必须大于 0")
    private BigDecimal price;

    /** 原价（展示几折用，可不填） */
    private BigDecimal originalPrice;

    /** 新旧：1全新 2九成新 3八成新 4有明显使用痕迹，默认 3 */
    private Integer quality;

    /** 商品描述 */
    private String description;

    /** 图片URL数组，最多9张 */
    @Size(max = 9, message = "图片最多 9 张")
    private List<String> images;

    /** 期望面交地点 */
    private Integer locationId;
}
