package com.pixeltribe.forumsys.forumcollect.controller;

import com.pixeltribe.forumsys.forumcollect.model.ForumCollectDTO;
import com.pixeltribe.forumsys.forumcollect.model.ForumCollectService;
import com.pixeltribe.forumsys.forumcollect.model.ForumCollectUpdateDTO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
            @Valid @RequestBody ForumCollectUpdateDTO forumCollectUpdateDTO) {
        ForumCollectDTO forumCollectDTO = forumCollectService.addForumCollect(forNo, forumCollectUpdateDTO);
        return ResponseEntity.ok(forumCollectDTO);

    }

//    等會員做完再來寫
//    @GetMapping("/forums/collect/me")
//    @Operation(
//            summary = "取得會員收藏討論區列表"
//    )
//    public List<ForumCollectDTO> getForumCollectForMenber(){
//        return forumCollectService.getForumCollectForMenber(memberNo);
//    }

}
