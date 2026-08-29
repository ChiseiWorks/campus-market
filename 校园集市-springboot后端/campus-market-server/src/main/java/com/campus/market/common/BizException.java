package com.campus.market.common;

import lombok.Getter;

/**
 * 业务异常：抛出后由全局异常处理器转成统一响应体
 * code 默认 500（业务错误），msg 直接展示给用户
 */
@Getter
public class BizException extends RuntimeException {

    private final Integer code;

    public BizException(String msg) {
        this(500, msg);
    }

    public BizException(Integer code, String msg) {
        super(msg);
        this.code = code;
    }
}
