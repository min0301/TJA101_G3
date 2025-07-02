package com.pixeltribe.forumsys.forumcategory.model;


import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class ForumCategoryDetailDTO {

    private Integer id;
    private String catName;
    private String catDes;
    private Instant catDate;
    private List<ForumSummaryDTO> forums;

}
