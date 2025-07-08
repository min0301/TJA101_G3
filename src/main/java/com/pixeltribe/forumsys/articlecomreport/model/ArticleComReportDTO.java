package com.pixeltribe.forumsys.articlecomreport.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@Schema(description = "討論區留言檢舉DTO")
public class ArticleComReportDTO {

    @Schema(description = "留言檢舉編號")
    private Integer id;

    @Schema(description = "留言檢舉處理狀態")
    private Character artComRepStatus;
    @Schema(description = "留言檢舉建立時間")
    private Instant createTime;
    @Schema(description = "留言檢舉處理完成時間")
    private Instant finishTime;

    @Schema(description = "檢舉留言編號")
    private Integer messageNo;
    @Schema(description = "檢舉者名稱")
    private String member;
    @Schema(description = "檢舉類型")
    private String reportType;


    public static ArticleComReportDTO convertToArticleComReportDTO(ArticleComReport articleComReport) {
        return ArticleComReportDTO.builder()
                .id(articleComReport.getId())
                .artComRepStatus(articleComReport.getArtComRepStatus())
                .createTime(articleComReport.getCreateTime())
                .finishTime(articleComReport.getFinishTime())
                .messageNo(articleComReport.getMesNo().getId() == null ? null : articleComReport.getMesNo().getId())
                .member(articleComReport.getReporter().getMemName() == null ? null : articleComReport.getReporter().getMemName())
                .reportType(articleComReport.getRpiNo().getRpiType() == null ? null : articleComReport.getRpiNo().getRpiType())
                .build();

    }

}
