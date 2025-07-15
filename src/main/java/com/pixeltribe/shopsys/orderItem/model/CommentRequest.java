package com.pixeltribe.shopsys.orderItem.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequest {
    
    @NotNull(message = "評分不能為空")
    @Min(value = 1, message = "評分要給1顆星")
    @Max(value = 5, message = "評分最多5顆星")
    private Integer proStar;
    
    @Size(max = 255, message = "評價內容長度不能超過255字")
    private String productComment;
    
    // 建構子
    public CommentRequest() {}
    
    public CommentRequest(Integer proStar, String productComment) {
        this.proStar = proStar;
        this.productComment = productComment;
    }
}