package com.pixeltribe.shopsys.cart.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


@ControllerAdvice(basePackages = "com.pixeltribe.shopsys.cart")
public class CartExceptionHandler {

	@ExceptionHandler(CartException.class)
	@ResponseBody
	public ResponseEntity<ErrorResponse> handleCartException(CartException ex) {
		ErrorResponse error = new ErrorResponse(
			ex.getErrorCode().getCode(),
	        ex.getErrorCode().getMessage()
		);
		return ResponseEntity.badRequest().body(error);
	}
	
	// 處理驗證例外  (當API驗證失敗時觸發   例如@Valid註解的DTO驗證失敗  可能在Controller中對應的request驗證失敗，就會觸發)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseBody
	public ResponseEntity<ErrorResponse> handleValidationException(
			MethodArgumentNotValidException ex) {
		ErrorResponse error = new ErrorResponse("VALIDATION_ERROR", "輸入資料有誤，小精靈找不到");
        return ResponseEntity.badRequest().body(error);
	}
	
	// 處理所有未抓到的例外  (避免如果不小心觸發了沒有抓到的例外，系統也不會自己丟出500的錯誤訊息給用戶)
	@ExceptionHandler(Exception.class)
	@ResponseBody
	public ResponseEntity<ErrorResponse> handleAllException(Exception ex) {
        ErrorResponse error = new ErrorResponse("SYSTEM_ERROR", "系統發生錯誤，部落小精靈正在確認中...");
        return ResponseEntity.status(500).body(error);
    }
}