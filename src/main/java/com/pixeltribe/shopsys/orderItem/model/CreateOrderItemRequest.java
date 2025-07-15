package com.pixeltribe.shopsys.orderItem.model;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateOrderItemRequest {
    private Integer proNo;
    private Integer quantity;
}