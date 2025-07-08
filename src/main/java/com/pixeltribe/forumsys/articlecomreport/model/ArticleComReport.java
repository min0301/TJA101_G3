package com.pixeltribe.forumsys.articlecomreport.model;

import com.pixeltribe.forumsys.message.model.ForumMes;
import com.pixeltribe.forumsys.reporttype.model.ReportType;
import com.pixeltribe.membersys.member.model.Member;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Data
@Entity
@Table(name = "article_com_report")
public class ArticleComReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NO", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MES_NO")
    private ForumMes mesNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REPORTER")
    private Member reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RPI_NO")
    private ReportType rpiNo;

    @ColumnDefault("'0'")
    @Column(name = "ART_COM_REP_STATUS", insertable = false)
    private Character artComRepStatus;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "CREATE_TIME", insertable = false, updatable = false)
    private Instant createTime;

    @Column(name = "FINISH_TIME", insertable = false)
    private Instant finishTime;

}