package com.pixeltribe.forumsys.message.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pixeltribe.forumsys.message.model.ForumMesDTO;
import com.pixeltribe.forumsys.message.model.ForumMesService;
import com.pixeltribe.forumsys.message.model.ForumMesUpdateDTO;
import com.pixeltribe.membersys.security.MemberDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ForumMesController {

    private final ForumMesService forumMesSvc;
    // 注入 RedisTemplate 和 ObjectMapper
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    // Redis 佇列的 Key
    private static final String MESSAGE_QUEUE_KEY = "forum:message:queue";

    public ForumMesController(ForumMesService forumMesSvc, RedisTemplate<String, String> redisTemplate) {
        this.forumMesSvc = forumMesSvc;
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/posts/message")
    @Operation(
            summary = "查所有開放的留言"
    )
    public List<ForumMesDTO> getAllForumMes() {

        return forumMesSvc.getAllForumMes();
    }

    @GetMapping("/admin/posts/message")
    @Operation(
            summary = "查所有的留言"
    )
    public List<ForumMesDTO> getAllAdminForumMes() {

        return forumMesSvc.getAllAdminForumMes();
    }

    @GetMapping("/posts/{mesno}")
    @Operation(
            summary = "查單一則留言"
    )
    public ForumMesDTO findOneForumMes(
            @Parameter(description = "留言編號", example = "1")
            @PathVariable("mesno") Integer mesNo) {
        return forumMesSvc.getOneForumMes(mesNo);
    }

    @GetMapping("/posts/{postno}/messages")
    @Operation(
            summary = "查單一文章公開留言list"
    )
    public List<ForumMesDTO> getForumMesByPost(
            @PathVariable("postno") Integer postNo) {
        return forumMesSvc.getForumMesByPost(postNo);
    }


    //    @PostMapping("/posts/{postno}/messages/")
//    @Operation(
//            summary = "新增文章留言"
//    )
    public ResponseEntity<ForumMesDTO> addForumMes(
            @Valid @RequestBody ForumMesUpdateDTO forumMesUpdateDTO,
            @PathVariable("postno") Integer postNo,
            //1.在方法參數中，使用 @AuthenticationPrincipal 注入當前用戶物件
            @AuthenticationPrincipal MemberDetails currentUser) {
        //2.直接從 currentUser 物件呼叫 getMemberId() 方法
        Integer memberId = currentUser.getMemberId();
        ForumMesDTO createdForumMes = forumMesSvc.addForumMes(postNo, memberId, forumMesUpdateDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdForumMes);
    }

    @PostMapping("/posts/{postno}/messages/")
    @Operation(
            summary = "新增文章留言到佇列"
    )
    public ResponseEntity<String> addForumMesToQueue(
            @Valid @RequestBody ForumMesUpdateDTO forumMesUpdateDTO,
            @PathVariable("postno") Integer postNo,
            @AuthenticationPrincipal MemberDetails currentUser) {
        Integer memberId = currentUser.getMemberId();
        forumMesUpdateDTO.setMemId(memberId);
        forumMesUpdateDTO.setPostId(postNo);
        // 將留言資料轉換為 JSON 字串
        try {
            String messageJson = objectMapper.writeValueAsString(forumMesUpdateDTO);
            // 將留言加入 Redis 佇列
            redisTemplate.opsForList().rightPush(MESSAGE_QUEUE_KEY, messageJson);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("留言已提交");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("系統忙碌，請稍後再試");
        }
    }

    @PutMapping("/posts/messages/{mesno}")
    @Operation(
            summary = "修改文章留言"
    )
    public ResponseEntity<ForumMesDTO> updateForumMes(
            @PathVariable("mesno") Integer mesNo,
            @Valid @RequestBody ForumMesUpdateDTO forumMesUpdateDTO) {
        ForumMesDTO updateForumMes = forumMesSvc.updateForumMes(mesNo, forumMesUpdateDTO);
        return ResponseEntity.ok(updateForumMes);
    }

    @GetMapping("admin/posts/messagecount")
    @Operation(summary = "取得討論區留言數量")
    public Long getForumMesCount() {
        return forumMesSvc.getForumMesCount();
    }


}
