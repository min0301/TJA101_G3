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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.pixeltribe.shopsys.malltag.model.MallTag;
import com.pixeltribe.shopsys.malltag.model.MallTagRepository;
import com.pixeltribe.shopsys.proSerialNumber.model.ProSerialNumberService;

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
	public void setPreorderInventory(Integer proNo, Integer quantity) {
        if (proNo == null || quantity == null) {
            return;
        }
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
    
   /*---------------------庫存相關-------------------------*/

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
            // 情況1：Redis中沒有數據或數量為0
            display.setInventory(0);
            display.setIsAvailable(false);
            display.setDisplayText("預購已滿");
        } else {
            // 情況2：有預購庫存
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
