package com.campus.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 在线聊天记录表
 * </p>
 *
 * @author GraduationTutor
 * @since 2025-12-22
 */
@Getter
@Setter
@TableName("chat_message")
@Schema(name = "ChatMessage对象", description = "在线聊天记录表")
public class ChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "发送者ID")
    @TableField("sender_id")
    private Long senderId;

    @Schema(description = "接收者ID")
    @TableField("receiver_id")
    private Long receiverId;

    @Schema(description = "消息内容 ")
    @TableField("content")
    private String content;

    @Schema(description = "是否已读")
    @TableField("is_read")
    private Byte isRead;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
