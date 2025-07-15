package com.pixeltribe.shopsys.orderItem.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice(basePackages = "com.pixeltribe.shopsys.orderItem")
public class OrderItemExceptionHandler {
    
    @ExceptionHandler(OrderItemException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleOrderItemException(OrderItemException ex) {
        ErrorResponse error = new ErrorResponse(
            ex.getErrorCode().getCode(),
            ex.getErrorCode().getMessage()
        );
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {
        ErrorResponse error = new ErrorResponse("VALIDATION_ERROR", "輸入資料有誤");
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleAllException(Exception ex) {
        ErrorResponse error = new ErrorResponse("SYSTEM_ERROR", "系統發生錯誤");
        return ResponseEntity.status(500).body(error);
    }
}