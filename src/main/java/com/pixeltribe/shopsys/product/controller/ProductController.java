package com.pixeltribe.shopsys.product.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.pixeltribe.forumsys.forum.model.ForumDetailDTO;
import com.pixeltribe.shopsys.product.exception.ProductExistException;
import com.pixeltribe.shopsys.product.exception.ProductIncompleteException;
import com.pixeltribe.shopsys.product.model.Product;
import com.pixeltribe.shopsys.product.model.ProductAddDTO;
import com.pixeltribe.shopsys.product.model.ProductDTOMapper;
import com.pixeltribe.shopsys.product.model.ProductDisplayDTO;
import com.pixeltribe.shopsys.product.model.ProductEditDTO;
import com.pixeltribe.shopsys.product.model.ProductInventoryDTO;
import com.pixeltribe.shopsys.product.model.ProductIsmarketDTO;
import com.pixeltribe.shopsys.product.model.ProductManageDTO;
import com.pixeltribe.shopsys.product.model.ProductPreorderService;
import com.pixeltribe.shopsys.product.model.ProductSearchDTO;
import com.pixeltribe.shopsys.product.model.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class ProductController {
	
		@Autowired
		ProductService productService;
		@Autowired
		ProductDTOMapper productDTOMapper;
		@Autowired
		ProductPreorderService proudctPreorderService;
		
		@GetMapping("product")
		public List<ProductManageDTO> getAllProducts() {
	        return productService.getAllProducts();
	    }
		
		@PostMapping("/product/addproduct")
		public ResponseEntity<?> addProduct(
		            @RequestPart("ProductAddDTO") @Valid ProductAddDTO productAddDTO,
		            BindingResult result,
		            @RequestPart(value = "imageFile", required = false) MultipartFile[] imageFile
		            ) throws IOException{
		    if (result.hasErrors()) {
		        return ResponseEntity.badRequest().body("輸入資料有誤！");
		    }     
			if (imageFile[0].isEmpty()) { // 使用者未選擇要上傳的新圖片時
					
			} else {
				for (MultipartFile multipartFile : imageFile) {
					byte[] upFiles = multipartFile.getBytes();
					productAddDTO.setProCover(upFiles);
				}
			}
			
			if (result.hasErrors()) {
				System.out.println("error");
			}

			
			try {
				productAddDTO.setProIsmarket('1');
		        Product addProduct = productService.add(productDTOMapper.paDTOToProduct(productAddDTO));
		        return ResponseEntity.ok(addProduct);
		        
		    } catch (ProductExistException e) {
		        return ResponseEntity.status(HttpStatus.CONFLICT)
		            .body(e.getMessage());
		            
		    } catch (Exception e) {
		        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
		            .body(e.getMessage());
		    }
		  
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
				try {
					Product updateProduct = productService.update(productDTOMapper.peDTOToProduct(productEditDTO));
			        return ResponseEntity.ok(updateProduct);
			        
			    } catch (ProductExistException e) {
					  
			        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());        
			        
			    } catch (ProductIncompleteException e) {
			        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
			        
			    }catch (Exception e) {
			        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
			        
			    } 
		    }
		
		@GetMapping("/product/{id}")
		public ProductEditDTO getOneProduct(@Parameter
											@PathVariable Integer id) {
		   return productDTOMapper.toProductEditDTO(productService.getOneProduct(id));
		 }
		
		@GetMapping("/product/{id}/search")
		public ResponseEntity<?> searchOneProduct(@Parameter
										@PathVariable Integer id) {
			ProductDisplayDTO product = productDTOMapper.toProductDisplayDTO(productService.getOneProduct(id));
			ProductInventoryDTO inventory = proudctPreorderService.getProductInventoryDisplay(id);
			Map<String, Object> result = new HashMap<>();
			result.put("product", product);
			result.put("inventory", inventory);
			return ResponseEntity.ok(result);
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
		
		@PutMapping("/product/{proNo}/market-status")
	    public ResponseEntity<Map<String, Object>> updateMarketStatus(
	            @PathVariable Integer proNo,
	            @RequestBody ProductIsmarketDTO request) {
	        
	        Map<String, Object> response = new HashMap<>();
	        
	        try {
	            boolean success = productService.updateMarketStatus(proNo, request.getProIsmarket());
	            
	                response.put("success", true);
	                response.put("message", "狀態更新成功");
	                response.put("proNo", proNo);
	                response.put("proIsmarket", request.getProIsmarket());
	                return ResponseEntity.ok(response);
	           
	        }catch (ProductIncompleteException e) {
	        	response.put("success", false);
	            response.put("message", e.getMessage());
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	        } catch (Exception e) {
	            response.put("success", false);
	            response.put("message", "系統錯誤：" + e.getMessage());
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	        }
	    }
		@GetMapping("/product/search/{mallTagNo}")
		public List<ProductSearchDTO> findByMallTagAndMarket(
				@PathVariable Integer mallTagNo,
	            @RequestParam(required = false, defaultValue = "0") Character proIsMarket) {

				return productService.findByMallTagAndMarket(mallTagNo, proIsMarket);
		        
		}
		
		@GetMapping("/product/searchall")
		public List<ProductSearchDTO> findByMarket(
	            @RequestParam(required = false, defaultValue = "0") Character proIsMarket) {
				return productService.findByMarket(proIsMarket);  
		}
		
		@GetMapping("/product/complexquery")
		public List<ProductManageDTO> findProductsByComplexQuery( @RequestParam(required = false) String proName,
													              @RequestParam(required = false) Integer minPrice,
													              @RequestParam(required = false) Integer maxPrice,
													              @RequestParam(required = false) String proStatus,
													              @RequestParam(required = false) Integer mallTagNo,
													              @RequestParam(required = false) Character proIsMarke){
			return productService.findProductsByComplexQuery(proName, minPrice, maxPrice, proStatus, mallTagNo, proIsMarke);
		}
	        
}
