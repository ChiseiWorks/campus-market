package com.campus.market.interceptor;

import com.campus.market.common.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * 登录认证拦截器
 * - 解析请求头 Authorization: Bearer {token}，把 userId 放进 request attribute
 *   （Controller 用 @RequestAttribute Long userId 取）
 * - 未带 token / 过期 / 签名错误：返回 HTTP 401 + 统一 JSON
 *   （前端 request.js 收到 401 会清登录态并跳登录页）
 * - 开放路径在 WebMvcConfig 中配置排除
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        // 跨域预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Long userId = jwtUtil.parseUserId(token);
                request.setAttribute("userId", userId);
                return true;
            } catch (JwtException | IllegalArgumentException e) {
                // token 过期 / 签名错误 / 格式错误，统一按 401 处理
            }
        }
        writeUnauthorized(response);
        return false;
    }

    /** HTTP 401 + 统一响应体结构（前端按 statusCode===401 跳登录） */
    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"msg\":\"未登录或登录已过期\",\"data\":null}");
    }
}
