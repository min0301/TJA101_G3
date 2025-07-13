package com.pixeltribe.newssys.newscomreport.controller;

import com.pixeltribe.common.PageResponse;
import com.pixeltribe.newssys.newscomreport.model.NewsComReportCreationDTO;
import com.pixeltribe.newssys.newscomreport.model.NewsComReportDTO;
import com.pixeltribe.newssys.newscomreport.model.NewsComReportService;
import com.pixeltribe.newssys.newscomreport.model.NewsComReportUpdateDTO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
class NewsComReportController {
    private final NewsComReportService newsComReportService;


    NewsComReportController(NewsComReportService newsComReportService) {
        this.newsComReportService = newsComReportService;
    }

    @GetMapping("/admin/NewsComReport")
    @Operation(summary = "取得所有新聞評論檢舉")
    public PageResponse<NewsComReportDTO> findAllReport(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size) {
        return newsComReportService.findAllReport(page, size);
    }

    @PostMapping("create/newscommentreport")
    @Operation(summary = "新增新聞評論檢舉")
    public NewsComReportCreationDTO createReport(@RequestBody NewsComReportCreationDTO creationDTO) {
        Integer reporterId = creationDTO.getReporterId();
        Integer reportTypeId = creationDTO.getReportTypeId();
        Integer newsCommentId = creationDTO.getNcomNoId();

        return newsComReportService.createReport(reporterId,  reportTypeId,  newsCommentId);
    }


    @PatchMapping("/admin/update/newscommentreport")
    @Operation(summary = "更新新聞評論檢舉")
    public NewsComReportUpdateDTO updateReport(@RequestBody @Valid NewsComReportUpdateDTO updateDTO) {
//        TODO
//        Integer reportId = updateDTO.getId();
//        Integer reporterId = updateDTO.getReporterId();
//        String reporterName = updateDTO.getReporterMemNickName();
//        Integer reportTypeId = updateDTO.getReportTypeId();
//        String reportType = updateDTO.getReportTypeRpiType();
//        Character status = updateDTO.getNewsComReportStatus();
//        Integer newsCommentId = updateDTO.getNcomNoId();
//        String newsComment = updateDTO.getNcomNoNcomCon();
//        Character commentStatus = updateDTO.getNcomNoNcomStatus();
//
//        return newsComReportService.updateReport(reportId, reporterId, reporterName, reportTypeId, reportType, status, newsCommentId, newsComment, commentStatus);

    return newsComReportService.updateReport(updateDTO);
    }


}
