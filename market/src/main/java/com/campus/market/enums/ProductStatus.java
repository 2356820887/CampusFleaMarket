package com.campus.market.enums;

import lombok.Getter;

/**
 * 商品状态枚举
 */
@Getter
public enum ProductStatus {
    AUDITING(0, "审核中"),
    ON_SALE(1, "在售"),
    OFF_SHELF(2, "已下架"),
    SOLD(3, "已售出");

    private final int code;
    private final String description;

    ProductStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public byte getByteCode() {
        return (byte) code;
    }
}
