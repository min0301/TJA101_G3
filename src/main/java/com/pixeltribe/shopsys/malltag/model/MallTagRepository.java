package com.pixeltribe.shopsys.malltag.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MallTagRepository extends JpaRepository<MallTag, Integer>{
	
	@Query(value ="SELECT MALL_TAG_NO FROM mall_tag WHERE LOWER(TRIM(MALL_TAG_NAME)) = LOWER(TRIM(:mallTagName)) ", nativeQuery = true)
    Integer isExistMallTag(@Param("mallTagName") String mallTagName);
    
}
