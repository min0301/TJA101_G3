package com.pixeltribe.shopsys.order.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
	

	// 跟訂單查詢狀態查詢
	List<Order> findByOrderStatus(String Status);
	Order findByOrderNo(Integer orderNo);
	
	// 根據會員編號查詢訂單，按時間降序排列
    @Query(value = "SELECT * FROM `order` o WHERE o.MEM_NO = :memNo ORDER BY o.ORDER_DATETIME DESC", nativeQuery = true)
    List<Order> findByMemNoOrderByOrderDatetimeDesc(@Param("memNo") Integer memNo);
    
    // 根據會員編號查詢訂單，按訂單編號降序排列
    @Query(value = "SELECT * FROM `order` o WHERE o.MEM_NO = :memNo ORDER BY o.ORDER_NO DESC", nativeQuery = true)
    List<Order> findByMemNoOrderByOrderNoDesc(@Param("memNo") Integer memNo);
	
	

	
}