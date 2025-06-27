package com.pixeltribe.newssys.newscomment.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NewsCommentService {
    @Autowired
    NewsCommentRepository newsCommentRepository;

    public List<NewsComment> findAll() {
        return newsCommentRepository.findAll();
    }
}
