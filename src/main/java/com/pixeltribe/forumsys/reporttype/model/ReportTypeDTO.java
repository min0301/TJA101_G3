package com.pixeltribe.forumsys.reporttype.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Schema(description = "檢舉類型DTO")
@Builder
public class ReportTypeDTO {

    @Schema(description = "檢舉類型編號")
    private Integer id;
    @Schema(description = "檢舉類型")
    private String rpiType;

    public static ReportTypeDTO convertToReportTypeDTO(ReportType reportType){
        return ReportTypeDTO.builder()
                .id(reportType.getId())
                .rpiType(reportType.getRpiType())
                .build();
    }

}
