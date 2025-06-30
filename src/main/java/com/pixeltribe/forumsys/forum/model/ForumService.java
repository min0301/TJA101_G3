package com.pixeltribe.forumsys.forum.model;

import com.pixeltribe.forumsys.forumcategory.model.ForumCategory;
import com.pixeltribe.forumsys.forumcategory.model.ForumCategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service("forumService")
public class ForumService {

    @Autowired
    ForumRepository forumRepository;
    @Autowired
    ForumCategoryRepository forumCategoryRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;
    @Value("${file.base-url}")
    private String baseUrl;

    @Transactional
    public Forum add(Forum forum, MultipartFile imageFile) {
        // 1. 處理檔案儲存
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                // 2. 產生一個唯一的檔名，避免檔名衝突
                String originalFilename = imageFile.getOriginalFilename();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
                // 3. 儲存檔案到伺服器指定路徑
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath); // 如果目錄不存在，則建立
                }
                Path filePath = uploadPath.resolve(uniqueFilename);
                try (InputStream inputStream = imageFile.getInputStream()) {
                    Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
                // 4. 產生公開存取 URL
                String imageUrl = baseUrl + "/uploads/" + uniqueFilename; // 靜態資源路徑是 /uploads/
                // 5. 將 URL 設定到 forum 物件中
                forum.setForImgUrl(imageUrl);

            } catch (IOException e) {
                // 在真實專案中，應拋出一個自訂的執行時例外，讓 @ControllerAdvice 統一處理
                throw new RuntimeException("檔案儲存失敗", e);
            }
        }

        //===============================
        // 從傳入的 forum 物件中取得 categoryId
        Integer categoryId = forum.getCategoryId();

        ForumCategory category = forumCategoryRepository.findById(categoryId).get();

        // 將查找到的 Category 物件設定回 forum 的 catNo 屬性
        forum.setCatNo(category);
        //===============================
        // 6. 將包含 imageURL 的 forum 物件存入資料庫
        return forumRepository.save(forum);
    }



    public void update(Forum forum) {
        forumRepository.save(forum);
    }

    public void delete(Forum forum) {
            forumRepository.deleteById(forum.getId());
    }

    public Forum getOneForum(Integer forNO) {
        Optional<Forum> optional = forumRepository.findById(forNO);
        return optional.orElse(null);
    }

    public List<Forum> getAllForum() {
        return forumRepository.findAll();
    }





}
