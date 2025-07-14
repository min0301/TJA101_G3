package com.pixeltribe.newssys.newscomment.model;

import com.pixeltribe.membersys.member.model.MemRepository;
import com.pixeltribe.membersys.member.model.Member;
import com.pixeltribe.newssys.news.model.News;
import com.pixeltribe.newssys.news.model.NewsRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NewsCommentService {

    private final NewsCommentRepository newsCommentRepository;
    private final NewsRepository newsRepository;
    private final MemRepository memRepository;

    public NewsCommentService(NewsCommentRepository newsCommentRepository, NewsRepository newsRepository, MemRepository memRepository) {
        this.newsCommentRepository = newsCommentRepository;
        this.newsRepository = newsRepository;
        this.memRepository = memRepository;
    }

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

        return new NewsCommentCreationDTO(content, newsId, memberId);
    }

    public NewsCommentUpdateDTO save(@NotNull NewsCommentUpdateDTO dto) {
        Member member = memRepository.findById(dto.getMemNoId()).
                orElseThrow(() -> new EntityNotFoundException("not found member"));
        News news = newsRepository.findById(dto.getNewsNoId())
                .orElseThrow(() -> new EntityNotFoundException(" not found news"));
        NewsComment newsComment = new NewsComment();
        newsComment.setId(dto.getId());
        newsComment.setNcomCon(dto.getNcomCon());
        newsComment.setNcomStatus(dto.getNcomStatus());
        newsComment.setNewsNo(news);
        newsComment.setMemNo(member);
        NewsComment saved = newsCommentRepository.save(newsComment);
        return  new NewsCommentUpdateDTO(saved.getId(),saved.getNcomCon(),saved.getNcomStatus(),saved.getMemNo().getId(),saved.getNewsNo().getId());
    }
}
