package com.pixeltribe.forumsys.forumcollect.controller;

import com.pixeltribe.forumsys.forum.model.ForumDetailDTO;
import com.pixeltribe.forumsys.forumcollect.model.ForumCollectDTO;
import com.pixeltribe.forumsys.forumcollect.model.ForumCollectService;
import com.pixeltribe.forumsys.shared.CollectStatus;
import com.pixeltribe.membersys.security.MemberDetails;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            summary = "切換討論區收藏狀態"
    )
    public ResponseEntity<ForumCollectDTO> updateForumCollect(
            @PathVariable("forno") Integer forNo,
            @AuthenticationPrincipal MemberDetails currentUser) {
        Integer memberId = currentUser.getMemberId();
        ForumCollectDTO forumCollectDTO = forumCollectService.addForumCollect(memberId, forNo);
        return ResponseEntity.ok(forumCollectDTO);

    }

    @GetMapping("/forums/collect/me")
    @Operation(
            summary = "取得會員收藏討論區列表"
    )
    public List<ForumDetailDTO> getForumCollectForMenber(
            @AuthenticationPrincipal MemberDetails currentUser
    ) {
        Integer memberId = currentUser.getMemberId();
        CollectStatus collectStatus = CollectStatus.COLLECT;
        return forumCollectService.findByMemNoAndCollectStatus(memberId, collectStatus);
    }

}
