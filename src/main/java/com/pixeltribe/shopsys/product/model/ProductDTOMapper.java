package com.pixeltribe.shopsys.product.model;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductDTOMapper {
	
	public  ProductManageDTO toProductManageDTO(Product product) {
		ProductManageDTO pmDTO = new ProductManageDTO();
		pmDTO.setId(product.getId());
		pmDTO.setProName(product.getProName());
		pmDTO.setProPrice(product.getProPrice());
		pmDTO.setProStatus(product.getProStatus());
		pmDTO.setProVersion(product.getProVersion());
		pmDTO.setProDate(product.getProDate());
		pmDTO.setProCover(product.getProCover());
		pmDTO.setProInclude(product.getProInclude());
		if (product.getMallTagNo() != null) {
	            pmDTO.setMallTagName(product.getMallTagNo().getMallTagName());
	        }
		pmDTO.setProIsmarket(product.getProIsmarket());
		return pmDTO;
	}
	
	public List<ProductManageDTO> toProductManageDTOList(List<Product> products){
		return products.stream()
                .map(this::toProductManageDTO)
                .collect(Collectors.toList());
	}
}
