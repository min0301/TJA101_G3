package com.pixeltribe.forumsys.articlereport.model;

import com.pixeltribe.forumsys.forumpost.model.ForumPost;
import com.pixeltribe.forumsys.reporttype.model.ReportType;
import com.pixeltribe.membersys.member.model.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "article_report")
public class ArticleReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NO", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REPORTER")
    private Member reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RPI_NO")
    private ReportType rpiNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POST_NO")
    private ForumPost postNo;

    @Column(name = "ART_REP_STATUS", nullable = false)
    private Character artRepStatus;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "CREATE_TIME")
    private Instant createTime;

    @Column(name = "FINISH_TIME")
    private Instant finishTime;

}