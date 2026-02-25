package com.campus.market.controller;

import com.campus.market.common.Result;
import com.campus.market.entity.Product;
import com.campus.market.service.ICartService;
import com.campus.market.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 购物车 前端控制器
 * </p>
 *
 * @author GraduationTutor
 * @since 2026-01-14
 */
@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private ICartService cartService;

    @PostMapping("/add")
    public Result<String> add(@RequestBody Map<String, Long> params) {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录或登录已过期");
        }
        Long productId = params.get("productId");
        if (productId == null) {
            return Result.error("商品ID不能为空");
        }

        if (cartService.add(userId, productId)) {
            return Result.success("添加成功");
        }
        return Result.error("添加失败");
    }

    @PostMapping("/remove")
    public Result<String> remove(@RequestBody Map<String, Long> params) {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录或登录已过期");
        }
        Long productId = params.get("productId");
        if (productId == null) {
            return Result.error("商品ID不能为空");
        }

        if (cartService.remove(userId, productId)) {
            return Result.success("移除成功");
        }
        return Result.error("移除失败");
    }

    @GetMapping("/list")
    public Result<List<Product>> list() {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录或登录已过期");
        }
        return Result.success(cartService.listMyCart(userId));
    }
}
