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
 * 评价及维权表
 * </p>
 *
 * @author GraduationTutor
 * @since 2025-12-22
 */
@Getter
@Setter
@TableName("order_review")
@Schema(name = "OrderReview对象", description = "评价及维权表")
public class OrderReview implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "关联订单ID")
    @TableField("order_id")
    private Long orderId;

    @Schema(description = "成色/服务评分(1-5) ")
    @TableField("rating")
    private Byte rating;

    @Schema(description = "文字评价内容 ")
    @TableField("content")
    private String content;

    @Schema(description = "维权证据图片路径(JSON数组) ")
    @TableField("evidence_pics")
    private String evidencePics;

    @Schema(description = "是否发起维权介入 ")
    @TableField("is_dispute")
    private Byte isDispute;

    @Schema(description = "平台介入处理结果 ")
    @TableField("admin_reply")
    private String adminReply;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
