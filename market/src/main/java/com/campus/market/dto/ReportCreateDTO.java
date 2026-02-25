package com.campus.market.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "举报/维权创建 DTO")
public class ReportCreateDTO {

    @Schema(description = "举报对象类型: ORDER(订单维权), PRODUCT(商品举报), USER(用户举报)", required = true)
    private String targetType;

    @Schema(description = "被举报对象ID (订单ID/商品ID/用户ID)", required = true)
    private Long targetId;

    @Schema(description = "举报/维权原因", required = true)
    private String reason;

    @Schema(description = "证据图片 (逗号分隔URL)")
    private String evidenceImages;
}
