package com.pixeltribe.newssys.news.model;

import com.pixeltribe.common.PageResponse;
import com.pixeltribe.common.PageResponseFactory;
import com.pixeltribe.membersys.administrator.model.AdmRepository;
import com.pixeltribe.membersys.administrator.model.Administrator;
import com.pixeltribe.newssys.newscategory.model.NewsCategory;
import com.pixeltribe.newssys.newscategory.model.NewsCategoryRepository;
import com.pixeltribe.newssys.newscontentclassification.model.NewContentClassification;
import com.pixeltribe.newssys.newscontentclassification.model.NewContentClassificationRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NewsService {
    @Autowired
    NewsRepository newsRepository;
    @Autowired
    AdmRepository admRepository;
    @Autowired
    NewsCategoryRepository newsCategoryRepository;
    @Autowired
    NewContentClassificationRepository newContentClassificationRepository;

    @Transactional(readOnly = true)
    public NewsDTO getOneNews(Integer id) {
        NewsDTO newsDto = newsRepository.getNewsById(id);
        return newsDto;
    }

    @Transactional(readOnly = true)
    public PageResponse<NewsDTO> findAll(int page, int size) {
        Page<NewsDTO> pageData = newsRepository.getPageNews(PageRequest.of(page, size));
        return PageResponseFactory.fromPage(pageData);
    }

    @Transactional
    public NewsCreationDTO createNews(String tit, String con, Integer adminNoId, List<Integer> tags) {

        Administrator admin = admRepository.findById(adminNoId).orElseThrow(() -> new EntityNotFoundException("admin"));

        News news = new News();
        news.setNewsTit(tit);
        news.setNewsCon(con);
        news.setAdminNo(admin);

        newsRepository.save(news);

        // 將每個 tag id 建立對應關聯
        for (Integer tagId : tags) {
            NewsCategory tag = newsCategoryRepository.findById(tagId)
                    .orElseThrow(() -> new EntityNotFoundException("分類編號不存在：" + tagId));

            NewContentClassification ncc = new NewContentClassification();
            ncc.setNewsNo(news);
            ncc.setNcatNo(tag);

            newContentClassificationRepository.save(ncc);
        }

        NewsCreationDTO nCDTO = new NewsCreationDTO(news.getId(), tit, con, adminNoId, tags);

        return nCDTO;
    }

    @Transactional(readOnly = true)
    public PageResponse<NewsAdminDTO> findAllAdminNews(int page, int size) {
        Page<NewsAdminDTO> pageData = newsRepository.findAdminPageNews(PageRequest.of(page, size));
        return PageResponseFactory.fromPage(pageData);
    }

    @Transactional
    public NewsAdminUpdateDto updateNews(@Valid NewsAdminUpdateDto nauDTO) {
        News news = newsRepository.findById(nauDTO.getId()).orElseThrow(() -> new EntityNotFoundException("not found news"));

        news.setNewsTit(nauDTO.getNewsTit());
        news.setNewsCon(nauDTO.getNewsCon());
        news.setIsShowed(nauDTO.getIsShowed());


        return nauDTO;

    }
}
