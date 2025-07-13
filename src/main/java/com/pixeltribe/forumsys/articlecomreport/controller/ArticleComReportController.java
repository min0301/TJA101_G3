package com.pixeltribe.forumsys.articlecomreport.controller;

import com.pixeltribe.forumsys.articlecomreport.model.ArticleComReportCreateDTO;
import com.pixeltribe.forumsys.articlecomreport.model.ArticleComReportDTO;
import com.pixeltribe.forumsys.articlecomreport.model.ArticleComReportService;
import com.pixeltribe.forumsys.articlecomreport.model.ArticleComReportUpdateDTO;
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
public class ArticleComReportController {

    private final ArticleComReportService articleComReportService;

    public ArticleComReportController(ArticleComReportService articleComReportService) {
        this.articleComReportService = articleComReportService;
    }

    @PostMapping("/posts/message/report")
    @Operation(
            summary = "新增文章留言檢舉"
    )
    public ResponseEntity<ArticleComReportDTO> addArticleComReport(
            @Valid @RequestBody ArticleComReportCreateDTO articleComReportCreateDTO,
            @AuthenticationPrincipal MemberDetails currentUser
    ) {
        Integer memberId = currentUser.getMemberId();
        return ResponseEntity.status(HttpStatus.CREATED).body(
                articleComReportService.add(memberId, articleComReportCreateDTO));
    }

    @PutMapping("/articlecomreports/{comno}")
    @Operation(
            summary = "文章留言檢舉處理"
    )
    public ResponseEntity<ArticleComReportDTO> updateArticleComReport(
            @PathVariable("comno") Integer comNo,
            @Valid @RequestBody ArticleComReportUpdateDTO articleComReportUpdateDTO
    ) {
        return ResponseEntity.ok(articleComReportService.update(comNo, articleComReportUpdateDTO));
    }

    @GetMapping("/articlecomreports")
    @Operation(
            summary = "文章留言檢舉列表"
    )
    public List<ArticleComReportDTO> getAllArticleComReport() {
        return articleComReportService.getAllArticleComReport();
    }


}
