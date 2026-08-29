package com.campus.market.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应体：{ "code": 200, "msg": "success", "data": {...} }
 * 与前端 request.js 约定一致：code=200 成功，非 200 即业务错误（前端弹 msg）
 */
@Data
public class Result<T> implements Serializable {

    /** 200 成功；非 200 业务错误；401 未登录（拦截器直接写 HTTP 401） */
    private Integer code;

    private String msg;

    private T data;

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMsg("success");
        r.setData(data);
        return r;
    }

    public static <T> Result<T> fail(String msg) {
        return fail(500, msg);
    }

    public static <T> Result<T> fail(Integer code, String msg) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }
}
