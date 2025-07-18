package com.pixeltribe.newssys.newscomment.model;

import com.pixeltribe.membersys.member.model.MemRepository;
import com.pixeltribe.membersys.member.model.Member;
import com.pixeltribe.newssys.news.model.News;
import com.pixeltribe.newssys.news.model.NewsRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    @Transactional
    public NewsCommentUpdateDTO save(@NotNull NewsCommentUpdateDTO dto) {
        Member member = memRepository.findById(dto.getMemNoId()).
                orElseThrow(() -> new EntityNotFoundException("not found member"));
        News news = newsRepository.findById(dto.getNewsNoId())
                .orElseThrow(() -> new EntityNotFoundException(" not found news"));
        NewsComment origin = newsCommentRepository.findById(dto.getId())
                .orElseThrow(() -> new EntityNotFoundException("not found comment"));

        NewsComment newsComment = new NewsComment();
//        newsComment.setId(dto.getId());
        origin.setNcomCon(dto.getNcomCon());
        origin.setNcomStatus(dto.getNcomStatus());
//        newsComment.setNewsNo(news);
//        newsComment.setMemNo(member);
        NewsComment saved = newsCommentRepository.save(origin);
        return  new NewsCommentUpdateDTO(saved.getId(),saved.getNcomCon(),saved.getNcomStatus(),saved.getMemNo().getId(),saved.getNewsNo().getId());
    }
    @Transactional(readOnly = true)
    public Page<AdminNewsCommentPageDTO> findAdminPage(Pageable p, Integer newsId, Integer memberId, Character status, String keyword) {
        return newsCommentRepository.findAdminPage(p, newsId, memberId, status, keyword);
    }
    @Transactional
    public CommentHideDTO toggleHide(Integer id, Character hidden) {
        // 1: normal, 2: hidden
        int updated = newsCommentRepository.updateHideStatus(id, hidden);
        if (updated == 0) throw new EntityNotFoundException("comment");

        return new CommentHideDTO(id, hidden);
    }

    @Transactional(readOnly = true)
    public Integer countAllNewsComment() {

        Integer countActiveComments = (int)newsCommentRepository.countByNcomStatus('1');
        return countActiveComments;
    }

    @Transactional(readOnly = true)
    public AdminNewsCommentDetailDTO getCommentById(Integer commentId) {

        return newsCommentRepository.getNewsCommentById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("沒有找到 comment"));

    }
}
