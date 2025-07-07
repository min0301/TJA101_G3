package com.pixeltribe.shopsys.cart.model;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartDTO {
	
	// ========== 單個產品資訊 ========== //
	@Getter
	@Setter
	public static class CartItem {
		
		private Integer proNo;    // 產品編號
	
		@Size(max = 30)
		private String proName;   // 產品名稱
		
		private Integer proPrice; // 產品單價
		
		@Min(value =1, message = "訂購數量至少為1")
		@NotNull(message = "訂購數量不能為空")
		private Integer proNum;   // 產品訂購數量
		
		private Integer subtotal; // 小計	
		
		// ***** 只進行簡單的計算方法 ***** //
		public void calculateTotal() {
			if (this.proPrice != null && this.proNum != null) {
				this.subtotal = this.proPrice * this.proNum;
			} else {
				this.subtotal = 0;   // 預設值
			}
		}
	}
	
	
	// ========== 整個購物車的資訊 ========== //
	private Integer memNo;// 會員編號  (誰的購物車)
	private List<CartItem> item;// 全部的單個產品資訊 (用List包起來)
	private Integer totalItem;// 全部產品項目 (指全部的不同商品)
	private Integer totalPrice;// 總價
	private Integer totalQuantity;// 全部數量
	
	// ***** 只進行簡單的計算方法 ***** //
	public void calculateTotals() {
		if (item == null || item.isEmpty()) {
			this.totalItem = 0;
			this.totalQuantity = 0;
			this.totalPrice = 0;
			return;
		}
		
		this.totalItem = item.size();
		// 計算購物車總數量：取得每個商品的proNum並加總
		this.totalQuantity = item.stream()
				.filter(cartItem -> cartItem.getProNum() != null)
				.mapToInt(CartItem::getProNum)
				.sum();
		
		// 計算購物車總價格：取得每個產品的subtotal並加總
		this.totalPrice = item.stream()
				.filter(cartItem -> cartItem.getSubtotal() != null)
				.mapToInt(CartItem::getSubtotal)
				.sum();
	}
	
	
}