package com.campus.market.service;

import com.campus.market.entity.Product;
import com.baomidou.mybatisplus.extension.service.IService;

import com.campus.market.dto.ProductPublishDTO;
import java.util.List;

import com.campus.market.dto.ProductQueryDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * <p>
 * 闲置物品信息表 服务类
 * </p>
 *
 * @author GraduationTutor
 * @since 2025-12-22
 */
public interface IProductService extends IService<Product> {

    /**
     * 发布商品
     * 
     * @param productDTO 商品发布信息
     * @param userId     卖家ID
     * @return 是否发布成功
     */
    boolean publish(ProductPublishDTO productDTO, Long userId);

    /**
     * 分页查询商品列表
     * 
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    IPage<Product> listProducts(ProductQueryDTO queryDTO);

    /**
     * 获取商品详情并增加浏览量
     * 
     * @param id 商品ID
     * @return 商品详情
     */
    Product getProductDetail(Long id);

    /**
     * 分页查询我的商品列表
     * 
     * @param queryDTO 查询条件
     * @param userId   当前登录用户ID
     * @return 分页结果
     */
    IPage<Product> listMyProducts(ProductQueryDTO queryDTO, Long userId);

    /**
     * 获取热门商品列表
     * 
     * @param limit 获取数量
     * @return 热门商品列表
     */
    List<Product> listHotProducts(Integer limit);

    /**
     * 更新商品信息
     * 
     * @param product 商品信息
     * @param userId  当前操作用户ID
     * @return 是否成功
     */
    boolean updateProduct(Product product, Long userId);

    /**
     * 商品下架
     * 
     * @param id     商品ID
     * @param userId 当前操作用户ID
     * @return 是否成功
     */
    boolean offShelf(Long id, Long userId);

    /**
     * 管理员下架商品
     * 
     * @param id 商品ID
     * @return 是否成功
     */
    boolean adminOffShelf(Long id);
}
