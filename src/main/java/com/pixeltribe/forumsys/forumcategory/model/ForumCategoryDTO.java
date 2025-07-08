package com.pixeltribe.forumsys.forumcategory.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Data
@Builder
@Schema(description = "討論區類別list DTO")
public class ForumCategoryDTO {
    @Schema(description = "討論區類別編號")
    private Integer id;
    @Schema(description = "討論區類別名稱")
    private String catName;
    @Schema(description = "討論區類別描述")
    private String catDes;
    @Schema(description = "討論區類別建立時間")
    private Instant catDate;

    public static ForumCategoryDTO convertToCategoryDTO(ForumCategory forumCategory) {


        return ForumCategoryDTO.builder()
                .id(forumCategory.getId())
                .catName(forumCategory.getCatName())
                .catDes(forumCategory.getCatDes())
                .catDate(forumCategory.getCatDate())
                .build();
    }

}
