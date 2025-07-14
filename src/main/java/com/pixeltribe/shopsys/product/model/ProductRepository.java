package com.pixeltribe.shopsys.product.model;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Integer>{
	
		@Modifying
	    @Query(value="UPDATE Product p SET p.PRO_ISMARKET = :proIsMarket WHERE p.PRO_NO = :proNo", nativeQuery = true)
	    Integer updateMarketStatus(@Param("proNo") Integer proNo, @Param("proIsMarket") Character proIsMarket);
		
	    @Query(value="SELECT * FROM Product p WHERE p.MALL_TAG_NO = :mallTagNo "+"AND (p.PRO_ISMARKET IS NULL OR p.PRO_ISMARKET = :proIsMarket)", nativeQuery = true)
	    List<Product> findByMallTagAndMarket(@Param("mallTagNo") Integer mallTagNo,@Param("proIsMarket") Character proIsMarket);

	    List<Product> findByProStatus(String proStatus);
	    
//	    List<Product> findByProStatusAndOverProDate(String proStatus, LocalDate proDate);
	    
	    @Query(value="SELECT * FROM Product p WHERE p.PRO_STATUS = '預購中' AND p.PRO_DATE <= :date", nativeQuery = true)
	    List<Product> findPreorderProductsBeforeDate(@Param("date") LocalDate date);
	    
	    List<Product> findByProName(String proName);
	    
}
