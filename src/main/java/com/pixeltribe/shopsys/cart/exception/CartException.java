package com.pixeltribe.shopsys.cart.exception;


public class CartException extends RuntimeException {
	private CartErrorCode errorCode;
	
	public CartException(CartErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}
	
	public CartException(CartErrorCode errorCode, Throwable cause) {
		super(errorCode.getMessage(), cause);
		this.errorCode = errorCode;
	}
	
	public CartErrorCode getErrorCode() {
		return errorCode;
	}
}