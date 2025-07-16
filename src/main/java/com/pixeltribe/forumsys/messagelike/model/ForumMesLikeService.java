package com.pixeltribe.forumsys.messagelike.model;

import com.pixeltribe.forumsys.message.model.ForumMes;
import com.pixeltribe.forumsys.message.model.ForumMesRepository;
import com.pixeltribe.forumsys.shared.LikeStatus;
import com.pixeltribe.membersys.member.model.MemRepository;
import com.pixeltribe.membersys.member.model.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("forumMesLikeService")
public class ForumMesLikeService {

    private final ForumMesLikeRepository forumMesLikeRepository;
    private final MemRepository memRepository;
    private final ForumMesRepository forumMesRepository;

    public ForumMesLikeService(ForumMesLikeRepository forumMesLikeRepository, MemRepository memRepository, ForumMesRepository forumMesRepository) {
        this.forumMesLikeRepository = forumMesLikeRepository;
        this.memRepository = memRepository;
        this.forumMesRepository = forumMesRepository;
    }

    public List<ForumMesLikeDTO> getAllForumMesLike() {
        List<ForumMesLike> forumMesLike = forumMesLikeRepository.findAll();
        return forumMesLike.stream()
                .map(ForumMesLikeDTO::convertToForumMesLikeDTO)
                .toList();
    }

    @Transactional
    public ForumMesLikeDTO updateLike(Integer mesNo, Integer memberId, LikeStatus requestedStatus) {
        Member member = memRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("找不到會員編號:" + memberId));
        ForumMes message = forumMesRepository.findById(mesNo)
                .orElseThrow(() -> new IllegalArgumentException("找不到訊息編號:" + mesNo));
        ForumMesLike likeRecord = forumMesLikeRepository.findByMemNoAndMesNo(member, message)
                .orElseGet(() -> {
                    ForumMesLike newLike = new ForumMesLike();
                    newLike.setMemNo(member);
                    newLike.setMesNo(message);
                    newLike.setFmlikeStatus(LikeStatus.NEUTRAL);
                    return newLike;
                });
        LikeStatus oldStatus = likeRecord.getFmlikeStatus();
        LikeStatus newStatus = oldStatus == requestedStatus ? LikeStatus.NEUTRAL : requestedStatus;

        int likeCnt = 0;
        int disLikeCnt = 0;

        if (oldStatus == LikeStatus.LIKE) likeCnt--;
        if (oldStatus == LikeStatus.DISLIKE) disLikeCnt--;
        if (newStatus == LikeStatus.LIKE) likeCnt++;
        if (newStatus == LikeStatus.DISLIKE) disLikeCnt++;

        message.setMesLikeLc(message.getMesLikeLc() + likeCnt);
        message.setMesLikeDlc(message.getMesLikeDlc() + disLikeCnt);

        likeRecord.setFmlikeStatus(newStatus);

        return ForumMesLikeDTO.convertToForumMesLikeDTO(forumMesLikeRepository.save(likeRecord));
    }


}
