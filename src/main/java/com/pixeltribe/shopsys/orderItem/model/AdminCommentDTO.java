package com.pixeltribe.shopsys.orderItem.model;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminCommentDTO {
    
    private Integer orderItemNo;
    private Integer orderNo;
    private Integer proNo;
    private String proName;
    private Integer proStar;
    private String productComment;
    private Instant productCommentCrdate;
    private Character proComStatus;
    
    // 會員相關資訊
    private Integer memNo;
    private String memName;
    private String memEmail;
    
    // 訂單相關資訊
    private Instant orderDate;
    private String orderStatus;
    
    // 產品相關資訊
    private String productImage;
    private Integer productPrice;
    
    // 建構子
    public AdminCommentDTO() {}
    
    // ========== 便利方法 ========== //
    // 判斷是否有評價內容
    public boolean hasComment() {
        return productComment != null && !productComment.trim().isEmpty();
    }

    // 判斷是否有評分
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
    
    // 評價時間格式化
    public String getFormattedCommentDate() {
        if (productCommentCrdate == null) return "未評價";
        return productCommentCrdate.toString().substring(0, 19).replace('T', ' ');
    }
    
    
    
    public Integer getCommentStatus() {
        // 轉換 Character 為 Integer 給前端使用
    	if (proComStatus != null) {
            // 確保這個轉換是正確的
            if (proComStatus == '1') {
                return 1; // 正常
            } else if (proComStatus == '0') {
                return 0; // 停權
            }
        }
        return 1; // 預設為正常
    }

    
    public String getCommentDate() {
        if (productCommentCrdate != null) {
            return productCommentCrdate.toString();
        }
        return null;
    }
    
}