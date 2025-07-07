package com.pixeltribe.forumsys.forumcategory.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ForumCategoryRepository extends JpaRepository<ForumCategory, Integer> {
    Optional<ForumCategory> findByCatName(String catName);
}
