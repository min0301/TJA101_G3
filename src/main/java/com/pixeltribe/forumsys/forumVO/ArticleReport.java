package com.pixeltribe.forumsys.forumVO;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import com.pixeltribe.membersys.member.model.Member;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "article_report")
public class ArticleReport {
    @Id
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

    @NotNull
    @ColumnDefault("'0'")
    @Column(name = "ART_REP_STATUS", nullable = false)
    private Character artRepStatus;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "CREATE_TIME")
    private Instant createTime;

    @Column(name = "FINISH_TIME")
    private Instant finishTime;

}