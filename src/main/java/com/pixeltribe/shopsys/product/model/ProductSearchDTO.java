package com.pixeltribe.shopsys.product.model;

import com.pixeltribe.shopsys.malltag.model.MallTag;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductSearchDTO {
	
	private Integer id;
    private String proName;
    private Integer proPrice;
    private Integer mallTagNo;
    private Character proIsmarket;
}
