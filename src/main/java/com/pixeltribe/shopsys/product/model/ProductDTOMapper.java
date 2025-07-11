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
	public  ProductAddDTO toProductAddDTO(Product product) {
		ProductAddDTO paDTO = new ProductAddDTO();
		paDTO.setProName(product.getProName());
		paDTO.setProPrice(product.getProPrice());
		paDTO.setProStatus(product.getProStatus());
		paDTO.setProVersion(product.getProVersion());
		paDTO.setProDate(product.getProDate());
		paDTO.setProCover(product.getProCover());
		paDTO.setProDetails(product.getProDetails());
		paDTO.setProInclude(product.getProInclude());
		if (product.getMallTagNo() != null) {
			paDTO.setMallTagNo(product.getMallTagNo().getId());
	        }
		paDTO.setProIsmarket(product.getProIsmarket());
		return paDTO;
	}
	
	
	public List<ProductManageDTO> toProductManageDTOList(List<Product> products){
		return products.stream()
                .map(this::toProductManageDTO)
                .collect(Collectors.toList());
	}
	
	
	 public Product peDTOToProduct(ProductEditDTO peDTO) {
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
	 public Product paDTOToProduct(ProductAddDTO paDTO) {
	        if (paDTO == null) return null;
	        Product product = new Product();
	        product.setProName(paDTO.getProName());
	        product.setProPrice(paDTO.getProPrice());
	        product.setProStatus(paDTO.getProStatus());
	        product.setProVersion(paDTO.getProVersion());
	        product.setProDate(paDTO.getProDate());
	        product.setProCover(paDTO.getProCover());
	        product.setProDetails(paDTO.getProDetails());
	        product.setProInclude(paDTO.getProInclude());
	        MallTag malltag = malltagRepository.findById(paDTO.getMallTagNo()).get();
			product.setMallTagNo(malltag);
			product.setProIsmarket(paDTO.getProIsmarket());
			return product;

	    }
}
