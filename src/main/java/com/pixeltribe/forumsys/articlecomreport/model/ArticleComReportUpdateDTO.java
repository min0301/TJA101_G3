package com.pixeltribe.forumsys.articlecomreport.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Schema(description = "更新文章留言檢舉DTO")
@Builder
public class ArticleComReportUpdateDTO {


    @Schema(description = "留言檢舉處理狀態")
    private Character artComRepStatus;
    @Schema(description = "留言檢舉處理完成時間")
    private Instant finishTime;
    @Schema(description = "檢舉類型編號")
    private Integer reportTypeNo;
    @Schema(description = "是否同時隱藏被檢舉的留言 (true: 是, false/null: 否)")
    private Boolean hideMessage; // 使用 Boolean 而非 boolean，使其可以為 null，更具彈性

    public static ArticleComReportUpdateDTO convertToArticleComReportUpdateDTO(ArticleComReport articleComReport) {
        return ArticleComReportUpdateDTO.builder()
                .artComRepStatus(articleComReport.getArtComRepStatus())
                .finishTime(articleComReport.getFinishTime())
                .reportTypeNo(articleComReport.getRpiNo().getId())
                .build();
    }

}
