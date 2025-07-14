package com.pixeltribe.newssys.newscomment.controller;

import com.pixeltribe.newssys.newscomment.model.NewsCommentCreationDTO;
import com.pixeltribe.newssys.newscomment.model.NewsCommentDTO;
import com.pixeltribe.newssys.newscomment.model.NewsCommentService;
import com.pixeltribe.newssys.newscomment.model.NewsCommentUpdateDTO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
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
    public List<NewsCommentDTO> findAll(@PathVariable Integer id){
        return newsCommentService.findAll(id);
    }

    @PostMapping("NewsComment/add")
    @Operation(summary = "新增新聞評論")
    public NewsCommentCreationDTO addComment(
            @Valid @RequestBody NewsCommentCreationDTO dto){

        return newsCommentService.add(
                dto.getNewsNoId(),
                dto.getMemNoId(),
                dto.getNcomCon()
        );
    }

    @PatchMapping("admin/NewsComment/update")
    @Operation(summary = "修改後台評論")
    public NewsCommentUpdateDTO updateComment(NewsCommentUpdateDTO dto){
        return newsCommentService.save(dto);
    }


}
