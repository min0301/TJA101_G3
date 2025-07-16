package com.pixeltribe.forumsys.postlike.model;

import com.pixeltribe.forumsys.forumpost.model.ForumPost;
import com.pixeltribe.forumsys.shared.LikeStatus;
import com.pixeltribe.membersys.member.model.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "post_like")
public class PostLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PLIKE_NO", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POST_NO")
    private ForumPost postNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEM_NO")
    private Member memNo;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "PLIKE_UPDATE")
    private Instant plikeUpdate;

    @Column(name = "PLIKE_STATUS")
    @Enumerated(EnumType.STRING)
    private LikeStatus plikeStatus;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "PLIKE_CRDATE")
    private Instant plikeCrdate;



}