package com.campus.market.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.market.common.Result;
import com.campus.market.entity.ActivityTopic;
import com.campus.market.service.IActivityTopicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 特色专区活动表 前端控制器
 * </p>
 *
 * @author GraduationTutor
 * @since 2025-12-22
 */
@Tag(name = "ActivityTopic", description = "活动专区管理")
@RestController
@RequestMapping("/activityTopic")
public class ActivityTopicController {

    @Autowired
    private IActivityTopicService activityTopicService;

    @Operation(summary = "获取当前进行中的活动 (公开)")
    @GetMapping("/list/active")
    public Result<List<ActivityTopic>> listActive() {
        LambdaQueryWrapper<ActivityTopic> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityTopic::getIsActive, 1);
        LocalDateTime now = LocalDateTime.now();
        // start_time <= now <= end_time
        // 如果 start_time 为空，默认立即开始；end_time 为空，默认永久
        wrapper.and(w -> w.le(ActivityTopic::getStartTime, now).or().isNull(ActivityTopic::getStartTime));
        wrapper.and(w -> w.ge(ActivityTopic::getEndTime, now).or().isNull(ActivityTopic::getEndTime));

        wrapper.orderByDesc(ActivityTopic::getId);
        return Result.success(activityTopicService.list(wrapper));
    }

    @Operation(summary = "获取所有活动 (管理员)")
    @GetMapping("/admin/list")
    public Result<IPage<ActivityTopic>> listAdmin(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        // TODO: 鉴权
        Page<ActivityTopic> pg = new Page<>(page, size);
        return Result.success(activityTopicService.page(pg));
    }

    @Operation(summary = "创建/更新活动 (管理员)")
    @PostMapping("/admin/save")
    public Result<String> saveActivity(@RequestBody ActivityTopic activity) {
        // TODO: 鉴权
        if (activity.getId() == null) {
            activity.setIsActive((byte) 1); // 默认激活
        }
        if (activityTopicService.saveOrUpdate(activity)) {
            return Result.success("保存成功");
        }
        return Result.error("保存失败");
    }

    @Operation(summary = "删除活动 (管理员)")
    @PostMapping("/admin/delete/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        // TODO: 鉴权
        if (activityTopicService.removeById(id)) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}
