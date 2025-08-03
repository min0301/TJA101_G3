package com.pixeltribe.forumsys.forum.model;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ForumRepository extends JpaRepository<Forum, Integer> {

    //    依最後更新時間排序
    List<Forum> findAllByOrderByForUpdateDesc();

    List<Forum> findAllByForStatusOrderByForUpdateDesc(Character forStatus);

    @EntityGraph(attributePaths = { "catNo" })
    List<Forum> findByCatNo_Id(Integer catNo);

    List<Forum> findAllByForStatus(Character status);

    Optional<Forum> findByForName(String forName);

    @Query("SELECT f.id, COUNT(m.id) " +
            "FROM ForumMes m " +
            "JOIN m.postNo p " +
            "JOIN p.forNo f " +
            "WHERE m.mesCrdate >= :since " +
            "GROUP BY f.id")
    List<Object[]> findForumHotSince(@Param("since") Instant since);

    @Query("SELECT f FROM Forum f " +
            "WHERE LOWER (f.forName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(f.forDes) LIKE LOWER(CONCAT('%', :keyword, '%')) ")
    List<Forum> searchForumsByKeyword(@Param("keyword") String keyword);

}
