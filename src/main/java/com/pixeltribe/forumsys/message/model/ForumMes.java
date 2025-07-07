package com.pixeltribe.forumsys.message.model;

import com.pixeltribe.forumsys.entity.ArticleComReport;
import com.pixeltribe.forumsys.messagelike.model.ForumMesLike;
import com.pixeltribe.forumsys.forumpost.model.ForumPost;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

import com.pixeltribe.membersys.member.model.Member;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "forum_mes")
public class ForumMes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MES_NO", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POST_NO")
    private ForumPost postNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEM_NO")
    private Member memNo;

    @Size(max = 500)
    @Column(name = "MES_CON", length = 500)
    private String mesCon;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "MES_CRDATE", insertable = false, updatable = false)
    private Instant mesCrdate;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "MES_UPDATA", insertable = false, updatable = false)
    private Instant mesUpdata;

    @ColumnDefault("'0'")
    @Column(name = "MES_STATUS", insertable = false)
    private Character mesStatus;

    @ColumnDefault("0")
    @Column(name = "MES_LIKE_LC", insertable = false)
    private Integer mesLikeLc;

    @ColumnDefault("0")
    @Column(name = "MES_LIKE_DLC", insertable = false)
    private Integer mesLikeDlc;

    @OneToMany(mappedBy = "mesNo")
    private Set<ArticleComReport> articleComReports = new LinkedHashSet<>();

    @OneToMany(mappedBy = "mesNo")
    private Set<ForumMesLike> forumMesLikes = new LinkedHashSet<>();

}