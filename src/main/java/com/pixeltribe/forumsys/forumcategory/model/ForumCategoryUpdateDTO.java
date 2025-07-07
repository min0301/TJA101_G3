package com.pixeltribe.forumsys.forumcategory.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ForumCategoryUpdateDTO {

    @Schema(description = "討論區名稱")
    private String catName;
    @Schema(description = "討論區描述")
    private String catDes;

    public final ForumCategoryUpdateDTO convertToCategoryUpdateDTO(ForumCategory forumCategory){
        return ForumCategoryUpdateDTO.builder()
                .catName(forumCategory.getCatName())
                .catDes(forumCategory.getCatDes())
                .build();
    }

}
