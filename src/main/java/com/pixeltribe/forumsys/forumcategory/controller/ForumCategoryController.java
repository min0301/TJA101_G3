package com.pixeltribe.forumsys.forumcategory.controller;

import com.pixeltribe.forumsys.forumcategory.model.ForumCategory;
import com.pixeltribe.forumsys.forumcategory.model.ForumCategoryDTO;
import com.pixeltribe.forumsys.forumcategory.model.ForumCategoryService;
import com.pixeltribe.forumsys.forumcategory.model.ForumCategoryUpdateDTO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ForumCategoryController {


    private final ForumCategoryService forumCategoryService;

    public ForumCategoryController(ForumCategoryService forumCategoryService) {
        this.forumCategoryService = forumCategoryService;
    }

    @GetMapping("/categorys")
    @Operation(
            summary = "查全部討論區類別"
    )
    public List<ForumCategoryDTO> getAllForumCategory() {

        return forumCategoryService.getAllForumCategory();
    }

    @GetMapping("/category/{catno}")
    @Operation(
            summary = "查單一討論區類別"
    )
    public ForumCategoryDTO findOneForumCategory(
            @Valid @PathVariable("catno") Integer catNo) {
        return ForumCategoryDTO.convertToCategoryDTO(forumCategoryService.getOneForumCategory(catNo));
    }

    @PostMapping("/admin/category")
    @Operation(
            summary = "新增討論區類別"
    )
    ResponseEntity<?> addForumCategory(
            @Valid @RequestBody ForumCategoryUpdateDTO forumCategoryUpdateDTO
    ) {

        return ResponseEntity.status(HttpStatus.CREATED).body(forumCategoryService.add(forumCategoryUpdateDTO));
    }

    @PutMapping("/admin/category/{catno}")
    @Operation(
            summary = "修改討論區類別"
    )
    ResponseEntity<?> updateForumCategory(
            @Valid @RequestBody ForumCategoryUpdateDTO forumCategoryUpdateDTO,
            @PathVariable("catno") Integer catNo
    ) {
        return ResponseEntity.ok(forumCategoryService.update(catNo, forumCategoryUpdateDTO));
    }


}
