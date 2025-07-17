package com.pixeltribe.newssys.newscomment.model;

import lombok.Data;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link NewsComment}
 */
@Value
@Data
public class CommentHideDTO implements Serializable {
    Integer id;
    Character ncomStatus;
}