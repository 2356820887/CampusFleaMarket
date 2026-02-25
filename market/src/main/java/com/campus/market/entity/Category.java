package com.campus.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 商品分类体系表
 * </p>
 *
 * @author GraduationTutor
 * @since 2025-12-22
 */
@Getter
@Setter
@TableName("category")
@Schema(name = "Category对象", description = "商品分类体系表")
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "分类名称")
    @TableField("name")
    private String name;

    @Schema(description = "父分类ID")
    @TableField("parent_id")
    private Integer parentId;

    @Schema(description = "排序权重")
    @TableField("sort")
    private Integer sort;

    @Schema(description = "逻辑删除标识")
    @TableField("is_deleted")
    private Byte isDeleted;
}
