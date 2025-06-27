package com.pixeltribe.newssys.news.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NewsService {
    @Autowired
    NewsRepository newsRepository;

    public void addNews(News news) {
        newsRepository.save(news);
    }

    public void updateNews(News news) {
        newsRepository.save(news);
    }

    public News getOneNews(Integer id) {
        Optional<News> news = newsRepository.findById(id);
        return news.orElse(null);
    }

    public List<News> findAll() {
        return newsRepository.findAll();
    }

    public void deleteNews(Integer id) {
        newsRepository.deleteById(id);
    }


}
