package com.pixeltribe.newssys.newsimage.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NewsImageRepository extends JpaRepository<NewsImage, Integer> {

    List<NewsImageDTO> findNewsImageByNewsNo_Id(Integer newsNoId);

    @Query("""
            select new com.pixeltribe.newssys.newsimage.model.NewsImageIndexDTO(
            nl.imgUrl,
            nl.newsNo.id,
            nl.newsNo.newsTit,
            nl.imgType
            )from NewsImage nl
            join nl.newsNo n
            where nl.id in (
                select min(sub.id)
                from NewsImage sub
                group by sub.newsNo.id
            )and n.isShowed = true
            order by n.newsCrdate desc
            """)
    List<NewsImageIndexDTO> getTopFiveImage();
}