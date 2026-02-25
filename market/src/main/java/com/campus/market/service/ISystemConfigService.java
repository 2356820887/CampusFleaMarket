package com.campus.market.service;

import com.campus.market.entity.SystemConfig;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 系统规则配置表 服务类
 * </p>
 *
 * @author GraduationTutor
 * @since 2025-12-22
 */
public interface ISystemConfigService extends IService<SystemConfig> {

    /**
     * 根据键获取配置值
     * 
     * @param key 配置键
     * @return 配置值
     */
    String getValue(String key);
}
