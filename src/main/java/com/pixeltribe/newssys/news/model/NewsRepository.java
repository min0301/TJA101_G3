package com.pixeltribe.newssys.news.model;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NewsRepository extends JpaRepository<News, Integer> {

    @Query( """   
            select new com.pixeltribe.newssys.news.model.NewsDTO(
                         n.id,
                         n.newsTit,
                         n.newsCon,
                         n.newsCrdate,
                         n.newsUpdate,
                         count (distinct img),
                         cast( function('GROUP_CONCAT', c.ncatName) as string )
                       )
                from   News n
                left  join n.newsImages img
                left  join n.newContentClassifications cc
                left  join cc.ncatNo c
                group by n.id, n.newsTit, n.newsCon, n.newsCrdate, n.newsUpdate
                order by n.newsCrdate desc
            """)
    public List<NewsDTO> getLastFiveNews(Pageable p);

    @Query( """
            select new com.pixeltribe.newssys.news.model.NewsDTO(
                         n.id ,
                         n.newsTit,
                         n.newsCon,
                         n.newsCrdate,
                         n.newsUpdate,
                         count (distinct img),
                         cast( function('GROUP_CONCAT',  cat.ncatName) as string )
                       )
                from   News n
                left  join n.newsImages img
                left  join n.newContentClassifications cc
                left  join cc.ncatNo cat
                            where n.id = :id
                group by n.id, n.newsTit, n.newsCon, n.newsCrdate, n.newsUpdate
                order by n.newsCrdate desc
            """)
    public NewsDTO getNewsById(@Param("id") Integer id);

}