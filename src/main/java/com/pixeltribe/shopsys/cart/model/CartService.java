package com.pixeltribe.shopsys.cart.model;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pixeltribe.shopsys.order.model.Order;
import com.pixeltribe.shopsys.order.model.OrderService;

@Service
@Transactional
public class CartService {
	
	@Autowired
	private OrderService orderService;  // 注入訂單服務
	
	
	}
	
	

