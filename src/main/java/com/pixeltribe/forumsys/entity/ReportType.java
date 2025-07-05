package com.pixeltribe.forumsys.entity;

import com.pixeltribe.newssys.newscomreport.model.NewsComReport;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "report_type")
public class ReportType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RPI_NO", nullable = false)
    private Integer id;

    @Size(max = 255)
    @NotNull
    @Column(name = "RPI_TYPE", nullable = false)
    private String rpiType;

    @OneToMany(mappedBy = "rpiNo")
    private Set<ArticleComReport> articleComReports = new LinkedHashSet<>();

    @OneToMany(mappedBy = "rpiNo")
    private Set<ArticleReport> articleReports = new LinkedHashSet<>();

    @OneToMany(mappedBy = "reportType")
    private Set<ForumChatReport> forumChatReports = new LinkedHashSet<>();

    @OneToMany(mappedBy = "reportType")
    private Set<NewsComReport> newsComReports = new LinkedHashSet<>();

}