package com.pixeltribe.forumsys.forumpost.controller;

import com.pixeltribe.forumsys.forumpost.model.ForumPost;
import com.pixeltribe.forumsys.forumpost.model.ForumPostService;
import com.pixeltribe.forumsys.forumpost.model.ForumPostDTO; // **確保導入**

// **注意：以下 Service 如果在 Controller 的其他方法中沒有被直接使用，可以移除注入**
// 但為了 insert 方法，它們的注入通常是必要的。
import com.pixeltribe.forumsys.forum.model.ForumService;
//import com.pixeltribe.forumsys.forumtag.model.ForumTagService;
//import com.pixeltribe.membersys.service.MemberService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:8080"}, allowedHeaders = "*") // 如果有 CORS 需求，解除註釋
public class ForumPostController {

    private final ForumPostService forumPostSvc;
    private final ForumService forumService;
    //private final ForumTagService forumTagService;
    //private final MemberService memberService;

    // **建構子注入**
    @Autowired
    public ForumPostController(ForumPostService forumPostSvc, ForumService forumService)
    {this.forumPostSvc = forumPostSvc;this.forumService = forumService;}
//    ForumTagService forumTagService,
//    MemberService memberService
//     this.forumTagService = forumTagService;
//     this.memberService = memberService;

    // --- API 端點 ---

    // 1. 查詢所有文章 (返回 DTO 列表)
    // URL: /api/forumpost/allForumPost
    @GetMapping("/forumpost/allForumPost")
    public ResponseEntity<List<ForumPostDTO>> listAllForumPosts() { // 返回 List<ForumPostDTO>
        List<ForumPostDTO> forumPosts = forumPostSvc.getAllForumPost();
        return ResponseEntity.ok(forumPosts);
    }

    // 2. 查詢特定討論區的文章列表 (顯示所有欄位，返回 DTO 列表)
    // URL: /api/forum/{forNo}/posts
    @GetMapping("/forum/{forNo}/posts")
    public ResponseEntity<List<ForumPostDTO>> findByForum(@PathVariable("forNo") Integer forNo) { // 返回 List<ForumPostDTO>
        List<ForumPostDTO> posts = forumPostSvc.getPostsByForumId(forNo);
        return ResponseEntity.ok(posts);
    }

    // 3. 查詢特定討論區的文章數量
    // URL: /api/forum/{forNo}/posts/count
//    @GetMapping("/forum/{forNo}/posts/count")
//    public ResponseEntity<Long> countPostsByForum(@PathVariable("forNo") Integer forNo) {
//        long count = forumPostSvc.countPostsByForumId(forNo);
//        return ResponseEntity.ok(count);
//    }

    // 4. 查詢特定討論區下的特定文章 (根據文章 ID 和討論區 ID，返回 ForumPostDTO)
    // URL: /api/forum/{forNo}/posts/{postId}
//    @GetMapping("/forum/{forNo}/posts/{postId}")
//    public ResponseEntity<ForumPostDTO> getPostInForum(@PathVariable("forNo") Integer forNo,
//                                                       @PathVariable("postId") Integer postId) {
//        Optional<ForumPostDTO> postOptional = forumPostSvc.getPostByIdAndForumId(postId, forNo);
//        if (postOptional.isPresent()) {
//            return ResponseEntity.ok(postOptional.get());
//        } else {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//    }

    // 5. 新增文章 (保持現有邏輯)
    @PostMapping(value = "/forumpost/insert", consumes = {"multipart/form-data"})
    public ResponseEntity<Map<String, Object>> insert(
            @RequestParam(name = "forNoId") @NotNull(message = "討論區編號: 請選擇一個討論區編號") Integer forNoId,
            @RequestParam(name = "ftagNoId") @NotNull(message = "類別編號: 請選擇您的類別") Integer ftagNoId,
            @RequestParam(name = "postTitle") @NotEmpty(message = "文章標題: 請勿空白") @Size(max = 50, message = "文章標題長度不能超過50") String postTitle,
            @RequestParam(name = "postCon") @NotEmpty(message = "文章內容: 請勿空白(最少十個字) ") @Size(min = 10, max = 5000, message = "文章內容長度必需在10到5000之間") String postCon,
            @RequestParam(name = "postPin", required = false) Character postPin,
            @RequestParam(name = "postStatus", required = false) Character postStatus,
            @RequestParam(name = "mesNumbers", required = false) Integer mesNumbers,
            @RequestParam(name = "postLikeCount", required = false) Integer postLikeCount,
            @RequestParam(name = "postLikeDlc", required = false) Integer postLikeDlc,
            @RequestParam(name = "postCoverImageFile", required = false) Optional<MultipartFile> postCoverImageFile
    ) throws IOException {
        Integer memNoId = 1; // **臨時設置為1，請替換為實際登入會員的ID獲取邏輯**

        Map<String, Object> errors = new HashMap<>();
        if (postTitle == null || postTitle.isEmpty() || postTitle.length() > 50) {
            errors.put("postTitle", "文章標題: 請勿空白且長度不能超過50");
        }
        if (postCon == null || postCon.isEmpty() || postCon.length() < 10 || postCon.length() > 5000) {
            errors.put("postCon", "文章內容: 請勿空白(最少十個字) ");
        }
        if (forNoId == null) { errors.put("forNoId", "討論區編號: 請選擇一個討論區編號"); }
        if (ftagNoId == null) { errors.put("ftagNoId", "類別編號: 請選擇您的類別"); }

        if (!errors.isEmpty()) {
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }

        ForumPost forumPost = new ForumPost();
        forumPost.setPostTitle(postTitle);
        forumPost.setPostCon(postCon);
        forumPost.setPostCrdate(Instant.now());
        forumPost.setPostUpdate(Instant.now());
        forumPost.setPostPin(postPin != null ? postPin : '0');
        forumPost.setPostStatus(postStatus != null ? postStatus : '0');
        forumPost.setMesNumbers(mesNumbers != null ? mesNumbers : 0);
        forumPost.setPostLikeCount(postLikeCount != null ? postLikeCount : 0);
        forumPost.setPostLikeDlc(postLikeDlc != null ? postLikeDlc : 0);

//        Forum selectedForum = forumService.getForumById(forNoId)
//                .orElseThrow(() -> new IllegalArgumentException("無效的討論區編號: " + forNoId));
//        forumPost.setForNo(selectedForum);
//        ForumTag retrievedForumTag = forumTagService.getForumTagById(ftagNoId)
//                .orElseThrow(() -> new IllegalArgumentException("無效的文章類別編號: " + ftagNoId));
//        forumPost.setFtagNo(retrievedForumTag);
//        Member currentMember = memberService.getMemberById(memNoId)
//                .orElseThrow(() -> new IllegalArgumentException("會員不存在或未登入"));
//        forumPost.setMemNo(currentMember);
//        try {
//            if (postCoverImageFile.isPresent() && !postCoverImageFile.get().isEmpty()) {
//                forumPost.setPostCoverImage(postCoverImageFile.get().getBytes());
//            } else {
//                byte[] commonDefaultImageBytes = loadCommonDefaultImage();
//                forumPost.setPostCoverImage(commonDefaultImageBytes);
//            }
//        } catch (IOException e) {
//            Map<String, String> errorResponse = new HashMap<>();
//            errorResponse.put("imageError", "圖片處理失敗或預設圖片載入失敗: " + e.getMessage());
//            e.printStackTrace();
//            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
//        }

        ForumPost savedForumPost = forumPostSvc.add(forumPost);

        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("message", "文章新增成功");
        successResponse.put("forumPostId", savedForumPost.getId());
        successResponse.put("forumPost", savedForumPost);
        return new ResponseEntity<>(successResponse, HttpStatus.CREATED);
    }

