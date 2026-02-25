package com.campus.market.service.impl;

import com.campus.market.entity.Category;
import com.campus.market.mapper.CategoryMapper;
import com.campus.market.service.ICategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 商品分类体系表 服务实现类
 * </p>
 *
 * @author GraduationTutor
 * @since 2025-12-22
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

}
