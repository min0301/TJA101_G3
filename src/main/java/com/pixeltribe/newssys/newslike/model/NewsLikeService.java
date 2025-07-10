package com.pixeltribe.newssys.newslike.model;

import com.pixeltribe.membersys.member.model.MemRepository;
import com.pixeltribe.membersys.member.model.Member;
import com.pixeltribe.newssys.newscomment.model.NewsComment;
import com.pixeltribe.newssys.newscomment.model.NewsCommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NewsLikeService {

    private final NewsLikeRepository newsLikeRepository;
    private final MemRepository memRepository;
    private final NewsCommentRepository commentRepository;

    @Autowired
    NewsLikeService(NewsLikeRepository newsLikeRepository, MemRepository memRepository, NewsCommentRepository commentRepository) {
        this.newsLikeRepository = newsLikeRepository;
        this.memRepository = memRepository;
        this.commentRepository = commentRepository;
    }

    @Transactional(readOnly = true)
    public List<NewsLikeDTO> getAllNewsLike() {
        return newsLikeRepository.getAll();
    }

    @Transactional
    public NewsLikeDTO addNewsLike(Integer commendId, Integer memberId, Character status) {
        Member member = memRepository.getReferenceById(memberId);
        NewsComment comment = commentRepository.getReferenceById(commendId);

        NewsLike newsLike = new NewsLike();
        if (!newsLikeRepository.existsByNcomNo_IdAndMemNo_Id(commendId, memberId)) {
            newsLike.setMemNo(member);
            newsLike.setNcomNo(comment);
            newsLike.setNlikeStatus(status);
        } else {
            newsLike = newsLikeRepository.findByMemNo_IdAndNcomNo_Id(memberId, commendId);
            newsLike.setNlikeStatus(status);
        }
        newsLikeRepository.save(newsLike);

        return new NewsLikeDTO(newsLike.getId(), newsLike.getNlikeStatus(), memberId, commendId);
    }


    public List<NewsLikeDTO> getAllNewsLikeByComment(Integer id) {

        return newsLikeRepository.findAllByNcomNo_Id(id);

    }

    public List<NewsLikeDTO> getAllNewsLikeByMember(Integer id) {

        return newsLikeRepository.findAllByMemNo_Id(id);
    }
}
