package com.pixeltribe.forumsys.forumpost.controller;

import com.pixeltribe.forumsys.exception.ResourceNotFoundException;
import com.pixeltribe.forumsys.forumpost.model.ForumPostDTO;
import com.pixeltribe.forumsys.forumpost.model.ForumPostService;
import com.pixeltribe.forumsys.forumpost.model.ForumPostUpdateDTO;
import com.pixeltribe.forumsys.postcollect.model.PostCollectService;
import com.pixeltribe.membersys.security.MemberDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ForumPostController {

    private final ForumPostService forumPostSvc;
    private final PostCollectService postCollectService;

    @Autowired
    public ForumPostController(ForumPostService forumPostSvc, PostCollectService postCollectService) { // 3. 透過建構子注入
        this.forumPostSvc = forumPostSvc;
        this.postCollectService = postCollectService; // 4. 初始化
    }

    @GetMapping("/forum/{forumId}/posts/sorted")
    @Operation(summary = "查特定討論區文章並按更新時間倒序", description = "根據討論區ID查詢該討論區下的所有文章，並依更新時間倒序排列")
    public ResponseEntity<List<ForumPostDTO>> findPostsByForumSortedByUpdateDesc(
            @Parameter(description = "討論區編號", example = "1")
            @PathVariable("forumId") Integer forumId,
            @AuthenticationPrincipal MemberDetails currentUser) { // 新增 currentUser 參數

        Integer memberId = (currentUser != null) ? currentUser.getMemberId() : null; // 獲取會員ID，未登入則為 null
        List<ForumPostDTO> posts = forumPostSvc.findAllByOrderByPostUpdateDesc(forumId, memberId); // 調用服務層的方法，傳入 memberId

        if (posts.isEmpty()) {
            return ResponseEntity.ok(posts);
        }
        return ResponseEntity.ok(posts);
    }

    /**
     * 獲取單篇文章詳情
     *
     * @param id 文章編號
     * @return 文章詳情 DTO
     */
    @GetMapping("/forumpost/{id}")
    @Operation(summary = "查單一文章", description = "根據文章ID查詢單篇文章詳細資訊")
    public ResponseEntity<ForumPostDTO> getForumPostById(
            @Parameter(description = "文章編號", example = "1")
            @PathVariable("id") Integer id,
            @AuthenticationPrincipal MemberDetails currentUser) { // 新增 currentUser 參數
        Integer memberId = (currentUser != null) ? currentUser.getMemberId() : null; // 獲取會員ID，未登入則為 null
        return forumPostSvc.getForumPostDTOById(id, memberId) // 傳入 memberId
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * 獲取所有文章列表 (供一般會員查看，包含討論區與會員資訊)
     *
     * @return 所有文章的 DTO 列表
     */
    @GetMapping("/forumposts/all")
    @Operation(summary = "查所有文章 (含討論區與會員資訊)", description = "獲取所有文章的列表，包含其所屬討論區和發文會員資訊")
    public ResponseEntity<List<ForumPostDTO>> listAllForumPosts(
            @AuthenticationPrincipal MemberDetails currentUser) { // 新增 currentUser 參數
        Integer memberId = (currentUser != null) ? currentUser.getMemberId() : null; // 獲取會員ID，未登入則為 null
        List<ForumPostDTO> forumPosts = forumPostSvc.getAllForumPost(memberId); // 傳入 memberId
        return ResponseEntity.ok(forumPosts);
    }

    /**
     * 獲取特定討論區下的文章列表
     *
     * @param forNo 討論區編號
     * @return 特定討論區的文章 DTO 列表
     */
    @GetMapping("/forum/{forNo}/posts")
    @Operation(summary = "查討論區下的文章", description = "根據討論區ID查詢該討論區下的所有文章列表")
    public ResponseEntity<List<ForumPostDTO>> findPostsByForum(
            @Parameter(description = "討論區編號", example = "1")
            @PathVariable("forNo") Integer forNo,
            @AuthenticationPrincipal MemberDetails currentUser) { // 新增 currentUser 參數
        Integer memberId = (currentUser != null) ? currentUser.getMemberId() : null; // 獲取會員ID，未登入則為 null
        List<ForumPostDTO> posts = forumPostSvc.getPostsByForumId(forNo, memberId); // 傳入 memberId
        if (posts.isEmpty()) {
            return ResponseEntity.ok(posts);
        }
        return ResponseEntity.ok(posts);
    }

    /**
     * 獲取所有討論區的列表 (用於前端篩選)
     *
     * @return 所有討論區的 DTO 列表
     */
    @GetMapping("/forums/all") // 【新增】API 路徑
    @Operation(summary = "查所有討論區", description = "獲取所有討論區的列表 (用於前端篩選)")
    public ResponseEntity<List<Map<String, Object>>> listAllForums() { // 【新增】方法
        List<Map<String, Object>> forums = forumPostSvc.getAllForumsForFilter(); // 調用 Service 方法
        return ResponseEntity.ok(forums);
    }


//    /**
//     * 獲取特定討論區下的單篇文章
//     * @param forNo 討論區編號
//     * @param postId 文章編號
//     * @return 文章 DTO
//     */
//    @GetMapping("/forum/{forNo}/posts/{postId}")
//    @Operation(summary = "查討論區下的單一文章", description = "根據文章ID和討論區ID查詢特定討論區下的單篇文章")
//    public ResponseEntity<ForumPostDTO> getPostInForum(
//            @Parameter(description = "討論區編號", example = "1")
//            @PathVariable("forNo") Integer forNo,
//            @Parameter(description = "文章編號", example = "1")
//            @PathVariable("postId") Integer postId) {
//        return forumPostSvc.getPostByIdAndForNoId(postId, forNo) // 使用正確的Service方法
//                .map(ResponseEntity::ok)
//                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
//    }

    /**
     * 新增文章
     *
     * @param forumPostUpdateDTO 新增文章的 DTO
     * @param imageFile          封面圖片檔案 (可選)
     * @param result             驗證結果
     * @param defaultImageUrl    預設圖片的 URL
     * @param currentUser        當前登入會員的詳細資訊
     * @return 新增後的文章 DTO 或錯誤訊息
     */
    @PostMapping(value = "/forumpost/insert", consumes = {"multipart/form-data"})
    @Operation(summary = "新增文章", description = "會員新增文章，可包含封面圖片")
    public ResponseEntity<Map<String, Object>> insertForumPost(
            @RequestPart("forumPostUpdate") @Valid ForumPostUpdateDTO forumPostUpdateDTO,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestPart(value = "defaultImageUrl", required = false) String defaultImageUrl,
            BindingResult result,
            @AuthenticationPrincipal MemberDetails currentUser) { // <-- 確保有這個參數

        if (result.hasErrors()) {
            Map<String, Object> errors = result.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            fieldError -> fieldError.getField(),
                            fieldError -> fieldError.getDefaultMessage()
                    ));
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }
        Integer memberId = currentUser.getMemberId(); // 獲取當前登入會員的 ID
        // 檢查 currentUser 是否為 null。
        // 雖然您目前 SecurityConfig 是 permitAll，但如果後續收緊權限，這裡會需要檢查。
        if (currentUser == null) {
//            Map<String, Object> errorResponse = new HashMap<>();
//            errorResponse.put("message", "請先登入才能發文！");
//            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        try {
            // 將 memberId 物件直接傳遞給 Service 層
            ForumPostDTO savedForumPostDTO = forumPostSvc.addForumPost(memberId, forumPostUpdateDTO, imageFile, defaultImageUrl); // <-- 修正這裡，傳遞 currentUser

            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("message", "文章新增成功");
            successResponse.put("forumPostId", savedForumPostDTO.getId());
            successResponse.put("forumPost", savedForumPostDTO);
            return new ResponseEntity<>(successResponse, HttpStatus.CREATED);

        } catch (ResourceNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            errorResponse.put("details", "關聯的資料未找到 (例如討論區、文章類別或會員不存在)。");
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Internal Server Error");
            errorResponse.put("details", "系統發生未預期的錯誤，請聯繫管理員: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping(value = "/admin/forumpost/insert", consumes = {"multipart/form-data"})
    @Operation(summary = "新增文章", description = "管理員新增文章，可包含封面圖片")
    public ResponseEntity<Map<String, Object>> insertForumPost(
            @RequestPart("forumPostUpdate") @Valid ForumPostUpdateDTO forumPostUpdateDTO,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestPart(value = "defaultImageUrl", required = false) String defaultImageUrl,
            BindingResult result) { // <-- 確保有這個參數

        if (result.hasErrors()) {
            Map<String, Object> errors = result.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            fieldError -> fieldError.getField(),
                            fieldError -> fieldError.getDefaultMessage()
                    ));
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }
        Integer memberId = 1; // 獲取當前登入會員的 ID

        try {
            // 將 memberId 物件直接傳遞給 Service 層
            ForumPostDTO savedForumPostDTO = forumPostSvc.addForumPost(memberId, forumPostUpdateDTO, imageFile, defaultImageUrl); // <-- 修正這裡，傳遞 currentUser

            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("message", "文章新增成功");
            successResponse.put("forumPostId", savedForumPostDTO.getId());
            successResponse.put("forumPost", savedForumPostDTO);
            return new ResponseEntity<>(successResponse, HttpStatus.CREATED);

        } catch (ResourceNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            errorResponse.put("details", "關聯的資料未找到 (例如討論區、文章類別或會員不存在)。");
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Internal Server Error");
            errorResponse.put("details", "系統發生未預期的錯誤，請聯繫管理員: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/api/posts/{postId}/collect/status")
    @Operation(summary = "檢查文章是否已被當前會員收藏")
    public ResponseEntity<?> checkPostCollectStatus(@PathVariable Integer postId, @AuthenticationPrincipal MemberDetails currentUser) {
        if (currentUser == null) {
            return ResponseEntity.ok(Map.of("isCollected", false)); // 未登入則視為未收藏
        }
        boolean isCollected = postCollectService.isPostCollected(currentUser.getMemberId(), postId);
        return ResponseEntity.ok(Map.of("isCollected", isCollected));
    }

    /**
     * 更新文章
     *
     * @param forNo              討論區編號
     * @param postId             文章編號
     * @param forumPostUpdateDTO 更新文章的 DTO
     * @param imageFile          封面圖片檔案 (可選)
     * @param result             驗證結果
     * @return 更新後的文章 DTO 或錯誤訊息
     */
    @PutMapping(value = "/forum/{forNo}/posts/{postId}", consumes = {"multipart/form-data"}) // 修改路徑，同時包含 forNo 和 postId
    @Operation(summary = "修改文章", description = "會員修改指定討論區下的文章，可更新封面圖片")
    public ResponseEntity<Map<String, Object>> updateForumPost(
            @Parameter(description = "討論區編號", example = "1")
            @PathVariable Integer forNo, // 新增 forNo 參數
            @Parameter(description = "文章編號", example = "1")
            @PathVariable Integer postId,
            @RequestPart("forumPostUpdate") @Valid ForumPostUpdateDTO forumPostUpdateDTO,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestPart(value = "defaultImageUrl", required = false) String defaultImageUrl,
            BindingResult result) {

        if (result.hasErrors()) {
            Map<String, Object> errors = result.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            fieldError -> fieldError.getField(),
                            fieldError -> fieldError.getDefaultMessage()
                    ));
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }

        try {
            // Service 方法需要增加 forNo 參數進行額外驗證
            ForumPostDTO updatedPostDTO = forumPostSvc.updateForumPost(postId, forNo, forumPostUpdateDTO, imageFile, defaultImageUrl); // 修改調用 Service 方法
            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("message", "文章更新成功");
            successResponse.put("forumPost", updatedPostDTO);
            return ResponseEntity.ok(successResponse);

        } catch (ResourceNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Internal Server Error");
            errorResponse.put("details", "系統發生未預期的錯誤，請聯繫管理員: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 刪除文章
     *
     * @param forNo  討論區編號
     * @param postId 文章編號
     * @return 成功訊息
     */
//    @DeleteMapping("/forum/{forNo}/posts/{postId}") // 修改路徑，同時包含 forNo 和 postId
//    @Operation(summary = "刪除文章", description = "會員刪除指定討論區下的文章")
//    public ResponseEntity<Map<String, Object>> deleteForumPost(
//            @Parameter(description = "討論區編號", example = "1")
//            @PathVariable Integer forNo, // 新增 forNo 參數
//            @Parameter(description = "文章編號", example = "1")
//            @PathVariable Integer postId) {
//        try {
//            // Service 方法需要增加 forNo 參數進行額外驗證
//            forumPostSvc.deleteForumPost(forNo, postId); // 修改調用 Service 方法
//            Map<String, Object> response = new HashMap<>();
//            response.put("message", "文章刪除成功，ID: " + postId + "，所屬討論區 ID: " + forNo);
//            return ResponseEntity.ok(response);
//        } catch (ResourceNotFoundException e) {
//            Map<String, Object> errorResponse = new HashMap<>();
//            errorResponse.put("message", e.getMessage());
//            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
//        } catch (Exception e) {
//            Map<String, Object> errorResponse = new HashMap<>();
//            errorResponse.put("message", "Internal Server Error");
//            errorResponse.put("details", "刪除文章時發生錯誤: " + e.getMessage());
//            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
    @GetMapping("admin/forumpost/count")
    @Operation(summary = "取得討論區文章數量")
    public Long getPostCount() {
        return forumPostSvc.getPostCount();
    }

}