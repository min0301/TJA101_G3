package com.pixeltribe.newssys.newsimage.model;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NewsImageRepository extends JpaRepository<NewsImage, Integer> {

    @Query("""
                SELECT new com.pixeltribe.newssys.newsimage.model.NewsImageDTO(n.id, n.imgType)
                FROM NewsImage n
                WHERE n.newsNo.id = :newsNo
            """)
    List<NewsImageDTO> findAllMetaByNewsNo(@Param("newsNo") Integer newsNo);

}