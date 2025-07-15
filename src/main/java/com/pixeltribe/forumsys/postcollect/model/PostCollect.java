package com.pixeltribe.forumsys.postcollect.model;

import com.pixeltribe.forumsys.forumpost.model.ForumPost;
import com.pixeltribe.membersys.member.model.Member;
import com.pixeltribe.forumsys.shared.PostCollectStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "post_collect")
public class PostCollect {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PCOLL_NO", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEM_NO")
    private Member memNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POST_NO")
    private ForumPost postNo;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "PCOLL_UPDATE", insertable = false, updatable = false)
    private Instant pcollUpdate;

    @Column(name = "POST_COLLECT_STATUS", length = 50)
    @Enumerated(EnumType.STRING)
    private PostCollectStatus postCollectStatus;

}