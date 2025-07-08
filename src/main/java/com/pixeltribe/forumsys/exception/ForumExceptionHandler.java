package com.pixeltribe.forumsys.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// 註解：@ControllerAdvice 表示這是一個全域設定，會應用到所有 @Controller 或 @RestController。
@ControllerAdvice(basePackages = "com.pixeltribe.forumsys")
public class ForumExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ForumExceptionHandler.class);

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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(Exception ex, WebRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "系統發生未預期的錯誤，請聯繫管理員", // 對前端隱藏敏感資訊
                request.getDescription(false).replace("uri=", "")
        );
        logger.error("發生未捕獲的異常", ex);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    // --- 處理檔案儲存錯誤 (Internal Server Error, 500) ---
    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ErrorResponse> handleFileStorageException(FileStorageException ex, WebRequest request) {

        // 【重要】在伺服器後台的日誌中，記錄下包含原始 cause 的完整錯誤資訊
        logger.error("檔案處理時發生嚴重錯誤。", ex);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "檔案上傳失敗，請稍後再試或聯繫管理員。", // 對前端回傳一個通用的、友善的訊息
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }


}
