package com.campus.market.service;

import com.campus.market.entity.OrderReview;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 评价及维权表 服务类
 * </p>
 *
 * @author GraduationTutor
 * @since 2025-12-22
 */
public interface IOrderReviewService extends IService<OrderReview> {

    /**
     * 创建评价
     *
     * @param createDTO 评价信息
     * @param userId    当前用户ID
     * @return 评价ID
     */
    Long createReview(com.campus.market.dto.ReviewCreateDTO createDTO, Long userId);

    /**
     * 根据订单ID查询评价
     */
    OrderReview getByOrderId(Long orderId);

    /**
     * 根据商品ID查询评价列表
     */
    java.util.List<OrderReview> listByProductId(Long productId);

    /**
     * 根据卖家ID查询所有评价
     */
    java.util.List<OrderReview> listBySellerId(Long sellerId);
}
