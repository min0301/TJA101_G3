package com.pixeltribe.shopsys.product.model;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pixeltribe.forumsys.forumcategory.model.ForumCategory;
import com.pixeltribe.shopsys.malltag.model.MallTag;
import com.pixeltribe.shopsys.malltag.model.MallTagRepository;

@Service
public class ProductDTOMapper {
	@Autowired
	MallTagRepository malltagRepository;
	
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
	
	public  ProductEditDTO toProductEditDTO(Product product) {
		ProductEditDTO peDTO = new ProductEditDTO();
		peDTO.setId(product.getId());
		peDTO.setProName(product.getProName());
		peDTO.setProPrice(product.getProPrice());
		peDTO.setProStatus(product.getProStatus());
		peDTO.setProVersion(product.getProVersion());
		peDTO.setProDate(product.getProDate());
		peDTO.setProCover(product.getProCover());
		peDTO.setProDetails(product.getProDetails());
		peDTO.setProInclude(product.getProInclude());
		if (product.getMallTagNo() != null) {
			peDTO.setMallTagNo(product.getMallTagNo().getId());
	        }
		peDTO.setProIsmarket(product.getProIsmarket());
		return peDTO;
	}
	
	public List<ProductManageDTO> toProductManageDTOList(List<Product> products){
		return products.stream()
                .map(this::toProductManageDTO)
                .collect(Collectors.toList());
	}
	
	 public Product toProduct(ProductEditDTO peDTO) {
	        if (peDTO == null) return null;
	        Product product = new Product();
	        product.setId(peDTO.getId());
	        product.setProName(peDTO.getProName());
	        product.setProPrice(peDTO.getProPrice());
	        product.setProStatus(peDTO.getProStatus());
	        product.setProVersion(peDTO.getProVersion());
	        product.setProDate(peDTO.getProDate());
	        product.setProCover(peDTO.getProCover());
	        product.setProDetails(peDTO.getProDetails());
	        product.setProInclude(peDTO.getProInclude());
	        MallTag malltag = malltagRepository.findById(peDTO.getMallTagNo()).get();
			product.setMallTagNo(malltag);
			product.setProIsmarket(peDTO.getProIsmarket());
			return product;

	    }
}
