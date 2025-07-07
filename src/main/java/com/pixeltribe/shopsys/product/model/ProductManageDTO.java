package com.pixeltribe.shopsys.product.model;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pixeltribe.shopsys.malltag.model.MallTag;

import lombok.Getter;
import lombok.Setter;
import lombok.Value;

@Getter
@Setter
@Component
public class ProductManageDTO {
	
		private Integer id;
	    private String proName;
	    private Integer proPrice;
	    private String proStatus;
	    private String proVersion;
	    private LocalDate proDate;
	    private byte[] proCover;
	    private String proDetails;
	    private String proInclude;
	    private String mallTagName;
	    private Character proIsmarket;

}
