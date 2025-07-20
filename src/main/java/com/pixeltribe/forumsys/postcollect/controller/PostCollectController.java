package com.pixeltribe.forumsys.postcollect.controller;

import com.pixeltribe.forumsys.postcollect.model.PostCollectDTO;
import com.pixeltribe.forumsys.postcollect.model.PostCollectService;
import com.pixeltribe.forumsys.postcollect.model.PostCollectUpdateDTO;
import com.pixeltribe.membersys.security.MemberDetails;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PostCollectController {

    private final PostCollectService postCollectService;

    public PostCollectController(PostCollectService postCollectService) {
        this.postCollectService = postCollectService;
    }

    @PutMapping("/posts/{postno}/collect")
    @Operation(
            summary = "文章收藏"
    )
    public ResponseEntity<PostCollectDTO> updatePostCollect(
            @PathVariable("postno") Integer postNo,
            @Valid @RequestBody PostCollectUpdateDTO postCollectUpdateDTO,
            @AuthenticationPrincipal MemberDetails currentUser) {
        Integer memberId = currentUser.getMemberId();
        PostCollectDTO postCollectDTO = postCollectService.addPostCollect(memberId, postNo, postCollectUpdateDTO);
        return ResponseEntity.ok(postCollectDTO);
    }

    @GetMapping("/posts/collect/me")
    @Operation(
            summary = "取得會員收藏文章列表"
    )
        public List<PostCollectDTO> getPostCollectForMember (
                @AuthenticationPrincipal MemberDetails currentUser
        ){

    Integer memberId = currentUser.getMemberId();
    return postCollectService.getPostCollectForMember(memberId);
    }


}