package com.pixeltribe.shopsys.order.exception;


//========== 前後台合併 ==========//
public enum OrderErrorCode {
	
	
	//前台錯誤
	ORDER_001("ORDER_001", "XXXXXXXXX");
	
	
	//後台錯誤
	
	
	
	
	
	private final String code;
	private final String message;
	
	OrderErrorCode(String code, String message) {
		this.code =code;
		this.message = message;
	}
	
	public String getCode() {return code;}
	public String getMessage() {return message;}
	
}