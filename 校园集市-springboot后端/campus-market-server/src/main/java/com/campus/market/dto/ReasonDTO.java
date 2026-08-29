package com.campus.market.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 带原因的操作请求：取消订单 / 发起申诉共用
 */
@Data
public class ReasonDTO {

    @Size(max = 128, message = "原因最长 128 个字符")
    private String reason;
}
