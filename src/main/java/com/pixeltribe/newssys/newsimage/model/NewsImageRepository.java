package com.pixeltribe.newssys.newsimage.model;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NewsImageRepository extends JpaRepository<NewsImage, Integer> {

    List<NewsImageDTO> findNewsImageByNewsNo_Id(Integer newsNoId);

}