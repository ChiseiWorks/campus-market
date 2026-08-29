package com.campus.market.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 发布跑腿单请求
 */
@Data
public class ErrandPublishDTO {

    /** 1取快递 2代买餐 3代送物品 4其他 */
    @NotNull(message = "请选择跑腿类型")
    private Integer type;

    @NotBlank(message = "需求标题不能为空")
    @Size(max = 64, message = "标题最长 64 个字符")
    private String title;

    @NotNull(message = "请选择取货地点")
    private Integer pickupLocationId;

    @NotNull(message = "请选择送达地点")
    private Integer deliveryLocationId;

    /** 取货补充说明（如快递柜编号） */
    @Size(max = 128, message = "取货说明最长 128 个字符")
    private String pickupDetail;

    /** 取件码（仅接单后对接单人可见，接口层脱敏） */
    private String pickupCode;

    /** 物品描述（大小/重量/是否易碎） */
    @Size(max = 255, message = "物品描述最长 255 个字符")
    private String goodsDesc;

    @NotNull(message = "悬赏金额不能为空")
    @DecimalMin(value = "0.01", message = "悬赏金额必须大于 0")
    private BigDecimal reward;

    /** 期望完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expectTime;
}
