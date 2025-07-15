package com.pixeltribe.newssys.newslike.model;

import com.pixeltribe.membersys.member.model.MemRepository;
import com.pixeltribe.membersys.member.model.Member;
import com.pixeltribe.newssys.newscomment.model.NewsComment;
import com.pixeltribe.newssys.newscomment.model.NewsCommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
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

//TODO 由 updateNewsLike() 接管
//    @Transactional()
//    public NewsLikeDTO addNewsLike(Integer commendId, Integer memberId, Character status) {
//        Member member = memRepository.getReferenceById(memberId);
//        NewsComment comment = commentRepository.getReferenceById(commendId);
//        NewsLike newsLike = new NewsLike();
//        if (!newsLikeRepository.existsByNcomNo_IdAndMemNo_Id(commendId, memberId)) {
//            newsLike.setMemNo(member);
//            newsLike.setNcomNo(comment);
//            newsLike.setNlikeStatus(status);
//            if (status == '2') {
//                comment.setNcomLikeLc(comment.getNcomLikeLc() + 1);
//            } else {
//                comment.setNcomLikeDlc(comment.getNcomLikeDlc() + 1);
//            }
//        }
//        newsLikeRepository.save(newsLike);
//        return new NewsLikeDTO(newsLike.getId(), newsLike.getNlikeStatus(), memberId, commendId);
//    }

    @Transactional(readOnly = true)
    public List<NewsLikeDTO> getAllNewsLikeByComment(Integer id) {

        return newsLikeRepository.findAllByNcomNo_Id(id);

    }

    @Transactional(readOnly = true)
    public List<NewsLikeDTO> getAllNewsLikeByMember(Integer id) {

        return newsLikeRepository.findAllByMemNo_Id(id);
    }

    @Transactional(readOnly = true)
    public NewsLikeDTO getUserLikeStatus(Integer memNoId, Integer ncomNoId) {
        NewsLike like = newsLikeRepository.findByMemNo_IdAndNcomNo_Id(memNoId, ncomNoId);

        if (like == null) {
            // 回傳中立狀態（你可以決定中立用 '1' 或其他值）
            return new NewsLikeDTO(null, '1', memNoId, ncomNoId);
        }

        return new NewsLikeDTO(like.getId(), like.getNlikeStatus(), memNoId, ncomNoId);
    }

    public NewsLikeDTO updateNewsLike(Integer commendId, Integer memberId, Character status) {
        Member member = memRepository.getReferenceById(memberId);
        NewsComment comment = commentRepository.getReferenceById(commendId);

        NewsLike newsLike = newsLikeRepository.findByMemNo_IdAndNcomNo_Id(memberId, commendId);
        if (newsLike == null) {
            // 第一次操作：建立新的按讚紀錄
            newsLike = new NewsLike();
            newsLike.setMemNo(member);
            newsLike.setNcomNo(comment);
            newsLike.setNlikeStatus(status);
            newsLikeRepository.save(newsLike);

            // 更新留言的讚/倒讚數
            if (status == '2') {
                comment.setNcomLikeLc(comment.getNcomLikeLc() + 1);
            } else if (status == '3') {
                comment.setNcomLikeDlc(comment.getNcomLikeDlc() + 1);
            }
            commentRepository.save(comment);
            return new NewsLikeDTO(newsLike.getId(), status, memberId, commendId);
        }
        Character originalStatus = newsLike.getNlikeStatus();

        // 若狀態有變更，才調整留言統計數
        if (!originalStatus.equals(status)) {
            if (originalStatus == '2') {
                comment.setNcomLikeLc(comment.getNcomLikeLc() - 1); // 取消原本讚
            } else if (originalStatus == '3') {
                comment.setNcomLikeDlc(comment.getNcomLikeDlc() - 1); // 取消原本倒讚
            }

            if (status == '2') {
                comment.setNcomLikeLc(comment.getNcomLikeLc() + 1); // 新增讚
            } else if (status == '3') {
                comment.setNcomLikeDlc(comment.getNcomLikeDlc() + 1); // 新增倒讚
            }

            newsLike.setNlikeStatus(status); // 更新讚狀態
        }

        return new NewsLikeDTO(newsLike.getId(), status, memberId, commendId);

    }
}
