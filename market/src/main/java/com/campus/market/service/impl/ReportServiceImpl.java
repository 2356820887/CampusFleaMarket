package com.campus.market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.market.dto.ReportCreateDTO;
import com.campus.market.entity.Report;
import com.campus.market.mapper.ReportMapper;
import com.campus.market.service.IReportService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * 违规举报记录表 服务实现类
 * </p>
 *
 * @author GraduationTutor
 * @since 2025-12-22
 */
@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements IReportService {

    @Override
    public boolean createReport(ReportCreateDTO dto, Long userId) {
        Report report = new Report();
        report.setReporterId(userId);
        report.setTargetType(dto.getTargetType());
        report.setTargetId(dto.getTargetId());
        report.setReason(dto.getReason());
        report.setEvidenceImages(dto.getEvidenceImages());
        report.setStatus((byte) 0); // 待处理
        report.setCreatedAt(LocalDateTime.now());

        return save(report);
    }

    @Override
    public IPage<Report> listReports(Integer page, Integer size, Integer status) {
        Page<Report> pg = new Page<>(page, size);
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Report::getStatus, status.byteValue());
        }
        wrapper.orderByDesc(Report::getCreatedAt);
        return page(pg, wrapper);
    }

    @Override
    public boolean handleReport(Long reportId, Integer status, String reply) {
        Report report = getById(reportId);
        if (report == null) {
            return false;
        }
        report.setStatus(status.byteValue());
        report.setAdminReply(reply);
        return updateById(report);
    }
}
