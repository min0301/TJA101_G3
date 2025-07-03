package com.pixeltribe.newssys.news.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public NewsDTO getOneNews(Integer id) {
        NewsDTO newsDto = newsRepository.getNewsById(id);
        return newsDto;
    }

    public List<NewsDTO> findAll() {
//        return newsRepository.findAll()
        return newsRepository.getLastFiveNews(PageRequest.of(0, 5));
//        return newsRepository.findAllByOrderByNewsCrdateDesc();
    }


}
