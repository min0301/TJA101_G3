package com.pixeltribe.newssys.news.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.pixeltribe.newssys.newscontentclassification.model.NewContentClassification;
import com.pixeltribe.membersys.member.model.Member;
import com.pixeltribe.newssys.newscomment.model.NewsComment;
import com.pixeltribe.newssys.newsimage.model.NewsImage;
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
// test

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "NEWS_CRDATE", nullable = false)
    private Instant newsCrdate;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ColumnDefault("1")
    @JoinColumn(name = "MEM_NO", nullable = false)
    @JsonIgnore
    private Member memNo;

    @OneToMany(mappedBy = "newsNo")
    @JsonIgnore
    private Set<NewContentClassification> newContentClassifications = new LinkedHashSet<>();

    @OneToMany(mappedBy = "newsNo")
    @JsonIgnore
    private Set<NewsComment> newsComments = new LinkedHashSet<>();

    @OneToMany(mappedBy = "newsNo")
    @JsonIgnore
    private Set<NewsImage> newsImages = new LinkedHashSet<>();

}