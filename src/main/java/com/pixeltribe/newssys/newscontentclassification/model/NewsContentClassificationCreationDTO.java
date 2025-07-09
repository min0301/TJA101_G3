package com.pixeltribe.newssys.newscontentclassification.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link NewsContentClassification}
 */
@Value
public class NewsContentClassificationCreationDTO implements Serializable {
    @NotNull
    @Positive
    Integer categoryId;
    @NotNull
    @Positive
    Integer newsId;
}