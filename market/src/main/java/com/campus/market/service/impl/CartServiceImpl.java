package com.campus.market.service.impl;

import com.campus.market.entity.Cart;
import com.campus.market.entity.Product;
import com.campus.market.mapper.CartMapper;
import com.campus.market.service.ICartService;
import com.campus.market.service.IProductService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.campus.market.common.exception.BusinessException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

/**
 * <p>
 * 购物车表 服务实现类
 * </p>
 *
 * @author GraduationTutor
 * @since 2026-01-14
 */
@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements ICartService {

    @Autowired
    private IProductService productService;

    @Override
    public boolean add(Long userId, Long productId) {
        // 1. 检查商品是否存在
        Product product = productService.getById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        // 2. 检查是否是自己的商品
        if (product.getSellerId().equals(userId)) {
            throw new BusinessException("不能添加自己的商品到购物车");
        }
        // 3. 检查商品状态
        if (product.getStatus() != 1) { // 1-在售
            throw new BusinessException("商品已下架或已售出");
        }
        // 4. 检查是否已添加
        Cart exists = getOne(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getProductId, productId));
        if (exists != null) {
            throw new BusinessException("该商品已在购物车中");
        }

        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setProductId(productId);
        return save(cart);
    }

    @Override
    public boolean remove(Long userId, Long productId) {
        return remove(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getProductId, productId));
    }

    @Override
    public List<Product> listMyCart(Long userId) {
        // 1. 获取购物车记录
        List<Cart> carts = list(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));
        if (carts.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 获取商品ID列表
        List<Long> productIds = carts.stream().map(Cart::getProductId).collect(Collectors.toList());

        // 3. 查询商品详情
        return productService.listByIds(productIds);
    }
}
