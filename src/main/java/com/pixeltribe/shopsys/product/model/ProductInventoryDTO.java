package com.pixeltribe.shopsys.product.model;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductInventoryDTO {
	
	private Integer id;
	private String proStatus;
	private LocalDate proDate;
	
	private Integer inventory;
	private String displayText;
	private Boolean isAvailable;
	
	
	public boolean isPreorderStatus() {
        return displayText != null && 
               (displayText.contains("預購") || displayText.equals("預購已滿"));
    }
	
	 public boolean hasInventory() {
	        return inventory != null && inventory > 0;
	}
}
