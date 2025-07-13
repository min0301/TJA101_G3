package com.pixeltribe.shopsys.order.controller;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pixeltribe.shopsys.order.model.Order;
import com.pixeltribe.shopsys.order.model.OrderDTO;
import com.pixeltribe.shopsys.order.model.OrderRepository;
import com.pixeltribe.shopsys.order.model.OrderService;


@RestController
@RequestMapping("/api")
public class OrderController {
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private OrderService orderService;
	
	
	@GetMapping("orders")
    public List<Map<String, Object>> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(order -> {
                	Map<String, Object> orderMap = new HashMap<>();
                    orderMap.put("orderNo", order.getOrderNo());
                    // 只處理 Member 部分
                    orderMap.put("memNo", order.getMemNo() != null ? order.getMemNo().getId() : null);
                    // CouponWallet 暫時設為 null，等類別建立後再修改
                    orderMap.put("couponWalletNo", null);
                    orderMap.put("orderDatetime", order.getOrderDatetime());
                    orderMap.put("orderStatus", order.getOrderStatus());
                    orderMap.put("orderTotal", order.getOrderTotal());
                    return orderMap;
                })
                .collect(Collectors.toList());
    }
	
	@GetMapping("/orders/status/{status}")
    public List<Map<String, Object>> getOrdersByStatus(@PathVariable String status) {
        return orderRepository.findByOrderStatus(status).stream()
                .map(order -> {
                	Map<String, Object> orderMap = new HashMap<>();
                    orderMap.put("orderNo", order.getOrderNo());
                    // 只處理 Member 部分
                    orderMap.put("memNo", order.getMemNo() != null ? order.getMemNo().getId() : null);
                    // CouponWallet 暫時設為 null
                    orderMap.put("couponWalletNo", null);
                    orderMap.put("orderDatetime", order.getOrderDatetime());
                    orderMap.put("orderStatus", order.getOrderStatus());
                    orderMap.put("orderTotal", order.getOrderTotal());
                    return orderMap;
                })
                .collect(Collectors.toList());
                
    }
	
	
	@GetMapping("orders/{orderNo}")
	public Map<String, Object> getOrderByNo(@PathVariable Integer orderNo) {
	    // 根據訂單編號查詢訂單
	    Order order = orderRepository.findByOrderNo(orderNo);
	    
	    if (order == null) {
	        throw new RuntimeException("訂單不存在");
	    }
	    
	    Map<String, Object> orderMap = new HashMap<>();
	    orderMap.put("orderNo", order.getOrderNo());
	    orderMap.put("memNo", order.getMemNo() != null ? order.getMemNo().getId() : null);
	    orderMap.put("couponWalletNo", null); // CouponWallet 還沒做
	    orderMap.put("orderDatetime", order.getOrderDatetime());
	    orderMap.put("orderStatus", order.getOrderStatus());
	    orderMap.put("orderTotal", order.getOrderTotal());
	    orderMap.put("pointUsed", 0); // 積分功能暫不開放
	    orderMap.put("orderItems", new ArrayList<>()); // 訂單項目，等 OrderItem 做好後再加入
	    
	    return orderMap;
	}
	
	@GetMapping("/orders/member/{id}")
	public List<OrderDTO> getOrderByMemId(@PathVariable Integer id){
	    List<OrderDTO> orders = orderService.getmemOrders(id);
	    return orders;
	}
}