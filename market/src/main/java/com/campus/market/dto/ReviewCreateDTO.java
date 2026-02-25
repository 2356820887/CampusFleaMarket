package com.campus.market.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "ReviewCreateDTO", description = "创建评价DTO")
public class ReviewCreateDTO {
    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "评分(1-5)")
    private Byte rating;

    @Schema(description = "评价内容")
    private String content;
}
