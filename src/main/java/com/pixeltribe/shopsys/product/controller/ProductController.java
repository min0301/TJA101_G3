package com.pixeltribe.shopsys.product.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pixeltribe.shopsys.product.model.Product;
import com.pixeltribe.shopsys.product.model.ProductService;

@RestController
@RequestMapping("/prdouct")
public class ProductController {
	
		@Autowired
		ProductService productService;
		
		@GetMapping("Product")
		public List<Product> getAllProducts() {
	        return productService.getAllProducts();
	    }
}
