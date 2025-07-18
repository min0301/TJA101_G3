package com.pixeltribe.newssys.newscomreport.model;

import com.pixeltribe.common.PageResponse;
import com.pixeltribe.common.PageResponseFactory;
import com.pixeltribe.forumsys.reporttype.model.ReportType;
import com.pixeltribe.forumsys.reporttype.model.ReportTypeRepository;
import com.pixeltribe.membersys.member.model.MemRepository;
import com.pixeltribe.membersys.member.model.Member;
import com.pixeltribe.newssys.newscomment.model.NewsComment;
import com.pixeltribe.newssys.newscomment.model.NewsCommentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
public class NewsComReportService {

    private final NewsComReportRepository newsComReportRepository;
    private final MemRepository memRepository;
    private final NewsCommentRepository newsCommentRepository;
    private final ReportTypeRepository reportTypeRepository;


    NewsComReportService(NewsComReportRepository newsComReportRepository, MemRepository memRepository, NewsCommentRepository newsCommentRepository, ReportTypeRepository reportTypeRepository) {
        this.newsComReportRepository = newsComReportRepository;
        this.memRepository = memRepository;
        this.newsCommentRepository = newsCommentRepository;
        this.reportTypeRepository = reportTypeRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<NewsComReportDTO> findAllReport(int page, int size ,Character status) {
        Page<NewsComReportDTO> reportDTOPage = newsComReportRepository.findNewsComReport(PageRequest.of(page, size));
        if (status != null) {
            reportDTOPage = newsComReportRepository.findByStatus(status, PageRequest.of(page, size));
        } else {
            reportDTOPage = newsComReportRepository.findNewsComReport(PageRequest.of(page, size));
        }

        return PageResponseFactory.fromPage(reportDTOPage);
    }

    @Transactional
    public NewsComReportCreationDTO createReport(Integer reporterId, Integer reportTypeId, Integer newsCommentId) {
        Member reporter = memRepository.findById(reporterId).orElseThrow(() -> new EntityNotFoundException("member not found"));
        NewsComment comment = newsCommentRepository.findById(newsCommentId).orElseThrow(() -> new EntityNotFoundException("comment not found"));
        ReportType type = reportTypeRepository.findById(reportTypeId).orElseThrow(() -> new EntityNotFoundException("reportType not found"));

        NewsComReport newsComReport = new NewsComReport();
        newsComReport.setReporter(reporter);
        newsComReport.setReportType(type);
        newsComReport.setNcomNo(comment);

        NewsComReport saved = newsComReportRepository.save(newsComReport);

        return new NewsComReportCreationDTO(saved.getId(),
                type.getId(),
                comment.getId());
    }

    @Transactional
    public NewsComReportUpdateDTO updateReport(NewsComReportUpdateDTO dto) {
        NewsComReport report = newsComReportRepository.findById(dto.getId())
                .orElseThrow(() -> new EntityNotFoundException("report not found"));

        report.setNewsComReportStatus(dto.getNewsComReportStatus());
        report.setFinishTime(Instant.now());
        if (dto.getNcomNoNcomStatus() != null) {
            NewsComment comment = report.getNcomNo();
            comment.setNcomStatus(dto.getNcomNoNcomStatus());
        }

        NewsComReport saved = newsComReportRepository.save(report);

        return new NewsComReportUpdateDTO(
                saved.getId(),
                saved.getNewsComReportStatus(),
                saved.getNcomNo().getNcomStatus()
        );
    }

    public NewsComReportDTO findReportById(Integer id) {
        NewsComReport report = newsComReportRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("report not found"));

        return new NewsComReportDTO(
                report.getId(),
                report.getReporter().getId(),
                report.getReporter().getMemNickName(),
                report.getReportType().getId(),
                report.getReportType().getRpiType(),
                report.getNewsComReportStatus(),
                report.getNcomNo().getId(),
                report.getNcomNo().getNcomCon(),
                report.getNcomNo().getNcomStatus(),
                report.getCreateTime(),
                report.getFinishTime()
        );
    }
}
