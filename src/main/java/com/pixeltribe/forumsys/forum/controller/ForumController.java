package com.pixeltribe.forumsys.forum.controller;

import com.pixeltribe.forumsys.forum.model.ForumDetailDTO;
import com.pixeltribe.forumsys.forum.model.ForumService;
import com.pixeltribe.forumsys.forum.model.ForumUpdateDTO;
import com.pixeltribe.membersys.security.MemberDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ForumController {

    private final ForumService forumSvc;

    public ForumController(ForumService forumSvc) {

        this.forumSvc = forumSvc;

    }

    @PostMapping("/admin/forum")
    @Operation(summary = "新增討論區")
    public ResponseEntity<?> addForum(
            @RequestPart("forumcreat") @Valid ForumUpdateDTO forumcreationDTO,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
            BindingResult result) {

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body("輸入資料有誤！");
        }
        ForumDetailDTO createdForum = forumSvc.add(forumcreationDTO, imageFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdForum);

    }

    @PutMapping("/admin/forum/{forNo}")
    @Operation(summary = "修改討論區")
    public ResponseEntity<?> updateForum(
            @PathVariable Integer forNo,
            @RequestPart("forumDTO") @Valid ForumUpdateDTO forumDTO,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
            BindingResult result) {

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body("輸入資料有誤！");
        }
        ForumDetailDTO updateForum = forumSvc.update(forNo, forumDTO, imageFile);
        return ResponseEntity.ok(updateForum);

    }

    @GetMapping("/forum/{forNo}")
    @Operation(summary = "查單一討論區")
    public ForumDetailDTO findOneForum(
            @Parameter(description = "討論區編號", example = "1")
            @PathVariable Integer forNo,
            @AuthenticationPrincipal MemberDetails currentUser) {

        return forumSvc.getOneForum(forNo, currentUser);

    }

    @GetMapping("/forums/hot")
    @Operation(summary = "查全部討論區(按熱度排序)")
    public List<ForumDetailDTO> findHotForums() {

        return forumSvc.getHotForumsRedis();

    }

    @GetMapping("/search/forums")
    @Operation(summary = "查詢討論區(按關鍵字)",
            description = "根據關鍵字搜尋討論區")
    public List<ForumDetailDTO> findHotForumsByKeyword(
            @Parameter(description = "關鍵字", example = "魔物獵人")
            @RequestParam String keyword) {

        return forumSvc.searchForumsByKeyword(keyword);

    }

    @GetMapping("/forums")
    @Operation(summary = "查全部開放的討論區")
    public List<ForumDetailDTO> findAll() {

        return forumSvc.getAllForum();

    }

    @GetMapping("/admin/forums")
    @Operation(summary = "後台查全部討論區")
    public List<ForumDetailDTO> findAdminAll() {

        return forumSvc.getAllAdminForum();

    }

    @GetMapping("/category/{catNo}/forums")
    @Operation(summary = "查類別中有哪些討論區")
    public List<ForumDetailDTO> findByCatNo_Id(
            @Parameter(description = "討論區類別編號", example = "1")
            @PathVariable Integer catNo) {

        return forumSvc.getForumsByCategory(catNo);

    }

    @GetMapping("admin/forumcount")
    @Operation(summary = "取得討論區數量")
    public Long getNewsCount() {

        return forumSvc.getForumCount();

    }


}






