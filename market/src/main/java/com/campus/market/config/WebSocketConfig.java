package com.campus.market.config;

import com.campus.market.utils.JwtUtils;
import com.campus.market.websocket.ChatWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Configuration
@EnableWebSocket
@Slf4j
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .setAllowedOrigins("*")
                .addInterceptors(new JwtHandshakeInterceptor());
    }

    /**
     * 从查询参数中提取 token 并进行验证的拦截器
     */
    static class JwtHandshakeInterceptor implements HandshakeInterceptor {

        @Override
        public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                WebSocketHandler wsHandler, Map<String, Object> attributes) {
            if (request instanceof ServletServerHttpRequest servletRequest) {
                String query = servletRequest.getServletRequest().getQueryString();
                if (query != null && query.contains("token=")) {
                    String token = null;
                    for (String param : query.split("&")) {
                        if (param.startsWith("token=")) {
                            token = param.substring(6);
                            break;
                        }
                    }

                    if (token != null) {
                        try {
                            Long userId = JwtUtils.getUserId(token);
                            if (userId != null) {
                                attributes.put("userId", userId);
                                return true;
                            }
                        } catch (Exception e) {
                            log.error("WebSocket Token validation failed: {}", e.getMessage());
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                Exception exception) {
        }
    }
}
