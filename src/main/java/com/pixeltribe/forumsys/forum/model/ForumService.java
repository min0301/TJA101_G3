package com.pixeltribe.forumsys.forum.model;

import com.pixeltribe.forumsys.exception.ConflictException;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
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
    private final RedisTemplate<String, Object> redisTemplate;
    //    註解掉kafka，避免錯誤
//    private final KafkaTemplate<String, Integer> kafkaTemplate;

    public ForumService(ForumRepository forumRepository,
                        ForumCategoryRepository forumCategoryRepository,
                        RedisTemplate<String, Object> redisTemplate,
                        //    註解掉kafka，避免錯誤
//                        KafkaTemplate<String, Integer> kafkaTemplate,
                        ForumCollectRepository forumCollectRepository
    ) {

        this.forumRepository = forumRepository;
        this.forumCategoryRepository = forumCategoryRepository;
        this.forumCollectRepository = forumCollectRepository;
        this.redisTemplate = redisTemplate;
        //    註解掉kafka，避免錯誤
//        this.kafkaTemplate = kafkaTemplate;
    }


    private static final String HOT_FORUMS_KEY = "forums:hot";
    //    註解掉kafka，避免錯誤
//    private static final String FORUM_CATEGORY_UPDATE_TOPIC = "forum-category-update-topic";

    @Value("${file.upload-dir}")
    private String uploadDir;
    @Value("${file.base-url}")
    private String baseUrl;

    @Transactional
    public ForumDetailDTO add(ForumUpdateDTO forumUpdateDTO, MultipartFile imageFile) {

        forumRepository.findByForName(forumUpdateDTO.getForName())
                .ifPresent(existingForum -> {
                    throw new ConflictException("討論區名稱：" + existingForum.getForName() + " 已經存在");
                });

        Forum forum = new Forum();
        Forum saveOrUpdateForum = saveOrUpdateForum(forum, forumUpdateDTO, imageFile);

        this.refreshHotForumsInRedis();
        //    註解掉kafka，避免錯誤
//        kafkaTemplate.send(FORUM_CATEGORY_UPDATE_TOPIC, saveOrUpdateForum.getCatNo().getId());

        return convertToForumDetailDTO(saveOrUpdateForum);
    }

    @Transactional
    public ForumDetailDTO update(Integer forNo, ForumUpdateDTO forumUpdateDTO, MultipartFile imageFile) {

        Forum forum = forumRepository.findById(forNo)
                .orElseThrow(() -> new ResourceNotFoundException("找不到討論區編號: " + forNo));
        //    註解掉kafka，避免錯誤
//        Integer oldCategoryId = forum.getCatNo().getId();
//        if (!oldCategoryId.equals(forumUpdateDTO.getCategoryId())) {
//            kafkaTemplate.send(FORUM_CATEGORY_UPDATE_TOPIC, oldCategoryId);
//        }

        Forum saveOrUpdateForum = saveOrUpdateForum(forum, forumUpdateDTO, imageFile);
        this.refreshHotForumsInRedis();
        //    註解掉kafka，避免錯誤
//        kafkaTemplate.send(FORUM_CATEGORY_UPDATE_TOPIC, saveOrUpdateForum.getCatNo().getId());
        return convertToForumDetailDTO(saveOrUpdateForum);
    }


    public List<ForumDetailDTO> getAllForum() {

        List<Forum> forums = forumRepository.findAllByForStatusOrderByForUpdateDesc('0');
        return forums.stream()
                .map(ForumDetailDTO::convertToForumDetailDTO)
                .toList();
    }

    public List<ForumDetailDTO> getAllAdminForum() {

        List<Forum> forums = forumRepository.findAllByOrderByForUpdateDesc();

        return forums.stream()
                .map(x -> convertToForumDetailDTO(x))
                .toList();
    }

    @Cacheable(value = "forumsByCategory", key = "#catNO")
    public List<ForumDetailDTO> getForumsByCategory(Integer catNO) {
        List<Forum> forums = forumRepository.findByCatNo_Id(catNO);

        return forums.stream()
                .map(ForumDetailDTO::convertToForumDetailDTO)
                .toList();
    }

    public ForumDetailDTO getOneForum(Integer forNo, MemberDetails currentUser) {


        Forum forum = forumRepository.findById(forNo)
                .orElseThrow(() -> new ResourceNotFoundException("找不到討論區編號: " + forNo));

        ForumDetailDTO dto = convertToForumDetailDTO(forum);

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


        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String originalFilename = imageFile.getOriginalFilename();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
                Path uploadPath = Paths.get(uploadDir + "/forumsys/forum");
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Path filePath = uploadPath.resolve(uniqueFilename);
                try (InputStream inputStream = imageFile.getInputStream()) {
                    Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
                String imageUrl = baseUrl + "/uploads/forumsys/forum/" + uniqueFilename;
                forum.setForImgUrl(imageUrl);

            } catch (IOException e) {
                throw new FileStorageException("檔案儲存失敗，無法寫入目標路徑。", e);
            }
        }
        return forumRepository.save(forum);

    }

    public List<ForumDetailDTO> getHotForumsRedis() {
        List<ForumDetailDTO> hotForums = (List<ForumDetailDTO>) redisTemplate.opsForValue().get(HOT_FORUMS_KEY);
        return hotForums != null ? hotForums : List.of();
    }

    @Scheduled(fixedRate = 3_600_000)
    @Transactional
    public void refreshHotForumsInRedis() {
        List<Forum> forums = forumRepository.findAllByForStatusOrderByForUpdateDesc('0');
        if (forums.isEmpty()) {
            redisTemplate.opsForValue().set(HOT_FORUMS_KEY, List.of(), 2, TimeUnit.HOURS);
            return;
        }
        Instant since = Instant.now().minus(30, ChronoUnit.DAYS);

        Map<Integer, Long> forumHotMap = forumRepository.findForumHotSince(since).stream()
                .collect(Collectors.toMap(
                        row -> (Integer) row[0],
                        row -> (Long) row[1]
                ));

        List<ForumDetailDTO> hotForumDTOs = forums.stream()
                .map(forum -> {
                    ForumDetailDTO dto = convertToForumDetailDTO(forum);
                    Long hotScore = forumHotMap.get(forum.getId());
                    if (hotScore != null) {
                        dto.setHotScore(hotScore);
                    } else {
                        dto.setHotScore(0L);
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        hotForumDTOs.sort(Comparator.comparing(ForumDetailDTO::getHotScore).reversed());

        redisTemplate.opsForValue().set(HOT_FORUMS_KEY, hotForumDTOs, 2, TimeUnit.HOURS);

    }

    public List<ForumDetailDTO> searchForumsByKeyword(String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return Collections.emptyList();
        }

        List<Forum> searchForum = forumRepository.searchForumsByKeyword(keyword);
        if (searchForum.isEmpty()) {
            return List.of();
        }

        List<Forum> forums = searchForum;
        return forums.stream()
                .map(ForumDetailDTO::convertToForumDetailDTO)
                .collect(Collectors.toList());
    }

    public Long getForumCount() {
        return forumRepository.count();
    }


}
