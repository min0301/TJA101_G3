package com.pixeltribe.newssys.news.controller;

import com.pixeltribe.newssys.news.model.News;
import com.pixeltribe.newssys.news.model.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RestController
@RequestMapping("/api")
public class NewsController {

    @Autowired
    NewsService newsService;

    @GetMapping("News")
    public List<News> findAll() {
        return newsService.findAll();
    }

}
