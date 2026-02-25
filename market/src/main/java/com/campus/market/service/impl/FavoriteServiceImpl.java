package com.campus.market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.market.entity.Favorite;
import com.campus.market.entity.Product;
import com.campus.market.mapper.FavoriteMapper;
import com.campus.market.service.IFavoriteService;
import com.campus.market.service.IProductService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 商品收藏表 服务实现类
 * </p>
 *
 * @author GraduationTutor
 * @since 2025-12-22
 */
@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements IFavoriteService {

    @Autowired
    private IProductService productService;

    @Override
    public boolean addFavorite(Long userId, Long productId) {
        // 先检查是否已收藏
        if (isFavorite(userId, productId)) {
            return true;
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setProductId(productId);
        favorite.setCreatedAt(LocalDateTime.now());
        return save(favorite);
    }

    @Override
    public boolean removeFavorite(Long userId, Long productId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId);
        wrapper.eq(Favorite::getProductId, productId);
        return remove(wrapper);
    }

    @Override
    public boolean isFavorite(Long userId, Long productId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId);
        wrapper.eq(Favorite::getProductId, productId);
        return count(wrapper) > 0;
    }

    @Override
    public IPage<Product> listMyFavorites(Long userId, Integer page, Integer size) {
        // 1. 分页查询收藏记录
        Page<Favorite> favoritePage = new Page<>(page, size);
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreatedAt);

        Page<Favorite> resultPage = page(favoritePage, wrapper);

        // 2. 获取商品ID列表
        List<Long> productIds = resultPage.getRecords().stream()
                .map(Favorite::getProductId)
                .collect(Collectors.toList());

        // 3. 构造返回结果
        IPage<Product> productPage = new Page<>();
        productPage.setCurrent(resultPage.getCurrent());
        productPage.setSize(resultPage.getSize());
        productPage.setTotal(resultPage.getTotal());
        productPage.setPages(resultPage.getPages());

        if (productIds.isEmpty()) {
            return productPage;
        }

        // 4. 查询商品详情
        List<Product> products = productService.listByIds(productIds);

        // 保持收藏顺序
        java.util.Map<Long, Product> productMap = products.stream().collect(Collectors.toMap(Product::getId, p -> p));
        List<Product> sortedProducts = productIds.stream()
                .map(productMap::get)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        productPage.setRecords(sortedProducts);

        return productPage;
    }
}
