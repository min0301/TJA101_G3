package com.pixeltribe.forumsys.forumtag.controller;

import com.pixeltribe.forumsys.forumtag.model.ForumTagDTO;
import com.pixeltribe.forumsys.forumtag.model.ForumTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ForumTagController {

    private final ForumTagService forumTagService;

    @Autowired
    public ForumTagController(ForumTagService forumTagService) {
        this.forumTagService = forumTagService;
    }

    @GetMapping("/forumtag")
    public ResponseEntity<List<ForumTagDTO>> getAllForumTags() {
        List<ForumTagDTO> tags = forumTagService.getAllForumTags();
        return ResponseEntity.ok(tags);
    }

    /**
     * @description 處理獲取特定文章類別「預設文章封面圖片 URL」的 GET 請求。
     * 這個 API 返回的是 URL 字串，而不是圖片二進位資料。
     * 對應前端的 `GET /api/forumtag/default-image/{ftagNoId}`
     *
     * @param ftagNoId 文章類別 ID
     * @return 預設圖片的 URL 字串，如果找不到則返回 404 Not Found。
     */
    @GetMapping("/forumtag/default-image/{ftagNoId}") // 【不可變】路徑需與前端呼叫一致
    public ResponseEntity<String> getDefaultImageUrlByTagId(@PathVariable Integer ftagNoId) { // `ftagNoId` 參數名可變
        String defaultImageUrl = forumTagService.getDefaultImageUrl(ftagNoId); // 調用 Service 層獲取預設圖片 URL
        if (defaultImageUrl != null && !defaultImageUrl.isEmpty()) {
            return ResponseEntity.ok(defaultImageUrl); // 返回 200 OK 和 URL 字串
        } else {
            return ResponseEntity.notFound().build(); // 返回 404 Not Found
        }
    }

    // 您之前提到的 getForumTagById 方法，如果需要可以保留，它返回的是 ForumTagDTO
    @GetMapping("/forumtag/{id}")
    public ResponseEntity<ForumTagDTO> getForumTagById(@PathVariable("id") Integer id) {
        ForumTagDTO tagDTO = forumTagService.getForumTagById(id);
        if (tagDTO != null) {
            return ResponseEntity.ok(tagDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}