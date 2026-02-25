package com.campus.market.controller;

import com.campus.market.common.Result;
import com.campus.market.entity.Category;
import com.campus.market.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 商品分类体系表 前端控制器
 * </p>
 *
 * @author GraduationTutor
 * @since 2025-12-22
 */
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private ICategoryService categoryService;

    /**
     * 获取全部分类列表
     * 
     * @return 分类列表
     */
    @GetMapping("/list")
    public Result<List<Category>> list() {
        return Result.success(categoryService.list());
    }

    /**
     * 添加分类
     * 
     * @param category 分类信息
     * @return 成功信息
     */
    @PostMapping("/add")
    public Result<String> add(@RequestBody Category category) {
        if (categoryService.save(category)) {
            return Result.success("添加成功");
        }
        return Result.error("添加失败");
    }

    /**
     * 更新分类
     * 
     * @param category 分类信息
     * @return 成功信息
     */
    @PostMapping("/update")
    public Result<String> update(@RequestBody Category category) {
        if (categoryService.updateById(category)) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    /**
     * 删除分类
     * 
     * @param id 分类ID
     * @return 成功信息
     */
    @PostMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        if (categoryService.removeById(id)) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}
