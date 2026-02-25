package com.campus.market.controller;

import com.campus.market.common.Result;
import com.campus.market.dto.OrderCreateDTO;
import com.campus.market.entity.Orders;
import com.campus.market.service.IOrdersService;
import com.campus.market.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 交易订单表 前端控制器
 * </p>
 *
 * @author GraduationTutor
 * @since 2025-12-22
 */
@RestController
@RequestMapping("/order")
public class OrdersController {

    @Autowired
    private IOrdersService ordersService;

    /**
     * 创建订单
     * 
     * @param createDTO 订单创建信息
     * @return 订单ID
     */
    @PostMapping("/create")
    public Result<Long> create(@RequestBody OrderCreateDTO createDTO) {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录或登录已过期");
        }

        Long orderId = ordersService.createOrder(createDTO, userId);
        return Result.success(orderId);
    }

    /**
     * 支付订单
     * 
     * @param orderId 订单ID
     * @return 支付宝表单HTML
     */
    @PostMapping("/pay/{orderId}")
    public Result<String> payOrder(@PathVariable Long orderId) {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录或登录已过期");
        }
        return Result.success(ordersService.payOrder(orderId, userId));
    }

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @return 是否成功
     */
    @PostMapping("/cancel/{orderId}")
    public Result<Boolean> cancelOrder(@PathVariable Long orderId) {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录或登录已过期");
        }
        return Result.success(ordersService.cancelOrder(orderId, userId));
    }

    /**
     * 发货 (卖家)
     */
    @PostMapping("/ship/{orderId}")
    public Result<Boolean> shipOrder(@PathVariable Long orderId) {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录或登录已过期");
        }
        return Result.success(ordersService.shipOrder(orderId, userId));
    }

    /**
     * 确认收货 (买家)
     */
    @PostMapping("/confirm/{orderId}")
    public Result<Boolean> confirmReceipt(@PathVariable Long orderId) {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录或登录已过期");
        }
        return Result.success(ordersService.confirmReceipt(orderId, userId));
    }

    /**
     * 获取订单详情
     * 
     * @param id 订单ID
     * @return 订单详情
     */
    @GetMapping("/{id}")
    public Result<Orders> getDetail(
            @PathVariable Long id) {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录或登录已过期");
        }
        return Result.success(ordersService.getOrderDetail(id, userId));
    }

    /**
     * 支付宝异步通知
     * 
     * @param request 请求对象
     * @return success/fail
     */
    @PostMapping("/alipay/notify")
    public String alipayNotify(jakarta.servlet.http.HttpServletRequest request) {
        java.util.Map<String, String> params = new java.util.HashMap<>();
        java.util.Map<String, String[]> requestParams = request.getParameterMap();
        for (java.util.Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = iter.next();
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }
        return ordersService.handleAlipayNotify(params);
    }

    /**
     * 获取我的订单列表
     * 
     * @return 订单列表
     */
    @GetMapping("/my-list")
    public Result<java.util.List<Orders>> listMyOrders() {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录或登录已过期");
        }
        return Result.success(ordersService.listMyOrders(userId));
    }

    /**
     * 获取我卖出的订单列表
     */
    @GetMapping("/my-sales")
    public Result<java.util.List<Orders>> listMySales() {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录或登录已过期");
        }
        return Result.success(ordersService.listMySales(userId));
    }

    /**
     * 分页查询所有订单 (管理员)
     */
    @GetMapping("/admin/list")
    public Result<com.baomidou.mybatisplus.core.metadata.IPage<Orders>> listAllOrders(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "1") Integer page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "10") Integer size) {
        // TODO: 鉴权
        return Result.success(ordersService.listAllOrders(page, size));
    }
}
