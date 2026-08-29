package com.campus.market.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * 全局异常处理器：无论发生什么异常，永远返回统一响应体结构
 * - BizException          → 业务码 + 业务信息（直接展示给用户）
 * - 参数校验/格式错误      → 400 + 具体原因
 * - 其他未捕获异常         → 500 + 兜底提示（日志记录堆栈）
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常 */
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBiz(BizException e) {
        log.warn("业务异常：{}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /** @Valid / @Validated 参数校验失败（@RequestBody） */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBind(BindException e) {
        FieldError fieldError = e.getFieldError();
        String msg = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        return Result.fail(400, msg);
    }

    /** 请求体 JSON 格式错误 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleNotReadable(HttpMessageNotReadableException e) {
        return Result.fail(400, "请求体格式错误");
    }

    /** 缺少必填请求参数 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingParam(MissingServletRequestParameterException e) {
        return Result.fail(400, "缺少参数：" + e.getParameterName());
    }

    /** 参数类型不匹配（如 /goods/abc 传不了 Long） */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return Result.fail(400, "参数类型不正确：" + e.getName());
    }

    /** 上传文件超限 */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> handleUploadSize(MaxUploadSizeExceededException e) {
        return Result.fail(400, "文件大小超出限制（单文件最大 10MB）");
    }

    /** 兜底：未预期的服务器异常 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("服务器异常", e);
        return Result.fail(500, "服务器开小差了，请稍后重试");
    }
}
