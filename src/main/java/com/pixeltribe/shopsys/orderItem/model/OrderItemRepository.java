package com.pixeltribe.shopsys.orderItem.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
	
	
	//根據訂單編號查詢
	@Query(value = "SELECT * FROM order_item WHERE order_no = :orderNo", nativeQuery = true)
    List<OrderItem> findByOrderNoNative(@Param("orderNo") Integer orderNo);
}