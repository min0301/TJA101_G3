package com.pixeltribe.forumsys.exception;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;


// 註解：@ControllerAdvice 表示這是一個全域設定，會應用到所有 @Controller 或 @RestController。
@ControllerAdvice
public class ForumExceptionHandler {

    // 註解：@ExceptionHandler 指定這個方法專門處理 ReportTypeAlreadyExistsException 類型的例外。
    @ExceptionHandler(ReportTypeAlreadyExistsException.class)
    public ResponseEntity<?> handleReportTypeAlreadyExists(
            ReportTypeAlreadyExistsException ex, WebRequest request) {

        // 註解：建立一個簡單的 Map 來存放錯誤訊息，這樣回傳的 JSON 格式比較清晰。
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", System.currentTimeMillis());
        body.put("status", HttpStatus.CONFLICT.value()); // 409
        body.put("error", "Conflict");
        body.put("message", ex.getMessage()); // 取得從 Service 拋出的錯誤訊息
        body.put("path", request.getDescription(false).replace("uri=", ""));

        // 註解：回傳 ResponseEntity，並將 HTTP 狀態碼設為 409 Conflict。
        // 對於「資源已存在」的場景，409 是最符合語意的狀態碼。
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ForumCategoryAlreadyExistsException.class)
    public ResponseEntity<?> handleForumCategoryAlreadyExists(
            ForumCategoryAlreadyExistsException ex, WebRequest request) {

        // 註解：建立一個簡單的 Map 來存放錯誤訊息，這樣回傳的 JSON 格式比較清晰。
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", System.currentTimeMillis());
        body.put("status", HttpStatus.CONFLICT.value()); // 409
        body.put("error", "Conflict");
        body.put("message", ex.getMessage()); // 取得從 Service 拋出的錯誤訊息
        body.put("path", request.getDescription(false).replace("uri=", ""));

        // 註解：回傳 ResponseEntity，並將 HTTP 狀態碼設為 409 Conflict。
        // 對於「資源已存在」的場景，409 是最符合語意的狀態碼。
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }



}
