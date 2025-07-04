package com.pixeltribe.shopsys.cart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
	
	
	// ==========  獲取購物車 ============ //
	@GetMapping("/")
	public ResponseEntity<CartDTO> getCart(HttpServletRequest request) {
		
		Integer id = (Integer) request.getAttribute("currentId");
        CartDTO cart = cartService.getMemberCart(id);
        
        return ResponseEntity.ok(cart);
	}
	
	
	// ========== 移除購物車商品 ============ //
	@DeleteMapping("/remove/{proNo}")
	public ResponseEntity<CartDTO> removeFromCart(
            @PathVariable Integer proNo,
            HttpServletRequest request) {
		
		Integer id = (Integer) request.getAttribute("currentId");
        CartDTO cart = cartService.removeFromCart(id, proNo);
        
        return ResponseEntity.ok(cart);
	}
	
	// ========== 更新商品數量 ============ //
	@PutMapping("/update")
	public ResponseEntity<CartDTO> updateQuantity(
            @RequestParam Integer proNo,
            @RequestParam Integer newQuantity,
            HttpServletRequest request) {
		
		Integer id = (Integer) request.getAttribute("currentId");
        CartDTO cart = cartService.updateCartItemQuantity(id, proNo, newQuantity);
        
        return ResponseEntity.ok(cart);
	}
	
	
	// ========== 清空購物車 ============ //
	@DeleteMapping("/clear")
	public ResponseEntity<String> clearCart(HttpServletRequest request) {
		
		Integer id = (Integer) request.getAttribute("currentId");
		cartService.clearCart(id);
		
		return ResponseEntity.ok("購物車已清空");
	}
	
}