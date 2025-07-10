package com.pixeltribe.forumsys.forumcollect.model;

import com.pixeltribe.forumsys.forum.model.Forum;
import com.pixeltribe.forumsys.shared.CollectStatus;
import com.pixeltribe.membersys.member.model.Member;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Data
@Entity
@Table(name = "forum_collect")
public class ForumCollect {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FCOLL_NO", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FOR_NO")
    private Forum forNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEM_NO")
    private Member memNo;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "FCOLL_UPDATE", insertable = false, updatable = false)
    private Instant fcollUpdate;

    @Column(name = "COLLECT_STATUS", length = 50)
    @Enumerated(EnumType.STRING)
    private CollectStatus collectStatus;

}