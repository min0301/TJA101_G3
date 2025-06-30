package com.pixeltribe.forumsys.forumcategory.model;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

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
