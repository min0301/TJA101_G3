package com.pixeltribe.forumsys.forumpost.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // 確保有這個導入
import org.springframework.data.repository.query.Param; // 確保有這個導入

import java.util.List;
import java.util.Optional; // 確保有這個導入

public interface ForumPostRepository extends JpaRepository<ForumPost, Integer> {

    // **修正點1：將 forUpdate 改為 PostUpdate**
    List<ForumPost> findAllByOrderByPostUpdateDesc();

    // 保持不變：根據討論區 ID 查詢文章
    List<ForumPost> findByForNo_Id(Integer forNo);

    // **新增：計算文章數量的方法**
    long countByForNo_Id(Integer forNoId); // Spring Data JPA 會自動實現

    // **新增：根據文章 ID 和討論區 ID 查詢單篇文章**
    @Query("SELECT fp FROM ForumPost fp JOIN FETCH fp.forNo JOIN FETCH fp.memNo WHERE fp.id = :postId AND fp.forNo.id = :forumId")
    Optional<ForumPost> findByIdAndForNoId(@Param("postId") Integer postId, @Param("forumId") Integer forumId);

    // **新增：獲取所有文章，並急切加載 Forum 和 Member (用於 getAllForumPost 顯示名稱)**
    //後台較有用處 查詢一個討論區共有幾篇文章數量
    @Query("SELECT fp FROM ForumPost fp JOIN FETCH fp.forNo JOIN FETCH fp.memNo")
    List<ForumPost> findAllWithForumAndMember();
}
