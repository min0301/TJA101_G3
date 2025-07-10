package com.pixeltribe.shopsys.order.model;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pixeltribe.shopsys.orderItem.model.OrderItemDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
	
	private Integer orderNo;
	private Integer memNo;
	private Integer couponWalletNo;
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Instant orderDatetime;
	
	private String orderStatus;
	private Integer orderTotal;
	private Integer discountAmount;    // OrderService計算後設定
	private List<OrderItemDTO> orderItems;
	
	
	// 計算實際付款的金額  (在OrderService設定discountAmount)
	public Integer getActualPayment() {
		if (discountAmount != null && discountAmount > 0) {
			return orderTotal - discountAmount;
		}
		return orderTotal;
	}
	
	// 檢查是否使用優惠券票夾
	public boolean hasUsedCouponWallet() {
		return couponWalletNo != null;
	}
	
	// 獲取訂單狀態資訊 (有中文的呈現跟數字百分比)
	public OrderStatusInfo getOrderStatusInfo() {
		switch (orderStatus) {
			case "PENDING":
				return new OrderStatusInfo("等待付款", 25);
			case "PROCESSING":
				return new OrderStatusInfo("處理中", 50);
			case "SHIPPED":
				return new OrderStatusInfo("已出貨", 75);
			case "COMPLETED":
				return new OrderStatusInfo("已完成", 100);
			case "FAILED":
				return new OrderStatusInfo("處理失敗", 0);
			case "CANCELLED":
				return new OrderStatusInfo("已取消", 0);
			default:
				return new OrderStatusInfo(orderStatus, 0);
			}
	}
	
	
	// ========= 內部類別 訂單狀態資訊 ========= //
	public static class OrderStatusInfo {
		private final String displayName;
		private final int progressPercentage; 
		
		public OrderStatusInfo(String displayName, int progressPercentage) {
            this.displayName = displayName;
            this.progressPercentage = progressPercentage;
        }
		
		public String getDisplayName() {
            return displayName;
        }
        
        public int getProgressPercentage() {
            return progressPercentage;
        }
        
        // 獲取進度條顏色（可用於前台樣式）
        public String getProgressColor() {
            if (progressPercentage == 100) {
                return "success";   // 綠色 - 已完成
            } else if (progressPercentage == 0) {
                return "danger";    // 紅色 - 失敗/取消
            } else if (progressPercentage >= 75) {
                return "warning";   // 橙色 - 處理中/已出貨
            } else {
                return "info";      // 藍色 - 等待付款
            }
        }
        
        // 檢查是否已完成
        public boolean isCompleted() {
            return progressPercentage == 100;
        }
        
	}
	
}