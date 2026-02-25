package com.campus.market.controller;

import com.campus.market.common.Result;
import com.campus.market.dto.ReviewCreateDTO;
import com.campus.market.entity.OrderReview;
import com.campus.market.service.IOrderReviewService;
import com.campus.market.utils.UserHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * <p>
 * 评价及维权表 前端控制器
 * </p>
 *
 * @author GraduationTutor
 * @since 2025-12-22
 */
@Tag(name = "OrderReview", description = "评价管理")
@RestController
@RequestMapping("/orderReview")
public class OrderReviewController {

    @Autowired
    private IOrderReviewService orderReviewService;

    @Operation(summary = "提交评价", description = "买家确认收货后提交评价，订单将变为已完成状态")
    @PostMapping("/add")
    public Result<Long> addReview(@RequestBody ReviewCreateDTO createDTO) {
        Long userId = UserHolder.getUserId();
        return Result.success(orderReviewService.createReview(createDTO, userId));
    }

    @Operation(summary = "查询订单评价")
    @GetMapping("/order/{orderId}")
    public Result<OrderReview> getByOrderId(@PathVariable Long orderId) {
        return Result.success(orderReviewService.getByOrderId(orderId));
    }

    @Operation(summary = "查询商品评价列表")
    @GetMapping("/product/{productId}")
    public Result<List<OrderReview>> listByProductId(@PathVariable Long productId) {
        return Result.success(orderReviewService.listByProductId(productId));
    }

    @Operation(summary = "查询卖家收到评价列表")
    @GetMapping("/seller/{sellerId}")
    public Result<List<OrderReview>> listBySellerId(@PathVariable Long sellerId) {
        return Result.success(orderReviewService.listBySellerId(sellerId));
    }
}
