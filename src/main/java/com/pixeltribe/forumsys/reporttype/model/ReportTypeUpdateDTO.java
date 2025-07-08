package com.pixeltribe.forumsys.reporttype.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "檢舉類型新增＆修改DTO")
public class ReportTypeUpdateDTO {

    @Schema(description = "檢舉類型")
    private String rpiType;

    public static ReportTypeUpdateDTO convertToReportTypeUpdateDTO(ReportType reportType) {
        return ReportTypeUpdateDTO.builder()
                .rpiType(reportType.getRpiType())
                .build();
    }
}
