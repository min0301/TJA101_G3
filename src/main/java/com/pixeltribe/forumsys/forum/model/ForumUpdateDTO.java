package com.pixeltribe.forumsys.forum.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForumUpdateDTO {


    private String forName;
    private String forDes;
    private Character forStatus;
    private Integer categoryId;


}
