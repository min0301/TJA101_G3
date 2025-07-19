package com.pixeltribe.forumsys.articlecomreport.model;

import com.pixeltribe.forumsys.message.model.ForumMes;
import com.pixeltribe.membersys.member.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArticleComReportRepository extends JpaRepository<ArticleComReport, Integer> {

    Optional<ArticleComReport> findByMesNoAndReporter(ForumMes mesNo, Member reporter);

    Long countByArtComRepStatus(Character artComRepStatus);
}
