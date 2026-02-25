package com.campus.market.enums;

import lombok.Getter;

/**
 * 订单状态枚举
 */
@Getter
public enum OrderStatus {
    PENDING_PAYMENT(0, "待付款"),
    PAID(1, "已付款"),
    CONFIRMED_RECEIPT(2, "已确认收货"), // 实际上也代表交易成功
    PAID_FINAL(3, "已付尾款"),
    REFUNDING(4, "维权中"),
    CLOSED(5, "已关闭"),
    SHIPPED(6, "已发货"),
    PAYING(9, "支付中");

    private final int code;
    private final String description;

    OrderStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public byte getByteCode() {
        return (byte) code;
    }
}
