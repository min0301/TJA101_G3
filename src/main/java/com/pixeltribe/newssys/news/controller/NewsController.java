package com.pixeltribe.newssys.news.controller;


import com.pixeltribe.newssys.news.model.NewsCreationDTO;
import com.pixeltribe.newssys.news.model.NewsDTO;
import com.pixeltribe.newssys.news.model.NewsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class NewsController {

    @Autowired
    NewsService newsSrv;

    @GetMapping("News")
    public List<NewsDTO> findAll() {
        return newsSrv.findAll();
    }

    @GetMapping("News/{newsId}")
    public NewsDTO findById(@PathVariable Integer newsId) {
        return newsSrv.getOneNews(newsId);
    }

    @PostMapping("/News/admin/create")
    public NewsCreationDTO creationNews(@Valid @RequestBody NewsCreationDTO nCDTO) {
        return newsSrv.createNews(nCDTO.getNewsTit(), nCDTO.getNewsCon(),nCDTO.getAdminNo(), nCDTO.getTags());
    }


}
