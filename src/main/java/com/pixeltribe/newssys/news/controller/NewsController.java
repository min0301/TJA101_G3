package com.pixeltribe.newssys.news.controller;


import com.pixeltribe.common.PageResponse;
import com.pixeltribe.newssys.news.model.NewsAdminDTO;
import com.pixeltribe.newssys.news.model.NewsCreationDTO;
import com.pixeltribe.newssys.news.model.NewsDTO;
import com.pixeltribe.newssys.news.model.NewsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class NewsController {

    @Autowired
    NewsService newsSrv;

    @GetMapping("News")
    public PageResponse<NewsDTO> findAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size) {
        return newsSrv.findAll(page,size);
    }

    @GetMapping("News/{newsId}")
    public NewsDTO findById(@PathVariable Integer newsId) {
        return newsSrv.getOneNews(newsId);
    }

    @GetMapping("News/admin/allNews")
    public PageResponse<NewsAdminDTO> findAllAdminNews(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size) {
        return newsSrv.findAllAdminNews(page, size);
    }

    @PostMapping("/News/admin/create")
    public NewsCreationDTO creationNews(@Valid @RequestBody NewsCreationDTO nCDTO) {
        return newsSrv.createNews(nCDTO.getNewsTit(), nCDTO.getNewsCon(),nCDTO.getAdminNo(), nCDTO.getTags());
    }


}
