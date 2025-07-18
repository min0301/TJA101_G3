package com.pixeltribe.newssys.newscomreport.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NewsComReportRepository extends JpaRepository<NewsComReport, Integer> {
    NewsComReport findFirstByIdOrderByCreateTimeAsc(Integer id);

    List<NewsComReportDTO> findAllByNewsComReportStatusMatchesOrderByCreateTimeAsc(Character newsComReportStatus, Pageable pageable);

    @Query("""
            select new com.pixeltribe.newssys.newscomreport.model.NewsComReportDTO(
                        nr.id,
                        nr.reporter.id,
                        nr.reportType.id,
                        nr.reportType.rpiType,
                        nr.newsComReportStatus,
                        nr.ncomNo.id,
                        nr.ncomNo.ncomCon,
                        nr.ncomNo.ncomStatus,
                        nr.createTime,
                        nr.finishTime
                        )
                        from NewsComReport nr
                        order by nr.createTime asc 
            """
    )
    Page<NewsComReportDTO> findNewsComReport(Pageable p);

    // NewsComReportRepository.java
    @Query("""
    SELECT new com.pixeltribe.newssys.newscomreport.model.NewsComReportDTO(
        nr.id,
        nr.reporter.id,
        nr.reportType.id,
        nr.reportType.rpiType,
        nr.newsComReportStatus,
        nr.ncomNo.id,
        nr.ncomNo.ncomCon,
        nr.ncomNo.ncomStatus,
        nr.createTime,
        nr.finishTime
    )
    FROM NewsComReport nr
    WHERE (:status IS NULL OR nr.newsComReportStatus = :status)
    ORDER BY nr.createTime ASC
""")
    Page<NewsComReportDTO> findByStatus(@Param("status") Character status, Pageable pageable);

}