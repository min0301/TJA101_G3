package com.pixeltribe.newssys.newsimage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pixeltribe.newssys.news.model.News;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "news_image")
public class NewsImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IMG_NO", nullable = false)
    private Integer id;

    @Column(name = "IMG_URL")
    private String imgUrl;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "NEWS_NO", nullable = false)
    @JsonIgnore
    private News newsNo;

    @Size(max = 100)
    @Column(name = "IMG_TYPE", length = 100)
    private String imgType;

}