package com.campus.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 闲置物品信息表
 * </p>
 *
 * @author GraduationTutor
 * @since 2025-12-22
 */
@Getter
@Setter
@TableName("product")
@Schema(name = "Product对象", description = "闲置物品信息表")
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "卖家ID")
    @TableField("seller_id")
    private Long sellerId;

    @Schema(description = "分类ID")
    @TableField("category_id")
    private Integer categoryId;

    @Schema(description = "参与的活动ID (0或null表示不参加)")
    @TableField("activity_id")
    private Integer activityId;

    @Schema(description = "商品标题")
    @TableField("title")
    private String title;

    @Schema(description = "详细描述/成色说明 ")
    @TableField("description")
    private String description;

    @Schema(description = "预期售价")
    @TableField("price")
    private BigDecimal price;

    @Schema(description = "新旧程度 (1-10, 10代表全新) ")
    @TableField("condition_level")
    private Byte conditionLevel;

    @Schema(description = "图片列表 (JSON字符串或URL逗号分隔) ")
    @TableField("image_urls")
    private String imageUrls;

    @Schema(description = "交易方式 ")
    @TableField("trade_type")
    private String tradeType;

    @Schema(description = "建议交易地点 ")
    @TableField("trade_location")
    private String tradeLocation;

    @Schema(description = "交易地点 ")
    @TableField("pick_up_location")
    private String pickUpLocation;

    @Schema(description = "状态: 0-审核中, 1-在售, 2-已下架, 3-已售出 ")
    @TableField("status")
    private Byte status;

    @Schema(description = "是否属于热门推荐 ")
    @TableField("is_hot")
    private Byte isHot;

    @Schema(description = "浏览次数")
    @TableField("view_count")
    private Integer viewCount;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @Schema(description = "关联的订单ID (非数据库字段)")
    @TableField(exist = false)
    private Long orderId;

    @Schema(description = "关联的订单状态 (非数据库字段)")
    @TableField(exist = false)
    private Byte orderStatus;
}
