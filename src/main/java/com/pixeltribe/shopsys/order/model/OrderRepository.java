package com.pixeltribe.shopsys.order.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
	
	// 按照訂單時間降序排列
	List<Order> findByMemNo_MemNoOrderByOrderDatetimeDesc(Integer memNo);
	
	// 根據會員編號查詢訂單，按照訂單編號降序排列
    // 因為 Order.memNo 是Member物件，所以要用 memNo_memNo
    List<Order> findByMemNo_MemNoOrderByOrderNoDesc(Integer memNo);
   
	
	
	// 跟訂單查詢狀態查詢
	List<Order> findByOrderStatus(String Status);
	
	Order findByOrderNo(Integer orderNo);

	
}