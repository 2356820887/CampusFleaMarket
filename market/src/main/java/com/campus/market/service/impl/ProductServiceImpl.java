package com.campus.market.service.impl;

import com.campus.market.entity.Product;
import com.campus.market.mapper.ProductMapper;
import com.campus.market.service.IProductService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import com.campus.market.dto.ProductPublishDTO;
import com.campus.market.dto.ProductQueryDTO;
import com.campus.market.enums.ProductStatus;
import com.campus.market.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 闲置物品信息表 服务实现类
 * </p>
 *
 * @author GraduationTutor
 * @since 2025-12-22
 */
@Slf4j
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements IProductService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String HOT_PRODUCTS_KEY = "product:hot";

    @Override
    public boolean publish(ProductPublishDTO productDTO, Long userId) {
        // 基础校验
        if (productDTO.getTitle() == null || productDTO.getTitle().isEmpty()) {
            throw new BusinessException("商品标题不能为空");
        }
        if (productDTO.getPrice() == null) {
            throw new BusinessException("商品价格不能为空");
        }
        if (productDTO.getCategoryId() == null) {
            throw new BusinessException("请选择商品分类");
        }

        Product product = new Product();
        BeanUtils.copyProperties(productDTO, product);

        // 设置卖家 ID
        product.setSellerId(userId);
        // 设置初始状态 (在售)
        product.setStatus(ProductStatus.ON_SALE.getByteCode());
        // 设置发布时间
        product.setCreatedAt(LocalDateTime.now());
        // 设置初始浏览量
        product.setViewCount(0);

        boolean saved = save(product);
        if (saved) {
            // 发布成功，清除热门商品缓存（或者等待过期）
            redisTemplate.delete(HOT_PRODUCTS_KEY);
        }
        return saved;
    }

    @Override
    public IPage<Product> listProducts(ProductQueryDTO queryDTO) {
        // ... (保持不变)
        // 1. 构造分页对象
        Page<Product> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());

        // 2. 构造查询条件
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        // 分类筛选
        wrapper.eq(queryDTO.getCategoryId() != null, Product::getCategoryId, queryDTO.getCategoryId());

        // 活动筛选
        wrapper.eq(queryDTO.getActivityId() != null, Product::getActivityId, queryDTO.getActivityId());

        // 状态筛选: 如果未指定状态，默认仅显示在售 (1)
        if (queryDTO.getStatus() != null) {
            wrapper.eq(Product::getStatus, queryDTO.getStatus());
        } else {
            wrapper.eq(Product::getStatus, ProductStatus.ON_SALE.getByteCode());
        }

        // 关键词搜索 (标题或描述)
        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().trim().isEmpty()) {
            String keyword = queryDTO.getKeyword().trim();
            wrapper.and(w -> w.like(Product::getTitle, keyword)
                    .or()
                    .like(Product::getDescription, keyword));
        }

        // 价格区间筛选
        if (queryDTO.getMinPrice() != null) {
            wrapper.ge(Product::getPrice, queryDTO.getMinPrice());
        }
        if (queryDTO.getMaxPrice() != null) {
            wrapper.le(Product::getPrice, queryDTO.getMaxPrice());
        }

        // 排除指定用户发布的商品 (例如自己)
        if (queryDTO.getExcludeUserId() != null) {
            wrapper.ne(Product::getSellerId, queryDTO.getExcludeUserId());
        }

        // 排序
        // 1. 优先展示热门商品
        wrapper.orderByDesc(Product::getIsHot);

        // 2. 其次按用户选择或默认排序
        if (queryDTO.getSortBy() != null) {
            boolean isAsc = queryDTO.getIsAsc() != null && queryDTO.getIsAsc();
            switch (queryDTO.getSortBy()) {
                case "price":
                    wrapper.orderBy(true, isAsc, Product::getPrice);
                    break;
                case "view_count":
                    wrapper.orderBy(true, isAsc, Product::getViewCount);
                    break;
                case "created_at":
                    wrapper.orderBy(true, isAsc, Product::getCreatedAt);
                    break;
                default:
                    wrapper.orderByDesc(Product::getCreatedAt);
            }
        } else {
            // 默认按创建时间倒序
            wrapper.orderByDesc(Product::getCreatedAt);
        }

        // 3. 执行查询
        return page(page, wrapper);
    }

    @Override
    public Product getProductDetail(Long id) {
        // ... (保持不变)
        // 1. 查询商品
        Product product = getById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 2. 增加浏览量 (异步或直接更新，这里直接更新)
        // 使用 update view_count = view_count + 1 保证原子性
        this.update().setSql("view_count = view_count + 1").eq("id", id).update();

        // 3. 返回最新的浏览量（虽然上面更新了，但当前对象还是旧的）
        product.setViewCount(product.getViewCount() + 1);

        return product;
    }

    @Autowired
    private com.campus.market.mapper.OrdersMapper ordersMapper;

    @Override
    public IPage<Product> listMyProducts(ProductQueryDTO queryDTO, Long userId) {
        // 1. 构造分页对象
        Page<Product> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());

        // 2. 构造查询条件
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        // 核心：必须是当前用户的商品
        wrapper.eq(Product::getSellerId, userId);

        // 其他筛选条件 (复用逻辑)
        wrapper.eq(queryDTO.getCategoryId() != null, Product::getCategoryId, queryDTO.getCategoryId());
        wrapper.eq(queryDTO.getStatus() != null, Product::getStatus, queryDTO.getStatus());

        if (queryDTO.getKeyword() != null && !queryDTO.getKeyword().trim().isEmpty()) {
            String keyword = queryDTO.getKeyword().trim();
            wrapper.and(w -> w.like(Product::getTitle, keyword)
                    .or()
                    .like(Product::getDescription, keyword));
        }

        // 排序：默认按创建时间倒序
        wrapper.orderByDesc(Product::getCreatedAt);

        // 3. 执行查询
        Page<Product> resultPage = page(page, wrapper);
        List<Product> products = resultPage.getRecords();

        // 4. 为【已售出】(status=3) 的商品填充关联的订单信息
        for (Product p : products) {
            if (p.getStatus() == ProductStatus.SOLD.getByteCode()) {
                // 查询该商品的最新订单 (虽然理论上一个商品只有一个有效订单，但业务复杂时可能有多条，取最新的)
                // 这里需要 OrdersMapper
                com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.campus.market.entity.Orders> orderWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
                orderWrapper.eq(com.campus.market.entity.Orders::getProductId, p.getId());
                // 排除已关闭(CANCELLED/CLOSED)的订单，或者直接取最新的
                orderWrapper.orderByDesc(com.campus.market.entity.Orders::getCreatedAt);
                orderWrapper.last("LIMIT 1");

                com.campus.market.entity.Orders order = ordersMapper.selectOne(orderWrapper);
                if (order != null) {
                    p.setOrderId(order.getId());
                    p.setOrderStatus(order.getStatus());
                }
            }
        }

        return resultPage;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Product> listHotProducts(Integer limit) {
        String cacheKey = HOT_PRODUCTS_KEY + ":" + (limit != null ? limit : 10);

        // 1. 先从缓存获取
        List<Product> hotProducts = (List<Product>) redisTemplate.opsForValue().get(cacheKey);
        if (hotProducts != null) {
            log.info("热门商品缓存命中: {}", cacheKey);
            return hotProducts;
        }

        log.info("热门商品缓存未命中，查询数据库: {}", cacheKey);
        // 2. 缓存未命中，从数据库查询
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, ProductStatus.ON_SALE.getByteCode());
        wrapper.orderByDesc(Product::getViewCount);
        wrapper.last("LIMIT " + (limit != null ? limit : 10));

        hotProducts = list(wrapper);

        // 3. 存入缓存，设置过期时间（例如 10 分钟）
        if (hotProducts != null && !hotProducts.isEmpty()) {
            log.info("热门商品存入缓存: {}", cacheKey);
            redisTemplate.opsForValue().set(cacheKey, hotProducts, 10, TimeUnit.MINUTES);
        }

        return hotProducts;
    }

    @Override
    public boolean updateProduct(Product product, Long userId) {
        // 1. 权限校验
        Product oldProduct = getById(product.getId());
        if (oldProduct == null) {
            throw new BusinessException("商品不存在");
        }
        if (!oldProduct.getSellerId().equals(userId)) {
            throw new BusinessException("无权修改他人商品");
        }

        // 2. 更新商品
        boolean updated = updateById(product);
        if (updated) {
            // 清除热门商品缓存
            redisTemplate.delete(HOT_PRODUCTS_KEY);
        }
        return updated;
    }

    @Override
    public boolean offShelf(Long id, Long userId) {
        // 1. 权限校验
        Product oldProduct = getById(id);
        if (oldProduct == null) {
            throw new BusinessException("商品不存在");
        }
        if (!oldProduct.getSellerId().equals(userId)) {
            throw new BusinessException("无权下架他人商品");
        }

        // 2. 修改状态为已下架
        Product product = new Product();
        product.setId(id);
        product.setStatus(ProductStatus.OFF_SHELF.getByteCode());

        boolean updated = updateById(product);
        if (updated) {
            // 清除热门商品缓存
            redisTemplate.delete(HOT_PRODUCTS_KEY);
        }
        return updated;
    }

    @Override
    public boolean adminOffShelf(Long id) {
        Product product = new Product();
        product.setId(id);
        product.setStatus(ProductStatus.OFF_SHELF.getByteCode());
        boolean updated = updateById(product);
        if (updated) {
            redisTemplate.delete(HOT_PRODUCTS_KEY);
        }
        return updated;
    }
}
