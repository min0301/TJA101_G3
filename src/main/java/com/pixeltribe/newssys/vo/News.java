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
@Table(name = "news")
public class News {
    @Id
    @Column(name = "NEWS_NO", nullable = false)
    private Integer id;

    @Size(max = 255)
    @NotNull
    @Column(name = "NEWS_TIT", nullable = false)
    private String newsTit;

    @Size(max = 9000)
    @NotNull
    @Column(name = "NEWS_CON", nullable = false, length = 9000)
    private String newsCon;

    @Column(name = "NEWS_UPDATE")
    private Instant newsUpdate;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "NEWS_CRDATE", nullable = false)
    private Instant newsCrdate;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ColumnDefault("1")
    @JoinColumn(name = "MEM_NO", nullable = false)
    private Member memNo;

    @OneToMany(mappedBy = "newsNo")
    private Set<NewContentClassification> newContentClassifications = new LinkedHashSet<>();

    @OneToMany(mappedBy = "newsNo")
    private Set<NewsComment> newsComments = new LinkedHashSet<>();

    @OneToMany(mappedBy = "newsNo")
    private Set<NewsImage> newsImages = new LinkedHashSet<>();

}