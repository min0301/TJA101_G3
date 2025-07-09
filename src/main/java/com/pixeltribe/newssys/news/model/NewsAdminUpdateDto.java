package com.pixeltribe.newssys.news.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for {@link News}
 */
@Data
@NoArgsConstructor
public class NewsAdminUpdateDto implements Serializable {
    Integer id;
    @NotNull
    @Size(max = 255)
    String newsTit;
    @NotNull
    @Size(max = 9000)
    String newsCon;
    Boolean isShowed;

}