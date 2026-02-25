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
 * 商品收藏表
 * </p>
 *
 * @author GraduationTutor
 * @since 2025-12-22
 */
@Getter
@Setter
@TableName("favorite")
@Schema(name = "Favorite对象", description = "商品收藏表")
public class Favorite implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "商品ID")
    @TableField("product_id")
    private Long productId;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
