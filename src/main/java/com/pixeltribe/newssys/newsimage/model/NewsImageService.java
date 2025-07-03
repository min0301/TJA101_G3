package com.pixeltribe.newssys.newsimage.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NewsImageService {

    @Autowired
    NewsImageRepository newsImageRepository;

    public List<NewsImageDTO> getNewsImage(int id) {
        return newsImageRepository.findAllMetaByNewsNo(id);
    }

    public Optional<NewsImage> findById(int id) {
        return newsImageRepository.findById(id);
    }

}
