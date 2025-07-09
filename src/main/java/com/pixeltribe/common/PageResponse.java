package com.pixeltribe.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;        // 資料內容
    private int pageNumber;         // 第幾頁（從 0 開始）
    private int pageSize;           // 每頁筆數
    private long totalElements;     // 總筆數
    private int totalPages;         // 總頁數
    private boolean last;           // 是否為最後一頁
}
