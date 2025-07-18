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
public class AdminNewsCommentDetailDTO implements Serializable {
    Integer id;
    @NotNull
    @Size(max = 4000)
    String ncomCon;
    Instant ncomCre;
    Character ncomStatus;
    Integer newsNoId;
    String newsNoNewsTit;
    Integer memNoId;
    String memNoMemNickName;
    Integer ncomLikeLc;
    Integer ncomLikeDlc;
}