package com.pixeltribe.shopsys.cart.model;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pixeltribe.shopsys.order.model.Order;
import com.pixeltribe.shopsys.order.model.OrderService;

@Service
public class CartService {
	
	@Autowired
	private OrderService orderService;
	
	
	}
	
	

