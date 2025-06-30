package com.pixeltribe.forumsys.forumcategory.controller;

import com.pixeltribe.forumsys.forumcategory.model.ForumCategory;
import com.pixeltribe.forumsys.forumcategory.model.ForumCategoryDetailDTO;
import com.pixeltribe.forumsys.forumcategory.model.ForumCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ForumCategoryController {

    @Autowired
    private ForumCategoryService forumCategoryService;

    @GetMapping("forum-gategory")
    public List<ForumCategoryDetailDTO> getAllForumCategories() {
        return forumCategoryService.getAllForumCategoriesWithForums();
    }

}
