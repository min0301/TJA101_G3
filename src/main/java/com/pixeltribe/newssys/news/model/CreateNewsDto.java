package com.pixeltribe.newssys.news.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link News}
 */
@Value
public class CreateNewsDto implements Serializable {
    @NotNull
    @Size(max = 255)
    String newsTit;
    @NotNull
    @Size(max = 9000)
    String newsCon;
}