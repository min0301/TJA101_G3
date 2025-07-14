package com.pixeltribe.newssys.newscomment.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * DTO for {@link NewsComment}
 */
//@Value
@Data
public class NewsCommentUpdateDTO implements Serializable {
    Integer id;
    @NotNull
    @Size(max = 4000)
    String ncomCon;
    Character ncomStatus;
    Integer newsNoId;
    Integer memNoId;

    public NewsCommentUpdateDTO(Integer id, String ncomCon, Character ncomStatus, Integer newsNoId, Integer memNoId) {
        this.id = id;
        this.ncomCon = ncomCon;
        this.ncomStatus = ncomStatus;
        this.newsNoId = newsNoId;
        this.memNoId = memNoId;
    }
}