package com.pixeltribe.forumsys.reporttype.controller;

import com.pixeltribe.forumsys.reporttype.model.ReportTypeDTO;
import com.pixeltribe.forumsys.reporttype.model.ReportTypeService;
import com.pixeltribe.forumsys.reporttype.model.ReportTypeUpdateDTO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    ResponseEntity<ReportTypeDTO> addReportType(
            @Valid @RequestBody ReportTypeUpdateDTO reportTypeUpdateDTO) {

        return ResponseEntity.status(HttpStatus.CREATED).body(reportTypeService.add(reportTypeUpdateDTO));
    }


    @PostMapping("/admin/report-type/{rpino}")
    @Operation(
            summary = "更新檢舉類型"
    )
    public ResponseEntity<ReportTypeDTO> updateReportType(
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
        return ReportTypeDTO.convertToReportTypeDTO(reportTypeService.getOneReportType(rpiNo));
    }

    @GetMapping("/report-types")
    @Operation(summary = "查詢所有檢舉類型")
    public ResponseEntity<List<ReportTypeDTO>> getAllReportTypes() {
        List<ReportTypeDTO> reportTypes = reportTypeService.getAllReportTypes();
        return ResponseEntity.ok(reportTypes);
    }

}
