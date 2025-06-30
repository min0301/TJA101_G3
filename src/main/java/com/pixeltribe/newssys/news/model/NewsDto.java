package com.pixeltribe.newssys.news.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;

/**
 * DTO for {@link News}
 */
@Value
public class NewsDto implements Serializable {
    Integer id;
    @NotNull
    @Size(max = 255)
    String newsTit;
    @NotNull
    @Size(max = 9000)
    String newsCon;
    Instant newsUpdate;
    @NotNull
    Instant newsCrdate;
    Set<NewContentClassificationDto> newContentClassifications;

    /**
     * DTO for {@link com.pixeltribe.newssys.newscontentclassification.model.NewContentClassification}
     */
    @Value
    public static class NewContentClassificationDto implements Serializable {
        Integer id;
        @NotNull
        NewsDto.NewContentClassificationDto.NewsCategoryDto ncatNo;

        /**
         * DTO for {@link com.pixeltribe.newssys.newscategory.model.NewsCategory}
         */
        @Value
        public static class NewsCategoryDto implements Serializable {
            Integer id;
            @NotNull
            @Size(max = 50)
            String ncatName;
        }
    }
}