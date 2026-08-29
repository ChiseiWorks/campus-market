package com.campus.market.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 通用 Bean 配置
 * 只引入 spring-security-crypto 的 BCrypt 做密码加密，不引入完整 Spring Security
 */
@Configuration
public class CommonConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
