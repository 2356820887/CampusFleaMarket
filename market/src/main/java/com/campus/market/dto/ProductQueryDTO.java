package com.campus.market.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 商品查询 DTO
 */
@Data
@Schema(description = "商品查询 DTO")
public class ProductQueryDTO {

    @Schema(description = "页码")
    private Integer page = 1;

    @Schema(description = "每页大小")
    private Integer size = 10;

    @Schema(description = "分类ID")
    private Integer categoryId;

    @Schema(description = "活动ID")
    private Integer activityId;

    @Schema(description = "搜索关键词 (标题或描述)")
    private String keyword;

    @Schema(description = "商品状态: 0-审核中, 1-在售, 2-已下架, 3-已售出")
    private Byte status;

    @Schema(description = "排序字段: price, view_count, created_at")
    private String sortBy;

    @Schema(description = "是否升序")
    private Boolean isAsc = false;

    @Schema(description = "最低价格")
    private java.math.BigDecimal minPrice;

    @Schema(description = "最高价格")
    private java.math.BigDecimal maxPrice;

    @Schema(description = "排除的用户ID (用于首页不显示自己发布的商品)")
    private Long excludeUserId;
}
