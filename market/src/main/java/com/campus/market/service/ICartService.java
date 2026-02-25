package com.campus.market.service;

import com.campus.market.entity.Cart;
import com.campus.market.entity.Product;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * <p>
 * 购物车表 服务类
 * </p>
 *
 * @author GraduationTutor
 * @since 2026-01-14
 */
public interface ICartService extends IService<Cart> {

    /**
     * 添加商品到购物车
     * 
     * @param userId    用户ID
     * @param productId 商品ID
     * @return 是否成功
     */
    boolean add(Long userId, Long productId);

    /**
     * 从购物车移除商品
     * 
     * @param userId    用户ID
     * @param productId 商品ID
     * @return 是否成功
     */
    boolean remove(Long userId, Long productId);

    /**
     * 获取用户购物车中的商品列表
     * 
     * @param userId 用户ID
     * @return 商品列表
     */
    List<Product> listMyCart(Long userId);
}
