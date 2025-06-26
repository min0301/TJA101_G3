package com.pixeltribe.forumsys.forumVO;

import com.pixeltribe.forumsys.forum.model.Forum;
import com.pixeltribe.membersys.vo.Member;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "forum_post")
public class ForumPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "POST_NO", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FOR_NO")
    private Forum forNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "MEM_NO")
    private Member memNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "FTAG_NO")
    private ForumTag ftagNo;

    @Size(max = 50)
    @Column(name = "POST_TITLE", length = 50)
    private String postTitle;

    @Size(max = 5000)
    @NotNull
    @Column(name = "POST_CON", nullable = false, length = 5000)
    private String postCon;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "POST_CRDATE")
    private Instant postCrdate;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "POST_UPDATE")
    private Instant postUpdate;

    @NotNull
    @ColumnDefault("'0'")
    @Column(name = "POST_PIN", nullable = false)
    private Character postPin;

    @NotNull
    @ColumnDefault("'0'")
    @Column(name = "POST_STATUS", nullable = false)
    private Character postStatus;

    @ColumnDefault("0")
    @Column(name = "MES_NUMBERS")
    private Integer mesNumbers;

    @ColumnDefault("0")
    @Column(name = "POST_LIKE_COUNT")
    private Integer postLikeCount;

    @ColumnDefault("0")
    @Column(name = "POST_LIKE_DLC")
    private Integer postLikeDlc;

    @OneToMany(mappedBy = "postNo")
    private Set<ArticleReport> articleReports = new LinkedHashSet<>();

    @OneToMany(mappedBy = "postNo")
    private Set<ForumImage> forumImages = new LinkedHashSet<>();

    @OneToMany(mappedBy = "postNo")
    private Set<ForumMes> forumMes = new LinkedHashSet<>();

    @OneToMany(mappedBy = "postNo")
    private Set<PostCollect> postCollects = new LinkedHashSet<>();

}