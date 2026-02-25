package com.campus.market.service;

import com.campus.market.entity.ChatMessage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 在线聊天记录表 服务类
 * </p>
 *
 * @author GraduationTutor
 * @since 2025-12-22
 */
public interface IChatMessageService extends IService<ChatMessage> {

    /**
     * 获取最近联系人列表
     */
    java.util.List<com.campus.market.entity.User> listContacts(Long userId);

    /**
     * 获取与某人的聊天记录
     */
    java.util.List<ChatMessage> listHistory(Long userId, Long otherId);
}
