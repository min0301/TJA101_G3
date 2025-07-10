package com.pixeltribe.forumsys.forumpost.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ForumPostRepository extends JpaRepository<ForumPost, Integer> {

    List<ForumPost> findAllByOrderByPostUpdateDesc();

    List<ForumPost> findByForNo_Id(Integer forNo);

    long countByForNo_Id(Integer forNoId);

    // 變數名稱 `postId` 和 `forumId` 可變，但 `@Param` 的值 `:postId` 和 `:forumId` 不可變，必須與 HQL 查詢中的參數名一致
    @Query("SELECT fp FROM ForumPost fp JOIN FETCH fp.forNo JOIN FETCH fp.memNo WHERE fp.id = :postId AND fp.forNo.id = :forumId")
    Optional<ForumPost> findByIdAndForNoId(@Param("postId") Integer postId, @Param("forumId") Integer forumId);

    @Query("SELECT fp FROM ForumPost fp JOIN FETCH fp.forNo JOIN FETCH fp.memNo")
    List<ForumPost> findAllWithForumAndMember();

    // 變數名稱 `id` 可變，但 `@Param` 的值 `:id` 不可變
    @Query("SELECT fp FROM ForumPost fp JOIN FETCH fp.forNo JOIN FETCH fp.memNo WHERE fp.id = :id")
    Optional<ForumPost> findByIdWithForumAndMember(@Param("id") Integer id); // `findByIdWithForumAndMember` 是可變的方法名稱
}