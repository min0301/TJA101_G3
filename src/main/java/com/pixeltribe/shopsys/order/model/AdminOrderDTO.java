package com.pixeltribe.shopsys.order.model;


import lombok.Data;
import lombok.EqualsAndHashCode;


// 後台訂單DTO-繼承OrderDTO  ((新增後台管理專用欄位和功能)
@Data
@EqualsAndHashCode(callSuper = true)
public class AdminOrderDTO extends OrderDTO {
	
	// ========== 後台專用欄位 ========== //
	// ***** 會員詳細資訊 ***** //
	private String memName;              //會員姓名
	private String memEmail;             //會員Email
	
	// ***** 管理員操作權限 ***** //
	private boolean canCancel;           // 是否可取消
    private boolean canComplete;         // 是否可強制完成
    private boolean canEdit;             // 是否可編輯
	
	// ***** 統計資訊 ***** //
    private Integer itemCount;           // 商品項目數量
    private Integer serialCount;         // 已分配序號數量
    private Boolean allSerialsAllocated; // 是否所有序號都已分配
    
    // 優惠券資訊 (後台查看用)
    private String couponWalletCode;  // 優惠券票夾代碼
    private String couponWalletName;  // 優惠券票夾名稱
    
    // 建構子
    public AdminOrderDTO() {
    	super();
    }
    
    // 從OrderDTO轉換的建構子  
    public AdminOrderDTO(OrderDTO orderDTO) {
    	this.setOrderNo(orderDTO.getOrderNo());
        this.setMemNo(orderDTO.getMemNo());
        this.setCouponWalletNo(orderDTO.getCouponWalletNo());
        this.setOrderDatetime(orderDTO.getOrderDatetime());
        this.setOrderStatus(orderDTO.getOrderStatus());
        this.setOrderTotal(orderDTO.getOrderTotal());
        this.setDiscountAmount(orderDTO.getDiscountAmount());
        this.setOrderItems(orderDTO.getOrderItems());
        this.setContactEmail(orderDTO.getContactEmail());
        
        // 初始化後台專用欄位的預設值
        this.canCancel = false;
        this.canComplete = false;
        this.canEdit = false;
        this.itemCount = orderDTO.getOrderItems() != null ? orderDTO.getOrderItems().size() : 0;
    }
    
    
    // ***** 後台專用方法 ***** //
    
    // 獲取後台訂單狀台
    public OrderStatusInfo getAdminStatusInfo() {
        switch (getOrderStatus()) {
	        case "PENDING":
				return new OrderStatusInfo("等待付款", 20);
			case "PAYING":              
	            return new OrderStatusInfo("付款處理中", 40);
			case "PROCESSING":
				return new OrderStatusInfo("處理中", 60);
			case "SHIPPED":
				return new OrderStatusInfo("已出貨", 80);
			case "COMPLETED":
				return new OrderStatusInfo("已完成", 100);
			case "FAILED":
				return new OrderStatusInfo("處理失敗", 0);
			case "CANCELLED":
				return new OrderStatusInfo("已取消", 0);
			default:
				return new OrderStatusInfo(getOrderStatus(), 0);
        }
    }
    
    // 管理員權限 (可執行的操作)
    public java.util.List<String> getAvailableActions() {
        java.util.List<String> actions = new java.util.ArrayList<>();
        
        if (canCancel) {
            actions.add("CANCEL");
        }
        if (canComplete) {
            actions.add("FORCE_COMPLETE");
        }
        if (canEdit) {
            actions.add("EDIT");
        }
        
        // 根據狀態添加其他可能的操作
        switch (getOrderStatus()) {
            case "PENDING":
                actions.add("SEND_REMINDER");
                break;
            case "PROCESSING":
                actions.add("CHECK_SERIAL");
                break;
            case "FAILED":
                actions.add("RETRY_PAYMENT");
                break;
        }
        
        return actions;
    }
    
    
    // 查看是否有使用優惠券
    public boolean hasUsedCoupon() {
        return getCouponWalletNo() != null || 
               (couponWalletCode != null && !couponWalletCode.trim().isEmpty());
    }
    
    
    // 查看優惠券的資訊
    public String getCouponSummary() {
        if (!hasUsedCoupon()) {
            return "未使用優惠券";
        }
        
        if (couponWalletName != null && !couponWalletName.trim().isEmpty()) {
            return couponWalletName;
        }
        
        if (couponWalletCode != null && !couponWalletCode.trim().isEmpty()) {
            return "優惠券: " + couponWalletCode;
        }
        
        return "已使用優惠券 (MemNo: " + getCouponWalletNo() + ")";
    }
    
    
    // 查看訂單處理狀態
    public boolean isFullyProcessed() {
        return Boolean.TRUE.equals(allSerialsAllocated) && 
               ("SHIPPED".equals(getOrderStatus()) || "COMPLETED".equals(getOrderStatus()));
    }
   
}