
package com.pixeltribe.shopsys.orderItem.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CreateOrderItemRequest {
    
    @NotNull(message = "訂單編號不能為空")
    private Integer orderNo;
    
    @NotNull(message = "產品編號不能為空")
    private Integer proNo;
    
    @NotNull(message = "數量不能為空")
    @Min(value = 1, message = "數量至少為1")
    private Integer quantity;
    
    // 建構子
    public CreateOrderItemRequest() {}
    
    public CreateOrderItemRequest(Integer orderNo, Integer proNo, Integer quantity) {
        this.orderNo = orderNo;
        this.proNo = proNo;
        this.quantity = quantity;
    }
}