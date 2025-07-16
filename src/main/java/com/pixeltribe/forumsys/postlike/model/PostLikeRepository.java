package com.pixeltribe.forumsys.postlike.model;

import com.pixeltribe.forumsys.forumpost.model.ForumPost;
import com.pixeltribe.membersys.member.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Integer> {

    Optional<PostLike> findByMemNoAndPostNo(Member member, ForumPost post);
}
