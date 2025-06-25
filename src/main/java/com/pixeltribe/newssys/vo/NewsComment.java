package com.pixeltribe.newssys.vo;

import com.pixeltribe.membersys.vo.Member;
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
    @Column(name = "NCOM_NO", nullable = false)
    private Integer id;

    @Size(max = 4000)
    @NotNull
    @Column(name = "NCOM_CON", nullable = false, length = 4000)
    private String ncomCon;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "NCOM_CRE", nullable = false)
    private Instant ncomCre;

    @NotNull
    @ColumnDefault("'1'")
    @Column(name = "NCOM_STATUS", nullable = false)
    private Character ncomStatus;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "NEWS_NO", nullable = false)
    private News newsNo;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MEM_NO", nullable = false)
    private Member memNo;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "NCOM_LIKE_LC", nullable = false)
    private Integer ncomLikeLc;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "NCOM_LIKE_DLC", nullable = false)
    private Integer ncomLikeDlc;

    @OneToMany(mappedBy = "ncomNo")
    private Set<NewsComReport> newsComReports = new LinkedHashSet<>();

    @OneToMany(mappedBy = "ncomNo")
    private Set<NewsLike> newsLikes = new LinkedHashSet<>();

}