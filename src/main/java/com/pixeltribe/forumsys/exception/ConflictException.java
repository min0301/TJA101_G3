package com.pixeltribe.forumsys.exception;

/**
 * 基礎異常類別：資源衝突 (HTTP 409 Conflict)
 * 作用：作為所有「資源已存在」相關異常的父類別。
 * 繼承 RuntimeException，讓異常可以被 @ControllerAdvice 統一捕獲。
 */

public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message); // 將錯誤訊息傳遞給父類
    }


}
