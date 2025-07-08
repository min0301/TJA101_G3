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


    private final ForumRepository forumRepository;
    private final ForumCategoryRepository forumCategoryRepository;

    public ForumService(ForumRepository forumRepository, ForumCategoryRepository forumCategoryRepository) {

        this.forumRepository = forumRepository;
        this.forumCategoryRepository = forumCategoryRepository;
    }


    @Value("${file.upload-dir}")
    private String uploadDir;
    @Value("${file.base-url}")
    private String baseUrl;

    @Transactional
    public ForumDetailDTO add(ForumUpdateDTO forumUpdateDTO, MultipartFile imageFile) {

        Forum forum = new Forum();

        return ForumDetailDTO.convertToForumDetailDTO(saveOrUpdateForum(forum, forumUpdateDTO, imageFile));
    }

    @Transactional
    public ForumDetailDTO update(Integer forNo, ForumUpdateDTO forumUpdateDTO, MultipartFile imageFile) {

        Forum forum = forumRepository.findById(forNo).get();

        return ForumDetailDTO.convertToForumDetailDTO(saveOrUpdateForum(forum, forumUpdateDTO, imageFile));
    }


    public List<ForumDetailDTO> getAllForum() {

        // 1. 從資料庫取得原始的 Entity 列表
        List<Forum> forums = forumRepository.findAllByOrderByForUpdateDesc();


//        for (Forum x : forums) {
//          ForumDetailDTO.convertToForumDetailDTO(x);
//        }

        // 2. 使用 Stream API 將 List<Forum> 轉換為 List<ForumDetailDTO>
        return forums.stream()

                .map(x -> ForumDetailDTO.convertToForumDetailDTO(x))
                //      .map(ForumDetailDTO::convertToForumDetailDTO) // 對每個 forum 執行轉換
                .collect(Collectors.toList());
    }


    public List<ForumDetailDTO> getForumsByCategory(Integer catNO) {
        List<Forum> forums = forumRepository.findByCatNo_Id(catNO);

        return forums.stream()
                .map(ForumDetailDTO::convertToForumDetailDTO) // 對每個 forum 執行轉換
                .collect(Collectors.toList());
    }

    public ForumDetailDTO getOneForum(Integer forNo) {
        Forum forum = forumRepository.findById(forNo).get();
        return ForumDetailDTO.convertToForumDetailDTO(forum);
    }


    private Forum saveOrUpdateForum(Forum forum, ForumUpdateDTO forumUpdateDTO, MultipartFile imageFile) {

        forum.setForName(forumUpdateDTO.getForName());
        forum.setForDes(forumUpdateDTO.getForDes());
        forum.setForStatus(forumUpdateDTO.getForStatus());

        ForumCategory category = forumCategoryRepository.findById(forumUpdateDTO.getCategoryId()).get();
        forum.setCatNo(category);


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
        // 6. 將包含 imageURL 的 forum 物件存入資料庫
        return forumRepository.save(forum);

    }

}
