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
    
    public StockInfoResponse(Integer proNo, String proName, String proStatus, 
                           Integer stock, String stockSource) {
        this.proNo = proNo;
        this.proName = proName;
        this.proStatus = proStatus;
        this.stock = stock;
        this.stockSource = stockSource;
        this.timestamp = System.currentTimeMillis();
    }
}