package com.campus.market.config;

import com.campus.market.interceptor.AdminInterceptor;
import com.campus.market.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web MVC 配置：登录拦截器注册 + 上传文件静态资源映射 + 全局 CORS
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final AdminInterceptor adminInterceptor;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 用户端拦截器：/api/** 默认需登录，开放路径与整个管理端 /api/admin/** 都排除
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/user/login",
                        "/api/user/register",
                        "/api/user/sms",
                        "/api/notice/list",
                        "/api/category/list",
                        "/api/location/list",
                        "/api/goods/list",    // 浏览商品列表免登录（详情/下单仍需登录）
                        "/api/errand/hall",   // 浏览跑腿大厅免登录（抢单仍需登录+跑男认证）
                        "/api/admin/**"   // 管理端由 AdminInterceptor 单独校验，互不影响
                );
        // 管理端拦截器：校验 JWT 且 role=admin，仅放行登录接口
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/api/admin/**")
                .excludePathPatterns("/api/admin/login");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 上传文件本地存储目录映射为 /uploads/**，返回的 URL 可直接访问
        String dir = new File(uploadDir).getAbsolutePath();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + dir + File.separator);
    }

    /**
     * 全局 CORS：允许本地开发来源（admin-web 的 5173、H5 预览的 7100、127.0.0.1 任意端口）
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(
                        "http://localhost:*",    // 本地开发全放行（admin-web / H5 / Kimi 预览随机端口）
                        "http://127.0.0.1:*"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
