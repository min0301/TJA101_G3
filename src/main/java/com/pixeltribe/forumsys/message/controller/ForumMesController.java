package com.pixeltribe.forumsys.message.controller;

import com.pixeltribe.forumsys.message.model.ForumMesDTO;
import com.pixeltribe.forumsys.message.model.ForumMesService;
import com.pixeltribe.forumsys.message.model.ForumMesUptateDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ForumMesController {

    private final ForumMesService forumMesSvc;

    public ForumMesController(ForumMesService forumMesSvc) {
        this.forumMesSvc = forumMesSvc;
    }

    @GetMapping("/forumMessages")
    @Operation(
            summary = "查所有的留言"
    )
    public List<ForumMesDTO> getAllForumMes() {

        return forumMesSvc.getAllForumMes();
    }

    @GetMapping("/forumMes/{mesNo}")
    @Operation(
            summary = "查單一則留言"
    )
    public ForumMesDTO findOneForumMes(
            @Parameter(description = "留言編號", example = "1")
            @PathVariable Integer mesNo) {
        ForumMesDTO forumMesDTO = forumMesSvc.getOneForumMes(mesNo);
        return forumMesDTO;
    }

    @GetMapping("/forumMesList/{postNo}")
    @Operation(
            summary = "查單一討論區文章留言"
    )
    public List<ForumMesDTO> getForumMesByPost(Integer postNo) {
        List<ForumMesDTO> forumMes = forumMesSvc.getForumMesByPost(postNo);
        return forumMes;
    }


    @PostMapping("/posts/{postNo}/messages/")
    @Operation(
            summary = "新增文章留言"
    )
    public ResponseEntity<?> addForumMes(
            @Valid @RequestBody ForumMesUptateDTO forumMesUptateDTO,
            @PathVariable Integer postNo,
            BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body("輸入資料有誤！");
        }
        ForumMesDTO createdForumMes = forumMesSvc.addForumMes(postNo, forumMesUptateDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdForumMes);
    }

    @PutMapping("/posts/messages/{mesNo}")
    @Operation(
            summary = "修改文章留言"
    )
    public ResponseEntity<?> updateForumMes(
            @PathVariable Integer mesNo,
            @Valid @RequestBody ForumMesUptateDTO forumMesUptateDTO,
            BindingResult result){
                if (result.hasErrors()) {
                    return ResponseEntity.badRequest().body("輸入資料有誤！");
                }

                ForumMesDTO updateForumMes = forumMesSvc.updateForumMes(mesNo, forumMesUptateDTO);
                return ResponseEntity.ok(updateForumMes);
    }


}
