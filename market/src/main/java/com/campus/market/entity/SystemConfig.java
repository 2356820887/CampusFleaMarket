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
 * 系统规则配置表
 * </p>
 *
 * @author GraduationTutor
 * @since 2025-12-22
 */
@Getter
@Setter
@TableName("system_config")
@Schema(name = "SystemConfig对象", description = "系统规则配置表")
public class SystemConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "配置项键(如: deposit_rate)")
    @TableField("config_key")
    private String configKey;

    @Schema(description = "配置项值")
    @TableField("config_value")
    private String configValue;

    @Schema(description = "规则说明")
    @TableField("remark")
    private String remark;
}
