package com.pixeltribe.shopsys.product.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pixeltribe.shopsys.malltag.model.MallTagRepository;
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
               preorderInventoryDisplay(display, proNo.toString());
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
