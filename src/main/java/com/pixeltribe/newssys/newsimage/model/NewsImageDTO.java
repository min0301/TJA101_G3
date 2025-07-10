package com.pixeltribe.newssys.newsimage.model;

import lombok.Data;

@Data
public class NewsImageDTO {

    private Integer id;
    private String imgType;
    private String imgUrl;

    public NewsImageDTO(Integer id, String imgType, String imgUrl) {
        this.id = id;
        this.imgType = imgType;
        this.imgUrl = imgUrl;
    }

}
