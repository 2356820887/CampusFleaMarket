package com.campus.market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.market.entity.ChatMessage;
import com.campus.market.entity.User;
import com.campus.market.mapper.ChatMessageMapper;
import com.campus.market.service.IChatMessageService;
import com.campus.market.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * 在线聊天记录表 服务实现类
 * </p>
 *
 * @author GraduationTutor
 * @since 2025-12-22
 */
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements IChatMessageService {

    @Autowired
    private IUserService userService;

    @Override
    public List<User> listContacts(Long userId) {
        // 1. 查询我发送的
        LambdaQueryWrapper<ChatMessage> sendWrapper = new LambdaQueryWrapper<>();
        sendWrapper.eq(ChatMessage::getSenderId, userId);
        sendWrapper.select(ChatMessage::getReceiverId);
        List<ChatMessage> sendList = list(sendWrapper);

        // 2. 查询我接收的
        LambdaQueryWrapper<ChatMessage> receiveWrapper = new LambdaQueryWrapper<>();
        receiveWrapper.eq(ChatMessage::getReceiverId, userId);
        receiveWrapper.select(ChatMessage::getSenderId);
        List<ChatMessage> receiveList = list(receiveWrapper);

        // 3. 汇总去重 ID
        Set<Long> contactIds = new HashSet<>();
        for (ChatMessage msg : sendList) {
            contactIds.add(msg.getReceiverId());
        }
        for (ChatMessage msg : receiveList) {
            contactIds.add(msg.getSenderId());
        }

        if (contactIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 4. 查询用户信息
        return userService.listByIds(contactIds);
    }

    @Override
    public List<ChatMessage> listHistory(Long userId, Long otherId) {
        // (sender = me AND receiver = other) OR (sender = other AND receiver = me)
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
                .eq(ChatMessage::getSenderId, userId).eq(ChatMessage::getReceiverId, otherId)
                .or()
                .eq(ChatMessage::getSenderId, otherId).eq(ChatMessage::getReceiverId, userId));
        wrapper.orderByAsc(ChatMessage::getCreatedAt);
        return list(wrapper);
    }
}
