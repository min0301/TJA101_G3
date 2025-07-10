package com.pixeltribe.forumsys.forum.model;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface ForumRepository extends JpaRepository<Forum, Integer> {

    //    依最後更新時間排序
    List<Forum> findAllByOrderByForUpdateDesc();

    List<Forum> findAllByForStatusOrderByForUpdateDesc(Character forStatus);

    List<Forum> findByCatNo_Id(Integer catNo); // 方法名稱 findBy{欄位名稱} 是固定的，參數名稱可

    List<Forum> findAllByForStatus(Character status);


    @Query("SELECT f.id, COUNT(m.id), MAX(m.mesCrdate) " +
            "FROM ForumMes m " +
            "JOIN m.postNo p " +
            "JOIN p.forNo f " +
            "WHERE m.mesCrdate >= :since " +
            "GROUP BY f.id")
    List<Object[]> findForumHotSince(@Param("since") Instant since);
}
