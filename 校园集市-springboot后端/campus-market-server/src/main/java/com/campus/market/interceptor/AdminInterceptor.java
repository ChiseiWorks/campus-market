package com.campus.market.interceptor;

import com.campus.market.common.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * 管理端认证拦截器
 * - 拦截 /api/admin/**（/api/admin/login 在 WebMvcConfig 中放行）
 * - 校验 JWT 且 role claim = "admin"，管理员账号走配置不走数据库
 * - 失败返回 HTTP 401 + 统一 JSON；成功把 adminUsername 放进 request attribute
 */
@Component
@RequiredArgsConstructor
public class AdminInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Claims claims = jwtUtil.parse(token);
                if ("admin".equals(claims.get("role", String.class))) {
                    request.setAttribute("adminUsername", claims.getSubject());
                    return true;
                }
            } catch (JwtException | IllegalArgumentException e) {
                // token 过期 / 签名错误 / 格式错误，统一按 401 处理
            }
        }
        writeUnauthorized(response);
        return false;
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"msg\":\"未登录或无管理员权限\",\"data\":null}");
    }
}
