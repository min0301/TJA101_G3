package com.pixeltribe.shopsys.product.model;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
public class ProductEditDTO {
	
	private Integer id;
    private String proName;
    private Integer proPrice;
    private String proStatus;
    private String proVersion;
    private LocalDate proDate;
    private byte[] proCover;
    private String proDetails;
    private String proInclude;
    private Integer mallTagNo;
    private Character proIsmarket;
	
}
