package com.pixeltribe.shopsys.product.model;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProdouctPreorderRepositroy extends JpaRepository<Product, Integer>{
	
	@Query(value="SELECT * FROM Product WHERE PRO_STATUS = '預購中' AND PRO_DATE <= :date", nativeQuery = true)
    List<Product> findPreorderProductsBeforeDate(@Param("date") LocalDate date);
    
}
