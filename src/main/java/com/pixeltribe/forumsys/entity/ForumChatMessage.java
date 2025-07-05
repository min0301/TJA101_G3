package com.pixeltribe.forumsys.entity;

import com.pixeltribe.forumsys.forum.model.Forum;
import com.pixeltribe.membersys.member.model.Member;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "forum_chat_message")
public class ForumChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CMES_NO", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEM_NO")
    private Member memNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FOR_NO")
    private Forum forNo;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "CMES_AT")
    private Instant cmesAt;

    @Size(max = 5000)
    @Column(name = "CMES_TEXT", length = 5000)
    private String cmesText;

    @ColumnDefault("'0'")
    @Column(name = "CMES_DEL")
    private Character cmesDel;

    @ColumnDefault("'0'")
    @Column(name = "CMES_STATUS")
    private Character cmesStatus;

}