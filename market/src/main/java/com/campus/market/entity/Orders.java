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
 * 交易订单表
 * </p>
 *
 * @author GraduationTutor
 * @since 2025-12-22
 */
@Getter
@Setter
@TableName("orders")
@Schema(name = "Orders对象", description = "交易订单表")
public class Orders implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "唯一订单编号")
    @TableField("order_sn")
    private String orderSn;

    @Schema(description = "关联商品ID")
    @TableField("product_id")
    private Long productId;

    @Schema(description = "买家ID")
    @TableField("buyer_id")
    private Long buyerId;

    @Schema(description = "已支付定金 ")
    @TableField("deposit_amount")
    private BigDecimal depositAmount;

    @Schema(description = "待付尾款 ")
    @TableField("final_amount")
    private BigDecimal finalAmount;

    @Schema(description = "约定交易时间 ")
    @TableField("trade_time")
    private LocalDateTime tradeTime;

    @Schema(description = "约定交易地点 ")
    @TableField("trade_location")
    private String tradeLocation;

    @Schema(description = "状态: 0-待付款, 1-已付款, 2-已确认收货, 3-已付尾款, 4-维权中, 5-已关闭 ")
    @TableField("status")
    private Byte status;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
