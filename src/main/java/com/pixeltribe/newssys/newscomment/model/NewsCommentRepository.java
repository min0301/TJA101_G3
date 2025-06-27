package com.pixeltribe.newssys.newscomment.model;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsCommentRepository extends JpaRepository<NewsComment, Integer> {
}