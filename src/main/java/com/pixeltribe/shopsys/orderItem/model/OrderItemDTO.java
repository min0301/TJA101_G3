package com.pixeltribe.shopsys.orderItem.model;

import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemDTO {
	
	// **** 基本資料 **** //
	private Integer orderItemNo;
	private Integer orderNo;
	private Integer proNo;
	private String proName;
	private Integer proPrice;
	private Integer orderAmount;
	
	// **** 評論相關 **** //
	private String productComment;
	private Instant productCommentCrdate;
	private Integer proStar;
	private Character proComStatus;
	

    // ****產品相關資訊（從 Product 實體安全取得） **** //
	private String productName;
	private String productCover;    // 用來存 Base64（從 byte[] 轉換）
	private String productDetails;  // 直接存 String
    
    // **** 序列號資訊 **** //
	private Integer serialNumberCount;
	
	// **** 關聯顯示資訊 (在訂單明細上正常會看到的) **** //
    private String orderDate;       // 訂單日期
    private String orderStatus;     // 訂單狀態
    private String memName;         // 會員名稱（用於評價顯示）
    
    // **** 業務狀態標記 **** //
    private Boolean canComment;     // 是否可以評價
    private Boolean hasCommented;   // 是否已評價
    
    
    // ========== 建構子 ========== //
    // **** 預設建構子 **** //
    public OrderItemDTO() {}

    // **** 從OrderItem實體轉換的建構子 **** //
    public OrderItemDTO(OrderItem orderItem) {
        this.orderItemNo = orderItem.getOrderItemNo();
        this.orderNo = orderItem.getOrderNo();
        this.orderAmount = orderItem.getOrderAmount();
        this.proPrice = orderItem.getProPrice();
        this.proName = orderItem.getProName();
        this.productComment = orderItem.getProductComment();
        this.productCommentCrdate = orderItem.getProductCommentCrdate();
        this.proStar = orderItem.getProStar();
        this.proComStatus = orderItem.getProComStatus();
        
        this.hasCommented = hasComment();
        this.canComment = !hasCommented && canAddComment(orderItem);
        
        //  安全地取得產品資訊 
        if (orderItem.getProNo() != null) {
            // 使用 Product 的 getId() 方法取得產品編號
            this.proNo = orderItem.getProNo().getId();
            this.productName = orderItem.getProNo().getProName();
            
            // 轉換 byte[] 為 Base64 字串（只有 productCover 是 byte[]）
            byte[] coverBytes = orderItem.getProNo().getProCover();
            this.productCover = coverBytes != null ? 
            		Base64.getEncoder().encodeToString(coverBytes) : null;
            
            // productDetails 直接是 String 類型
            this.productDetails = orderItem.getProNo().getProDetails();
        }
        
        
        //  序列號數量 
        if (orderItem.getProSerialNumbers() != null) {
            this.serialNumberCount = orderItem.getProSerialNumbers().size();
        } else {
            this.serialNumberCount = 0;
        }
    }
    
    
    // *** 取得產品名稱 *** //
    public String getProductName() {
        // 優先使用 proName，因為它是當時下單時的產品名稱
        if (proName != null && !proName.trim().isEmpty()) {
            return proName;
        }
        
        // 如果 proName 為空，則使用從 Product 關聯取得的 productName
        if (productName != null && !productName.trim().isEmpty()) {
            return productName;
        }
        
        // 都沒有的話返回預設值
        return "未知商品";
    }
    
    
    // *** 檢查是否有有效的產品名稱 *** //
    public boolean hasValidProductName() {
        return (proName != null && !proName.trim().isEmpty()) || 
               (productName != null && !productName.trim().isEmpty());
    }
    
    
    
    
    
    
    
    
    
    
    
    // **** 簡化版建構子（只包含基本資訊，避免所有懶加載）**** //
    public OrderItemDTO(OrderItem orderItem, boolean simpleVersion) {
        // 必須先呼叫主要建構子或直接設定值
        this.orderItemNo = orderItem.getOrderItemNo();
        this.orderNo = orderItem.getOrderNo();
        this.orderAmount = orderItem.getOrderAmount();
        this.proPrice = orderItem.getProPrice();
        this.proName = orderItem.getProName();
        this.productComment = orderItem.getProductComment();
        this.productCommentCrdate = orderItem.getProductCommentCrdate();
        this.proStar = orderItem.getProStar();
        this.proComStatus = orderItem.getProComStatus();
        
        if (simpleVersion) {
            // 簡化版：只取得基本的產品編號
            if (orderItem.getProNo() != null) {
                this.proNo = orderItem.getProNo().getId();
            }
            this.serialNumberCount = 0;
        } else {
            // 完整版：取得產品資訊 (互較完整建構子邏輯)
            if (orderItem.getProNo() != null) {
                this.proNo = orderItem.getProNo().getId();
                this.productName = orderItem.getProNo().getProName();
                
                // 轉換 byte[] 為 Base64 字串（只有 productCover 是 byte[]）
                byte[] coverBytes = orderItem.getProNo().getProCover();
                this.productCover = coverBytes != null ? 
                		Base64.getEncoder().encodeToString(coverBytes) : null;
                
                // productDetails 直接是 String 類型
                this.productDetails = orderItem.getProNo().getProDetails();
            }
            
            // 序列號數量
            if (orderItem.getProSerialNumbers() != null) {
                this.serialNumberCount = orderItem.getProSerialNumbers().size();
            } else {
                this.serialNumberCount = 0;
            }
        }
        
        // 設定業務狀態
        this.hasCommented = hasComment();
        this.canComment = !hasCommented && canAddComment(orderItem);
        
        
    }

    // ========== 靜態工廠方法 ========= //
    public static OrderItemDTO from(OrderItem orderItem) {
        return new OrderItemDTO(orderItem);
    }
    
    // 簡化版靜態工廠方法
    public static OrderItemDTO fromSimple(OrderItem orderItem) {
        return new OrderItemDTO(orderItem, true);
    }
    
    // 批量轉換方法
    public static List<OrderItemDTO> fromList(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(OrderItemDTO::from)
                .collect(Collectors.toList());
    }
    
    // 批量轉換方法（簡化版）
    public static List<OrderItemDTO> fromListSimple(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(OrderItemDTO::fromSimple)
                .collect(Collectors.toList());
    }
    
    
    // 批量轉換的關聯載入版本
    public static List<OrderItemDTO> fromListWithDetails(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(OrderItemDTO::from)  // 使用完整版
                .collect(Collectors.toList());
    }
    
    
    
    // ========== 業務邏輯方法 ========= //
    // 計算小計金額
    public Integer getSubTotal() {
        if (proPrice != null && orderAmount != null) {
            return proPrice * orderAmount;
        }
        return 0;
    }
    
    // 判斷是否已評論
    public boolean hasComment() {
        return productComment != null && !productComment.trim().isEmpty();
    }
    
    // 判斷是否已評分
    public boolean hasRating() {
        return proStar != null && proStar > 0;
    }
    
    
    // 評價狀態文字顯示
    public String getCommentStatusText() {
        // 先檢查是否有評價內容
        if (!hasComment() && !hasRating()) {
            return "未評價";
        }
        
        // 有評價內容時，顯示評價狀態
        if (proComStatus == null) return "狀態未設定";
        
        switch (proComStatus) {
            case '0': return "評價已停權";    // 不當評價，前端後台可以選擇隱藏
            case '1': return "評價正常";      // 正常顯示
            default: return "未知狀態";
        }
    }
    

    
    // 星級評分顯示
    public String getStarDisplay() {
        if (proStar == null) return "未評價";
        
        StringBuilder stars = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            if (i <= proStar) {
                stars.append("★");
            } else {
                stars.append("☆");
            }
        }
        return stars.toString();
    }
    
    // 判斷是否可以新增評價（訂單狀態要是已完成）
    private boolean canAddComment(OrderItem orderItem) {
        
        if (orderItem.getOrder() != null) {
            // 假設訂單狀態 2 = 已完成
            return "2".equals(orderItem.getOrder().getOrderStatus());
        }
        return false;
    }
    
    
    // 評價時間格式化
    public String getFormattedCommentDate() {
        if (productCommentCrdate == null) return "未評價";
        return productCommentCrdate.toString().substring(0, 19).replace('T', ' ');
    }
    
    
    
    
    
}