package com.pixeltribe.newssys.newscomreport.model;

import com.pixeltribe.forumsys.entity.ReportType;
import com.pixeltribe.membersys.member.model.Member;
import com.pixeltribe.newssys.newscomment.model.NewsComment;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "news_com_report")
public class NewsComReport {
    @Id
    @Column(name = "NEWS_COM_REPORT_NO", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "REPORTER", nullable = false)
    private Member reporter;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "REPORT_TYPE", nullable = false)
    private ReportType reportType;

    @NotNull
    @ColumnDefault("'0'")
    @Column(name = "NEWS_COM_REPORT_STATUS", nullable = false)
    private Character newsComReportStatus;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "NCOM_NO", nullable = false)
    private NewsComment ncomNo;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "CREATE_TIME", nullable = false)
    private Instant createTime;

    @Column(name = "FINISH_TIME")
    private Instant finishTime;

}