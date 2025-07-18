package com.pixeltribe.forumsys.postcollect.model;

import com.pixeltribe.forumsys.exception.ResourceNotFoundException;
import com.pixeltribe.forumsys.forumpost.model.ForumPost;
import com.pixeltribe.forumsys.forumpost.model.ForumPostRepository;
import com.pixeltribe.forumsys.shared.PostCollectStatus;
import com.pixeltribe.membersys.member.model.MemRepository;
import com.pixeltribe.membersys.member.model.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PostCollectService {

    private final PostCollectRepository postCollectRepository;
    private final ForumPostRepository forumPostRepository;
    private final MemRepository memRepository;

    public PostCollectService(PostCollectRepository postCollectRepository, ForumPostRepository forumPostRepository, MemRepository memRepository){
        this.postCollectRepository = postCollectRepository;
        this.forumPostRepository = forumPostRepository;
        this.memRepository = memRepository;
    }

    @Transactional
    public PostCollectDTO addPostCollect(Integer memberId, Integer postId, PostCollectUpdateDTO postCollectUpdateDTO ){

        Member member = memRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到會員, 編號: " + postCollectUpdateDTO.getMemberNo()));

        ForumPost forumPost = forumPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到文章, 編號: " + postId));

        Optional<PostCollect> existingCollectOpt = postCollectRepository.findByPostNoAndMemNo(forumPost, member);
        PostCollect postCollect;
        if (existingCollectOpt.isPresent()) {
            postCollect = existingCollectOpt.get();
            if (postCollect.getPostCollectStatus() == PostCollectStatus.COLLECT) {
                postCollect.setPostCollectStatus(PostCollectStatus.NORMAL);
            } else {
                postCollect.setPostCollectStatus(PostCollectStatus.COLLECT);
            }
        } else {
            postCollect = new PostCollect();
            postCollect.setPostNo(forumPost);
            postCollect.setMemNo(member);
            postCollect.setPostCollectStatus(PostCollectStatus.COLLECT);
        }
        return PostCollectDTO.convertToPostCollectDTO(postCollectRepository.save(postCollect));
    }

    public List<PostCollectDTO> getPostCollectForMember(Integer memberId){
        Member member = memRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到會員, 編號: " + memberId));
        List<PostCollect> postCollect = postCollectRepository.findByMemNo(member);
        return postCollect.stream()
                .map(PostCollectDTO::convertToPostCollectDTO)
                .toList();

    }

    public boolean isPostCollected(Integer memberId, Integer postId) {
     Member member = memRepository.findById(memberId).orElse(null);
     ForumPost forumPost = forumPostRepository.findById(postId).orElse(null);
     if (member == null || forumPost == null) {
         return false;
     }
     Optional<PostCollect> collect = postCollectRepository.findByPostNoAndMemNo(forumPost, member);
     return collect.isPresent() && collect.get().getPostCollectStatus() == PostCollectStatus.COLLECT;
 }
}

