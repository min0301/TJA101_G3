package com.pixeltribe.newssys.news.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for {@link News}
 */
@Value
public class NewsCreationDTO implements Serializable {

    Integer id;
    @NotNull
    @Size(max = 255)
    String newsTit;
    @NotNull
    @Size(max = 9000)
    String newsCon;
//TODO
//    @PositiveOrZero
//    Integer imgCount;

    @PositiveOrZero
    Integer adminNo;

    @NotNull(message = "至少要有一個分類")
    List<Integer> tags;

}