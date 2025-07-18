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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.pixeltribe.shopsys.malltag.model.MallTag;
import com.pixeltribe.shopsys.malltag.model.MallTagRepository;
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
	RedisTemplate<String, Object> redisTemplate;
	@Autowired
	ProSerialNumberService proSerialNumberService;
	

	public Product add(Product product) {
		isExistProduct(product);
		return productRepository.save(product);
    }

	public Product update(Product product) {
		isExistProduct(product);
		if(product.getProIsmarket().charValue() == '0') {
			if(product.getProCover()==null||product.getProDate()==null||product.getProDetails()==null||product.getProInclude()==null) {
				throw new ProductIncompleteException("商品資料不完整，上架前請先更新");
			}
		}
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
		 Optional<Product> product = productRepository.findById(proNo);
		 Product p = product.get();
		 if (proIsMarket.charValue() == '0') {
			 	if(p.getProCover() == null || p.getProDate()== null|| p.getProDetails() == null || p.getProInclude() == null) {
			 		throw new ProductIncompleteException("商品資料不完整，上架前請先更新");
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
	
	private Boolean isExistProduct(Product product) {
		Integer exist = productRepository.isExistProduct(product.getProName(), product.getProVersion(), product.getMallTagNo().getId());
		if(product.getId() == null || exist != product.getId()) {
			throw new ProductExistException("商品已存在，請重新確認");
		}else {
			return false;
		}
	}
	   /*---------------------庫存相關-------------------------*/
	public void setPreorderInventory(Integer proNo, Integer quantity) {
		 redisTemplate.opsForValue().set(proNo.toString(), quantity);
    }

    public Integer getPreorderInventory(Integer proNo) {
        if (proNo == null) {
            return null;
        }
        Object value = redisTemplate.opsForValue().get(proNo.toString());
        return value != null ? (Integer) value : null;
    }

    public void deletePreorderInventory(Integer proNo) {
        if (proNo != null) {
            redisTemplate.delete(proNo.toString());
        }
    }
    
    public List<ProductInventoryDTO> getAllInventory(List<Integer> proNo) {
        return proNo.stream()
            .map(this::getProductInventoryDisplay)
            .collect(Collectors.toList());
    }

    public ProductInventoryDTO getProductInventoryDisplay(Integer proNo) {
        // 1. 查詢商品基本信息
        Optional<Product> product= productRepository.findById(proNo);
        
        // 2. 初始化顯示對象
        ProductInventoryDTO display = new ProductInventoryDTO();
        display.setId(proNo);
        display.setProStatus(product.get().getProStatus());
        display.setProDate(product.get().getProDate());
        
        // 3. 根據商品狀態選擇庫存計算方式
        switch (product.get().getProStatus()) {
            case "預購中":
                // 處理預購商品：從Redis獲取預購剩餘數量
                preorderInventoryDisplay(display, proNo);
                break;
            case "已發售":
                // 處理已發售商品：使用現有的proSerialNumberService
                releasedInventoryDisplay(display, proNo);
                break;
            default:
                display.setInventory(0);
                display.setDisplayText("無庫存");
                display.setIsAvailable(false);
        }
        return display;
    }
    
    private void preorderInventoryDisplay(ProductInventoryDTO display, Integer proNo) {
        Integer preorderStock = getPreorderInventory(proNo);

        if (preorderStock == null || preorderStock == 0) {
            // Redis中沒有數據或數量為0
            display.setInventory(0);
            display.setIsAvailable(false);
            display.setDisplayText("預購已滿");
        } else {
            // 有預購庫存
            display.setInventory(preorderStock);
            display.setIsAvailable(preorderStock > 0);
            display.setDisplayText("預購中");
        }
    }

    private void releasedInventoryDisplay(ProductInventoryDTO display, Integer proNo) {
        // 使用現有的proSerialNumberService查詢庫存
        Integer actualStock = proSerialNumberService.countStock(proNo);
        Integer stockCount = actualStock != null ? actualStock : 0;

        display.setInventory(stockCount);
        display.setIsAvailable(stockCount > 0);

        if (stockCount > 0) {
            display.setDisplayText("現貨");
        } else {
            display.setDisplayText("暫時缺貨");
        }
    }

    
}
