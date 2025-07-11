package com.pixeltribe.forumsys.forumtag.model; // 確保這個 package 名稱正確

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; // 確保有這個導入

// 繼承 JpaRepository，第一個泛型參數是你的 Entity 類，第二個是 Entity ID 的類型
@Repository // 標註這是一個 Repository 層元件
public interface ForumTagRepository extends JpaRepository<ForumTag, Integer> {
    // Spring Data JPA 會自動實現 findAll()、findById() 等基本方法
    // 無需在這裡寫任何具體的方法實現
}