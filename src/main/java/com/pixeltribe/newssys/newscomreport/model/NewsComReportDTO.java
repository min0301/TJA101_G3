package com.pixeltribe.newssys.newscomreport.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.time.Instant;

/**
 * DTO for {@link NewsComReport}
 */
@Data
public class NewsComReportDTO implements Serializable {
    Integer id;
    Integer reporterId;
    Integer reportTypeId;
    String reportTypeRpiType;
    @NotNull
    Character newsComReportStatus;
    Integer ncomNoId;
    String newsComment;
    Character newsCommentStatus;
    Instant createTime;
    Instant finishTime;

    public NewsComReportDTO(Integer id, Integer reporterId, Integer reportTypeId, String reportTypeRpiType, Character newsComReportStatus, Integer ncomNoId, String newsComment, Character newsCommentStatus, Instant createTime, Instant finishTime) {
        this.id = id;
        this.reporterId = reporterId;
        this.reportTypeId = reportTypeId;
        this.reportTypeRpiType = reportTypeRpiType;
        this.newsComReportStatus = newsComReportStatus;
        this.ncomNoId = ncomNoId;
        this.newsComment = newsComment;
        this.newsCommentStatus = newsCommentStatus;
        this.createTime = createTime;
        this.finishTime = finishTime;
    }
}