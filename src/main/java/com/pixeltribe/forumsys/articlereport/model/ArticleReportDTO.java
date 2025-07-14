package com.pixeltribe.forumsys.articlereport.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@Schema(description = "文章區留言檢舉DTO")
public class ArticleReportDTO {

    @Schema(description = "文章檢舉編號")
    private Integer id;

    @Schema(description = "文章檢舉處理狀態")
    private Character artRepStatus;

    @Schema(description = "文章檢舉建立時間")
    private Instant createTime;

    @Schema(description = "文章檢舉處理完成時間")
    private Instant finishTime;

    @Schema(description = "檢舉文章編號")
    private Integer postNo;

    @Schema(description = "檢舉者名稱")
    private String member;

    @Schema(description = "檢舉類型")
    private String reportType;

    public static ArticleReportDTO convertToArticleReportDTO(ArticleReport articleReport) {
        return ArticleReportDTO.builder()
                .id(articleReport.getId())
                .artRepStatus(articleReport.getArtRepStatus())
                .createTime(articleReport.getCreateTime())
                .finishTime(articleReport.getFinishTime())
                .postNo(articleReport.getPostNo().getId() == null ? null : articleReport.getPostNo().getId())
                .member(articleReport.getReporter().getMemName() == null ? null : articleReport.getReporter().getMemName())
                .reportType(articleReport.getRpiNo().getRpiType() == null ? null : articleReport.getRpiNo().getRpiType())
                .build();
    }

}
