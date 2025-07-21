package com.pixeltribe.shopsys.productImage.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductImageRepository extends JpaRepository<ProductImage, Integer>{
	
    @Query(value = "SELECT * FROM product_image WHERE PRO_NO = :proNo ORDER BY PRO_IMG_NO ASC",  nativeQuery = true)
    List<ProductImage> findByProNo(@Param("proNo") Integer proNo);
    
    @Query(value = "SELECT COUNT(*) FROM product_image WHERE PRO_NO = :proNo",  nativeQuery = true)
     Integer countByProNo(@Param("proNo") Integer proNo);
}
