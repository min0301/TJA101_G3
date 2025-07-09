package com.pixeltribe.shopsys.order.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
	
	// 根據會員編號查詢訂單，按照時間排序
//	List<Order> findByMemNo_MemNoOrderByOrderDatetimeDesc(Integer Id);
	
	// 跟訂單查詢狀態查詢
	List<Order> findByOrderStatus(String Status);
	
	Order findByOrderNo(Integer orderNo);

	
}