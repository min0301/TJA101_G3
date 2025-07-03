package com.pixeltribe.forumsys.forumcategory.controller;

import com.pixeltribe.forumsys.forumcategory.model.ForumCategoryDTO;
import com.pixeltribe.forumsys.forumcategory.model.ForumCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ForumCategoryController {



    private final ForumCategoryService forumCategoryService;

    public ForumCategoryController(ForumCategoryService forumCategoryService) {
        this.forumCategoryService = forumCategoryService;
    }

    @GetMapping("categorys")
    @Operation(
            summary = "查全部討論區類別"
    )
    public List<ForumCategoryDTO> getAllForumCategory() {
        return forumCategoryService.getAllForumCategory();
    }



}
