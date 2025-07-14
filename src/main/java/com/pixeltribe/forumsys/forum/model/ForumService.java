package com.pixeltribe.forumsys.forum.model;

import com.pixeltribe.forumsys.exception.FileStorageException;
import com.pixeltribe.forumsys.exception.ResourceNotFoundException;
import com.pixeltribe.forumsys.forumcategory.model.ForumCategory;
import com.pixeltribe.forumsys.forumcategory.model.ForumCategoryRepository;
import com.pixeltribe.forumsys.forumcollect.model.ForumCollect;
import com.pixeltribe.forumsys.forumcollect.model.ForumCollectRepository;
import com.pixeltribe.forumsys.shared.CollectStatus;
import com.pixeltribe.membersys.member.model.Member;
import com.pixeltribe.membersys.security.MemberDetails;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.pixeltribe.forumsys.forum.model.ForumDetailDTO.convertToForumDetailDTO;

@Service("forumService")
public class ForumService {


    private final ForumRepository forumRepository;
    private final ForumCategoryRepository forumCategoryRepository;
    private final ForumCollectRepository forumCollectRepository;

    public ForumService(ForumRepository forumRepository, ForumCategoryRepository forumCategoryRepository, ForumCollectRepository forumCollectRepository) {

        this.forumRepository = forumRepository;
        this.forumCategoryRepository = forumCategoryRepository;
        this.forumCollectRepository = forumCollectRepository;
    }

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String HOT_FORUMS_KEY = "forums:hot";


    @Value("${file.upload-dir}")
    private String uploadDir;
    @Value("${file.base-url}")
    private String baseUrl;

    @Transactional
    public ForumDetailDTO add(ForumUpdateDTO forumUpdateDTO, MultipartFile imageFile) {

        Forum forum = new Forum();
        Forum saveOrUpdateForum = saveOrUpdateForum(forum, forumUpdateDTO, imageFile);

        this.refreshHotForumsInRedis();

        return convertToForumDetailDTO(saveOrUpdateForum);
    }

    @Transactional
    public ForumDetailDTO update(Integer forNo, ForumUpdateDTO forumUpdateDTO, MultipartFile imageFile) {

        Forum forum = forumRepository.findById(forNo)
                .orElseThrow(() -> new ResourceNotFoundException("找不到討論區編號: " + forNo));
        Forum saveOrUpdateForum = saveOrUpdateForum(forum, forumUpdateDTO, imageFile);
        this.refreshHotForumsInRedis();
        return convertToForumDetailDTO(saveOrUpdateForum);
    }


    public List<ForumDetailDTO> getAllForum() {

        // 1. 從資料庫取得原始的 Entity 列表，
        // 討論區狀態為'0'(正常)
        List<Forum> forums = forumRepository.findAllByForStatusOrderByForUpdateDesc('0');

//  TODO
//        for (Forum x : forums) {
//          ForumDetailDTO.convertToForumDetailDTO(x);
//        }

        // 2. 使用 Stream API 將 List<Forum> 轉換為 List<ForumDetailDTO>
        return forums.stream()
                .map(x -> convertToForumDetailDTO(x))
                //  TODO
                //      .map(ForumDetailDTO::convertToForumDetailDTO) // 對每個 forum 執行轉換
                .toList();
    }

    public List<ForumDetailDTO> getAllAdminForum() {

        List<Forum> forums = forumRepository.findAllByOrderByForUpdateDesc();

        return forums.stream()
                .map(x -> convertToForumDetailDTO(x))
                .toList();
    }


    public List<ForumDetailDTO> getForumsByCategory(Integer catNO) {
        List<Forum> forums = forumRepository.findByCatNo_Id(catNO);

        return forums.stream()
                .map(ForumDetailDTO::convertToForumDetailDTO) // 對每個 forum 執行轉換
                .toList();
    }

    public ForumDetailDTO getOneForum(Integer forNo, MemberDetails currentUser) {


        Forum forum = forumRepository.findById(forNo)
                .orElseThrow(() -> new ResourceNotFoundException("找不到討論區編號: " + forNo));

        ForumDetailDTO dto = convertToForumDetailDTO(forum);

        // 2. 如果使用者未登入，isCollected 直接為 false
        if (currentUser == null) {
            dto.setCollected(false);
            return dto;
        }

        Integer memberId = currentUser.getMemberId();

        Member member = new Member();
        member.setId(memberId);
        Optional<ForumCollect> existingCollectOpt = forumCollectRepository.findByForNoAndMemNo(forum, member);

        if (existingCollectOpt.isPresent() && existingCollectOpt.get().getCollectStatus() == CollectStatus.COLLECT) {
            dto.setCollected(true);
        } else {
            dto.setCollected(false);
        }
        return dto;
    }


