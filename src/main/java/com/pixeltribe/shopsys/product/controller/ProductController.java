package com.pixeltribe.shopsys.product.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pixeltribe.shopsys.product.model.Product;
import com.pixeltribe.shopsys.product.model.ProductManageDTO;
import com.pixeltribe.shopsys.product.model.ProductService;

@RestController
@RequestMapping("/api")
public class ProductController {
	
		@Autowired
		ProductService productService;
		@Autowired
		ProductManageDTO productManageDTO;
		
		@GetMapping("product")
		public List<ProductManageDTO> getAllProducts() {
	        return productService.getAllProducts();
	    }
		
		@GetMapping("/product/cover/{id}")
		public ResponseEntity<byte[]> getProductCover(@PathVariable Integer id) {
		    // 假設 product.getImageData() 可以從資料庫拿到 byte[]
		    Product product = productService.getOneProduct(id);
		    byte[] proCover = product.getProCover();

		    return ResponseEntity.ok()
		            .contentType(MediaType.IMAGE_GIF) 
		            .body(proCover);
		}
}
