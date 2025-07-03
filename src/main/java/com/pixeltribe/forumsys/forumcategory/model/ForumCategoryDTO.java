package com.pixeltribe.forumsys.forumcategory.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Schema(description = "討論區類別list DTO")
public class ForumCategoryDTO {
    private Integer id;
    private String catName;
    private String catDes;
    private Instant catDate;

}