    private Forum saveOrUpdateForum(Forum forum, ForumUpdateDTO forumUpdateDTO, MultipartFile imageFile) {

        forum.setForName(forumUpdateDTO.getForName());
        forum.setForDes(forumUpdateDTO.getForDes());
        forum.setForStatus(forumUpdateDTO.getForStatus());

        ForumCategory category = forumCategoryRepository.findById(forumUpdateDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("找不到討論區類別"));
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
                throw new FileStorageException("檔案儲存失敗，無法寫入目標路徑。", e);
            }
        }
        // 6. 將包含 imageURL 的 forum 物件存入資料庫
        return forumRepository.save(forum);

    }


    public List<ForumDetailDTO> getHotForums() {
        List<Forum> forums = forumRepository.findAllByForStatusOrderByForUpdateDesc('0');
        if (forums.isEmpty()) {
            return List.of();
        }
        // 計算查詢起始時間（n天前）
        Instant since = Instant.now().minus(30, ChronoUnit.DAYS);

        Map<Integer, Object[]> forumHotMap = forumRepository.findForumHotSince(since).stream()
                .collect(Collectors.toMap(
                        row -> (Integer) row[0], // Key: Forum ID
                        row -> new Object[]{row[1], row[2]} // Value: [留言數, 最後留言時間]
                ));
        List<ForumDetailDTO> hotForumDTOs = forums.stream()
                .map(forum -> {
                    ForumDetailDTO dto = convertToForumDetailDTO(forum);
                    Object[] stats = forumHotMap.get(forum.getId());
                    if (stats != null) {
                        // 如果找到了，表示n天內有留言
                        dto.setHotScore((Long) stats[0]);
                        dto.setLastMessageTime((Instant) stats[1]);
                    } else {
                        // 如果沒找到，表示n天內沒有留言，都設為預設值
                        dto.setHotScore(0L);
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        hotForumDTOs.sort(Comparator.comparing(ForumDetailDTO::getHotScore).reversed());
        return hotForumDTOs;

    }

    public List<ForumDetailDTO> getHotForumsRedis() {
        List<ForumDetailDTO> hotForums = (List<ForumDetailDTO>) redisTemplate.opsForValue().get(HOT_FORUMS_KEY);
        return hotForums != null ? hotForums : List.of();
    }

    @Scheduled(fixedRate = 3_600_000)
    @Transactional
    public void refreshHotForumsInRedis() {
//        System.out.println("====== 開始更新熱門看板快取 ======");
        List<Forum> forums = forumRepository.findAllByForStatusOrderByForUpdateDesc('0');
        if (forums.isEmpty()) {
            // 如果沒有任何討論區，也更新一個空列表到快取，避免前端拿到舊資料
            redisTemplate.opsForValue().set(HOT_FORUMS_KEY, List.of(), 2, TimeUnit.HOURS);
//            System.out.println("====== 熱門看板快取更新完成（無資料） ======");
            return;
        }
        Instant since = Instant.now().minus(30, ChronoUnit.DAYS);

        Map<Integer, Object[]> forumHotMap = forumRepository.findForumHotSince(since).stream()
                .collect(Collectors.toMap(
                        row -> (Integer) row[0],
                        row -> new Object[]{row[1], row[2]}
                ));

        List<ForumDetailDTO> hotForumDTOs = forums.stream()
                .map(forum -> {
                    ForumDetailDTO dto = convertToForumDetailDTO(forum);
                    Object[] stats = forumHotMap.get(forum.getId());
                    if (stats != null) {
                        dto.setHotScore((Long) stats[0]); // 注意：COUNT(id) 在 JPA 中通常返回 Long
                        dto.setLastMessageTime((Instant) stats[1]);
                    } else {
                        dto.setHotScore(0L);
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        hotForumDTOs.sort(Comparator.comparing(ForumDetailDTO::getHotScore).reversed());

        // --- 最重要的一步：將計算好的結果存入 Redis ---
        // 方法 'opsForValue().set()': 不可變，是 RedisTemplate 的 API。
        // 我們設定 2 小時過期，比定時任務的 1 小時長，確保資料不會在更新前就失效。
        redisTemplate.opsForValue().set(HOT_FORUMS_KEY, hotForumDTOs, 2, TimeUnit.HOURS);

//        System.out.println("====== 熱門看板快取更新完成 ======");
    }


}
