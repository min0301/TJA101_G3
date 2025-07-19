package com.pixeltribe.shopsys.cart.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartValidationResponse {
    private boolean success = true;
    private Integer memNo;
    private boolean valid;              // 是否通過驗證
    private List<String> issues;        // 問題清單
    private Integer totalItems;         // 驗證的商品種類數
    private Integer totalQuantity;      // 驗證的商品總數量
    private Integer totalPrice;         // 驗證的總價
    private long timestamp;
    
    public CartValidationResponse() {
        this.timestamp = System.currentTimeMillis();
    }
}