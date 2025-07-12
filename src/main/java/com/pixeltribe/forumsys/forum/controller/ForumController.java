package com.pixeltribe.forumsys.forum.controller;

import com.pixeltribe.forumsys.forum.model.*;
import io.swagger.v3.oas.annotations.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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


    @GetMapping("/forums")
    @Operation(
            summary = "查全部開放的討論區"
    )
    public List<ForumDetailDTO> findAll() {

        return forumSvc.getAllForum();
    }

    @GetMapping("/admin/forums")
    @Operation(
            summary = "查全部開放的討論區"
    )
    public List<ForumDetailDTO> findAdminAll() {

        return forumSvc.getAllAdminForum();
    }


    @GetMapping("/category/{catNo}/forums")
    @Operation(
            summary = "查類別中有哪些討論區",
            description = "222"
    )
    public List<ForumDetailDTO> findByCatNo_Id(
            @Parameter(description = "討論區類別編號", example = "1")
            @PathVariable Integer catNo) {

        return forumSvc.getForumsByCategory(catNo);
    }


    @PostMapping("/admin/forum")
    @Operation(
            summary = "新增討論區"
    )
    public ResponseEntity<?> addForum(
            @RequestPart("forumcreat") @Valid ForumUpdateDTO forumcreationDTO,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
            BindingResult result) {
        if (result.hasErrors()) {
            // 如果 JSON 資料驗證失敗，回傳 400 錯誤
            return ResponseEntity.badRequest().body("輸入資料有誤！");
        }
        //*************************** 2. 開始新增資料 *****************************************/
        // 讓 Service 層回傳新增成功後、包含新 ID 的物件，這在 API 中是個好習慣
        ForumDetailDTO createdForum = forumSvc.add(forumcreationDTO, imageFile);

        //*************************** 3. 新增完成,準備轉交(Send the Success view) **************/
        // 回傳 201 Created 狀態碼，並在 body 中附上新增成功的員工資料
        // 這能讓前端立刻知道新增資源的 ID 是多少
        return ResponseEntity.status(HttpStatus.CREATED).body(createdForum);
    }

    @PutMapping("/admin/forum/{forNo}")
    @Operation(
            summary = "修改討論區"
    )
    public ResponseEntity<?> updateForum(
            @PathVariable Integer forNo,
            @RequestPart("forumDTO") @Valid ForumUpdateDTO forumDTO,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
            BindingResult result) {
        if (result.hasErrors()) {
            // 如果 JSON 資料驗證失敗，回傳 400 錯誤
            return ResponseEntity.badRequest().body("輸入資料有誤！");
        }
        //*************************** 2. 開始更新資料 *****************************************/
        // 讓 Service 層回傳新增成功後、包含新 ID 的物件，這在 API 中是個好習慣
        ForumDetailDTO updateForum = forumSvc.update(forNo, forumDTO, imageFile);

        //*************************** 3. 更新完成,準備轉交(Send the Success view) **************/
        // 回傳 201 Created 狀態碼，並在 body 中附上新增成功的員工資料
        // 這能讓前端立刻知道新增資源的 ID 是多少
        return ResponseEntity.ok(updateForum);
    }

    @GetMapping("/forum/{forNo}")
    @Operation(
            summary = "查單一討論區"
    )
    public ForumDetailDTO findOneForum(
            @Parameter(description = "討論區編號", example = "1")
            @PathVariable Integer forNo) {

        return forumSvc.getOneForum(forNo);
    }

    @GetMapping("/forums/hot")
    @Operation(summary = "查全部討論區(按熱度排序)")
    public List<ForumDetailDTO> findHotForums() {
        return forumSvc.getHotForumsRedis();
    }


}






