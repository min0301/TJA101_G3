package com.pixeltribe.newssys.newscomreport.model;

import com.pixeltribe.common.PageResponse;
import com.pixeltribe.common.PageResponseFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NewsComReportService {

    private final NewsComReportRepository newsComReportRepository;


    NewsComReportService(NewsComReportRepository newsComReportRepository) {
        this.newsComReportRepository = newsComReportRepository;
    }

    public PageResponse<NewsComReportDTO> findAllReport(int page, int size) {
        Page<NewsComReportDTO> reportDTOPage = newsComReportRepository.findNewsComReport(PageRequest.of(page, size));
        //0:未處理 1:已處理
        return PageResponseFactory.fromPage(reportDTOPage);
    }
}
