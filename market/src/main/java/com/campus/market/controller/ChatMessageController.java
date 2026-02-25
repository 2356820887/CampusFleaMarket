package com.campus.market.controller;

import com.campus.market.common.Result;
import com.campus.market.entity.ChatMessage;
import com.campus.market.entity.User;
import com.campus.market.service.IChatMessageService;
import com.campus.market.utils.UserHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 在线聊天 前端控制器
 * </p>
 *
 * @author GraduationTutor
 * @since 2025-12-22
 */
@Tag(name = "ChatMessage", description = "在线聊天")
@RestController
@RequestMapping("/chatMessage")
public class ChatMessageController {

    @Autowired
    private IChatMessageService chatMessageService;

    @Operation(summary = "获取联系人列表")
    @GetMapping("/contacts")
    public Result<List<User>> listContacts() {
        Long userId = UserHolder.getUserId();
        return Result.success(chatMessageService.listContacts(userId));
    }

    @Operation(summary = "获取聊天记录")
    @GetMapping("/history")
    public Result<List<ChatMessage>> listHistory(@RequestParam Long otherId) {
        Long userId = UserHolder.getUserId();
        return Result.success(chatMessageService.listHistory(userId, otherId));
    }
}
