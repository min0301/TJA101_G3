package com.pixeltribe.forumsys.entity;

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
@Table(name = "forum_chat_report")
public class ForumChatReport {
    @Id
    @Column(name = "NO", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REPORTER")
    private Member reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REPORT_TYPE")
    private ReportType reportType;

    @NotNull
    @ColumnDefault("'0'")
    @Column(name = "FOR_CHAT_REP_STATUS", nullable = false)
    private Character forChatRepStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CMES_NO")
    private ForumChatMessage cmesNo;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "CREATE_TIME")
    private Instant createTime;

    @Column(name = "FINISH_TIME")
    private Instant finishTime;

}