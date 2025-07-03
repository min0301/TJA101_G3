package com.pixeltribe.shopsys.cart.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Cart {
	
	// ========== 單個產品資料 ========== //
	@Getter
	@Setter
	public static class CartItem {
		
		private Integer proNo;    // 產品編號
		
		@Size(max = 30)
		private String proName;   // 產品名稱
		
		private Integer proPrice; // 產品單價
		
		@Min(value = 1, message = "訂購數量至少為1")
		@NotNull(message = "訂購數量不能為空")
		private Integer proNum;   // 產品訂購數量
		
		private Integer subtotal; // 小計

		private Boolean available;  // 確認產品是否缺貨
		
		// 計算方法
		public void calculateTotal() {
			if (this.proPrice != null && this.proNum != null) {
				this.subtotal = this.proPrice * this.proNum;
			}
		}
		
		// 建構子
		public CartItem() {
			this.available = true; // 預設可購買
		}
		
		public CartItem(Integer proNo, String proName, Integer proPrice, Integer proNum) {
			this();
			this.proNo = proNo;
			this.proName = proName;
			this.proPrice = proPrice;
			this.proNum = proNum;
			calculateTotal();
		}		
	}
	
	
	// ========== 整個購物車的資訊 ========== //
	private Integer id;           // 會員編號 (誰的購物車)
	private List<CartItem> item;  // 全部的單個產品資訊 (用List包起來)
	private Integer totalItem;    // 全部產品項目 (指全部的不同商品)
	private Integer totalPrice;   // 總價
	private Integer totalQuantity; // 全部數量
	
	private Boolean isEmpty;              // 確認購物車是否為空
	private Boolean hasUnavailableItems; // 確認是否有無法購買的商品
	private Integer unavailableCount;     // 確認無法購買的商品數量
	
	// 計算方法
	public void calculateTotals() {
		if (item == null || item.isEmpty()) {
			this.totalItem = 0;
			this.totalQuantity = 0;
			this.totalPrice = 0;
			this.isEmpty = true;
			this.hasUnavailableItems = false;
			this.unavailableCount = 0;
			return;
		}
		
	// 先確保每個商品的小計都正確
	item.forEach(CartItem::calculateTotal);
	
	this.totalItem = item.size();
	// 計算購物車總數量：取得每個商品的proNum並加總
	this.totalQuantity = item.stream().mapToInt(CartItem::getProNum).sum();
	// 計算購物車總價格：取得每個產品的subtotal並加總
	this.totalPrice = item.stream().mapToInt(CartItem::getSubtotal).sum();
	
	// 計算實用的狀態資訊
	this.isEmpty = item.isEmpty();
	this.unavailableCount = (int) item.stream()
			.filter(cartItem -> !cartItem.getAvailable())
			.count();
	this.hasUnavailableItems = this.unavailableCount > 0;
	}			


//========== 實作 ========== //

	// *****   產品加入購物車  ***** //
	public void addItem(CartItem newItem) {
		if (item == null) {
			item = new ArrayList<>();
		}
		
		// 檢查是否已存在相同產品
		CartItem existingItem = item.stream()
				.filter(cartItem -> cartItem.getProNo().equals(newItem.getProNo()))
				.findFirst()
				.orElse(null);
		
		if (existingItem != null) {
			//更新數量
			existingItem.setProNum(existingItem.getProNum() + newItem.getProNum());
			existingItem.calculateTotal();
		} else {
			//新增產品
			newItem.calculateTotal();
			item.add(newItem);
		}
		calculateTotals();
	}



	//*****   移除產品  ***** //
	public void removeItem(Integer proNo) {
		if (item != null) {
			item.removeIf(cartItem -> cartItem.getProNo().equals(proNo));
			calculateTotals();
		}
	}


	//*****   更新產品數量  ***** //
	public void updateItemQuantity(Integer proNo, Integer newQuantity) {
		if (item != null) {
			CartItem targetItem = item.stream()
					.filter(cartItem -> cartItem.getProNo().equals(proNo))
					.findFirst()
					.orElse(null);
			
			if (targetItem != null) {
				targetItem.setProNum(newQuantity);
				targetItem.calculateTotal();
				calculateTotals();
			}
		}
	}

	//*****   清空購物車  ***** //
	public void clearCart() {
		if (item != null) {
			item.clear();
		}
		calculateTotals();
	}

	//*****   結帳：取的購買的產品  ***** //
	public List<CartItem> getAvailableItems() {
		if (item == null) {
			return new ArrayList<>();
		}
		return item.stream()
				.filter(CartItem::getAvailable)
				.collect(java.util.stream.Collectors.toList());
	}

	//*****   提醒會員：取的不可以購買的產品  ***** //
	public List<CartItem> getUnavailableItems() {
		if (item == null) {
			return new ArrayList<>();
		}
		return item.stream()
				.filter(cartItem -> !cartItem.getAvailable())
				.collect(java.util.stream.Collectors.toList());
	}


	//*****   計算所有可以購買的產品總價  ***** //
	public Integer getAvailableTotalPrice() {
		if (item == null) {
			return 0;
		}
		return item.stream()
				.filter(CartItem::getAvailable)
				.mapToInt(CartItem::getSubtotal)
				.sum();
	}


	//*****   檢查特定產品是否在購物車中  ***** //
	public boolean containsProduct(Integer proNo) {
		if (item == null) {
			return false;
		}
		return item.stream()
				.anyMatch(cartItem -> cartItem.getProNo().equals(proNo));
	}
	
	// 建構子
	public Cart() {
		this.isEmpty = true;
		this.hasUnavailableItems = false;
		this.unavailableCount = 0;
		this.item = new ArrayList<>();
	}
	
	public Cart(Integer id) {
		this();
		this.id = id;
	}
	// 轉換方法：轉換為 CartDTO（API 回應用）
	public CartDTO toDTO() {
		CartDTO dto = new CartDTO();
		dto.setId(this.id);
		dto.setTotalItem(this.totalItem);
		dto.setTotalPrice(this.totalPrice);
		dto.setTotalQuantity(this.totalQuantity);
		
		// 轉換產品清單
		if (this.item != null) {
			List<CartDTO.CartItem> dtoItems = new ArrayList<>();
			for (CartItem voItem : this.item) {
				CartDTO.CartItem dtoItem = new CartDTO.CartItem();
				dtoItem.setProNo(voItem.getProNo());
				dtoItem.setProName(voItem.getProName());
				dtoItem.setProPrice(voItem.getProPrice());
				dtoItem.setProNum(voItem.getProNum());
				dtoItem.setSubtotal(voItem.getSubtotal());
				dtoItems.add(dtoItem);
			}
			dto.setItem(dtoItems);
		}
		
		return dto;
	}

	//*****   從CartDTO轉換  ***** //
	public static Cart fromDTO(CartDTO dto) {
		Cart vo = new Cart();
		vo.setId(dto.getId());
		vo.setTotalItem(dto.getTotalItem());
		vo.setTotalPrice(dto.getTotalPrice());
		vo.setTotalQuantity(dto.getTotalQuantity());
	
		// 轉換產品清單
		if (dto.getItem() != null) {
			List<CartItem> voItems = new ArrayList<>();
			for (CartDTO.CartItem dtoItem : dto.getItem()) {
				CartItem voItem = new CartItem();
				voItem.setProNo(dtoItem.getProNo());
				voItem.setProName(dtoItem.getProName());
				voItem.setProPrice(dtoItem.getProPrice());
				voItem.setProNum(dtoItem.getProNum());
				voItem.setSubtotal(dtoItem.getSubtotal());
				voItems.add(voItem);
			}
			vo.setItem(voItems);
		}
	
		vo.calculateTotals();
		return vo;
	}

}
