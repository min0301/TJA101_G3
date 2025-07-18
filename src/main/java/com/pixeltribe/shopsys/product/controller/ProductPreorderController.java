package com.pixeltribe.shopsys.product.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pixeltribe.shopsys.product.model.ProductDTOMapper;
import com.pixeltribe.shopsys.product.model.ProductPreorderService;
import com.pixeltribe.shopsys.product.model.ProductService;

import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api")
public class ProductPreorderController {
	
	@Autowired
	ProductService productService;
	@Autowired
	ProductPreorderService proudctPreorderService;
	
	@PostMapping("/preorder/addinventory/{proNo}")
    public ResponseEntity<String> setPreorderInventory(
            @PathVariable Integer proNo, 
            @RequestParam Integer quantity) {
        
        try {
            if (quantity == null || quantity < 0) {
                return ResponseEntity.badRequest().body("庫存數量不能為空或小於0");
            }
            
            proudctPreorderService.setPreorderInventory(proNo.toString(), quantity);
            
            return ResponseEntity.ok("預購庫存設定成功");
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("設定預購庫存失敗");
        }
    }
	@GetMapping("/preorder/inventory/{proNo}")
	public Integer getPreorderInventory(@Parameter
										@PathVariable Integer proNo) {
			return proudctPreorderService.getPreorderInventory(proNo.toString());
	}
}
