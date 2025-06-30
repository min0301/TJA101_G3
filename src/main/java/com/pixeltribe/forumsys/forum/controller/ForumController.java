package com.pixeltribe.forumsys.forum.controller;

import com.pixeltribe.forumsys.forum.model.Forum;
import com.pixeltribe.forumsys.forum.model.ForumService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
@RestController
@RequestMapping("/api")
public class ForumController {

    @Autowired
    ForumService forumSvc;


    @GetMapping("forums")
    public List<Forum> findAll() {

        return forumSvc.getAllForum();
    }

    @PostMapping("/admin/forum")
    public ResponseEntity<?> addForum(
            @RequestPart("forum") @Valid Forum forum,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
            BindingResult result) {
        if (result.hasErrors()) {
            // 如果 JSON 資料驗證失敗，回傳 400 錯誤
            return ResponseEntity.badRequest().body("輸入資料有誤！");
        }
        /*************************** 2. 開始新增資料 *****************************************/
        // 讓 Service 層回傳新增成功後、包含新 ID 的物件，這在 API 中是個好習慣
        Forum createdForum = forumSvc.add(forum, imageFile);

        /*************************** 3. 新增完成,準備轉交(Send the Success view) **************/
        // 回傳 201 Created 狀態碼，並在 body 中附上新增成功的員工資料
        // 這能讓前端立刻知道新增資源的 ID 是多少
        return ResponseEntity.status(HttpStatus.CREATED).body(createdForum);

    }


}



