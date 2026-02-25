package com.campus.market.common;

import lombok.Data;

/**
 * 统一接口返回对象
 * 规范：所有的 Controller 必须返回这个对象
 */
@Data
public class Result<T> {
    private Integer code;    // 状态码：200-成功，500-服务器错误，401-未登录等
    private String message;  // 提示信息
    private T data;          // 核心数据体

    // 成功静态方法
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    // 失败静态方法
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    // 自定义状态码失败方法
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
