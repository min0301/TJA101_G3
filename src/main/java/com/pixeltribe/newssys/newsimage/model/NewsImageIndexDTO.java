package com.pixeltribe.newssys.newsimage.model;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link NewsImage}
 */
@Value
@Data
public class NewsImageIndexDTO implements Serializable {
    String imgUrl;
    Integer newsNoId;
    String newsNoNewsTit;
    @Size(max = 100)
    String imgType;
}