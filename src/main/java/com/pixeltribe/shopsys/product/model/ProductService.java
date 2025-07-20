package com.pixeltribe.shopsys.product.model;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.pixeltribe.shopsys.malltag.model.MallTag;
import com.pixeltribe.shopsys.malltag.model.MallTagRepository;
import com.pixeltribe.shopsys.proSerialNumber.model.ProSerialNumberRepository;
import com.pixeltribe.shopsys.proSerialNumber.model.ProSerialNumberService;
import com.pixeltribe.shopsys.product.exception.ProductExistException;
import com.pixeltribe.shopsys.product.exception.ProductIncompleteException;

@Service
public class ProductService {

	@Autowired
	ProductRepository productRepository;
	@Autowired
	ProductDTOMapper productDTOMapper;
	@Autowired
	MallTagRepository mallTagRepository;
	@Autowired 
	ProSerialNumberRepository proSerialNumberRepository;
	@Autowired
	ProductPreorderService productPreorderService;
	
	@Transactional
	public Product add(Product product) {
		isExistProduct(product);
		return productRepository.save(product);
    }
	@Transactional
	public ProductEditDTO update(Product product) {
		isExistProduct(product);
		Integer proNo = product.getId();
		if(product.getProIsmarket().charValue() == '0') {
			if(product.getProCover()==null||product.getProDate()==null||product.getProDetails()==null||product.getProInclude()==null) {
				throw new ProductIncompleteException("商品資料不完整，上架前請先更新");
			}
			if(proSerialNumberRepository.countStock(proNo) == 0 && productPreorderService.getPreorderInventory(proNo.toString()) == 0) {
				throw new ProductIncompleteException("商品庫存不足");
			}
		}
		Product updateProduct =  productRepository.save(product);
		return productDTOMapper.toProductEditDTO(updateProduct);	
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
	@Transactional
	public boolean updateMarketStatus(Integer proNo, Character proIsMarket) {
		 Optional<Product> product = productRepository.findById(proNo);
		 Product p = product.get();
		 if (proIsMarket.charValue() == '0') {
			 	if(p.getProCover() == null || p.getProDate()== null|| p.getProDetails() == null || p.getProInclude() == null) {
			 		throw new ProductIncompleteException("商品資料不完整，上架前請先更新");
		        }
			 	if(proSerialNumberRepository.countStock(proNo) == 0 && productPreorderService.getPreorderInventory(proNo.toString()) == 0) {
					throw new ProductIncompleteException("商品庫存不足");
				}
		   }
		 Integer updatedRows = productRepository.updateMarketStatus(proNo, proIsMarket);
		 return updatedRows > 0;
	}

	public List<ProductSearchDTO> findByMallTagAndMarket(Integer mallTagNo, Character proIsMarket) {
		List<Product> products = productRepository.findByMallTagAndMarket(mallTagNo, proIsMarket);
		return productDTOMapper.toProductSearchDTOList(products);
	}
	
	public List<ProductSearchDTO> findByMarket(Character proIsMarket) {
		List<Product> products = productRepository.findByMarket(proIsMarket);
		return productDTOMapper.toProductSearchDTOList(products);
	}

	public List<ProductSearchDTO> findByMallTagAllMarket(Integer mallTagNo) {
		List<Product> products = productRepository.findByMallTagAndMarket(mallTagNo, null);
		return productDTOMapper.toProductSearchDTOList(products);
	}
	
	public List<ProductManageDTO> findProductsByComplexQuery(String proName, Integer minPrice, Integer maxPrice,
																String proStatus, Integer mallTagNo, Character proIsMarket){
		List<Product> products = productRepository.findProductsByComplexQuery(proName, minPrice, maxPrice, proStatus, mallTagNo, proIsMarket);
		return productDTOMapper.toProductManageDTOList(products);
	}
	
	private Boolean isExistProduct(Product product) {
		Integer exist = productRepository.isExistProduct(product.getProName(), product.getProVersion(), product.getMallTagNo().getId());
		Integer productId = product.getId();
		if( exist != null){
			if( productId == null || productId != exist) {
				throw new ProductExistException("商品已存在，請重新確認");
			}
		}
		return false;
	}
}
