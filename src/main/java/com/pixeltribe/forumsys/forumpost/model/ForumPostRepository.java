package com.pixeltribe.forumsys.forumpost.model;// ForumPostRepository.java

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ForumPostRepository extends JpaRepository<ForumPost, Integer> {

    // 獲取所有狀態為 '0' 的文章
    List<ForumPost> findByPostStatus(Character status);

    // 獲取特定討論區下，狀態為 '0' 的文章
    List<ForumPost> findByForNo_IdAndPostStatus(Integer forNoId, Character status);

    // 獲取特定討論區下，狀態為 '0' 的文章，並依更新時間倒序排列
    List<ForumPost> findByForNo_IdAndPostStatusOrderByPostUpdateDesc(Integer forNoId, Character status);

    // 根據文章 ID 和討論區 ID 查詢特定狀態的文章
    // 如果單篇文章也需要限制狀態，可以這樣加
    Optional<ForumPost> findByIdAndForNoIdAndPostStatus(Integer postId, Integer forNoId, Character status);

    // 保持原有帶 Join Fetch 的方法，但需要自行在 Service 層過濾或新增帶 status 參數的 Join Fetch
    @Query("SELECT fp FROM ForumPost fp LEFT JOIN FETCH fp.forNo LEFT JOIN FETCH fp.memNo WHERE fp.id = :postId")
    Optional<ForumPost> findByIdWithForumAndMember(@Param("postId") Integer postId);

    @Query("SELECT fp FROM ForumPost fp LEFT JOIN FETCH fp.forNo LEFT JOIN FETCH fp.memNo")
    List<ForumPost> findAllWithForumAndMember();
}