package com.pixeltribe.shopsys.order.model;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
	
	@Autowired
	private OrderRepository orderRepository;
	
	//  ========== 新增 ========== //
	//  訂單編號、會員編號、優惠票夾代碼、訂購時間、訂購狀態、訂單總額、使用積分
	public void add(Order order) {
		orderRepository.save(order);
	}
	
	 public Order addOrder(Order order) {
	        return orderRepository.save(order);
	    }
	
	//  ========== 改 ========== //
	public void update() {
		
	}
	
	//  ========== 查-單一查詢 ========== //
	public Order getOneOrder(Integer orderNo) {
		Optional<Order> optional = orderRepository.findById(orderNo);
		return optional.orElse(null);
	}
	
	//  ========== 查-查全部 ========== //
	public List<Order> getAllOrder(Integer orderNo){
		return orderRepository.findAll();
	}
	
	//  ========== 刪 ========== //
	
	
}