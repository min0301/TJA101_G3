package com.pixeltribe.shopsys.orderItem.model;

import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import com.pixeltribe.shopsys.orderItem.model.OrderItem;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemDTO {
    private Integer orderItemNo;
    private Integer orderNo;
    private Integer proNo;
    private String proName;
    private Integer proPrice;
    private Integer orderAmount;
    private String productComment;
    private Instant productCommentCrdate;
    private Integer proStar;
    private Character proComStatus;
    
    // 產品相關資訊（從 Product 實體安全取得）
    private String productName;
    private String productCover;    // 用來存 Base64（從 byte[] 轉換）
    private String productDetails;  // 直接存 String
    
    // 序列號資訊
    private Integer serialNumberCount;
    
    // 預設建構子
    public OrderItemDTO() {}

    // 從 OrderItem 實體轉換的建構子
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
        
        // 安全地取得產品資訊
        if (orderItem.getProNo() != null) {
            // 使用 Product 的 getId() 方法取得產品編號
            this.proNo = orderItem.getProNo().getId();
            this.productName = orderItem.getProNo().getProName();
            
            // 轉換 byte[] 為 Base64 字串（只有 productCover 是 byte[]）
            byte[] coverBytes = orderItem.getProNo().getProCover();
            this.productCover = coverBytes != null ? Base64.getEncoder().encodeToString(coverBytes) : null;
            
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
    
    // 簡化版建構子（只包含基本資訊，避免所有懶加載）
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
            // 完整版：取得產品資訊
            if (orderItem.getProNo() != null) {
                this.proNo = orderItem.getProNo().getId();
                this.productName = orderItem.getProNo().getProName();
                
                // 轉換 byte[] 為 Base64 字串（只有 productCover 是 byte[]）
                byte[] coverBytes = orderItem.getProNo().getProCover();
                this.productCover = coverBytes != null ? Base64.getEncoder().encodeToString(coverBytes) : null;
                
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
    }

    // 靜態工廠方法
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
    
    // 計算小計金額的便利方法
    public Integer getSubTotal() {
        if (proPrice != null && orderAmount != null) {
            return proPrice * orderAmount;
        }
        return 0;
    }
    
    // 判斷是否已評論的便利方法
    public boolean hasComment() {
        return productComment != null && !productComment.trim().isEmpty();
    }
    
    // 判斷是否已評分的便利方法
    public boolean hasRating() {
        return proStar != null && proStar > 0;
    }
}