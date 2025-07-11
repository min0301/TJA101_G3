package com.pixeltribe.newssys.newscomreport.controller;

import com.pixeltribe.common.PageResponse;
import com.pixeltribe.newssys.newscomreport.model.NewsComReportDTO;
import com.pixeltribe.newssys.newscomreport.model.NewsComReportService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
class NewsComReportController {
    private final NewsComReportService newsComReportService;


    NewsComReportController(NewsComReportService newsComReportService) {
        this.newsComReportService = newsComReportService;
    }

    @GetMapping("/NewsComReport")
    public PageResponse<NewsComReportDTO> findAllReport(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size) {
        return newsComReportService.findAllReport(page,size);
    }
}
