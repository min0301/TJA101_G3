package com.pixeltribe.newssys.newscomment.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Value;

import java.io.Serializable;
import java.time.Instant;

/**
 * DTO for {@link NewsComment}
 */
@Value
@Data
public class NewsCommentDTO implements Serializable {
    Integer id;
    @NotNull
    @Size(max = 4000)
    String ncomCon;
    @NotNull
    Instant ncomCre;
    @NotNull
    Character ncomStatus;
    Integer memNoMemNo;
    String memNoMemNickName;
    @NotNull
    Integer ncomLikeLc;
    @NotNull
    Integer ncomLikeDlc;
}