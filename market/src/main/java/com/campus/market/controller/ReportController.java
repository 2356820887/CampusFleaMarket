package com.campus.market.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.market.common.Result;
import com.campus.market.dto.ReportCreateDTO;
import com.campus.market.entity.Report;
import com.campus.market.service.IReportService;
import com.campus.market.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * <p>
 * 违规举报记录表 前端控制器
 * </p>
 *
 * @author GraduationTutor
 * @since 2025-12-22
 */
@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private IReportService reportService;

    /**
     * 提交举报/维权 (买家/用户)
     */
    @PostMapping("/add")
    public Result<String> addReport(@RequestBody ReportCreateDTO dto) {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录或登录已过期");
        }
        if (reportService.createReport(dto, userId)) {
            return Result.success("举报提交成功，等待管理员审核");
        }
        return Result.error("提交失败");
    }

    /**
     * 分页查询举报列表 (管理员)
     */
    @GetMapping("/admin/list")
    public Result<IPage<Report>> listReports(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        // TODO: 鉴权
        return Result.success(reportService.listReports(page, size, status));
    }

    /**
     * 处理举报 (管理员)
     */
    @PostMapping("/admin/handle")
    public Result<String> handleReport(@RequestBody Map<String, Object> params) {
        // TODO: 鉴权
        Long reportId = Long.valueOf(params.get("reportId").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        String reply = (String) params.get("reply");

        if (reportService.handleReport(reportId, status, reply)) {
            return Result.success("处理成功");
        }
        return Result.error("处理失败");
    }
}
