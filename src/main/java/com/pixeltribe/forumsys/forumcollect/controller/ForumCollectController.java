package com.pixeltribe.forumsys.forumcollect.controller;

import com.pixeltribe.forumsys.forumcollect.model.ForumCollectDTO;
import com.pixeltribe.forumsys.forumcollect.model.ForumCollectService;
import com.pixeltribe.forumsys.forumcollect.model.ForumCollectUpdateDTO;
import com.pixeltribe.membersys.security.MemberDetails;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ForumCollectController {

    private final ForumCollectService forumCollectService;

    public ForumCollectController(ForumCollectService forumCollectService) {
        this.forumCollectService = forumCollectService;
    }

    @PutMapping("/forums/{forno}/collect")
    @Operation(
            summary = "討論區收藏"
    )
    public ResponseEntity<ForumCollectDTO> updateForumCollect(
            @PathVariable("forno") Integer forNo,
            @Valid @RequestBody ForumCollectUpdateDTO forumCollectUpdateDTO,
            @AuthenticationPrincipal MemberDetails currentUser) {
        Integer memberId = currentUser.getMemberId();
        ForumCollectDTO forumCollectDTO = forumCollectService.addForumCollect(memberId, forNo, forumCollectUpdateDTO);
        return ResponseEntity.ok(forumCollectDTO);

    }

    @GetMapping("/forums/collect/me")
    @Operation(
            summary = "取得會員收藏討論區列表"
    )
    public List<ForumCollectDTO> getForumCollectForMenber(
            @AuthenticationPrincipal MemberDetails currentUser
    ){
        Integer memberId = currentUser.getMemberId();
        return forumCollectService.getForumCollectForMenber(memberId);
    }

}
