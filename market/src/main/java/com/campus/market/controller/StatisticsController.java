package com.campus.market.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.market.common.Result;
import com.campus.market.entity.Orders;
import com.campus.market.entity.Product;
import com.campus.market.entity.User;
import com.campus.market.service.IOrdersService;
import com.campus.market.service.IProductService;
import com.campus.market.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 后台统计控制器
 */
@RestController
@RequestMapping("/admin/stats")
public class StatisticsController {

    @Autowired
    private IUserService userService;

    @Autowired
    private IProductService productService;

    @Autowired
    private IOrdersService ordersService;

    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        Map<String, Object> data = new HashMap<>();

        // Count Users
        long totalUsers = userService.count();
        data.put("totalUsers", totalUsers);

        // Count Products
        long totalProducts = productService.count();
        data.put("totalProducts", totalProducts);

        // Count Orders
        long totalOrders = ordersService.count();
        data.put("totalOrders", totalOrders);

        // Calculate Total Sales (For ALL finished orders status=3)
        // This is not efficient for large datasets but fine for demo
        List<Orders> finishedOrders = ordersService.list(new QueryWrapper<Orders>().eq("status", 3));
        BigDecimal totalSales = finishedOrders.stream()
                .map(Orders::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        data.put("totalSales", totalSales);

        return Result.success(data);
    }
}
