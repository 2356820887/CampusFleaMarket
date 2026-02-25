package com.campus.market.common.interceptor;

import com.campus.market.common.Result;
import com.campus.market.entity.User;
import com.campus.market.service.IUserService;
import com.campus.market.utils.JwtUtils;
import com.campus.market.utils.UserHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT 认证拦截器
 */
@Slf4j
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    @Autowired
    @Lazy
    private IUserService userService;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull Object handler) throws Exception {

        // 放行 CORS 预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        // 1. 从请求头中获取 Token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 2. 校验 Token 是否存在且有效
        if (token != null && !token.isEmpty()) {
            if (!JwtUtils.isTokenExpired(token)) {
                try {
                    // 解析出用户名和用户 ID
                    String username = JwtUtils.getUsername(token);
                    Long userId = JwtUtils.getUserId(token);

                    // 校验用户状态：实时拦截已封禁用户
                    User user = userService.getById(userId);
                    if (user != null && user.getStatus() != null && user.getStatus() == -1) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json;charset=utf-8");
                        Result<Object> result = Result.error(403, "您的账号已被封禁，无法进行此操作");
                        String json = new ObjectMapper().writeValueAsString(result);
                        response.getWriter().write(json);
                        return false;
                    }

                    // 存入 ThreadLocal，方便在 Service 层或 Controller 层直接获取
                    UserHolder.saveUser(username, userId);
                    return true; // 校验通过，放行
                } catch (Exception e) {
                    // 解析异常
                    log.warn("JWT 解析失败", e);
                }
            }
        }

        // 3. 校验失败，返回 401 状态码和统一结果
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=utf-8");

        Result<Object> result = Result.error(401, "未登录或登录已过期，请重新登录");
        String json = new ObjectMapper().writeValueAsString(result);

        response.getWriter().write(json);
        return false; // 拦截请求
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull Object handler, @Nullable Exception ex) throws Exception {
        // 请求结束后，移除 ThreadLocal 中的用户信息，防止内存泄漏
        UserHolder.removeUser();
    }
}
