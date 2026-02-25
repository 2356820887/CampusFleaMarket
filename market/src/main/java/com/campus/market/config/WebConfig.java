package com.campus.market.config;

import com.campus.market.common.interceptor.JwtAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置类，注册拦截器
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtAuthInterceptor jwtAuthInterceptor;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        // 注册 JWT 拦截器
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/**") // 拦截所有路径
                .excludePathPatterns(
                        "/product/list",
                        "/product/hot",
                        "/category/list",
                        "/activityTopic/list/active",
                        "/user/login", // 排除登录接口
                        "/user/register", // 排除注册接口
                        "/error", // 排除错误页面
                        "/swagger-ui/**", // 排除 Swagger UI
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**",
                        "/order/alipay/notify",
                        "/ws/**");
    }
}
