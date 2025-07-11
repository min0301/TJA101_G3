package com.pixeltribe.shopsys.product.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Integer>{
	
		@Modifying
	    @Query(value="UPDATE Product p SET p.PRO_ISMARKET = :proIsMarket WHERE p.id = :proNo", nativeQuery = true)
	    Integer updateMarketStatus(@Param("proNo") Integer proNo, @Param("proIsmarket") Character proIsMarket);
		
	    @Query(value="SELECT * FROM Product p WHERE p.MALL_TAG_NO = :malltagNo "+"AND (p.PRO_ISMARKET IS NULL OR p.PRO_ISMARKET = :proIsMarket)", nativeQuery = true)
	    List<ProductSearchDTO> findByMallTagWithMarket(@Param("malltagNo") Integer malltagNo,@Param("proIsmarket") Character proIsMarket);

}
