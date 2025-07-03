package com.pixeltribe.newssys.news.model;

import lombok.Data;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Data
public class NewsDTO {
    private Integer id;
    private String newsTit;
    private String newsCon;
    private Instant newsCrdate;
    private Instant newsUpdate;
    private Long numberOfNewsPhoto;
    private List<String> categoryTags;

    public NewsDTO(Integer id, String title, String content,
                   Instant cr, Instant up,
                   Long imgCnt, String tagsCsv) {
        this.id = id;
        this.newsTit = title;
        this.newsCon = content;
        this.newsCrdate = cr;
        this.newsUpdate = up;
        this.numberOfNewsPhoto = imgCnt == null ? 0 : imgCnt.longValue();
        this.categoryTags = tagsCsv == null ? List.of()
                : Arrays.stream(tagsCsv.split(",")).distinct().toList();
    }
}