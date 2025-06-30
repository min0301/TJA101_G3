package com.pixeltribe.shopsys.order.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pixeltribe.shopsys.order.model.Order;
import com.pixeltribe.shopsys.order.model.OrderService;

@RestController
@RequestMapping("/api")
public class OrderController {
	
	@Autowired
	OrderService orderService;
	
	@GetMapping("order")
	public List<Order> findAll(){
		
		return orderService.getAllOrder();
	}
	
	@GetMapping("/api/orders")
	public ResponseEntity<List<Order>> getAllOrders() {
	    return ResponseEntity.ok(orderService.getAllOrder());
	}
	
}