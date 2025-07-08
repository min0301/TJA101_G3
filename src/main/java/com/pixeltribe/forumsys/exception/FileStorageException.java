package com.pixeltribe.forumsys.exception;
/**
 * 自訂例外：用於處理檔案儲存或讀取過程中發生的錯誤。
 * 繼承自 RuntimeException，使其成為一個 Unchecked Exception。
 */
public class FileStorageException extends RuntimeException {

    /**
     * 建構子
     * @param message 錯誤訊息
     */
    public FileStorageException(String message) {
        super(message);
    }

    /**
     * 建構子
     * @param message 錯誤訊息
     * @param cause 原始的例外物件 (例如 IOException)
     */
    public FileStorageException(String message, Throwable cause) {
        super(message, cause); // 將原始的 cause 傳遞給父類別，這對於追蹤錯誤堆疊非常重要！
    }

}
