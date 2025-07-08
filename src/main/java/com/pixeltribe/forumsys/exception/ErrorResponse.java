package com.pixeltribe.forumsys.exception;

import lombok.Data;

import java.time.Instant;

// DTO: Data Transfer Object，專門用來傳遞資料的物件
@Data
public class ErrorResponse {

    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;

    public ErrorResponse(int status, String error, String message, String path) {
        this.timestamp = Instant.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }


}
