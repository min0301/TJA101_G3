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
		private Integer totalCarts;         
		
		
		
		// 建構子
		public StatisticsData() {}
		
		public StatisticsData(Integer totalCarts) {
			this.totalCarts = totalCarts;
		}
	}
}