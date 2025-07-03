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
import java.util.UUID;
import java.util.stream.Collectors;

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
    public Forum add(ForumCreationDTO forumDTO, MultipartFile imageFile) {

        // DTO -> Entity 的轉換 (手動映射)
        Forum forum = new Forum();
        forum.setForName(forumDTO.getForName());
        forum.setForDes(forumDTO.getForDes());
        forum.setForStatus(forumDTO.getForStatus());

        // 1. 處理檔案儲存
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                // 2. 產生一個唯一的檔名，避免檔名衝突
                String originalFilename = imageFile.getOriginalFilename();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
                // 3. 儲存檔案到伺服器指定路徑
                Path uploadPath = Paths.get(uploadDir + "/forumsys/forum");
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath); // 如果目錄不存在，則建立
                }
                Path filePath = uploadPath.resolve(uniqueFilename);
                try (InputStream inputStream = imageFile.getInputStream()) {
                    Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
                // 4. 產生公開存取 URL
                String imageUrl = baseUrl + "/uploads/forumsys/forum/" + uniqueFilename; // 靜態資源路徑是 /uploads/
                // 5. 將 URL 設定到 forum 物件中
                forum.setForImgUrl(imageUrl);

            } catch (IOException e) {
                // 在真實專案中，應拋出一個自訂的執行時例外，讓 @ControllerAdvice 統一處理
                throw new RuntimeException("檔案儲存失敗", e);
            }
        }

        //===============================
        // 從傳入的 forum 物件中取得 categoryId
        Integer categoryId = forumDTO.getCategoryId();

        ForumCategory category = forumCategoryRepository.findById(categoryId).get();

        // 將查找到的 Category 物件設定回 forum 的 catNo 屬性
        forum.setCatNo(category);
        //===============================
        // 6. 將包含 imageURL 的 forum 物件存入資料庫
        return forumRepository.save(forum);
    }


    public Forum update(ForumUpdateDTO forumEditDTO) {

        Forum forum = new Forum();
        forum.setForName(forumEditDTO.getForName());
        forum.setForDes(forumEditDTO.getForDes());
        forum.setForStatus(forumEditDTO.getForStatus());

        ForumCategory category = forumCategoryRepository.findById(forumEditDTO.getCategoryId()).get();
        forum.setCatNo(category);

        return forumRepository.save(forum);
    }


    public List<ForumDetailDTO> getAllForum() {

        // 1. 從資料庫取得原始的 Entity 列表
        List<Forum> forums = forumRepository.findAllByOrderByForUpdateDesc();

        // 2. 使用 Stream API 將 List<Forum> 轉換為 List<ForumDetailDTO>
        return forums.stream()
                .map(this::convertToForumDetailDTO) // 對每個 forum 執行轉換
                .collect(Collectors.toList());
    }

    // 這是一個輔助方法，負責將單一 Forum Entity 轉換為 DTO
    private ForumDetailDTO convertToForumDetailDTO(Forum forum) {
        ForumDetailDTO dto = new ForumDetailDTO();

        // 複製基本屬性
        dto.setId(forum.getId());
        dto.setForName(forum.getForName());
        dto.setForDes(forum.getForDes());
        dto.setForImgUrl(forum.getForImgUrl());
        dto.setForDate(forum.getForDate());
        dto.setForUpdate(forum.getForUpdate());
        dto.setForStatus(forum.getForStatus());


        // 關鍵：處理關聯物件的屬性
        // 必須做 null 檢查，防止 Forum 沒有被分配到 Category 的情況
        if (forum.getCatNo() != null) {
            dto.setCategoryName(forum.getCatNo().getCatName());
            dto.setCategoryId(forum.getCatNo().getId());
        } else {
            // 如果沒有分類，可以設定為 null 或是一個預設值，例如 "未分類"
            // 這取決於你的前端想要如何顯示
            dto.setCategoryName(null);
            dto.setCategoryId(null);
        }

        return dto;
    }


    public List<ForumDetailDTO> getForumsByCategory(Integer catNO) {
        List<Forum> forums = forumRepository.findByCatNo_Id(catNO);

        return forums.stream()
                .map(this::convertToForumDetailDTO) // 對每個 forum 執行轉換
                .collect(Collectors.toList());
    }





}
