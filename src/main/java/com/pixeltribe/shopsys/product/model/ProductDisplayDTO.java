package com.pixeltribe.shopsys.product.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductDisplayDTO {
	private Integer id;
    private String proName;
    private Integer proPrice;
    private String proStatus;
    private String proVersion;
    private LocalDate proDate;
    private byte[] proCover;
    private String proInclude;
    private String proDetails;
    private Integer mallTagNo;
    private Integer proTotalCommit;
    private BigDecimal proTotalStar;
}
