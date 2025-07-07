package com.pixeltribe.newssys.news.model;

import com.pixeltribe.membersys.administrator.model.AdmRepository;
import com.pixeltribe.membersys.administrator.model.Administrator;
import com.pixeltribe.membersys.member.model.MemRepository;
import com.pixeltribe.membersys.member.model.Member;
import com.pixeltribe.newssys.newscategory.model.NewsCategory;
import com.pixeltribe.newssys.newscategory.model.NewsCategoryRepository;
import com.pixeltribe.newssys.newscontentclassification.model.NewContentClassification;
import com.pixeltribe.newssys.newscontentclassification.model.NewContentClassificationRepository;
import com.pixeltribe.newssys.newsimage.model.NewsImage;
import com.pixeltribe.newssys.newsimage.model.NewsImageRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

//TODO
//    @Transactional(readOnly = true)
//    public void addNews(News news) {
//        newsRepository.save(news);
//    }
//    public void updateNews(News news) {
//        newsRepository.save(news);
//    }
    @Transactional(readOnly = true)
    public NewsDTO getOneNews(Integer id) {
        NewsDTO newsDto = newsRepository.getNewsById(id);
        return newsDto;
    }

    @Transactional(readOnly = true)
    public List<NewsDTO> findAll() {
        return newsRepository.getLastFiveNews(PageRequest.of(0, 5));
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

        NewsCreationDTO nCDTO = new NewsCreationDTO(tit,con,adminNoId,tags);

        return  nCDTO;
    }

}
