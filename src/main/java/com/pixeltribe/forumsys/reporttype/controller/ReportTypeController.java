package com.pixeltribe.forumsys.reporttype.controller;

import com.pixeltribe.forumsys.reporttype.model.ReportType;
import com.pixeltribe.forumsys.reporttype.model.ReportTypeDTO;
import com.pixeltribe.forumsys.reporttype.model.ReportTypeService;
import com.pixeltribe.forumsys.reporttype.model.ReportTypeUpdateDTO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ReportTypeController {

    private final ReportTypeService reportTypeService;

    public ReportTypeController(ReportTypeService reportTypeService) {

        this.reportTypeService = reportTypeService;
    }

    @PostMapping("/admin/report-type")
    @Operation(
            summary = "新增檢舉類型"
    )
    ResponseEntity<?> addReportType(
            @Valid @RequestBody ReportTypeUpdateDTO reportTypeUpdateDTO) {

        return ResponseEntity.status(HttpStatus.CREATED).body(reportTypeService.add(reportTypeUpdateDTO));
    }


    @PostMapping("/admin/report-type/{rpino}")
    @Operation(
            summary = "更新檢舉類型"
    )
    public ResponseEntity<?> updateReportType(
            @Valid @RequestBody ReportTypeUpdateDTO reportTypeUpdateDTO,
            @PathVariable("rpino") Integer rpiNo
    ) {
        return ResponseEntity.ok(reportTypeService.update(rpiNo, reportTypeUpdateDTO));
    }

    @GetMapping("/report-types/{rpino}")
    @Operation(
            summary = "查單一檢舉類型"
    )
    public ReportTypeDTO findOneReportType(
            @Valid @PathVariable("rpino") Integer rpiNo
    ) {
        ReportType reportType = new ReportType();
        reportType = reportTypeService.getOneReportType(rpiNo);
        return ReportTypeDTO.convertToReportTypeDTO(reportType);
    }

}
