package com.pixeltribe.shopsys.productImage.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.pixeltribe.shopsys.product.model.Product;
import com.pixeltribe.shopsys.product.model.ProductRepository;
@Service
public class ProductImageService {
	
	@Autowired
    private ProductImageRepository productImageRepository;
    @Autowired
    private ProductRepository productRepository;

   
    public ProductImage uploadImage(Integer proNo, MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("上傳的檔案不能為空");
        }

        String contentType = file.getContentType();
        if (!isValidImageType(contentType)) {
            throw new IllegalArgumentException("只允許上傳圖片檔案 (jpg, jpeg, png, gif)");
        }
  
        ProductImage productImage = new ProductImage();
        productImage.setProNo(productRepository.findById(proNo).get());
        productImage.setProImgData(file.getBytes());
        productImage.setProImgType(contentType);
        
        return productImageRepository.save(productImage);
    }

    public List<ProductImage> getImagesByProNo(Integer proNo) {
        return productImageRepository.findByProNo(proNo);
    }
    
    public Integer getImageCountByProNo(Integer proNo) {
        return productImageRepository.countByProNo(proNo);
    }
    public ProductImage getImageById(Integer imageId) {
        return productImageRepository.findById(imageId)
            .orElseThrow(() -> new IllegalArgumentException("圖片不存在: " + imageId));
    }
    
    public List<ProductImage> uploadMultipleImages(Integer proNo, List<MultipartFile> files) throws IOException {
        
    	if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("請選擇要上傳的檔案");
        }
        
        List<ProductImage> uploadedImages = new ArrayList<>();
        
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            try {
                if (file.isEmpty()) {
                    throw new IllegalArgumentException("第 " + (i + 1) + " 個檔案為空");
                }
                
                String contentType = file.getContentType();
                if (!isValidImageType(contentType)) {
                    throw new IllegalArgumentException("第 " + (i + 1) + " 個檔案不是有效的圖片格式");
                }
                
                ProductImage productImage = new ProductImage();
                productImage.setProNo(productRepository.findById(proNo).get());
                productImage.setProImgData(file.getBytes());
                productImage.setProImgType(contentType);
                
                ProductImage savedImage = productImageRepository.save(productImage);
                uploadedImages.add(savedImage);
                
            } catch (IOException e) {
                throw new IOException("第 " + (i + 1) + " 個檔案上傳失敗: " + e.getMessage());
            }
        }
        
        return uploadedImages;
    }
    
    private boolean isValidImageType(String contentType) {
        return contentType != null && (
            contentType.equals("image/jpeg") ||
            contentType.equals("image/jpg") ||
            contentType.equals("image/png") ||
            contentType.equals("image/gif")
        );
    }
}
