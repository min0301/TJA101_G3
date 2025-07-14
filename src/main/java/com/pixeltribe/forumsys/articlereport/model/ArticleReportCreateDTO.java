package com.pixeltribe.forumsys.articlereport.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Schema(description = "新增文章檢舉DTO")
@Builder
public class ArticleReportCreateDTO {
    @Schema(description = "檢舉文章編號")
    private Integer postNo;
    @Schema(description = "檢舉者編號")
    private Integer memberNo;
    @Schema(description = "檢舉類型編號")
    private Integer reportTypeNo;

    public static ArticleReportCreateDTO converToArticleReportCreateDTO(ArticleReport articleReport) {
        return ArticleReportCreateDTO.builder()
                .postNo(articleReport.getPostNo().getId())
                .memberNo(articleReport.getReporter().getId())
                .reportTypeNo(articleReport.getRpiNo().getId())
                .build();
    }
}
