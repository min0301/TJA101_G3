package com.pixeltribe.forumsys.forumcollect.model;

import com.pixeltribe.forumsys.forum.model.Forum;
import com.pixeltribe.forumsys.shared.CollectStatus;
import com.pixeltribe.membersys.member.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ForumCollectRepository extends JpaRepository<ForumCollect, Integer> {

    Optional<ForumCollect> findByForNoAndMemNo(Forum forum, Member member);

    List<ForumCollect> findByMemNo(Member member);

    List<ForumCollect> findByMemNoAndCollectStatus(Member memNo, CollectStatus collectStatus);

}
