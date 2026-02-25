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
 * 特色专区活动表
 * </p>
 *
 * @author GraduationTutor
 * @since 2025-12-22
 */
@Getter
@Setter
@TableName("activity_topic")
@Schema(name = "ActivityTopic对象", description = "特色专区活动表")
public class ActivityTopic implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "活动名称(如: 毕业季清仓) ")
    @TableField("title")
    private String title;

    @Schema(description = "活动简介")
    @TableField("description")
    private String description;

    @Schema(description = "活动宣传图URL")
    @TableField("banner_url")
    private String bannerUrl;

    @Schema(description = "活动开始时间")
    @TableField("start_time")
    private LocalDateTime startTime;

    @Schema(description = "活动结束时间")
    @TableField("end_time")
    private LocalDateTime endTime;

    @Schema(description = "是否展示")
    @TableField("is_active")
    private Byte isActive;
}
