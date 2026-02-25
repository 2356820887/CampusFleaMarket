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
 * 违规举报记录表
 * </p>
 *
 * @author GraduationTutor
 * @since 2025-12-22
 */
@Getter
@Setter
@TableName("report")
@Schema(name = "Report对象", description = "违规举报记录表")
public class Report implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "举报人ID")
    @TableField("reporter_id")
    private Long reporterId;

    @Schema(description = "举报对象类型")
    @TableField("target_type")
    private String targetType;

    @Schema(description = "被举报对象主键")
    @TableField("target_id")
    private Long targetId;

    @Schema(description = "举报原因描述")
    @TableField("reason")
    private String reason;

    @Schema(description = "证据图片 (逗号分隔URL)")
    @TableField("evidence_images")
    private String evidenceImages;

    @Schema(description = "处理状态: 0-待处理, 1-已生效(通过), 2-已驳回")
    @TableField("status")
    private Byte status;

    @Schema(description = "管理员回复/处理备注")
    @TableField("admin_reply")
    private String adminReply;

    @TableField("created_at")
    private LocalDateTime createdAt;

}
