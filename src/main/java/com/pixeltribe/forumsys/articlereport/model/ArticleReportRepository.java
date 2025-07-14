package com.pixeltribe.forumsys.articlereport.model;

import com.pixeltribe.forumsys.forumpost.model.ForumPost;
import com.pixeltribe.membersys.member.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArticleReportRepository extends JpaRepository<ArticleReport, Integer> {
    Optional<ArticleReport> findByForNoAndReporter(ForumPost postNo, Member reporter);

    ForumPost postNo(ForumPost postNo);
}
