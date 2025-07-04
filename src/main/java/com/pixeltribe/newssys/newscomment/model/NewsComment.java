package com.pixeltribe.newssys.newscomment.model;

import com.pixeltribe.membersys.member.model.Member;
import com.pixeltribe.newssys.news.model.News;
import com.pixeltribe.newssys.newscomreport.model.NewsComReport;
import com.pixeltribe.newssys.newslike.model.NewsLike;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "news_comments")
public class NewsComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NCOM_NO", nullable = false)
    private Integer id;

    @Size(max = 4000)
    @NotNull
    @Column(name = "NCOM_CON", nullable = false, length = 4000)
    private String ncomCon;


    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "NCOM_CRE", nullable = false,insertable = false)
    private Instant ncomCre;


    @ColumnDefault("'1'")
    @Column(name = "NCOM_STATUS", nullable = false,insertable = false)
    private Character ncomStatus;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "NEWS_NO", nullable = false)
    private News newsNo;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MEM_NO", nullable = false)
    private Member memNo;


    @ColumnDefault("0")
    @Column(name = "NCOM_LIKE_LC", nullable = false,insertable = false)
    private Integer ncomLikeLc;


    @ColumnDefault("0")
    @Column(name = "NCOM_LIKE_DLC", nullable = false,insertable = false)
    private Integer ncomLikeDlc;

    @OneToMany(mappedBy = "ncomNo")
    private Set<NewsComReport> newsComReports = new LinkedHashSet<>();

    @OneToMany(mappedBy = "ncomNo")
    private Set<NewsLike> newsLikes = new LinkedHashSet<>();

}