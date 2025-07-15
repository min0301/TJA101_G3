package com.pixeltribe.newssys.newscomreport.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pixeltribe.forumsys.reporttype.model.ReportType;
import com.pixeltribe.membersys.member.model.Member;
import com.pixeltribe.newssys.newscomment.model.NewsComment;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "news_com_report")
public class NewsComReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NEWS_COM_REPORT_NO", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "REPORTER", nullable = false)
    @JsonIgnore
    private Member reporter;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "REPORT_TYPE", nullable = false)
    private ReportType reportType;

    @ColumnDefault("'0'")
    @Column(name = "NEWS_COM_REPORT_STATUS", nullable = false,insertable = false)
    private Character newsComReportStatus;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "NCOM_NO", nullable = false)
    @JsonIgnore
    private NewsComment ncomNo;

    @CreationTimestamp
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "CREATE_TIME", nullable = false, updatable = false,insertable = false)
    private Instant createTime;

    @UpdateTimestamp
    @Column(name = "FINISH_TIME")
    private Instant finishTime;

}