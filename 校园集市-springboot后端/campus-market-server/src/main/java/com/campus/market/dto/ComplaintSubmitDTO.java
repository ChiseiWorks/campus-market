package com.campus.market.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 提交投诉请求
 */
@Data
public class ComplaintSubmitDTO {

    /** 1闲置 2跑腿 */
    @NotNull(message = "订单类型不能为空")
    private Integer orderType;

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "被投诉人不能为空")
    private Long defendantId;

    /** 1爽约 2商品与描述不符 3物品损坏 4态度恶劣 5其他 */
    @NotNull(message = "投诉类型不能为空")
    private Integer type;

    @NotBlank(message = "投诉描述不能为空")
    private String content;

    /** 证据图片数组 */
    private List<String> evidence;
}
