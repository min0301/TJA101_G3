package com.pixeltribe.shopsys.cart.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminCartDTO {
	private Integer memNo;
	private String memName;  
	private Integer totalItems; 
	private Integer totalQuantity;
	private Integer totalPrice;
	private List<AdminCartItemDTO> items;
	private Long lastUpdateTime;          // 購物車最後更新時間
    private Integer daysSinceCreated;     // 購物車存在天數
	
	// 建構子
	public AdminCartDTO() {}
	
	    //未來會員名稱加入後，也要加入這邊
	public AdminCartDTO(Integer memNo, String memName, Integer totalItems, 
						Integer totalQuantity, Integer totalPrice) {
		this.memNo = memNo;
		this.memName = memName;  
		this.totalItems = totalItems;
		this.totalQuantity = totalQuantity;
		this.totalPrice = totalPrice;
	}
	
	// 內部類別 -後台購物車商品項目
	@Getter
	@Setter
	public static class AdminCartItemDTO {
		private Integer proNo;
		private String proName;
		private Integer proPrice;
		private Integer proNum;
		private Integer subtotal;
		
		private String proStatus;        // 商品狀態（預購/上架）
	    private Boolean hasStockIssue;   // 是否有庫存問題
	    private String stockWarning;     // 庫存警告訊息
		
		//建構子
		public AdminCartItemDTO() {}
		
		public AdminCartItemDTO(Integer proNo, String proName, 
								Integer proPrice, Integer proNum, 
								Integer subtotal, String proStatus, 
								Boolean hasStockIssue,String stockWarning) {
			this.proNo = proNo;
			this.proName = proName;
			this.proPrice = proPrice;
			this.proNum = proNum;
			this.subtotal = subtotal;
			this.proStatus= proStatus;        // 商品狀態（預購/上架）
			this.hasStockIssue= hasStockIssue;   // 是否有庫存問題
			this.stockWarning= stockWarning;     // 庫存警告訊息
			
		}
	}
}