package com.pixeltribe.forumsys.forumtag.controller; // 確保這個 package 名稱正確

import com.pixeltribe.forumsys.forumtag.model.ForumTagDTO;    // 確保有這個導入，你的 DTO
import com.pixeltribe.forumsys.forumtag.model.ForumTagService; // 確保有這個導入，你的 Service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;             // 確保有這個導入
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController; // 確保有這個導入

import java.util.List;

@RestController // 標註這是一個 RESTful API 控制器，會自動將方法返回值序列化為 JSON/XML
@RequestMapping("/api/forumtags") // 【重要】這裡的基礎路徑要與前端請求的 `/api/forumtags` 相符
public class ForumTagController {

    private final ForumTagService forumTagService; // 【私有 final 屬性】用於依賴注入

    // 【建構子注入】 Spring 會自動找到 ForumTagService 的實例並注入
    @Autowired
    public ForumTagController(ForumTagService forumTagService) {
        this.forumTagService = forumTagService;
    }

    /**
     * 處理獲取所有文章類別的 GET 請求。
     * 對應前端的 `GET http://localhost:8080/api/forumtags`
     *
     * @return 包含 ForumTagDTO 列表的 ResponseEntity，如果成功則返回 200 OK
     */
    @GetMapping // 【重要】當請求到 `/api/forumtags` (即基礎路徑) 時，會調用這個方法
    public ResponseEntity<List<ForumTagDTO>> getAllForumTags() { // `getAllForumTags` 是方法名，可變
        List<ForumTagDTO> tags = forumTagService.getAllForumTags(); // 調用 Service 層獲取所有 ForumTag DTOs
        return ResponseEntity.ok(tags); // 返回 200 OK 狀態碼和標籤列表
    }

    // 如果你有其他與 ForumTag 相關的 API，例如根據 ID 獲取，可以繼續在這裡添加方法
    // @GetMapping("/{id}")
    // public ResponseEntity<ForumTagDTO> getForumTagById(@PathVariable Integer id) { ... }

    // @PostMapping
    // public ResponseEntity<ForumTagDTO> addForumTag(@RequestBody ForumTagDTO tagDTO) { ... }
}
