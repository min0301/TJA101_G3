package com.pixeltribe.forumsys.exception;

/**
 * 基礎異常類別：資源不存在 (HTTP 404 Not Found)
 * 作用：作為所有「找不到資源」相關異常的父類別。
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

}
