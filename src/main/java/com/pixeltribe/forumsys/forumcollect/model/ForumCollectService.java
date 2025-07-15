package com.pixeltribe.forumsys.forumcollect.model;

import com.pixeltribe.forumsys.exception.ResourceNotFoundException;
import com.pixeltribe.forumsys.forum.model.Forum;
import com.pixeltribe.forumsys.forum.model.ForumRepository;
import com.pixeltribe.forumsys.shared.CollectStatus;
import com.pixeltribe.membersys.member.model.MemRepository;
import com.pixeltribe.membersys.member.model.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ForumCollectService {

    private final ForumCollectRepository forumCollectRepository;
    private final ForumRepository forumRepository;
    private final MemRepository memRepository;

    public ForumCollectService(ForumCollectRepository forumCollectRepository, ForumRepository forumRepository, MemRepository memRepository) {
        this.forumCollectRepository = forumCollectRepository;
        this.forumRepository = forumRepository;
        this.memRepository = memRepository;

    }

    @Transactional
    public ForumCollectDTO addForumCollect(Integer memberId, Integer forNo) {

        Member member = memRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到會員, 編號: " + memberId));
        Forum forum = forumRepository.findById(forNo)
                .orElseThrow(() -> new ResourceNotFoundException("找不到論壇文章, 編號: " + forNo));

        Optional<ForumCollect> existingCollectOpt = forumCollectRepository.findByForNoAndMemNo(forum, member);
        ForumCollect forumCollect;
        if (existingCollectOpt.isPresent()) {
            forumCollect = existingCollectOpt.get();
            if (forumCollect.getCollectStatus() == CollectStatus.COLLECT) {
                forumCollect.setCollectStatus(CollectStatus.NORMAL);
            } else {
                forumCollect.setCollectStatus(CollectStatus.COLLECT);
            }
        } else {
            forumCollect = new ForumCollect();
            forumCollect.setForNo(forum);
            forumCollect.setMemNo(member);
            forumCollect.setCollectStatus(CollectStatus.COLLECT);
        }
        return ForumCollectDTO.convertToForumCollectDTO(forumCollectRepository.save(forumCollect));
    }

    public List<ForumCollectDTO> getForumCollectForMenber(Integer memberId) {
        Member member = memRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到會員, 編號: " + memberId));
        List<ForumCollect> forumCollect = forumCollectRepository.findByMemNo(member);
        return forumCollect.stream()
                .map(ForumCollectDTO::convertToForumCollectDTO)
                .toList();

    }

}
