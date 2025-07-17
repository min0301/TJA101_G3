package com.pixeltribe.newssys.newscomment.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NewsCommentRepository extends JpaRepository<NewsComment, Integer> {

    @Query("""
            select new com.pixeltribe.newssys.newscomment.model.NewsCommentDTO(
                        n.id,
                        n.ncomCon,
                        n.ncomCre,
                        n.ncomStatus,
                        n.memNo.id,
                        n.memNo.memNickName,
                        n.ncomLikeLc,
                        n.ncomLikeDlc) from NewsComment n
                        where n.newsNo.id=:newsNo and n.ncomStatus='1'
                        order by n.ncomCre desc
            """)
    List<NewsCommentDTO> getNewsCommentsByNewsNo(Integer newsNo);

    NewsComment save(NewsComment newsComment);

    /* ========= 1. 後台留言清單 ========= */
    @Query("""
            SELECT new com.pixeltribe.newssys.newscomment.model.AdminNewsCommentPageDTO(
                       c.id,
                       c.ncomCon,
                       c.ncomCre,
                       c.ncomStatus,
                       n.id,
                       n.newsTit,
                       m.id,
                       m.memName,
                       c.ncomLikeLc,
                       c.ncomLikeDlc
            )
            FROM NewsComment c
            JOIN c.newsNo  n
            JOIN c.memNo   m
            WHERE (:newsId   IS NULL OR n.id  = :newsId)
              AND (:memberId IS NULL OR m.id  = :memberId)
              AND (:status   IS NULL OR c.ncomStatus = :status)
              AND (:keyword  IS NULL OR LOWER(c.ncomCon) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<AdminNewsCommentPageDTO> findAdminPage(
            Pageable pageable,
            @Param("newsId") Integer newsId,
            @Param("memberId") Integer memberId,
            @Param("status") Character status,
            @Param("keyword") String keyword);

    /* ========= 2. 後台單筆詳情 ========= */
    @Query("""
            SELECT new com.pixeltribe.newssys.newscomment.model.AdminNewsCommentDetailDTO(
                       c.id,
                       c.ncomCon,
                       c.ncomCre,
                       c.ncomStatus,
                       n.id,
                       n.newsTit,
                       m.id,
                       m.memNickName,
                       c.ncomLikeLc,
                       c.ncomLikeDlc
            )
            FROM NewsComment c
            JOIN c.newsNo n
            JOIN c.memNo  m
            WHERE c.id = :id
            """)
    Optional<AdminNewsCommentDetailDTO> findAdminDetailById(@Param("id") Integer id);

    /* ========= 3. 隱藏 / 取消隱藏 ========= */
    @Modifying
    @Query("UPDATE NewsComment c SET c.ncomStatus = :status WHERE c.id = :id")
    int updateHideStatus(@Param("id") Integer id, @Param("status") Character status);
}