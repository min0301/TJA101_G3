package com.pixeltribe.newssys.newscontentclassification.model;

import com.pixeltribe.newssys.news.model.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface NewsContentClassificationRepository extends JpaRepository<NewsContentClassification, Integer> {
    @Modifying
    @Query(value = "INSERT into NEWS_CONTENT_CLASSIFICATION (NEWS_NO,NCAT_NO) VALUES (:newsId,:categoryId)", nativeQuery = true)
    public NewsContentClassificationCreationDTO add(Integer newsId, Integer categoryId);

    boolean existsByNewsNoIdAndNcatNoId(Integer newsId, Integer categoryId);

    void deleteByNewsNoIdAndNcatNoId(Integer newsId, Integer categoryId);

    @Modifying
    @Query("""
            delete from NewsContentClassification n where n.newsNo.id=:newsId
            """)
    void deleteByNewsNoId(Integer newsId);

    @Query("""
        select ncc from NewsContentClassification ncc
        join fetch ncc.ncatNo
        where ncc.newsNo.id = :newsId
    """)
    List<NewsContentClassification> findByNewsId(Integer newsId);
}
