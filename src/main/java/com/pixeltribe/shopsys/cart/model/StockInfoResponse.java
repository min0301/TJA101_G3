package com.pixeltribe.shopsys.cart.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockInfoResponse {
    private boolean success = true;
    private Integer proNo;
    private String proName;
    private String proStatus;
    private Integer stock;
    private String stockSource;
    private long timestamp;
    private String productType; // 新增：商品實際類型

    // 無參數建構子
    public StockInfoResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    // 🔥 原有建構子（向後相容）- 必須保留！
    public StockInfoResponse(Integer proNo, String proName, String proStatus,
                           Integer stock, String stockSource) {
        this.proNo = proNo;
        this.proName = proName;
        this.proStatus = proStatus;
        this.stock = stock;
        this.stockSource = stockSource;
        this.timestamp = System.currentTimeMillis();

        // 自動判斷商品類型
        if (stockSource != null && stockSource.contains("Redis")) {
            this.productType = "預購商品";
        } else {
            this.productType = "現貨商品";
        }
    }

    // 🔥 新增建構子（包含商品類型）
    public StockInfoResponse(Integer proNo, String proName, String proStatus,
                           Integer stock, String stockSource, String productType) {
        this.proNo = proNo;
        this.proName = proName;
        this.proStatus = proStatus;
        this.stock = stock;
        this.stockSource = stockSource;
        this.productType = productType;
        this.timestamp = System.currentTimeMillis();
    }

    // 簡化建構子（只有庫存數量）- 用於簡單的庫存查詢
    public StockInfoResponse(Integer stock) {
        this.stock = stock;
        this.timestamp = System.currentTimeMillis();
    }

    // 🔥 修正：更健壯的預購商品判斷方法
    public boolean isPreOrderProduct() {
        if (productType == null) return false;
        
        String type = productType.toLowerCase();
        return type.contains("預購") || 
               type.contains("preorder") || 
               type.contains("pre-order") ||
               type.contains("預定") ||
               type.contains("預售");
    }

    // 🔥 修正：更健壯的現貨商品判斷方法  
    public boolean isInStockProduct() {
        if (productType == null) return false;
        
        String type = productType.toLowerCase();
        return type.contains("現貨") || 
               type.contains("現貨供應") ||
               type.contains("有現貨") ||
               type.contains("立即") ||
               type.contains("instock") ||
               type.contains("available") ||
               type.contains("已發售") ||
               (!isPreOrderProduct() && stock != null && stock > 0);
    }

    // 🔥 新增：根據多個來源判斷商品類型
    public boolean isDefinitelyPreOrder() {
        // 綜合判斷：productType、stockSource、proStatus
        boolean typeIndicatesPreOrder = productType != null && 
            (productType.contains("預購") || productType.contains("preorder"));
        
        boolean sourceIndicatesPreOrder = stockSource != null && 
            stockSource.toLowerCase().contains("redis");
            
        boolean statusIndicatesPreOrder = proStatus != null && 
            (proStatus.equals("預購中") || proStatus.equals("預購"));
        
        return typeIndicatesPreOrder || sourceIndicatesPreOrder || statusIndicatesPreOrder;
    }

    // 🔥 新增：根據多個來源判斷現貨商品
    public boolean isDefinitelyInStock() {
        // 綜合判斷：不是預購 + 有庫存 + 來源是序號表
        boolean notPreOrder = !isDefinitelyPreOrder();
        boolean hasStock = stock != null && stock > 0;
        boolean sourceIndicatesInStock = stockSource != null && 
            (stockSource.contains("序號") || stockSource.toLowerCase().contains("serial"));
        boolean statusIndicatesInStock = proStatus != null && 
            (proStatus.equals("已發售") || proStatus.equals("上架"));
            
        return notPreOrder && hasStock && (sourceIndicatesInStock || statusIndicatesInStock);
    }

    // 新增：設定錯誤狀態的便利方法
    public static StockInfoResponse error(String message) {
        StockInfoResponse response = new StockInfoResponse();
        response.setSuccess(false);
        response.setStockSource("錯誤: " + message);
        response.setStock(0);
        return response;
    }

    // 新增：設定成功狀態的便利方法
    public static StockInfoResponse success(Integer proNo, String proName, String proStatus,
                                          Integer stock, String stockSource, String productType) {
        StockInfoResponse response = new StockInfoResponse(proNo, proName, proStatus, stock, stockSource, productType);
        response.setSuccess(true);
        return response;
    }

    @Override
    public String toString() {
        return "StockInfoResponse{" +
                "success=" + success +
                ", proNo=" + proNo +
                ", proName='" + proName + '\'' +
                ", proStatus='" + proStatus + '\'' +
                ", stock=" + stock +
                ", stockSource='" + stockSource + '\'' +
                ", productType='" + productType + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}