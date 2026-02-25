package com.campus.market.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品发布 DTO
 */
@Data
@Schema(description = "商品发布 DTO")
public class ProductPublishDTO {

    @Schema(description = "分类ID")
    private Integer categoryId;

    @Schema(description = "活动ID")
    private Integer activityId;

    @Schema(description = "商品标题")
    private String title;

    @Schema(description = "详细描述/成色说明")
    private String description;

    @Schema(description = "预期售价")
    private BigDecimal price;

    @Schema(description = "新旧程度 (1-10, 10代表全新)")
    private Byte conditionLevel;

    @Schema(description = "图片列表 (JSON字符串或URL逗号分隔)")
    private String imageUrls;

    @Schema(description = "交易方式")
    private String tradeType;

    @Schema(description = "建议交易地点")
    private String tradeLocation;

    @Schema(description = "交易地点")
    private String pickUpLocation;
}
