package com.pixeltribe.newssys.newslike.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsLikeRepository extends JpaRepository<NewsLike, Integer> {

    @Query("""
                select new com.pixeltribe.newssys.newslike.model.NewsLikeDTO(
                            nl.id,nl.nlikeStatus,nl.memNo.id,nl.ncomNo.id
                            )
                                        from NewsLike nl 
                                        order by nl.memNo.id
            """)
    public List<NewsLikeDTO> getAll();

    public Boolean existsByNcomNo_IdAndMemNo_Id(Integer ncomNoId, Integer memNoId);

    NewsLike findByMemNo_IdAndNcomNo_Id(Integer id, Integer id1);

    NewsLike findByNcomNo_Id(Integer id);

    List<NewsLikeDTO> findAllByNcomNo_Id(Integer ncomNoId);

    List<NewsLikeDTO> findAllByMemNo_Id(Integer memNoId);
}