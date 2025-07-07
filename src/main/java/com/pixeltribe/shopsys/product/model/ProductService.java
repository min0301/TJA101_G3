package com.pixeltribe.shopsys.product.model;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ProductService {

	@Autowired
	ProductRepository productRepository;
	@Autowired
	ProductDTOMapper productDTOMapper;
	
	public void add(Product product) {
        productRepository.save(product);
    }

    public void update(Product product) {
    	productRepository.save(product);
    }

    public void delete(Product product) {
    	productRepository.deleteById(product.getId());
    }

    public Product getOneProduct(Integer proNO) {
        Optional<Product> optional = productRepository.findById(proNO);
        return optional.orElse(null);
    }

    public List<ProductManageDTO> getAllProducts() {
    	 List<Product> products = productRepository.findAll();
         return productDTOMapper.toProductManageDTOList(products);

    }

	
}
