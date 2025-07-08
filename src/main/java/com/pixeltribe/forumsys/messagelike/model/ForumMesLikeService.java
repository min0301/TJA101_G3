package com.pixeltribe.forumsys.messagelike.model;

import com.pixeltribe.forumsys.message.model.ForumMes;
import com.pixeltribe.forumsys.message.model.ForumMesRepository;
import com.pixeltribe.forumsys.shared.LikeStatus;
import com.pixeltribe.membersys.member.model.MemRepository;
import com.pixeltribe.membersys.member.model.Member;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
    public ForumMesLikeDTO updateLike(Integer mesNo, Integer memId, LikeStatus requestedStatus) {
        Member member = memRepository.findById(memId)
                .orElseThrow(() -> new IllegalArgumentException("找不到會員編號:" + memId));
        ForumMes message = forumMesRepository.findById(mesNo)
                .orElseThrow(() -> new IllegalArgumentException("找不到訊息編號:" + mesNo));
        Optional<ForumMesLike> existingLikeOpt = forumMesLikeRepository.findByMemNoAndMesNo(member, message);

        if (existingLikeOpt.isPresent()) {
            ForumMesLike existingLike = existingLikeOpt.get();
            if (existingLike.getFmlikeStatus() == requestedStatus) {
                existingLike.setFmlikeStatus(LikeStatus.NEUTRAL);
            } else {
                // 否則，更新為新狀態 (例如從讚改為倒讚)
                existingLike.setFmlikeStatus(requestedStatus);
            }
            forumMesLikeRepository.save(existingLike); // save 方法會自動判斷是更新還是新增
            return ForumMesLikeDTO.convertToForumMesLikeDTO(existingLike);
        } else {
            // --- 紀錄不存在，直接新增 ---
            ForumMesLike newLike = new ForumMesLike();
            newLike.setMemNo(member);
            newLike.setMesNo(message);
            newLike.setFmlikeStatus(requestedStatus);
            forumMesLikeRepository.save(newLike);
            return ForumMesLikeDTO.convertToForumMesLikeDTO(newLike);
        }

    }


}
