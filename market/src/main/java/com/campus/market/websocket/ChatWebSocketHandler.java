package com.campus.market.websocket;

import com.campus.market.entity.ChatMessage;
import com.campus.market.service.IChatMessageService;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Map<Long, WebSocketSession> SESSIONS = new ConcurrentHashMap<>();

    @Autowired
    private IChatMessageService chatMessageService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            SESSIONS.put(userId, session);
            log.info("User {} connected, sessionId: {}", userId, session.getId());
        } else {
            session.close(CloseStatus.BAD_DATA);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long senderId = getUserIdFromSession(session);
        if (senderId == null) {
            return;
        }

        String payload = message.getPayload();
        Map<String, Object> data;
        try {
            data = objectMapper.readValue(payload, Map.class);
        } catch (Exception e) {
            log.error("Invalid message format", e);
            return;
        }

        Object toObj = data.get("to");
        String content = (String) data.get("content");

        if (toObj == null || !StringUtils.hasText(content)) {
            return;
        }

        Long receiverId;
        if (toObj instanceof Number) {
            receiverId = ((Number) toObj).longValue();
        } else {
            try {
                receiverId = Long.parseLong(toObj.toString());
            } catch (NumberFormatException e) {
                return;
            }
        }

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId(senderId);
        chatMessage.setReceiverId(receiverId);
        chatMessage.setContent(content);
        chatMessage.setIsRead((byte) 0);
        chatMessage.setCreatedAt(LocalDateTime.now());

        chatMessageService.save(chatMessage);

        WebSocketSession receiverSession = SESSIONS.get(receiverId);
        if (receiverSession != null && receiverSession.isOpen()) {
            Map<String, Object> response = Map.of(
                    "type", "message",
                    "data", chatMessage);
            String jsonResp = objectMapper.writeValueAsString(response);
            receiverSession.sendMessage(new TextMessage(jsonResp));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            SESSIONS.remove(userId);
            log.info("User {} disconnected", userId);
        }
    }

    private Long getUserIdFromSession(WebSocketSession session) {
        Object userIdObj = session.getAttributes().get("userId");
        if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        }
        return null;
    }
}
