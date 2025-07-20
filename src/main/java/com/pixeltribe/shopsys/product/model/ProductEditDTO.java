package com.pixeltribe.shopsys.product.model;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductEditDTO {
	
	private Integer id;
	@NotNull(message = "商品名稱：不可空白")
    private String proName;
	@NotNull(message = "商品價格：不可空白")
    private Integer proPrice;
	@NotNull
    private String proStatus;
	@NotNull(message = "商品版本：不可空白")
    private String proVersion;
    private LocalDate proDate;
    private byte[] proCover;
    private String proDetails;
    private String proInclude;
    @NotNull(message = "商城標籤：需拉選")
    private Integer mallTagNo;
    @NotNull
    private Character proIsmarket;
	
}
