package com.campus.market.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 闲置下单请求（立即交易）
 */
@Data
public class OrderCreateDTO {

    @NotNull(message = "商品ID不能为空")
    private Long goodsId;

    @Size(max = 255, message = "留言最长 255 个字符")
    private String remark;
}
