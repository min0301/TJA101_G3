package com.pixeltribe.forumsys.forumcategory.controller;

import com.pixeltribe.forumsys.forumcategory.model.ForumCategoryDetailDTO;
import com.pixeltribe.forumsys.forumcategory.model.ForumCategoryService;
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

    @GetMapping("forum-category")
    public List<ForumCategoryDetailDTO> getAllForumCategories() {
        return forumCategoryService.getAllForumCategoriesWithForums();
    }



}
