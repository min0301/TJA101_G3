package com.pixeltribe.shopsys.product.model;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.pixeltribe.shopsys.malltag.model.MallTag;
import com.pixeltribe.shopsys.malltag.model.MallTagRepository;


@Service
public class ProductService {

	@Autowired
	ProductRepository productRepository;
	@Autowired
	ProductDTOMapper productDTOMapper;
	@Autowired
	MallTagRepository mallTagRepository;
	
	public Product add(Product product) {
		return productRepository.save(product);
    }

    public Product update(Product product) {
    	return productRepository.save(product);
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
    
    public boolean updateMarketStatus(Integer proNo, Character proIsMarket) {
            Integer updatedRows = productRepository.updateMarketStatus(proNo, proIsMarket);
            return updatedRows > 0;
    }
   
    public List<ProductSearchDTO> findByMallTagAndMarket(Integer mallTagNo, Character proIsMarket){
    		List<Product> products = productRepository.findByMallTagAndMarket(mallTagNo, proIsMarket);
    	return productDTOMapper.toProductSearchDTOList(products);
    }
    
    public List<ProductSearchDTO> findByMallTagAllMarket(Integer mallTagNo){
    		List<Product> products = productRepository.findByMallTagAndMarket(mallTagNo,null);
    	return productDTOMapper.toProductSearchDTOList(products);
    }
	
}