    // 6. 獲取當前登入會員的所有文章
//    @GetMapping("/member/me/posts")
//    public ResponseEntity<Map<String, Object>> getMyPosts() {
//        Map<String, Object> response = new HashMap<>();
//        try {
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            if (authentication == null || !authentication.isAuthenticated()) {
//                response.put("message", "用戶未登入或認證失敗");
//                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
//            }
//            String username = authentication.getName();
//            Member currentMember = memberService.getMemberByAccount(username)
//                    .orElseThrow(() -> new IllegalArgumentException("無法找到對應的會員資訊"));
//
//            List<ForumPostDTO> myPosts = forumPostSvc.getMyPosts(currentMember.getId()); // 返回 DTO 列表
//            response.put("message", "成功獲取我的文章列表");
//            response.put("myPosts", myPosts);
//            return new ResponseEntity<>(response, HttpStatus.OK);
//
//        } catch (IllegalArgumentException e) {
//            response.put("message", e.getMessage());
//            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//        } catch (Exception e) {
//            response.put("message", "獲取文章列表失敗: " + e.getMessage());
//            e.printStackTrace();
//            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

//    // 7. 獲取文章封面圖片的 API
//    @GetMapping("/forumpost/image/{id}")
//    public ResponseEntity<byte[]> getForumPostImage(@PathVariable Integer id) {
//        Optional<ForumPost> forumPostOptional = forumPostSvc.getOneForumpost(id);
//
//        if (forumPostOptional.isPresent() && forumPostOptional.get().getPostCoverImage() != null) {
//            byte[] imageBytes = forumPostOptional.get().getPostCoverImage();
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.IMAGE_PNG);
//            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
//        } else {
//            try {
//                byte[] commonDefaultImageBytes = loadCommonDefaultImage();
//                HttpHeaders headers = new HttpHeaders();
//                headers.setContentType(MediaType.IMAGE_PNG);
//                return new ResponseEntity<>(commonDefaultImageBytes, headers, HttpStatus.OK);
//            } catch (IOException e) {
//                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//            }
//        }
//    }
//
//    // 8. 獲取 ForumTag 預設圖片的 API
//    @GetMapping("/forumtag/default-image/{tagId}")
//    public ResponseEntity<byte[]> getForumTagDefaultImage(@PathVariable Integer tagId) {
//        Optional<ForumTag> forumTagOptional = forumTagService.getForumTagById(tagId);
//
//        if (forumTagOptional.isPresent() && forumTagOptional.get().getDefaultImageBlob() != null) {
//            byte[] imageBytes = forumTagOptional.get().getDefaultImageBlob();
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.IMAGE_PNG);
//            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
//        } else {
//            try {
//                byte[] commonDefaultImageBytes = loadCommonDefaultImage();
//                HttpHeaders headers = new HttpHeaders();
//                headers.setContentType(MediaType.IMAGE_PNG);
//                return new ResponseEntity<>(commonDefaultImageBytes, headers, HttpStatus.OK);
//            } catch (IOException e) {
//                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//            }
//        }
//    }

    // 輔助方法：載入通用預設圖片的位元組陣列
    private static final String COMMON_DEFAULT_IMAGE_PATH = "static/images/common_default_cover.png";

    private byte[] loadCommonDefaultImage() throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(COMMON_DEFAULT_IMAGE_PATH)) {
            if (is == null) {
                throw new IOException("通用預設封面圖片未找到: " + COMMON_DEFAULT_IMAGE_PATH + ". 請確認圖片存在於 src/main/resources/static/images/");
            }
            return is.readAllBytes();
        }
    }


}