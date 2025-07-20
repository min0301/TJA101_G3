package com.pixeltribe.forumsys.postlike.model;

import com.pixeltribe.forumsys.forumpost.model.ForumPost;
import com.pixeltribe.forumsys.forumpost.model.ForumPostRepository;
import com.pixeltribe.forumsys.shared.LikeStatus;
import com.pixeltribe.membersys.member.model.MemRepository;
import com.pixeltribe.membersys.member.model.Member;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service("postLikeService")
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final MemRepository memRepository;
    private final ForumPostRepository forumPostRepository;

    public PostLikeService(PostLikeRepository postLikeRepository, MemRepository memRepository, ForumPostRepository forumPostRepository) {
        this.postLikeRepository = postLikeRepository;
        this.memRepository = memRepository;
        this.forumPostRepository = forumPostRepository;
    }

    public List<PostLikeDTO> getAllPostLike() {
        List<PostLike> postLike = postLikeRepository.findAll();
        return postLike.stream()
                .map(PostLikeDTO::convertToPostLikeDTO)
                .toList();
    }

    @Transactional
    public PostLikeDTO updatePostLike(Integer postNo, Integer memberId, LikeStatus requestedStatus) {

        Member member = memRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("找不到會員編號:" + memberId));
        ForumPost post = forumPostRepository.findById(postNo)
                .orElseThrow(() -> new IllegalArgumentException("找不到文章編號:" + postNo));
        PostLike likeRecord = postLikeRepository.findByMemNoAndPostNo(member, post)
                .orElseGet(() -> {
                    PostLike newLike = new PostLike();
                    newLike.setMemNo(member);
                    newLike.setPostNo(post);
                    newLike.setPlikeStatus(LikeStatus.NEUTRAL);
                    return newLike;
                });
        LikeStatus oldStatus = likeRecord.getPlikeStatus();
        LikeStatus newStatus = oldStatus == requestedStatus ? LikeStatus.NEUTRAL : requestedStatus;

        int likeCnt = 0;
        int disLikeCnt = 0;

        if (oldStatus == LikeStatus.LIKE) likeCnt--;
        if (oldStatus == LikeStatus.DISLIKE) disLikeCnt--;
        if (newStatus == LikeStatus.LIKE) likeCnt++;
        if (newStatus == LikeStatus.DISLIKE) disLikeCnt++;


        post.setPostLikeCount(post.getPostLikeCount() + likeCnt);
        post.setPostLikeDlc(post.getPostLikeDlc() + disLikeCnt);


        likeRecord.setPlikeStatus(newStatus);

        return PostLikeDTO.convertToPostLikeDTO(postLikeRepository.save(likeRecord));
    }
}
