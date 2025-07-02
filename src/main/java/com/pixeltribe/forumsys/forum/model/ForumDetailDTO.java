package com.pixeltribe.forumsys.forum.model;


import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ForumDetailDTO {

    private Integer id;
    private String forName;
    private String forDes;
    private String forImgUrl;
    private Instant forDate;
    private Instant forUpdate;
    private Character forStatus;

    // 用一個 String 來接收分類名稱，而不是整個 ForumCategory 物件
    private String categoryName;
    private Integer categoryId;

}
