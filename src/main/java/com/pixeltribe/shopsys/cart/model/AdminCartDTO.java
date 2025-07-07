package com.pixeltribe.shopsys.cart.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminCartDTO {
	private Integer memNo;
	//private Integer memName;  //會員名稱 (暫時註解，等會員系統完成後再開啟)
	private Integer totalItems; 
	private Integer totalQuantity;
	private Integer totalPrice;
	private List<AdminCartItemDTO> items;
	
	// 建構子
	public AdminCartDTO() {}
	
	    //未來會員名稱加入後，也要加入這邊
	public AdminCartDTO(Integer memNO, Integer totalItems, 
						Integer totalQuantity, Integer totalPrice) {
		this.memNo = memNo;
//		this.memName = memName;  //會員名稱 (暫時註解，等會員系統完成後再開啟)
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
		
		//建構子
		public AdminCartItemDTO() {}
		
		public AdminCartItemDTO(Integer proNo, String proName, Integer proPrice, 
								Integer proNum, Integer subtotal) {
			this.proNo = proNo;
			this.proName = proName;
			this.proPrice = proPrice;
			this.proNum = proNum;
			this.subtotal = subtotal;
		}
	}
}