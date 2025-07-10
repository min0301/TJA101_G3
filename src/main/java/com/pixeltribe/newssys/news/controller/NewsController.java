package com.pixeltribe.newssys.news.controller;


import com.pixeltribe.common.PageResponse;
import com.pixeltribe.newssys.news.model.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class NewsController {


    private final NewsService newsSrv;

    public NewsController(NewsService newsSrv) {
        this.newsSrv = newsSrv;
    }


    @GetMapping("News")
    @Operation(summary = "顯示所有前台新聞")
    public PageResponse<NewsDTO> findAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size) {
        return newsSrv.findAll(page, size);
    }

    @GetMapping("News/{newsId}")
    @Operation(summary = "顯示某一則新聞")
    public NewsDTO findById(@PathVariable Integer newsId) {
        return newsSrv.getOneNews(newsId);
    }

    @GetMapping("News/admin/allNews")
    @Operation(summary = "顯示所有新聞")
    public PageResponse<NewsAdminDTO> findAllAdminNews(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size) {
        return newsSrv.findAllAdminNews(page, size);
    }

    @PostMapping("/News/admin/create")
    @Operation(summary = "新增新聞")
    public NewsCreationDTO creationNews(@Valid @RequestBody NewsCreationDTO nCDTO) {
        return newsSrv.createNews(nCDTO.getNewsTit(), nCDTO.getNewsCon(), nCDTO.getAdminNo(), nCDTO.getTags());
    }

    @PatchMapping("News/admin/update/{id}")
    @Operation(summary = "修改某則新聞")
    public NewsAdminUpdateDto updateNews(@PathVariable Integer id, @Valid @RequestBody NewsAdminUpdateDto nauDTO) {
        nauDTO.setId(id);
        return newsSrv.updateNews(nauDTO);
    }


}
