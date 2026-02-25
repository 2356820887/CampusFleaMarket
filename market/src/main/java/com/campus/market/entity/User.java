package com.campus.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 用户基本信息表
 * </p>
 *
 * @author GraduationTutor
 * @since 2025-12-22
 */
@Getter
@Setter
@TableName("user")
@Schema(name = "User对象", description = "用户基本信息表")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户名")
    @TableField("username")
    private String username;

    @Schema(description = "密码")
    @TableField("password")
    private String password;

    @Schema(description = "昵称")
    @TableField("nickname")
    private String nickname;

    @Schema(description = "手机号")
    @TableField("phone")
    private String phone;

    @Schema(description = "校区")
    @TableField("school_area")
    private String schoolArea;

    @Schema(description = "宿舍")
    @TableField("dormitory")
    private String dormitory;

    @Schema(description = "学号")
    @TableField("student_card_no")
    private String studentCardNo;

    @Schema(description = "个人简介")
    @TableField("bio")
    private String bio;

    @Schema(description = "信用积分")
    @TableField("credit_score")
    private Integer creditScore;

    @Schema(description = "头像URL")
    @TableField("avatar")
    private String avatar;

    @Schema(description = "角色: 用户, 管理员 ")
    @TableField("role")
    private String role;

    @Schema(description = "状态: 1-正常, 0-禁言(不可聊天/评价), -1-封号")
    @TableField("status")
    private Byte status;

    @Schema(description = "注册时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

    @Schema(description = "最后更新时间")
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
