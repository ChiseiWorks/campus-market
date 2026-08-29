package com.campus.market.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 提交评价请求（每单每人限一次，uk_order_from 唯一约束兜底）
 */
@Data
public class EvaluateSubmitDTO {

    /** 1闲置订单 2跑腿订单 */
    @NotNull(message = "订单类型不能为空")
    private Integer orderType;

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "被评价人不能为空")
    private Long toUserId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分范围为 1~5")
    @Max(value = 5, message = "评分范围为 1~5")
    private Integer score;

    /** 标签数组：["准时","描述相符","态度好"] */
    private List<String> tags;

    @Size(max = 500, message = "评价内容最长 500 个字符")
    private String content;
}
