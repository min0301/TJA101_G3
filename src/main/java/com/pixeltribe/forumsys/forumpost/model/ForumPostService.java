package com.pixeltribe.forumsys.forumpost.model;

import com.pixeltribe.forumsys.exception.FileStorageException;
import com.pixeltribe.forumsys.exception.ResourceNotFoundException;
import com.pixeltribe.forumsys.forum.model.Forum;
import com.pixeltribe.forumsys.forum.model.ForumRepository;
import com.pixeltribe.forumsys.forumtag.model.ForumTag;
import com.pixeltribe.forumsys.forumtag.model.ForumTagRepository;
import com.pixeltribe.membersys.member.model.MemRepository;
import com.pixeltribe.membersys.member.model.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service("forumPostService") // 方法名稱 `forumPostService` 可變，但通常會與類名保持一致
public class ForumPostService {

    private final ForumPostRepository forumPostRepository;
    private final ForumRepository forumRepository; // 需要 ForumRepository 來獲取所有討論區
    private final ForumTagRepository forumTagRepository;
    private final MemRepository memRepository;

    // 引入圖片儲存相關配置
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.base-url}")
    private String baseUrl;

    private static final Map<Integer, String> DEFAULT_IMAGE_MAP = new HashMap<>(); // 變數名稱 `DEFAULT_IMAGE_MAP` 不可變，通常作為常數

    static {
        // 這些 ID 和圖片名稱需要根據您的實際情況進行配置
        DEFAULT_IMAGE_MAP.put(1, "01.jpg");
        DEFAULT_IMAGE_MAP.put(2, "02.jpg");
        DEFAULT_IMAGE_MAP.put(3, "03.jpg");
        DEFAULT_IMAGE_MAP.put(4, "04.jpg");
        DEFAULT_IMAGE_MAP.put(5, "05.jpg");
        DEFAULT_IMAGE_MAP.put(6, "06.jpg");
        DEFAULT_IMAGE_MAP.put(7, "07.jpg");
        DEFAULT_IMAGE_MAP.put(8, "08.jpg");
        DEFAULT_IMAGE_MAP.put(9, "09.jpg");
        DEFAULT_IMAGE_MAP.put(10, "10.jpg");
        DEFAULT_IMAGE_MAP.put(11, "11.jpg");
    }

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
     * 根據文章類別 ID 獲取其預設圖片 URL。
     *
     * @param categoryId 文章類別 ID。
     * @return 預設圖片的完整 URL。
     */
    public String getCategoryDefaultImageUrl(Integer categoryId) { // 方法名稱 `getCategorDefaultImageUrl` 可變
        String imageName = DEFAULT_IMAGE_MAP.get(categoryId); // 變數名稱 `imageName` 可變
        if (imageName != null) {
            // 注意：這裡的路徑需要與您的 Spring Boot 靜態資源映射一致
            // 如果圖片在 src/main/resources/imgseed/forumposttag_img/，則路徑應該是 /imgseed/forumposttag_img/
            // 並搭配 baseUrl
            return baseUrl + "/images/forumposttag_img/" + imageName;
        }
        // 如果沒有找到對應的圖片，返回一個通用的預設圖片
        return baseUrl + "/images/forumposttag_img/default.jpg"; // 提供一個通用的預設路徑
    }

    /**
     * 新增文章。
     * 接收 ForumPostUpdateDTO 和 MultipartFile，處理圖片儲存並保存文章。
     *
     * @param forumPostDTO                包含文章文字資訊的 DTO。
     * @param imageFile                   文章封面圖片檔案 (可選)。
     * @param defaultImageUrlFromFrontend
     * @return 新增後的 ForumPostDTO。
     */
    @Transactional // 交易註解，確保方法內的資料庫操作是原子性的
    public ForumPostDTO addForumPost(ForumPostUpdateDTO forumPostDTO, MultipartFile imageFile, String defaultImageUrlFromFrontend) {
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
        Member member = memRepository.findById(1)
                .orElseThrow(() -> new ResourceNotFoundException("找不到會員 ID: 1。請確認資料庫中是否存在 ID 為 1 的會員。" ));
        forumPost.setMemNo(member);

        Forum forum = forumRepository.findById(forumPostDTO.getForNoId())
                .orElseThrow(() -> new ResourceNotFoundException("找不到討論區 ID: " + forumPostDTO.getForNoId()));
        forumPost.setForNo(forum);

        ForumTag forumTag = forumTagRepository.findById(forumPostDTO.getFtagNoId())
                .orElseThrow(() -> new ResourceNotFoundException("找不到文章類別 ID: " + forumPostDTO.getFtagNoId()));
        forumPost.setFtagNo(forumTag);
        // 判斷使用者是否有上傳圖片檔案

        if (imageFile != null && !imageFile.isEmpty()) {
            // 如果有上傳自訂圖片，則處理並使用上傳圖片的 URL
            String imageUrl = processImageFile(imageFile, "forumsys/forumpost");
            forumPost.setPostImageUrl(imageUrl);
        } else if (defaultImageUrlFromFrontend != null && !defaultImageUrlFromFrontend.isEmpty()) {
            // 如果沒有上傳自訂圖片，但前端傳來了預設圖片的 URL，則使用該 URL
            forumPost.setPostImageUrl(defaultImageUrlFromFrontend);
        } else {
            // 如果兩者都沒有，可以設置一個通用預設圖片或根據文章類別 ID 獲取預設圖片
            // 這裡可以考慮根據 ftagNoId 再次獲取預設圖片，作為最終備用
            forumPost.setPostImageUrl(getCategoryDefaultImageUrl(forumPostDTO.getFtagNoId())); // 優先使用該類別的預設圖片
        }

        ForumPost savedForumPost = forumPostRepository.save(forumPost);
        return new ForumPostDTO(savedForumPost); // 返回 DTO
    }


    /**
     * 更新文章。
     *
     * @param postId       文章 ID。
     * @param forumPostDTO 包含更新文章文字資訊的 DTO。
     * @param imageFile    文章封面圖片檔案 (可選)。
     * @param defaultImageUrlFromFrontend 前端傳來的預設圖片 URL (如果選擇使用預設圖片)。
     * @return 更新後的 ForumPostDTO。
     */
    @Transactional
    public ForumPostDTO updateForumPost(Integer postId, Integer forNo, ForumPostUpdateDTO forumPostDTO, MultipartFile imageFile, String defaultImageUrlFromFrontend) {
        ForumPost existingPost = forumPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到文章 ID: " + postId));

        existingPost.setPostTitle(forumPostDTO.getPostTitle());
        existingPost.setPostCon(forumPostDTO.getPostCon());
        existingPost.setPostPin(forumPostDTO.getPostPin() != null ? forumPostDTO.getPostPin() : existingPost.getPostPin()); // 若未提供，保持原值
        existingPost.setPostStatus(forumPostDTO.getPostStatus() != null ? forumPostDTO.getPostStatus() : existingPost.getPostStatus()); // 若未提供，保持原值
        existingPost.setPostUpdate(Instant.now()); // 更新時間

        // 查找並設定關聯實體 (如果 DTO 中提供了新的 ID)
//        if (!existingPost.getMemNo().getId().equals(forumPostDTO.getMemId())) { // 檢查是否需要更新會員
//            Member member = memRepository.findById(forumPostDTO.getMemId())
//                    .orElseThrow(() -> new ResourceNotFoundException("找不到會員 ID: " + forumPostDTO.getMemId()));
//            existingPost.setMemNo(member);
//        }
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

        // 處理圖片更新邏輯：
        if (imageFile != null && !imageFile.isEmpty()) {
            // 情況 1: 有上傳新的自訂圖片
            String imageUrl = processImageFile(imageFile, "forumsys/forumpost");
            existingPost.setPostImageUrl(imageUrl);
        } else if (defaultImageUrlFromFrontend != null && !defaultImageUrlFromFrontend.isEmpty()) {
            // 情況 2: 沒有上傳自訂圖片，但前端傳來了預設圖片的 URL (表示選了「使用預設圖片」)
            existingPost.setPostImageUrl(defaultImageUrlFromFrontend);
        } else {
            // 情況 3: 兩者都沒有，但可能使用者從「自訂圖片」切換回「使用預設圖片」
            // 這種情況下，如果 `defaultImageUrlFromFrontend` 為空或 null (表示前端未傳遞預設圖片 URL)
            // 且沒有上傳新圖片，則應該根據新的 `ftagNoId` 再次獲取預設圖片。
            // 否則，如果前端明確傳遞了空字串，可能意味著清除圖片。
            // 為了簡化，如果沒有自訂圖片也沒有明確指定預設圖片URL，就根據新的 ftagNoId 獲取預設圖片作為備用方案。
            // 這也處理了使用者從「自訂圖片」模式切換回「使用預設圖片」模式，但未傳遞 defaultImageUrl 的情況
            existingPost.setPostImageUrl(getCategoryDefaultImageUrl(forumPostDTO.getFtagNoId())); // `forumPostDTO` 不可變，理由同上
        }

        ForumPost updatedPost = forumPostRepository.save(existingPost);
        return new ForumPostDTO(updatedPost);
    }

    /**
     * 刪除文章。
     *
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
     *
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
     *
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
     *
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
     *
     * @param postId  文章 ID。
     * @param forumId 討論區 ID。
     * @return 包含 ForumPostDTO 的 Optional。
     */
    @Transactional(readOnly = true)
    public Optional<ForumPostDTO> getPostByIdAndForumId(Integer postId, Integer forumId) { // 方法名稱 `getPostByIdAndForumId` 可變
        return forumPostRepository.findByIdAndForNoId(postId, forumId)
                .map(ForumPostDTO::new);
    }

    /**
     * 獲取所有討論區的 ID 和名稱列表 (用於前端篩選)。
     *
     * @return 包含討論區 ID 和名稱的 Map 列表。
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllForumsForFilter() { // 【新增】方法
        return forumRepository.findAll().stream()
                .map(forum -> {
                    Map<String, Object> forumMap = new HashMap<>();
                    forumMap.put("id", forum.getId());
                    forumMap.put("name", forum.getForName());
                    return forumMap;
                })
                .collect(Collectors.toList());
    }

    /**
     * 內部方法：處理圖片檔案的儲存。
     *
     * @param imageFile    上傳的圖片檔案。
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
}