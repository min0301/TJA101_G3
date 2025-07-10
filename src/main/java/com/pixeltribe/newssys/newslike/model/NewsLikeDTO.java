package com.pixeltribe.newssys.newslike.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link NewsLike}
 */
@Data
//@AllArgsConstructor
public class NewsLikeDTO implements Serializable {
    Integer id;
    @NotNull
    Character nlikeStatus;
    @NotNull
    @Positive
    Integer memNoId;
    @NotNull
    @Positive
    Integer ncomNoId;

    public NewsLikeDTO(Integer id, Character nlikeStatus, Integer memNoId, Integer ncomNoId) {
        this.id = id;
        this.nlikeStatus = nlikeStatus;
        this.memNoId = memNoId;
        this.ncomNoId = ncomNoId;
    }
}