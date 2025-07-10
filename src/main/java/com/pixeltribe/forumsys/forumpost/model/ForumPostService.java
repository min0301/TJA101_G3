package com.pixeltribe.forumsys.forumpost.model;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant; // 確保 Instant 導入
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.pixeltribe.forumsys.exception.FileStorageException; // 引入自定義異常
import com.pixeltribe.forumsys.exception.ResourceNotFoundException; // 引入自定義異常
import com.pixeltribe.forumsys.forum.model.Forum;
import com.pixeltribe.forumsys.forum.model.ForumRepository;
import com.pixeltribe.forumsys.forumtag.model.ForumTag;
import com.pixeltribe.forumsys.forumtag.model.ForumTagRepository;
import com.pixeltribe.membersys.member.model.Member;
import com.pixeltribe.membersys.member.model.MemRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // 引入 @Value
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile; // 引入 MultipartFile

@Service("forumPostService") // 方法名稱 `forumPostService` 可變，但通常會與類名保持一致
public class ForumPostService {

    private final ForumPostRepository forumPostRepository;
    private final ForumRepository forumRepository;
    private final ForumTagRepository forumTagRepository;
    private final MemRepository memRepository;

    // 引入圖片儲存相關配置
    @Value("${file.upload-dir}") // 從 application.properties 或 application.yml 讀取
    private String uploadDir; // 變數名稱 `uploadDir` 可變

    @Value("${file.base-url}") // 從 application.properties 或 application.yml 讀取
    private String baseUrl; // 變數名稱 `baseUrl` 可變

    @Autowired
    public ForumPostService(ForumPostRepository forumPostRepository,
                            ForumRepository forumRepository,
                            ForumTagRepository forumTagRepository,
                            MemRepository memRepository) {
        this.forumPostRepository = forumPostRepository;
        this.forumRepository = forumRepository;
        this.forumTagRepository = forumTagRepository;
        this.memRepository = memRepository;
    }

    // --- CRUD 操作 (返回 DTO) ---

    /**
     * 新增文章。
     * 接收 ForumPostUpdateDTO 和 MultipartFile，處理圖片儲存並保存文章。
     * @param forumPostDTO 包含文章文字資訊的 DTO。
     * @param imageFile 文章封面圖片檔案 (可選)。
     * @return 新增後的 ForumPostDTO。
     */
    @Transactional // 交易註解，確保方法內的資料庫操作是原子性的
    public ForumPostDTO addForumPost(ForumPostUpdateDTO forumPostDTO, MultipartFile imageFile) {
        ForumPost forumPost = new ForumPost();
        // 將 DTO 中的資料設定到 Entity
        forumPost.setPostTitle(forumPostDTO.getPostTitle());
        forumPost.setPostCon(forumPostDTO.getPostCon());
        forumPost.setPostPin(forumPostDTO.getPostPin() != null ? forumPostDTO.getPostPin() : '0'); // 提供預設值
        forumPost.setPostStatus(forumPostDTO.getPostStatus() != null ? forumPostDTO.getPostStatus() : '0'); // 提供預設值
        forumPost.setMesNumbers(0); // 初始化
        forumPost.setPostLikeCount(0); // 初始化
        forumPost.setPostLikeDlc(0); // 初始化
        forumPost.setPostCrdate(Instant.now()); // 設定建立時間
        forumPost.setPostUpdate(Instant.now()); // 設定更新時間

        // 查找並設定關聯實體
        Member member = memRepository.findById(forumPostDTO.getMemId())
                .orElseThrow(() -> new ResourceNotFoundException("找不到會員 ID: " + forumPostDTO.getMemId()));
        forumPost.setMemNo(member);

        Forum forum = forumRepository.findById(forumPostDTO.getForNoId())
                .orElseThrow(() -> new ResourceNotFoundException("找不到討論區 ID: " + forumPostDTO.getForNoId()));
        forumPost.setForNo(forum);

        ForumTag forumTag = forumTagRepository.findById(forumPostDTO.getFtagNoId())
                .orElseThrow(() -> new ResourceNotFoundException("找不到文章類別 ID: " + forumPostDTO.getFtagNoId()));
        forumPost.setFtagNo(forumTag);

        // 處理圖片儲存
        String imageUrl = processImageFile(imageFile, "forumsys/forumpost"); // 變數名稱 `imageUrl` 可變
        forumPost.setPostCoverImageUrl(imageUrl);

        ForumPost savedForumPost = forumPostRepository.save(forumPost);
        return new ForumPostDTO(savedForumPost); // 返回 DTO
    }

