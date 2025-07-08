package com.pixeltribe.forumsys.messagelike.controller;

import com.pixeltribe.forumsys.messagelike.model.ForumMesLikeDTO;
import com.pixeltribe.forumsys.messagelike.model.ForumMesLikeService;
import com.pixeltribe.forumsys.messagelike.model.ForumMesLikeUpdateDTO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ForumMesLikeController {

    private final ForumMesLikeService forumMesLikeService;

    public ForumMesLikeController(ForumMesLikeService forumMesLikeService) {
        this.forumMesLikeService = forumMesLikeService;
    }

    @GetMapping("/forummeslikes")
    @Operation(
            summary = "查所有的討論區留言喜愛"
    )
    public List<ForumMesLikeDTO> getAllForumMesLike() {
        return forumMesLikeService.getAllForumMesLike();
    }

    @PostMapping("/posts/message/{mesno}/like")
    @Operation(
            summary = "討論區留言踩讚"
    )
    public ResponseEntity<ForumMesLikeDTO> updateForumMesLike(
            @Valid @RequestBody ForumMesLikeUpdateDTO forumMesLikeUpdateDTO,
            @PathVariable("mesno") Integer mesNo) {
        ForumMesLikeDTO forumMesLikecreate = forumMesLikeService.updateLike(mesNo, forumMesLikeUpdateDTO.getMemberId(), forumMesLikeUpdateDTO.getFmlikeStatus());
        return ResponseEntity.status(HttpStatus.CREATED).body(forumMesLikecreate);
    }

}
