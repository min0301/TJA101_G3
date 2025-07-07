package com.pixeltribe.forumsys.messagelike.model;

import com.pixeltribe.forumsys.message.model.ForumMes;
import com.pixeltribe.membersys.member.model.Member;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ForumMesLikeRepository extends JpaRepository<ForumMesLike, Integer> {

    Optional<ForumMesLike> findByMemNoAndMesNo(Member member, ForumMes message);

}