    /**
     * 更新文章。
     * @param postId 文章 ID。
     * @param forumPostDTO 包含更新文章文字資訊的 DTO。
     * @param imageFile 文章封面圖片檔案 (可選)。
     * @return 更新後的 ForumPostDTO。
     */
    @Transactional
    public ForumPostDTO updateForumPost(Integer postId, ForumPostUpdateDTO forumPostDTO, MultipartFile imageFile) {
        ForumPost existingPost = forumPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到文章 ID: " + postId));

        existingPost.setPostTitle(forumPostDTO.getPostTitle());
        existingPost.setPostCon(forumPostDTO.getPostCon());
        existingPost.setPostPin(forumPostDTO.getPostPin() != null ? forumPostDTO.getPostPin() : existingPost.getPostPin()); // 若未提供，保持原值
        existingPost.setPostStatus(forumPostDTO.getPostStatus() != null ? forumPostDTO.getPostStatus() : existingPost.getPostStatus()); // 若未提供，保持原值
        existingPost.setPostUpdate(Instant.now()); // 更新時間

        // 查找並設定關聯實體 (如果 DTO 中提供了新的 ID)
        if (!existingPost.getMemNo().getId().equals(forumPostDTO.getMemId())) { // 檢查是否需要更新會員
            Member member = memRepository.findById(forumPostDTO.getMemId())
                    .orElseThrow(() -> new ResourceNotFoundException("找不到會員 ID: " + forumPostDTO.getMemId()));
            existingPost.setMemNo(member);
        }
        if (!existingPost.getForNo().getId().equals(forumPostDTO.getForNoId())) { // 檢查是否需要更新討論區
            Forum forum = forumRepository.findById(forumPostDTO.getForNoId())
                    .orElseThrow(() -> new ResourceNotFoundException("找不到討論區 ID: " + forumPostDTO.getForNoId()));
            existingPost.setForNo(forum);
        }
        if (!existingPost.getFtagNo().getId().equals(forumPostDTO.getFtagNoId())) { // 檢查是否需要更新文章類別
            ForumTag forumTag = forumTagRepository.findById(forumPostDTO.getFtagNoId())
                    .orElseThrow(() -> new ResourceNotFoundException("找不到文章類別 ID: " + forumPostDTO.getFtagNoId()));
            existingPost.setFtagNo(forumTag);
        }

        // 處理圖片更新
        String imageUrl = processImageFile(imageFile, "forumsys/forumpost");
        if (imageUrl != null) { // 如果有新圖片上傳，則更新 URL
            existingPost.setPostCoverImageUrl(imageUrl);
        }

        ForumPost updatedPost = forumPostRepository.save(existingPost);
        return new ForumPostDTO(updatedPost);
    }

    /**
     * 刪除文章。
     * @param forumPostId 文章 ID。
     */
    @Transactional
    public void deleteForumPost(Integer forumPostId) { // 方法名稱 `deleteForumPost` 可變
        if (!forumPostRepository.existsById(forumPostId)) {
            throw new ResourceNotFoundException("找不到文章 ID: " + forumPostId);
        }
        forumPostRepository.deleteById(forumPostId);
    }

    /**
     * 根據 ID 獲取單篇文章的 DTO。
     * @param forumPostId 文章 ID。
     * @return 包含 ForumPostDTO 的 Optional。
     */
    @Transactional(readOnly = true)
    public Optional<ForumPostDTO> getForumPostDTOById(Integer forumPostId) { // 方法名稱 `getForumPostDTOById` 可變
        // 使用 Repository 中 JOIN FETCH 的方法，避免 N+1 問題
        return forumPostRepository.findByIdWithForumAndMember(forumPostId)
                .map(ForumPostDTO::new);
    }

    /**
     * 獲取所有文章列表 (返回 DTO 列表)。
     * @return 所有文章的 ForumPostDTO 列表。
     */
    @Transactional(readOnly = true)
    public List<ForumPostDTO> getAllForumPost() { // 方法名稱 `getAllForumPost` 可變
        List<ForumPost> posts = forumPostRepository.findAllWithForumAndMember(); // 使用 JOIN FETCH 方法
        return posts.stream()
                .map(ForumPostDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * 獲取特定討論區的文章列表 (返回 DTO 列表)。
     * @param forumId 討論區 ID。
     * @return 特定討論區的文章 ForumPostDTO 列表。
     */
    @Transactional(readOnly = true)
    public List<ForumPostDTO> getPostsByForumId(Integer forumId) { // 方法名稱 `getPostsByForumId` 可變
        // 假設 findByForNo_Id 已經配置了 JOIN FETCH
        List<ForumPost> posts = forumPostRepository.findByForNo_Id(forumId);
        return posts.stream()
                .map(ForumPostDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * 根據文章 ID 和討論區 ID 查詢單篇文章 (返回 Optional<ForumPostDTO>)。
     * @param postId 文章 ID。
     * @param forumId 討論區 ID。
     * @return 包含 ForumPostDTO 的 Optional。
     */
    @Transactional(readOnly = true)
    public Optional<ForumPostDTO> getPostByIdAndForumId(Integer postId, Integer forumId) { // 方法名稱 `getPostByIdAndForumId` 可變
        return forumPostRepository.findByIdAndForNoId(postId, forumId)
                .map(ForumPostDTO::new);
    }

    /**
     * 內部方法：處理圖片檔案的儲存。
     * @param imageFile 上傳的圖片檔案。
     * @param subDirectory 儲存圖片的子目錄 (例如 "forumsys/forumpost")。
     * @return 儲存後的圖片 URL，如果沒有上傳檔案則返回 null。
     */
    private String processImageFile(MultipartFile imageFile, String subDirectory) { // 方法名稱 `processImageFile` 可變
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String originalFilename = imageFile.getOriginalFilename();
                String fileExtension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
                Path uploadPath = Paths.get(uploadDir + "/" + subDirectory); // 變數名稱 `uploadPath` 可變
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Path filePath = uploadPath.resolve(uniqueFilename); // 變數名稱 `filePath` 可變
                try (InputStream inputStream = imageFile.getInputStream()) {
                    Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
                // 靜態資源路徑是 /uploads/，因此 URL 結構為 baseUrl + /uploads/ + subDirectory + /filename
                return baseUrl + "/uploads/" + subDirectory + "/" + uniqueFilename; // 返回圖片的完整 URL

            } catch (IOException e) {
                throw new FileStorageException("檔案儲存失敗，無法寫入目標路徑。", e);
            }
        }
        return null; // 沒有上傳圖片，返回 null
    }

    // 移除原始的 insertPost 方法，因為它功能與 addForumPost 重複
    // @Transactional
    // public ForumPostDTO insertPost(...) { ... }
}