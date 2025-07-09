package com.pixeltribe.newssys.newscontentclassification.model;

import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link NewsContentClassification}
 */
@Data
@NoArgsConstructor
public class NewsContentClassificationUpdateDTO implements Serializable {
    @NonNull
    @Positive
    Integer id;

    @Positive
    Integer ncatNoId;

    @Positive
    Integer newsNoId;


}