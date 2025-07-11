package com.pixeltribe.shopsys.proSerialNumber.model;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pixeltribe.shopsys.product.model.Product;

public interface ProSerialNumberRepository extends JpaRepository<ProSerialNumber, Integer> {

	List<ProSerialNumber> findByProNo(Product product);

    @Query(value = "SELECT COUNT(p) FROM pro_serial_numbers p WHERE p.PRO_NO = :proNo", nativeQuery = true)
    Long countProduct(@Param("proNo") Integer proNo);

    @Query(value = "SELECT COUNT(*) FROM pro_serial_numbers WHERE PRO_NO = :proNo AND ORDER_ITEM_NO IS NULL", nativeQuery = true)
    Long countStock(@Param("proNo") Integer proNo);
}
