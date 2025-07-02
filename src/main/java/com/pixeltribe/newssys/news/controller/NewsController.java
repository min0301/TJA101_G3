package com.pixeltribe.newssys.news.controller;


import com.pixeltribe.newssys.news.model.NewsDto;
import com.pixeltribe.newssys.news.model.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RestController
@RequestMapping("/api")
public class NewsController {

    @Autowired
    NewsService newsSrv;

    @GetMapping("News")
    public List<NewsDto> findAll() {
        return newsSrv.findAll();
    }

    @GetMapping("News/{newsId}")
    public NewsDto findById(@PathVariable Integer newsId) {
        return newsSrv.getOneNews(newsId);
    }


}
