package com.pixeltribe.newssys.newscomment.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link NewsComment}
 */
@Value
public class NewsCommentCreationDTO implements Serializable {

    @NotNull(message = "不得為空")
    @Size(message = "字數過多", max = 4000)
    String ncomCon;
    Integer newsNoId;
    Integer memNoId;
}