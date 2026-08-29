package com.campus.market.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类：登录/注册成功后签发 token，拦截器解析 token 取 userId
 * secret / 过期时间在 application.yml 中可配置
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    /** 过期时间（小时） */
    @Value("${jwt.expire-hours:72}")
    private Long expireHours;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** 签发 token，subject 存 userId */
    public String generate(Long userId) {
        Date now = new Date();
        Date expireAt = new Date(now.getTime() + expireHours * 3600_000L);
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(expireAt)
                .signWith(key())
                .compact();
    }

    /** 解析 token 返回 userId；签名不对或过期会抛 JwtException，由调用方处理 */
    public Long parseUserId(String token) {
        return Long.valueOf(parse(token).getSubject());
    }

    /** 签发管理员 token，subject 存管理员账号，附 role=admin claim（AdminInterceptor 校验） */
    public String generateAdmin(String username) {
        Date now = new Date();
        Date expireAt = new Date(now.getTime() + expireHours * 3600_000L);
        return Jwts.builder()
                .setSubject(username)
                .claim("role", "admin")
                .setIssuedAt(now)
                .setExpiration(expireAt)
                .signWith(key())
                .compact();
    }

    /** 解析 token 返回全部 Claims；签名不对或过期会抛 JwtException，由调用方处理 */
    public Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
