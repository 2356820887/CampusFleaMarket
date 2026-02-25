package com.campus.market.service;

import com.campus.market.entity.Favorite;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 商品收藏表 服务类
 * </p>
 *
 * @author GraduationTutor
 * @since 2025-12-22
 */
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.market.entity.Product;

public interface IFavoriteService extends IService<Favorite> {

    /**
     * 添加收藏
     * 
     * @param userId    用户ID
     * @param productId 商品ID
     * @return 是否成功
     */
    boolean addFavorite(Long userId, Long productId);

    /**
     * 取消收藏
     * 
     * @param userId    用户ID
     * @param productId 商品ID
     * @return 是否成功
     */
    boolean removeFavorite(Long userId, Long productId);

    /**
     * 检查是否已收藏
     * 
     * @param userId    用户ID
     * @param productId 商品ID
     * @return true-已收藏，false-未收藏
     */
    boolean isFavorite(Long userId, Long productId);

    /**
     * 获取我的收藏列表
     * 
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页数量
     * @return 商品列表
     */
    IPage<Product> listMyFavorites(Long userId, Integer page, Integer size);
}
