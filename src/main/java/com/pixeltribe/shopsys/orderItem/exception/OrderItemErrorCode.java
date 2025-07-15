package com.pixeltribe.shopsys.orderItem.exception;


public enum OrderItemErrorCode {
	// 基礎錯誤
    ORDERITEM_NOT_FOUND("ORDERITEM_001", "訂單明細不存在"),
    ORDER_NOT_FOUND("ORDERITEM_002", "訂單不存在"),
    PRODUCT_NOT_FOUND("ORDERITEM_003", "產品不存在"),
    CREATE_FAILED("ORDERITEM_004", "建立訂單明細失敗"),
    UPDATE_FAILED("ORDERITEM_005", "更新失敗"),
    ACCESS_DENIED("ORDERITEM_006", "權限不足"),
    
    // 評價相關錯誤
    ORDER_NOT_COMPLETED("ORDERITEM_007", "訂單尚未完成，無法評價"),
    ALREADY_COMMENTED("ORDERITEM_008", "已經評價過了"),
    NOT_COMMENTED_YET("ORDERITEM_009", "尚未評價"),
    COMMENT_BLOCKED("ORDERITEM_010", "評價已被停權，無法修改");

    private final String code;
    private final String message;

    OrderItemErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
	
}