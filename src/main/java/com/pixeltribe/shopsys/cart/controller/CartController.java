package com.pixeltribe.shopsys.cart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pixeltribe.shopsys.cart.model.Cart;
import com.pixeltribe.shopsys.cart.model.CartDTO;
import com.pixeltribe.shopsys.cart.model.CartService;

import jakarta.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/api/cart")
public class CartController {
	
	@Autowired
	private CartService cartService;
	
	// ==========  將商品加到購物車 ============ //
	@PostMapping("/add")
	public ResponseEntity<CartDTO> addToCart(
			@RequestParam Integer proNo,
			@RequestParam String proName,
			@RequestParam Integer proPrice,
			@RequestParam Integer proNum,
			HttpServletRequest request) {
		
		// 取得會員ID
		Integer id = (Integer) request.getAttribute("currentId");
		
		// 呼叫CartService
		CartDTO cart = cartService.addToCart(id, proNo, proName, proPrice, proNum);
		
		return ResponseEntity.ok(cart);   // 配對成功
	}
	
	
}