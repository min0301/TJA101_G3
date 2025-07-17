package com.pixeltribe.forumsys.postlike.controller;

import com.pixeltribe.forumsys.postlike.model.PostLike;
import com.pixeltribe.forumsys.postlike.model.PostLikeDTO;
import com.pixeltribe.forumsys.postlike.model.PostLikeService;
import com.pixeltribe.membersys.security.MemberDetails;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import oracle.jdbc.proxy.annotation.Post;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PostLikeController {

    private final PostLikeService postLikeService;

    public PostLikeController(PostLikeService postLikeService) {
        this.postLikeService = postLikeService;
    }

    @GetMapping("/postlikes")
    @Operation(
            summary = "查所有的討論區文章喜愛"
    )
    public List<PostLikeDTO> getPostLike() { return postLikeService.getAllPostLike();}

    @PostMapping("/posts/{postNo}/like")
    @Operation(
            summary = "討論區文章踩讚"
    )

    public ResponseEntity<PostLikeDTO> UpdatePostLike(
            @Valid @RequestBody PostLikeDTO postLikeDTO,
            @PathVariable("postNo") Integer postNo,
            @AuthenticationPrincipal MemberDetails currentUser) {
        Integer memberId = currentUser.getMemberId();
        PostLikeDTO postLikeCreate = postLikeService.updatePostLike(postNo, memberId, postLikeDTO.getPLikeStatus());
        return ResponseEntity.status(HttpStatus.CREATED).body(postLikeCreate);
    }
}
