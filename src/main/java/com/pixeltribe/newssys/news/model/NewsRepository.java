package com.pixeltribe.newssys.news.model;

import io.lettuce.core.dynamic.annotation.Param;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.web.bind.annotation.RequestBody;

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
                where n.isShowed = true
                group by n.id, n.newsTit, n.newsCon, n.newsCrdate, n.newsUpdate
                order by n.newsCrdate desc
            """)
    public Page<NewsDTO> getPageNews(Pageable p);


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

    /* ---------- 後台分頁（管理端） ---------- */
    @Query("""
                    select new com.pixeltribe.newssys.news.model.NewsAdminDTO(
                        n.id,
                        n.newsTit,
                        n.newsCon,
                        n.newsUpdate,
                        n.newsCrdate,
                        n.isShowed,
                        cast( size(n.newsImages) as long),
                        cast(function('GROUP_CONCAT', c.ncatName) as string),
                        n.adminNo.id,
                        n.adminNo.admName
                    )
                    from  News n
                    left  join n.newContentClassifications cc
                    left  join cc.ncatNo c
                    group by n.id, n.newsTit, n.newsCon, n.newsUpdate,
                             n.newsCrdate, n.isShowed, n.adminNo.id,n.adminNo.admName
                    order by n.newsCrdate desc
            """)
    Page<NewsAdminDTO> findAdminPageNews(Pageable p);

//    public NewsCreationDTO save(@Valid @RequestBody News news);

}