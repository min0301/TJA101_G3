package com.pixeltribe.shopsys.proSerialNumber.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.pixeltribe.shopsys.proSerialNumber.model.ProSerialNumber;
import com.pixeltribe.shopsys.proSerialNumber.model.ProSerialNumberService;
import com.pixeltribe.shopsys.product.model.ProductPreorderService;
import com.pixeltribe.shopsys.product.model.ProductService;

import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/api")
public class ProSerialNumberController {

    @Autowired
    ProSerialNumberService proSerialNumberService;
    @Autowired
    ProductPreorderService productPreorderService;
    @Autowired
    ProductService productService;
    
    @PostMapping("/productSN/addone")
    public ResponseEntity<?> addProSN(@RequestBody ProSerialNumber proSN) {
        try {
            proSerialNumberService.addProSN(proSN);
            return ResponseEntity.ok("成功新增 1 筆序號");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("輸入錯誤: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("新增失敗: " + e.getMessage());
        }
    }
    
    @PostMapping("/productSN/addmultiple")
    public ResponseEntity<?> addMultipleProSN(@RequestParam("file") MultipartFile file,
                                             @RequestParam("proNo") Integer proNo) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 檔案驗證
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "檔案不能為空");
                return ResponseEntity.badRequest().body(response);
            }

            // 檔案格式驗證
            String fileName = file.getOriginalFilename();
            if (fileName == null || (!fileName.endsWith(".txt") && !fileName.endsWith(".csv"))) {
                response.put("success", false);
                response.put("message", "僅支援 .txt 和 .csv 檔案格式");
                return ResponseEntity.badRequest().body(response);
            }

            // 檔案大小驗證（限制 10MB）
            if (file.getSize() > 10 * 1024 * 1024) {
                response.put("success", false);
                response.put("message", "檔案大小不能超過 10MB");
                return ResponseEntity.badRequest().body(response);
            }

            // 產品編號驗證
            if (proNo == null || proNo <= 0) {
                response.put("success", false);
                response.put("message", "產品編號不正確");
                return ResponseEntity.badRequest().body(response);
            }

            // 執行匯入
            String result = proSerialNumberService.importFile(file, proNo);

            // 檢查結果是否為錯誤訊息
            if (result.contains("失敗") || result.contains("錯誤") || result.contains("不能") || result.contains("找不到")) {
                response.put("success", false);
                response.put("message", result);
                return ResponseEntity.badRequest().body(response);
            }

            response.put("success", true);
            response.put("message", "檔案上傳成功");
            response.put("result", result);
            response.put("fileName", fileName);
            response.put("fileSize", file.getSize());
            response.put("proNo", proNo);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "輸入錯誤：" + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "上傳過程中發生錯誤：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/productsn/allstock")
    public ResponseEntity<Map<Integer, Integer>> getProductStock(@RequestBody List<Integer> proNos) {
        try {
            Map<Integer, Integer> stockMap = new HashMap<>();
            for (Integer proNo : proNos) {
            	 Integer stockCount = proSerialNumberService.countStock(proNo);
            	 String productStatus = productService.getOneProduct(proNo).getProStatus();
            	 
            	 if ("預購中".equals(productStatus)) {
                     // 產品狀態為預購中，查詢預購數量
                     Integer preorderQuantity = productPreorderService.getPreorderInventory(proNo.toString());
                     stockMap.put(proNo, preorderQuantity);
                 }else {
                     stockMap.put(proNo, stockCount);
                 }
            }
            return ResponseEntity.ok(stockMap);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<>());
        }
    }
    
    @GetMapping("/productsn/stock/{proNo}")
    public ResponseEntity<Integer> getStock(@PathVariable Integer proNo) {
        try {
        	Integer stockCount = proSerialNumberService.countStock(proNo);
        	String productStatus = productService.getOneProduct(proNo).getProStatus();
        	if ("預購中".equals(productStatus)) {
                 // 產品狀態為預購中，查詢預購數量
                Integer preorderQuantity = productPreorderService.getPreorderInventory(proNo.toString());
                 if (preorderQuantity != null && preorderQuantity > 0) {
                     // 有預購庫存，返回預購資訊
                 	stockCount = preorderQuantity;
                 } else {
                     // 預購中但沒有設定預購數量
                 	stockCount = 0;
                 } 
        	}
            return ResponseEntity.ok(stockCount);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(0);
        }
    }
}