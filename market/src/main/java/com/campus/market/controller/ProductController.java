package com.campus.market.controller;

import com.campus.market.common.Result;
import com.campus.market.service.IProductService;
import com.campus.market.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import com.campus.market.dto.ProductPublishDTO;
import com.campus.market.dto.ProductQueryDTO;
import com.campus.market.entity.Product;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 闲置物品信息表 前端控制器
 * </p>
 *
 * @author GraduationTutor
 * @since 2025-12-22
 */
@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private IProductService productService;

    /**
     * 发布商品
     * 
     * @param productDTO 商品发布信息
     * @return 统一结果封装
     */
    @PostMapping("/publish")
    public Result<String> publish(@RequestBody ProductPublishDTO productDTO) {
        // 1. 获取当前登录用户 ID
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录或登录已过期");
        }

        // 2. 调用 Service
        if (productService.publish(productDTO, userId)) {
            return Result.success("商品发布成功");
        }

        return Result.error("发布失败，请稍后重试");
    }

    /**
     * 分页查询商品列表
     * 
     * @param queryDTO 查询条件
     * @return 商品分页列表
     */
    @GetMapping("/list")
    public Result<IPage<Product>> list(ProductQueryDTO queryDTO) {
        // 如果用户已登录，设置排除自己的商品
        Long userId = UserHolder.getUserId();
        if (userId != null) {
            queryDTO.setExcludeUserId(userId);
        }
        return Result.success(productService.listProducts(queryDTO));
    }

    /**
     * 获取商品详情
     * 
     * @param id 商品ID
     * @return 商品详情
     */
    @GetMapping("/{id}")
    public Result<Product> getDetail(@PathVariable Long id) {
        return Result.success(productService.getProductDetail(id));
    }

    /**
     * 查询当前登录用户的商品列表
     * 
     * @param queryDTO 查询条件
     * @return 商品分页列表
     */
    @GetMapping("/my-list")
    public Result<IPage<Product>> listMyProducts(ProductQueryDTO queryDTO) {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录或登录已过期");
        }
        return Result.success(productService.listMyProducts(queryDTO, userId));
    }

    /**
     * 获取热门商品列表
     * 
     * @param limit 获取数量，默认 10
     * @return 热门商品列表
     */
    @GetMapping("/hot")
    public Result<List<Product>> listHotProducts(
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        return Result.success(productService.listHotProducts(limit));
    }

    /**
     * 更新商品信息
     * 
     * @param product 商品信息
     * @return 统一结果封装
     */
    @PostMapping("/update")
    public Result<String> update(@RequestBody Product product) {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录或登录已过期");
        }

        if (productService.updateProduct(product, userId)) {
            return Result.success("商品更新成功");
        }
        return Result.error("更新失败");
    }

    /**
     * 下架商品
     * 
     * @param id 商品ID
     * @return 统一结果封装
     */
    @PostMapping("/off-shelf/{id}")
    public Result<String> offShelf(@PathVariable Long id) {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录或登录已过期");
        }

        if (productService.offShelf(id, userId)) {
            return Result.success("商品已下架");
        }
        return Result.error("操作失败");
    }

    /**
     * 管理员下架商品
     */
    @PostMapping("/admin/off-shelf/{id}")
    public Result<String> adminOffShelf(@PathVariable Long id) {
        // TODO: 鉴权
        if (productService.adminOffShelf(id)) {
            return Result.success("商品已强制下架");
        }
        return Result.error("操作失败");
    }
}
