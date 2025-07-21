package com.pixeltribe.shopsys.productImage.model;

import org.springframework.beans.factory.annotation.Autowired;

import com.pixeltribe.shopsys.product.model.Product;
import com.pixeltribe.shopsys.product.model.ProductRepository;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductImageUploadDTO {
	
	private Integer id;
	
	private Integer proNo;

    private byte[] proImgData;

    private String proImgType;
    
    @Autowired
    ProductRepository productRepository;
    public ProductImageUploadDTO toProductImageUploadDTO(ProductImage productImage) {
    	ProductImageUploadDTO proimg = new ProductImageUploadDTO();
    	proimg.setId(productImage.getId());
    	proimg.setProNo(productImage.getProNo().getId());
    	proimg.setProImgData(productImage.getProImgData());
    	proimg.setProImgType(productImage.getProImgType());
    	return proimg;
    }
    public ProductImage proimgUploadToProductImage(ProductImageUploadDTO productImageUploadDTO) {
    	ProductImage proimg = new ProductImage();
    	proimg.setId(productImageUploadDTO.getId());
    	Product product = productRepository.findById(productImageUploadDTO.getProNo()).get();
    	proimg.setProNo(product);
    	proimg.setProImgData(productImageUploadDTO.getProImgData());
    	proimg.setProImgType(productImageUploadDTO.getProImgType());
    	return proimg;
    }
}


