package com.pixeltribe.shopsys.orderItem.exception;


public class OrderItemException extends RuntimeException{
	
	private static final long serialVersionUID = 1L;
    
    private OrderItemErrorCode errorCode;

    public OrderItemException(OrderItemErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public OrderItemException(OrderItemErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    public OrderItemErrorCode getErrorCode() {
        return errorCode;
    }
}