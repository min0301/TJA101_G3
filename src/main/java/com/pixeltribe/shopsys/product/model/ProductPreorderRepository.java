package com.pixeltribe.shopsys.product.model;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductPreorderRepository extends JpaRepository<Product, Integer>{
	
    @Modifying
    @Query(value = "UPDATE Product SET PRO_STATUS = '已發售' WHERE PRO_STATUS = '預購中' AND DATE(PRO_DATE) = :date", nativeQuery = true)
    Integer updateProductStatus(@Param("date") LocalDate date);
    
    @Query(value = "SELECT * FROM Product WHERE DATE(PRO_DATE) = :date AND PRO_STATUS = '已發售'",nativeQuery = true)
    List<Product> findProductsByDate(@Param("date") LocalDate date);
}
