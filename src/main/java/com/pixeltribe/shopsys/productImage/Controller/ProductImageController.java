package com.pixeltribe.shopsys.productImage.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.pixeltribe.shopsys.productImage.model.ProductImage;
import com.pixeltribe.shopsys.productImage.model.ProductImageService;

@RestController
@RequestMapping("/api")
public class ProductImageController {
	

    @Autowired
    private ProductImageService productImageService;
    
    @PostMapping("/productimg/upload")
    public ResponseEntity<?> uploadProductImage(@RequestParam("proNo") Integer proNo,
            									@RequestParam("file") MultipartFile file) {  
        Map<String, Object> response = new HashMap<>();
        
        try {
            ProductImage productImage = productImageService.uploadImage(proNo, file);
            
            response.put("success", true);
            response.put("message", "圖片上傳成功");
            response.put("data", Map.of(
                "id", productImage.getId(),
                "proNo", productImage.getProNo().getId(),
                "proImgType", productImage.getProImgType()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "圖片上傳失敗: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/productimg/search/{proNo}")
    public ResponseEntity<Map<String, Object>> getImagesByProNo(@PathVariable Integer proNo) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<ProductImage> images = productImageService.getImagesByProNo(proNo);
            
            List<Map<String, Object>> imageData = images.stream()
            											.<Map<String, Object>>map(img -> Map.of(
            								                    "id", img.getId(),
            								                    "proNo", img.getProNo().getId(),
            								                    "proImgType", img.getProImgType(),
            								                    "imageUrl", "/api/productimg/" + img.getId()))
            											.toList();
            response.put("success", true);
            response.put("message", "查詢成功");
            response.put("data", imageData);
            response.put("count", images.size());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查詢失敗: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/productimg/{imageId}")
    public ResponseEntity<byte[]> viewProductImage(@PathVariable Integer imageId) {
    	
    	 ProductImage productImage = productImageService.getImageById(imageId);
         
         return ResponseEntity.ok()
             .contentType(MediaType.parseMediaType(productImage.getProImgType()))
             .body(productImage.getProImgData());
		    
    }
    
    @GetMapping("/productimg/count/{proNo}")
    public ResponseEntity<Map<String, Object>> getImageCount(@PathVariable Integer proNo) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Integer count = productImageService.getImageCountByProNo(proNo);
            
            response.put("success", true);
            response.put("message", "查詢成功");
            response.put("count", count);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查詢失敗: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
	
}
