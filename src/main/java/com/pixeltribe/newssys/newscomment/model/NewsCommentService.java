package com.pixeltribe.newssys.newscomment.model;

import com.pixeltribe.membersys.member.model.MemRepository;
import com.pixeltribe.membersys.member.model.Member;
import com.pixeltribe.newssys.news.model.News;
import com.pixeltribe.newssys.news.model.NewsRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class NewsCommentService {
    @Autowired
    NewsCommentRepository newsCommentRepository;
    @Autowired
    NewsRepository newsRepository;
    @Autowired
    MemRepository memRepository;

    @Transactional(readOnly = true)
    public List<NewsCommentDTO> findAll(Integer id) {
        return newsCommentRepository.getNewsCommentsByNewsNo(id);
    }

    @Transactional
    public NewsCommentCreationDTO add(Integer newsId, Integer memberId, String content) {

        News news = newsRepository.findById(newsId).orElseThrow(() -> new EntityNotFoundException("news"));

        Member member = memRepository.findById(memberId).orElseThrow(() -> new EntityNotFoundException("member"));

        NewsComment c = new NewsComment();
        c.setNewsNo(news);
        c.setMemNo(member);
        c.setNcomCon(content);
        newsCommentRepository.save(c);

        NewsCommentCreationDTO nCCDTO = new NewsCommentCreationDTO(content, newsId, memberId);

        return nCCDTO;
    }
}
