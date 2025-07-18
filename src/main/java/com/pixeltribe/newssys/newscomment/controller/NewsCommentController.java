package com.pixeltribe.newssys.newscomment.controller;

import com.pixeltribe.newssys.newscomment.model.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
class NewsCommentController {

    private final NewsCommentService newsCommentService;

    NewsCommentController(NewsCommentService newsCommentService) {
        this.newsCommentService = newsCommentService;
    }

    @GetMapping("NewsComment/{id}")
    @Operation(summary = "取得某則新聞的評論")
    public List<NewsCommentDTO> findAll(@PathVariable Integer id) {
        return newsCommentService.findAll(id);
    }

    @PostMapping("NewsComment/add")
    @Operation(summary = "新增新聞評論")
    public NewsCommentCreationDTO addComment(
            @Valid @RequestBody NewsCommentCreationDTO dto) {

        return newsCommentService.add(
                dto.getNewsNoId(),
                dto.getMemNoId(),
                dto.getNcomCon()
        );
    }

    @PatchMapping("admin/NewsComment/update")
    @Operation(summary = "修改後台評論")
    public NewsCommentUpdateDTO updateComment(@RequestBody @Validated NewsCommentUpdateDTO dto) {
        return newsCommentService.save(dto);
    }

    @GetMapping("admin/NewsComment")
    @Operation(summary = "後台取得評論清單")
    public Page<AdminNewsCommentPageDTO> getComments(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) Integer newsId,
            @RequestParam(required = false) Integer memberId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        Character st = (status == null || status.isBlank()) ? null : status.charAt(0);
        Pageable p = PageRequest.of(page, size, Sort.by("ncomCre").descending());
        return newsCommentService.findAdminPage(p, newsId, memberId, st, keyword);
    }

    @PatchMapping("admin/NewsComment/{id}/hide")
    @Operation(summary = "後台切換評論隱藏狀態")
    public CommentHideDTO toggleHide(@PathVariable Integer id, @RequestBody CommentHideDTO req) {

        return newsCommentService.toggleHide(id, req.getNcomStatus());
    }

    @GetMapping("admin/NewsComment/count")
    @Operation(summary = "後台取得評論數量")
    public Integer countAllNewsComment() {
        return newsCommentService.countAllNewsComment();
    }

    @GetMapping("admin/NewsComment/{commentId}")
    @Operation(summary = "後台取得單一評論")
    public AdminNewsCommentDetailDTO getCommentById(@PathVariable Integer commentId) {
        return newsCommentService.getCommentById(commentId);
    }
}
