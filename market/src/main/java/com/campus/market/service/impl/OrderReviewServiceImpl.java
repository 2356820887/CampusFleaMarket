package com.campus.market.service.impl;

import com.campus.market.entity.OrderReview;
import com.campus.market.mapper.OrderReviewMapper;
import com.campus.market.service.IOrderReviewService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import com.campus.market.common.exception.BusinessException;
import com.campus.market.dto.ReviewCreateDTO;
import com.campus.market.entity.Orders;
import com.campus.market.entity.Product;
import com.campus.market.enums.OrderStatus;
import com.campus.market.service.IOrdersService;
import com.campus.market.service.IProductService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 评价及维权表 服务实现类
 * </p>
 *
 * @author GraduationTutor
 * @since 2025-12-22
 */
@Service
public class OrderReviewServiceImpl extends ServiceImpl<OrderReviewMapper, OrderReview> implements IOrderReviewService {

    @Autowired
    private IOrdersService ordersService;

    @Autowired
    private IProductService productService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createReview(ReviewCreateDTO createDTO, Long userId) {
        // 1. 校验订单
        Orders order = ordersService.getById(createDTO.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException("无权评价他人订单");
        }

        // 2. 校验状态：必须是已确认收货(2)
        if (order.getStatus() != OrderStatus.CONFIRMED_RECEIPT.getByteCode()) {
            throw new BusinessException("订单状态不满足评价条件");
        }

        // 3. 检查是否已评价
        Long count = lambdaQuery().eq(OrderReview::getOrderId, order.getId()).count();
        if (count > 0) {
            throw new BusinessException("该订单已评价，请勿重复操作");
        }

        // 4. 保存评价
        OrderReview review = new OrderReview();
        review.setOrderId(order.getId());
        review.setRating(createDTO.getRating());
        review.setContent(createDTO.getContent());
        review.setIsDispute((byte) 0);
        review.setCreatedAt(LocalDateTime.now());
        save(review);

        // 5. 更新订单状态为已完成 (3)
        order.setStatus(OrderStatus.PAID_FINAL.getByteCode()); // 借用 PENDING_FINAL(3) 作为 COMPLETED
        order.setUpdatedAt(LocalDateTime.now());
        ordersService.updateById(order);

        return review.getId();
    }

    @Override
    public OrderReview getByOrderId(Long orderId) {
        return lambdaQuery().eq(OrderReview::getOrderId, orderId).one();
    }

    @Override
    public List<OrderReview> listByProductId(Long productId) {
        // 先找到该商品的所有订单
        List<Orders> ordersList = ordersService.lambdaQuery()
                .eq(Orders::getProductId, productId)
                .list();
        if (ordersList.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        List<Long> orderIds = ordersList.stream().map(Orders::getId).collect(Collectors.toList());
        return lambdaQuery().in(OrderReview::getOrderId, orderIds).orderByDesc(OrderReview::getCreatedAt).list();
    }

    @Override
    public List<OrderReview> listBySellerId(Long sellerId) {
        // 先找到该卖家的所有商品
        List<Product> products = productService.lambdaQuery()
                .eq(Product::getSellerId, sellerId)
                .list();
        if (products.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        List<Long> productIds = products.stream().map(Product::getId).collect(Collectors.toList());

        // 再找到这些商品对应的所有订单
        List<Orders> ordersList = ordersService.lambdaQuery()
                .in(Orders::getProductId, productIds)
                .list();
        if (ordersList.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        List<Long> orderIds = ordersList.stream().map(Orders::getId).collect(Collectors.toList());

        return lambdaQuery().in(OrderReview::getOrderId, orderIds).orderByDesc(OrderReview::getCreatedAt).list();
    }
}
