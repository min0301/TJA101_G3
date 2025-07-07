package com.pixeltribe.shopsys.cart.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse {
    private boolean success = false;
    private String errorCode;
    private String errorMessage;
    private long timestamp;
    
    // 建構子  (lombok注入getter/setter)
    public ErrorResponse(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.timestamp = System.currentTimeMillis();
    }
    
   
}