package com.pixeltribe.shopsys.cart.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartStatisticsResponse {
	private boolean success = true;
	private StatisticsData data;
	private long timestamp;
	
	// 建構子
	public CartStatisticsResponse() {
		this.timestamp = System.currentTimeMillis();
	}
	
	public CartStatisticsResponse(StatisticsData data) {
		this.data = data;
		this.success = true;
		this.timestamp = System.currentTimeMillis();
	}
	
	
	// 內部類別 - 統計資料的部分
	@Getter
	@Setter
	public static class StatisticsData {
		private Integer totalCarts;				 // 總購物車數
		
		private Integer totalActiveUsers;        // 有購物車的活躍用戶數
	    private Integer totalProducts;           // 購物車中的商品總數量
	    private Integer averageItemsPerCart;     // 平均每個購物車的商品數
	    private Long totalCartValue;             // 所有購物車的總價值
	    
	    private Integer cartsWithStockIssues;    // 有庫存問題的購物車數
	    
	    // 商品狀態統計
	    private Integer preOrderProductCount;    // 預購商品數量
	    private Integer onShelfProductCount;     // 上架商品數量
		
		
		
		// 建構子
		public StatisticsData() {}
		
		public StatisticsData(Integer totalCarts) {
			this.totalCarts = totalCarts;
			

			// 計算平均值
			this.averageItemsPerCart = totalActiveUsers > 0 ? 
			   totalProducts / totalActiveUsers : 0;
		}
	}
}