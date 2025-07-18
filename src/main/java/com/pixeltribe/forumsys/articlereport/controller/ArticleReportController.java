package com.pixeltribe.forumsys.articlereport.controller;

import com.pixeltribe.forumsys.articlereport.model.ArticleReportCreateDTO;
import com.pixeltribe.forumsys.articlereport.model.ArticleReportDTO;
import com.pixeltribe.forumsys.articlereport.model.ArticleReportService;
import com.pixeltribe.forumsys.articlereport.model.ArticleReportUpdateDTO;
import com.pixeltribe.membersys.security.MemberDetails;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ArticleReportController {

    private final ArticleReportService articleReportService;

    public ArticleReportController(ArticleReportService articleReportService) {
        this.articleReportService = articleReportService;
    }
    @PostMapping("/posts/report")
    @Operation(
            summary = "新增文章檢舉"
    )
    public ResponseEntity<ArticleReportDTO> addarticleReport(
            @Valid @RequestBody ArticleReportCreateDTO articleReportCreateDTO,
            @AuthenticationPrincipal MemberDetails currentUser
    ) {
        Integer memberId = currentUser.getMemberId();
        return ResponseEntity.status(HttpStatus.CREATED).body(
                articleReportService.add(memberId, articleReportCreateDTO));
    }

    @PutMapping("/articlereports/{articleReportNo}") // 變數名稱改為 articleReportNo
    @Operation(
            summary = "文章檢舉處理"
    )
    public ResponseEntity<ArticleReportDTO> updatearticleReport(
            @PathVariable("articleReportNo") Integer articleReportNo, // 參數名稱改為 articleReportNo
            @Valid @RequestBody ArticleReportUpdateDTO articleReportUpdateDTO
    ){
        return ResponseEntity.ok(articleReportService.update(articleReportNo,articleReportUpdateDTO));
    }
    @GetMapping("/articlereports")
    @Operation(
            summary = "文章檢舉列表"
    )
    public List<ArticleReportDTO> getAllArticleReport(){
        return articleReportService.getAllArticleReport();
    }

    @GetMapping("articlereports/{articleReportNo}") // 路徑變數名稱改為 articleReportNo
    @Operation(
            summary = "查詢單筆文章檢舉"
    )
    public ArticleReportDTO getArticleReportById(
            @PathVariable("articleReportNo") Integer articleReportNo // 參數名稱改為 articleReportNo
    ){
        return articleReportService.getoneArticleReport(articleReportNo);
    }
}