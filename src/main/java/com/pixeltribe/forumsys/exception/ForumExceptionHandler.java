package com.pixeltribe.forumsys.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;


// 註解：@ControllerAdvice 表示這是一個全域設定，會應用到所有 @Controller 或 @RestController。
@ControllerAdvice(basePackages = "com.pixeltribe.forumsys")
public class ForumExceptionHandler {

    /**
     * 處理所有「資源衝突」相關的業務異常。
     *
     * @param ex      捕獲到的 ConflictException 或其任何子類異常
     * @param request 當前的網頁請求
     * @return 回傳標準化的錯誤回應及 HTTP 409 狀態碼
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictExceptions(ConflictException ex, WebRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    // --- 處理資源不存在 (Not Found, 404) ---
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundExceptions(ResourceNotFoundException ex, WebRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // --- 建議新增：處理所有其他未捕獲的伺服器內部錯誤 (Internal Server Error, 500) ---
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(Exception ex, WebRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "系統發生未預期的錯誤，請聯繫管理員", // 對前端隱藏敏感資訊
                request.getDescription(false).replace("uri=", "")
        );
        // 在伺服器日誌中記錄完整的錯誤堆疊，以便追查問題
        // logger.error("發生未捕獲的異常", ex);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }


}
