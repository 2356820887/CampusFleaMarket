package com.campus.market.service.impl;

import com.campus.market.entity.SystemConfig;
import com.campus.market.mapper.SystemConfigMapper;
import com.campus.market.service.ISystemConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

/**
 * <p>
 * 系统规则配置表 服务实现类
 * </p>
 *
 * @author GraduationTutor
 * @since 2025-12-22
 */
@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig>
        implements ISystemConfigService {

    @Override
    public String getValue(String key) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, key);
        SystemConfig config = getOne(wrapper);
        return config != null ? config.getConfigValue() : null;
    }
}
