package com.pixeltribe.shopsys.product.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pixeltribe.shopsys.malltag.model.MallTagRepository;
import com.pixeltribe.shopsys.proSerialNumber.model.ProSerialNumberRepository;
import com.pixeltribe.shopsys.proSerialNumber.model.ProSerialNumberService;

@Service
public class ProductPreorderService {

	@Autowired
	ProductRepository productRepository;
	@Autowired
	ProductDTOMapper productDTOMapper;
	@Autowired
	MallTagRepository mallTagRepository;
	@Autowired
	RedisTemplate<String, Object> redisTemplate;
	@Autowired
	ProductPreorderRepository productPreorderRepository;
	@Autowired
	ProSerialNumberService proSerialNumberService;
	@Autowired
	ProSerialNumberRepository proSerialNumberRepository;

	
	@Transactional
	public void setPreorderInventory(String proNo, Integer quantity) {
		 redisTemplate.opsForValue().set(proNo, quantity);
   }
	
   public Integer getPreorderInventory(String proNo) {
       if (proNo == null) {
           return null;
       }
       Object value = redisTemplate.opsForValue().get(proNo);
       return value != null ? (Integer) value : 0;
   }
   
   @Transactional
   public Boolean deletePreorderInventory(String proNo) {
	   return Boolean.TRUE.equals(redisTemplate.delete(proNo));
   }
   
   public boolean preorderExists(String proNo) {
       return Boolean.TRUE.equals(redisTemplate.hasKey(proNo));
   }
   
   
   public List<ProductInventoryDTO> getAllInventory(List<Integer> proNos) {
	    if (proNos == null || proNos.isEmpty()) {
	        return Collections.emptyList();
	    }

	    List<Product> products = productRepository.findAllById(proNos);

	    List<Object[]> stockResults = proSerialNumberRepository.countStockByProNo(proNos);
	    Map<Integer, Integer> releasedStockMap = new HashMap<>();
	    for (Object[] row : stockResults) {
	        releasedStockMap.put((Integer) row[0], (Integer) row[1]);
	    }
	    
	    Map<Integer, Integer> preorderStockMap = new HashMap<>();
	    List<String> redisKeys = new ArrayList<>();
	    List<Integer> preorderProductIds = new ArrayList<>();

	    for (Product p : products) {
	        if ("預購中".equals(p.getProStatus())) {
	            preorderProductIds.add(p.getId());
	            redisKeys.add(p.getId().toString());
	        }
	    }

	    if (!redisKeys.isEmpty()) {
	        List<Object> stockValues = redisTemplate.opsForValue().multiGet(redisKeys);

	        if (stockValues != null) {
	            for (int i = 0; i < preorderProductIds.size(); i++) {
	                Integer productId = preorderProductIds.get(i);
	                Object stockValue = stockValues.get(i);

	                Integer finalStock = (stockValue != null) ? (Integer) stockValue : 0;
	                
	                preorderStockMap.put(productId, finalStock);
	            }
	        }
	    }
	    return products.stream()
	        .map(product -> getProductInventoryDisplay(product, releasedStockMap, preorderStockMap))
	        .collect(Collectors.toList());
	}
   
   public ProductInventoryDTO getProductInventoryDisplay(Product product, 
           Map<Integer, Integer> releasedStockMap, 
           Map<Integer, Integer> preorderStockMap) {

	   		ProductInventoryDTO display = new ProductInventoryDTO();
			display.setId(product.getId());
			display.setProStatus(product.getProStatus());
			display.setProDate(product.getProDate());
			
			switch (product.getProStatus()) {
			case "預購中":
				Integer preorderStock = preorderStockMap.getOrDefault(product.getId(), 0);
				display.setInventory(preorderStock);
				display.setDisplayText(preorderStock > 0 ? "預購中" : "預購已滿");
				display.setIsAvailable(preorderStock > 0);
				break;
			
			case "已發售":
				Integer releasedStock = releasedStockMap.getOrDefault(product.getId(), 0);
				display.setInventory(releasedStock.intValue());
				display.setDisplayText("現貨供應");
				display.setIsAvailable(releasedStock > 0);
				break;
			
			default:
				display.setInventory(0);
				display.setDisplayText("無庫存");
				display.setIsAvailable(false);
			}
			return display;
			}
   
   public ProductInventoryDTO getSingleProductInventory(Integer proNo) {
	    if (proNo == null) {
	        return null;
	    }
	    List<Integer> singleIdList = Collections.singletonList(proNo);
	    List<ProductInventoryDTO> resultList = this.getAllInventory(singleIdList);
	    return (resultList != null && !resultList.isEmpty()) ? resultList.get(0) : null;
	}
   
   private void preorderInventoryDisplay(ProductInventoryDTO display, String proNo) {
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
   
   @Transactional
   public Integer updaeProductStatus() {
       LocalDate today = LocalDate.now();
       return productPreorderRepository.updateProductStatus(today);
   }
   @Transactional
   public void cleanupRedis() {
       LocalDate yesterday = LocalDate.now().minusDays(1);
       List<Product> products = productPreorderRepository.findProductsByDate(yesterday);
       
       for (Product product : products) {
           if (preorderExists(product.getId().toString())) {
               if (deletePreorderInventory(product.getId().toString())) {
               }
           }
       }
   }

}
