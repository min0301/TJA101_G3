package com.pixeltribe.shopsys.cart.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminCartListResponse {
	private boolean success = true;
	private AdminCartData data;
	private long timestamp;
	
	// 建構子
	public AdminCartListResponse() {
		this.timestamp = System.currentTimeMillis();
	}
	
	public AdminCartListResponse(AdminCartData data) {
		this.data =data;
		this.success = true;
		this.timestamp = System.currentTimeMillis();
	}
	
	// 內部類別 - 回應資料的部分
	@Getter
	@Setter
	public static class AdminCartData {
		private List<AdminCartDTO> carts;
		private Integer totalCarts;
		private Integer totalPages;
		private Integer currentPage;
		
		// 建構子
		public AdminCartData() {}
			
			public AdminCartData(List<AdminCartDTO> carts, Integer totalCarts, 
								Integer totalPages, Integer currentPage) {
				this.carts = carts;
				this.totalCarts = totalCarts;
				this.totalPages = totalPages;
				this.currentPage = currentPage;
			}
		
	}
}