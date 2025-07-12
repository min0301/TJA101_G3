package com.pixeltribe.shopsys.product.model;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductAddDTO {
	@NotNull(message = "商品名稱：不可空白")
    private String proName;
	@NotNull(message = "商品價格：不可空白")
    private Integer proPrice;
	@NotNull(message = "商品狀態：需拉選")
    private String proStatus;
	@NotNull(message = "商品版本：不可空白")
    private String proVersion;
    private LocalDate proDate;
    private byte[] proCover;
    private String proDetails;
    private String proInclude;
    @NotNull(message = "商城標籤：需拉選")
    private Integer mallTagNo;
    @NotNull(message = "上下架：需拉選")
    private Character proIsmarket;
	
}
