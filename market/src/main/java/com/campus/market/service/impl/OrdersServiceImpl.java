package com.campus.market.service.impl;

import com.campus.market.entity.Orders;
import com.campus.market.mapper.OrdersMapper;
import com.campus.market.service.IOrdersService;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import com.campus.market.common.exception.BusinessException;
import com.campus.market.config.AlipayConfig;
import com.campus.market.dto.OrderCreateDTO;
import com.campus.market.entity.Product;
import com.campus.market.enums.OrderStatus;
import com.campus.market.enums.ProductStatus;
import com.campus.market.service.IProductService;

import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 交易订单表 服务实现类
 * </p>
 *
 * @author GraduationTutor
 * @since 2025-12-22
 */
@Slf4j
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements IOrdersService {

    @Autowired
    private IProductService productService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(OrderCreateDTO createDTO, Long userId) {
        // 1. 校验商品
        Product product = productService.getById(createDTO.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (product.getStatus() != ProductStatus.ON_SALE.getByteCode()) {
            throw new BusinessException("商品未在售");
        }
        if (product.getSellerId().equals(userId)) {
            throw new BusinessException("不能购买自己的商品");
        }

        // 2. 计算金额
        // 全额支付，无定金
        BigDecimal price = product.getPrice();
        BigDecimal depositAmount = BigDecimal.ZERO;
        BigDecimal finalAmount = price;

        // 3. 生成订单
        Orders order = new Orders();
        order.setOrderSn(generateOrderSn());
        order.setProductId(product.getId());
        order.setBuyerId(userId);
        order.setDepositAmount(depositAmount);
        order.setFinalAmount(finalAmount);
        order.setTradeTime(createDTO.getTradeTime());
        order.setTradeLocation(createDTO.getTradeLocation());
        order.setStatus(OrderStatus.PENDING_PAYMENT.getByteCode());
        order.setCreatedAt(LocalDateTime.now());

        save(order);

        // 4. 下单后立即将商品标记为已售出 (3)
        product.setStatus(ProductStatus.SOLD.getByteCode());
        productService.updateProduct(product, product.getSellerId());

        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelOrder(Long orderId, Long userId) {
        // 1. 校验订单
        Orders order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException("无权取消他人订单");
        }

        // 2. 只有待付款或支付中可以取消
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT.getByteCode()
                && order.getStatus() != OrderStatus.PAYING.getByteCode()) {
            throw new BusinessException("当前状态无法取消订单");
        }

        // 3. 更新订单状态为已关闭
        order.setStatus(OrderStatus.CLOSED.getByteCode());
        order.setUpdatedAt(LocalDateTime.now());
        updateById(order);

        // 4. 恢复商品状态为在售
        Product product = productService.getById(order.getProductId());
        if (product != null) {
            product.setStatus(ProductStatus.ON_SALE.getByteCode());
            // 使用 updateProduct 以便触发缓存清理等逻辑 (传入 sellerId 以通过内部校验)
            productService.updateProduct(product, product.getSellerId());
        }

        return true;
    }

    /**
     * 生成订单编号: ORD + 时间戳 + 随机数
     */
    private String generateOrderSn() {
        return System.currentTimeMillis() + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }

    @Override
    public String payOrder(Long orderId, Long userId) {
        // 1. 校验订单
        Orders order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException("无权操作他人订单");
        }
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT.getByteCode()
                && order.getStatus() != OrderStatus.PAYING.getByteCode()) {
            throw new BusinessException("订单状态不正确");
        }

        // 2. 更新状态为支付中
        order.setStatus(OrderStatus.PAYING.getByteCode());
        updateById(order);

        // 3. 调用支付宝接口
        try {
            return callAlipay(order);
        } catch (Exception e) {
            // 如果发起支付失败，可以考虑回滚状态，或者保留支付中状态等待超时关闭
            // 这里简单处理，抛出异常前不回滚，允许用户重试
            throw new BusinessException("支付发起失败: " + e.getMessage());
        }
    }

    @Override
    public Orders getOrderDetail(Long id, Long userId) {
        Orders order = getById(id);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        // 买家和卖家都可以查看订单详情
        Product product = productService.getById(order.getProductId());
        if (!order.getBuyerId().equals(userId) && !product.getSellerId().equals(userId)) {
            throw new BusinessException("无权查看该订单");
        }
        return order;
    }

    @Autowired
    private AlipayConfig alipayConfig;

    private String callAlipay(Orders order) throws Exception {
        AlipayClient alipayClient = new DefaultAlipayClient(
                "https://openapi-sandbox.dl.alipaydev.com/gateway.do",
                alipayConfig.getAppId(),
                alipayConfig.getPrivateKey(),
                "json",
                "utf-8",
                alipayConfig.getPublicKey(),
                "RSA2");

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(alipayConfig.getNotifyUrl());
        request.setReturnUrl(alipayConfig.getReturnUrl());
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", order.getOrderSn());
        bizContent.put("total_amount", order.getFinalAmount().toString());
        bizContent.put("subject", "订单支付-" + order.getOrderSn());
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        request.setBizContent(bizContent.toString());
        System.out.println("bizContent:" + bizContent.toString());

        return alipayClient.pageExecute(request).getBody();
    }

    @Override
    public String handleAlipayNotify(java.util.Map<String, String> params) {
        System.out.println("收到支付宝回调啦！");
        try {
            // 1. 验签
            boolean signVerified = com.alipay.api.internal.util.AlipaySignature.rsaCheckV1(
                    params,
                    alipayConfig.getPublicKey().trim(),
                    "utf-8",
                    "RSA2");

            if (!signVerified) {
                log.error("支付宝回调验签失败");
                return "fail";
            }

            // 2. 获取关键参数
            String outTradeNo = params.get("out_trade_no");
            String tradeStatus = params.get("trade_status");
            String totalAmount = params.get("total_amount");

            // 3. 校验订单
            // outTradeNo 即为 orderSn
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Orders> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            wrapper.eq(Orders::getOrderSn, outTradeNo);
            Orders order = getOne(wrapper);

            if (order == null) {
                log.error("支付宝回调订单不存在: {}", outTradeNo);
                return "fail";
            }

            // 4. 校验金额 (注意：支付宝返回的是字符串，需要转 BigDecimal 比较)
            if (new BigDecimal(totalAmount).compareTo(order.getFinalAmount()) != 0) {
                log.error("支付宝回调金额不一致: order={}, notify={}", order.getFinalAmount(), totalAmount);
                return "fail";
            }

            // 5. 处理业务逻辑
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                // 只有在待付款或支付中状态下才处理
                if (order.getStatus() == OrderStatus.PENDING_PAYMENT.getByteCode() ||
                        order.getStatus() == OrderStatus.PAYING.getByteCode()) {

                    order.setStatus(OrderStatus.PAID.getByteCode());
                    order.setUpdatedAt(LocalDateTime.now());
                    updateById(order);

                    // 6. 同步修改商品状态为已售出
                    Product product = productService.getById(order.getProductId());
                    if (product != null) {
                        product.setStatus(ProductStatus.SOLD.getByteCode());
                        productService.updateById(product);
                        log.info("商品状态已更新为已售出: productId={}", product.getId());
                    }

                    log.info("订单支付成功: {}", outTradeNo);
                }
            }

            return "success";
        } catch (Exception e) {
            log.error("处理支付宝回调异常", e);
            return "fail";
        }
    }

    @Override
    public java.util.List<Orders> listMyOrders(Long userId) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Orders> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(Orders::getBuyerId, userId);
        wrapper.orderByDesc(Orders::getCreatedAt);
        return list(wrapper);
    }

    @Override
    public java.util.List<Orders> listMySales(Long userId) {
        // 1. 先查出该用户发布的所有商品 ID
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product> productWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        productWrapper.eq(Product::getSellerId, userId);
        java.util.List<Product> products = productService.list(productWrapper);

        if (products == null || products.isEmpty()) {
            return new java.util.ArrayList<>();
        }

        java.util.List<Long> productIds = products.stream().map(Product::getId)
                .collect(java.util.stream.Collectors.toList());

        // 2. 根据商品ID查询订单
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Orders> orderWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        orderWrapper.in(Orders::getProductId, productIds);
        orderWrapper.orderByDesc(Orders::getCreatedAt);

        return list(orderWrapper);
    }

    @Override
    public com.baomidou.mybatisplus.core.metadata.IPage<Orders> listAllOrders(Integer page, Integer size) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Orders> pageParam = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                page, size);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Orders> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.orderByDesc(Orders::getCreatedAt);
        return this.page(pageParam, wrapper);
    }

    @Override
    public boolean shipOrder(Long orderId, Long userId) {
        Orders order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 校验是否是卖家
        Product product = productService.getById(order.getProductId());
        if (!product.getSellerId().equals(userId)) {
            throw new BusinessException("无权操作他人订单");
        }

        // 只有已付款的订单可以发货
        if (order.getStatus() != OrderStatus.PAID.getByteCode()) {
            throw new BusinessException("订单状态不满足发货条件");
        }

        order.setStatus(OrderStatus.SHIPPED.getByteCode());
        order.setUpdatedAt(LocalDateTime.now());
        return updateById(order);
    }

    @Override
    public boolean confirmReceipt(Long orderId, Long userId) {
        Orders order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 校验买家
        if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException("无权操作他人订单");
        }

        // 只有已发货的订单可以确认收货
        if (order.getStatus() != OrderStatus.SHIPPED.getByteCode()) {
            throw new BusinessException("订单状态不满足确认收货条件");
        }

        order.setStatus(OrderStatus.CONFIRMED_RECEIPT.getByteCode());
        order.setUpdatedAt(LocalDateTime.now());
        return updateById(order);
    }
}
