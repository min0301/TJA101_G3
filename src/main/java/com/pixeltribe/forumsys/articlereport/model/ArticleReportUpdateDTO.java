package com.pixeltribe.forumsys.articlereport.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Schema(description = "更新文章檢舉DTO")
@Builder
public class ArticleReportUpdateDTO {

    @Size(max = 1)
    @Schema(description = "文章檢舉處理狀態")
    private Character artRepStatus;
    @Schema(description = "文章檢舉處理完成時間")
    private Instant finishTime;
    @Schema(description = "檢舉類型編號")
    private Integer reportTypeNo;
    @Schema(description = "是否同時隱藏被檢舉的文章 (true: 是, false/null: 否)")
    private Boolean hidePost; // 使用 Boolean 而非 boolean，使其可以為 null，更具彈性

    public static ArticleReportUpdateDTO convertArticleReportUpdateDTO(ArticleReport articleReport) {
        return ArticleReportUpdateDTO.builder()
                .artRepStatus(articleReport.getArtRepStatus())
                .finishTime(articleReport.getFinishTime())
                .reportTypeNo(articleReport.getRpiNo().getId())
                .build();
    }
}
