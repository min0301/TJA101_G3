package com.pixeltribe.forumsys.postcollect.model;

import com.pixeltribe.forumsys.forumpost.model.ForumPost;
import com.pixeltribe.forumsys.shared.PostCollectStatus;
import com.pixeltribe.membersys.member.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PostCollectRepository extends JpaRepository<PostCollect, Integer> {

    Optional<PostCollect> findByPostNoAndMemNo(ForumPost forumPost, Member member);

    List<PostCollect> findByMemNo(Member member);

    // **新增這行：根據會員和收藏狀態查詢**
    List<PostCollect> findByMemNoAndPostCollectStatus(Member member, PostCollectStatus status);
}
