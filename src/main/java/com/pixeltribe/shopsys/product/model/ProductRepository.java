package com.pixeltribe.shopsys.product.model;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Integer>{
	
		@Modifying
	    @Query(value="UPDATE Product SET PRO_ISMARKET = :proIsMarket WHERE PRO_NO = :proNo", nativeQuery = true)
	    Integer updateMarketStatus(@Param("proNo") Integer proNo, @Param("proIsMarket") Character proIsMarket);
		
	    @Query(value="SELECT * FROM Product WHERE MALL_TAG_NO = :mallTagNo AND (PRO_ISMARKET IS NULL OR PRO_ISMARKET = :proIsMarket)", nativeQuery = true)
	    List<Product> findByMallTagAndMarket(@Param("mallTagNo") Integer mallTagNo,@Param("proIsMarket") Character proIsMarket);

	    @Query(value="SELECT * FROM Product WHERE PRO_ISMARKET = :proIsMarket", nativeQuery = true)
	    List<Product> findByMarket(@Param("proIsMarket") Character proIsMarket);

	    List<Product> findByProStatus(String proStatus);
	    
	    List<Product> findByProName(String proName);
	    
	    @Query(value ="SELECT PRO_NO FROM Product WHERE LOWER(TRIM(PRO_NAME)) = LOWER(TRIM(:proName)) AND PRO_VERSION = :proVersion AND MALL_TAG_NO = :mallTagNo", nativeQuery = true)
	    Integer isExistProduct(@Param("proName") String proName, @Param("proVersion") String proVersion, @Param("mallTagNo") Integer mallTagNo);
	    
}
