package com.campus.market.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.market.common.Result;
import com.campus.market.entity.Product;
import com.campus.market.service.IFavoriteService;
import com.campus.market.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 商品收藏表 前端控制器
 * </p>
 *
 * @author GraduationTutor
 * @since 2025-12-22
 */
@RestController
@RequestMapping("/favorite")
public class FavoriteController {

    @Autowired
    private IFavoriteService favoriteService;

    /**
     * 添加收藏
     */
    @PostMapping("/add/{productId}")
    public Result<String> addFavorite(@PathVariable Long productId) {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        if (favoriteService.addFavorite(userId, productId)) {
            return Result.success("收藏成功");
        }
        return Result.error("收藏失败");
    }

    /**
     * 取消收藏
     */
    @PostMapping("/remove/{productId}")
    public Result<String> removeFavorite(@PathVariable Long productId) {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        if (favoriteService.removeFavorite(userId, productId)) {
            return Result.success("已取消收藏");
        }
        return Result.error("取消失败");
    }

    /**
     * 检查是否收藏
     */
    @GetMapping("/check/{productId}")
    public Result<Boolean> checkFavorite(@PathVariable Long productId) {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.success(false);
        }
        return Result.success(favoriteService.isFavorite(userId, productId));
    }

    /**
     * 我的收藏列表
     */
    @GetMapping("/list")
    public Result<IPage<Product>> listMyFavorites(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        return Result.success(favoriteService.listMyFavorites(userId, page, size));
    }
}
