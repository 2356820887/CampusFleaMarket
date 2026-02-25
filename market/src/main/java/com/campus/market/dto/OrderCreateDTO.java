package com.campus.market.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单创建 DTO
 */
@Data
@Schema(description = "订单创建 DTO")
public class OrderCreateDTO {

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "约定交易时间")
    private LocalDateTime tradeTime;

    @Schema(description = "约定交易地点")
    private String tradeLocation;
}
