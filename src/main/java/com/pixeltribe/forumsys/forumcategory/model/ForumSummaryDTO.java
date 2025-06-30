package com.pixeltribe.forumsys.forumcategory.model;

import com.pixeltribe.forumsys.forum.model.ForumDetailDTO;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ForumSummaryDTO {

    private Integer id;
    private String forName;
    private String forDes;

}
