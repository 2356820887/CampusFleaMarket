package com.campus.market.service;

import com.campus.market.entity.Report;
import com.campus.market.dto.ReportCreateDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * <p>
 * 违规举报记录表 服务类
 * </p>
 *
 * @author GraduationTutor
 * @since 2025-12-22
 */
public interface IReportService extends IService<Report> {

    /**
     * 提交举报
     */
    boolean createReport(ReportCreateDTO dto, Long userId);

    /**
     * 查询举报列表
     */
    IPage<Report> listReports(Integer page, Integer size, Integer status);

    /**
     * 处理举报
     */
    boolean handleReport(Long reportId, Integer status, String reply);
}
