package com.campus.market.service;

import com.campus.market.entity.Orders;
import com.baomidou.mybatisplus.extension.service.IService;

import com.campus.market.dto.OrderCreateDTO;

/**
 * <p>
 * 交易订单表 服务类
 * </p>
 *
 * @author GraduationTutor
 * @since 2025-12-22
 */
public interface IOrdersService extends IService<Orders> {

    /**
     * 创建订单
     * 
     * @param createDTO 订单创建信息
     * @param userId    买家ID
     * @return 订单ID
     */
    Long createOrder(OrderCreateDTO createDTO, Long userId);

    /**
     * 支付订单
     * 
     * @param orderId 订单ID
     * @param userId  用户ID
     * @return 支付表单
     */
    String payOrder(Long orderId, Long userId);

    /**
     * 获取订单详情
     * 
     * @param id     订单ID
     * @param userId 用户ID
     * @return 订单详情
     */
    Orders getOrderDetail(Long id, Long userId);

    /**
     * 处理支付宝异步通知
     * 
     * @param params 支付宝回调参数
     * @return 是否处理成功
     */
    String handleAlipayNotify(java.util.Map<String, String> params);

    /**
     * 取消订单
     * 
     * @param orderId 订单ID
     * @param userId  用户ID
     * @return 是否成功
     */
    Boolean cancelOrder(Long orderId, Long userId);

    java.util.List<Orders> listMyOrders(Long userId);

    /**
     * 获取我卖出的订单 (卖家)
     * 
     * @param userId 卖家ID
     * @return 订单列表
     */
    java.util.List<Orders> listMySales(Long userId);

    /**
     * 分页查询所有订单 (管理员)
     * 
     * @param page 页码
     * @param size 每页大小
     * @return 订单分页列表
     */
    com.baomidou.mybatisplus.core.metadata.IPage<Orders> listAllOrders(Integer page, Integer size);

    /**
     * 发货
     * 
     * @param orderId 订单ID
     * @param userId  卖家ID
     * @return 是否成功
     */
    boolean shipOrder(Long orderId, Long userId);

    /**
     * 确认收货
     * 
     * @param orderId 订单ID
     * @param userId  买家ID
     * @return 是否成功
     */
    boolean confirmReceipt(Long orderId, Long userId);
}
