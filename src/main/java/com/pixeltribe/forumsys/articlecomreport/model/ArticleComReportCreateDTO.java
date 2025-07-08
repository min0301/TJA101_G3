package com.pixeltribe.forumsys.articlecomreport.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Schema(description = "新增文章留言檢舉DTO")
@Builder
public class ArticleComReportCreateDTO {

    @Schema(description = "檢舉留言編號")
    private Integer messageNo;
    @Schema(description = "檢舉者編號")
    private Integer memberNo;
    @Schema(description = "檢舉類型編號")
    private Integer reportTypeNo;

    public static ArticleComReportCreateDTO convertToArticleComReportCreateDTO (ArticleComReport articleComReport) {
         return ArticleComReportCreateDTO.builder()
                 .messageNo(articleComReport.getMesNo().getId())
                 .memberNo(articleComReport.getReporter().getId())
                 .reportTypeNo(articleComReport.getRpiNo().getId())
                 .build();
        }

}
