package com.pixeltribe.forumsys.forumVO;

import com.pixeltribe.forumsys.forum.model.ForumVO;
import com.pixeltribe.membersys.vo.Member;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

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
    @JoinColumn(name = "FOR_NO")
    private ForumVO forNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "MEM_NO")
    private Member memNo;

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

    @OneToMany(mappedBy = "cmesNo")
    private Set<ForumChatReport> forumChatReports = new LinkedHashSet<>();

}