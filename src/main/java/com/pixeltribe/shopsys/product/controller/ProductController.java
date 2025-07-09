package com.pixeltribe.shopsys.product.controller;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.pixeltribe.forumsys.forum.model.ForumDetailDTO;
import com.pixeltribe.shopsys.product.model.Product;
import com.pixeltribe.shopsys.product.model.ProductDTOMapper;
import com.pixeltribe.shopsys.product.model.ProductEditDTO;
import com.pixeltribe.shopsys.product.model.ProductManageDTO;
import com.pixeltribe.shopsys.product.model.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class ProductController {
	
		@Autowired
		ProductService productService;
		@Autowired
		ProductManageDTO productManageDTO;
		@Autowired
		ProductEditDTO productEditDTO;
		@Autowired
		ProductDTOMapper productDTOMapper;
		
		@GetMapping("product")
		public List<ProductManageDTO> getAllProducts() {
	        return productService.getAllProducts();
	    }
		
		 @PutMapping("/admin/product/{id}")
		    public ResponseEntity<?> updateProduct(
		            @PathVariable Integer id,
		            @RequestPart("ProductEditDTO") @Valid ProductEditDTO productEditDTO,
		            @RequestPart(value = "imageFile", required = false) MultipartFile[] imageFile,
		            BindingResult result) throws IOException{
			 	productEditDTO.setId(id); 
		        if (result.hasErrors()) {
		            // 如果 JSON 資料驗證失敗，回傳 400 錯誤
		            return ResponseEntity.badRequest().body("輸入資料有誤！");
		        }
		        
		        result = removeFieldError(productEditDTO, result, "upFiles");
				if (imageFile[0].isEmpty()) { // 使用者未選擇要上傳的新圖片時
					byte[] upFiles = productService.getOneProduct(id).getProCover();
					productEditDTO.setProCover(upFiles);
				} else {
					for (MultipartFile multipartFile : imageFile) {
						byte[] upFiles = multipartFile.getBytes();
						productEditDTO.setProCover(upFiles);
					}
				}
				if (result.hasErrors()) {
					System.out.println("error");
				}

			    Product updateProduct = productService.update(productDTOMapper.toProduct(productEditDTO));

		        return ResponseEntity.ok(updateProduct);
		    }
		
		@GetMapping("/product/{id}")
		public ProductEditDTO getOneProduct(
		            @Parameter
		            @PathVariable Integer id) {
			
		   return productDTOMapper.toProductEditDTO(productService.getOneProduct(id));
		 }
		 
		@GetMapping("/product/cover/{id}")
		public ResponseEntity<byte[]> getProductCover(@PathVariable Integer id) {
		    Product product = productService.getOneProduct(id);
		    byte[] proCover = product.getProCover();

		    return ResponseEntity.ok()
		            .contentType(MediaType.IMAGE_GIF) 
		            .body(proCover);
		}
		
		
		public BindingResult removeFieldError(ProductEditDTO productEditDTO, BindingResult result, String removedFieldname) {
			List<FieldError> errorsListToKeep = result.getFieldErrors().stream()
					.filter(fieldname -> !fieldname.getField().equals(removedFieldname))
					.collect(Collectors.toList());
			result = new BeanPropertyBindingResult(productEditDTO, "prodcut");
			for (FieldError fieldError : errorsListToKeep) {
				result.addError(fieldError);
			}
			return result;
		}
}
