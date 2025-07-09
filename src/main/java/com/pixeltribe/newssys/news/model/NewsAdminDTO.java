package com.pixeltribe.newssys.news.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * DTO for {@link News}
 */
@Data
public class NewsAdminDTO implements Serializable {
    Integer id;
    @NotNull
    @Size(max = 255)
    String newsTit;
    @NotNull
    @Size(max = 9000)
    String newsCon;
    Instant newsUpdate;
    Instant newsCrdate;
    @NotNull
    Boolean isShowed;
    Long imageCount;
    List<String> categoryTags;
    Integer adminNoId;
    String adminNoAdmName;

    public NewsAdminDTO(Integer id,
                        String newsTit,
                        String newsCon,
                        Instant newsUpdate,
                        Instant newsCrdate,
                        Boolean isShowed,
                        Long imageCount,
                        String tagString,
                        Integer adminNoId,
                        String adminNoAdmName) {

        this.id = id;
        this.newsTit = newsTit;
        this.newsCon = newsCon;
        this.newsUpdate = newsUpdate;
        this.newsCrdate = newsCrdate;
        this.isShowed = isShowed;
        this.imageCount = imageCount;
        this.categoryTags = (tagString == null || tagString.isBlank())
                ? List.of()
                : List.of(tagString.split(","));
        this.adminNoId = adminNoId;
        this.adminNoAdmName = adminNoAdmName;
    }
}