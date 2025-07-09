package com.pixeltribe.newssys.newscategory.controller;

import com.pixeltribe.newssys.newscategory.model.NewsCategory;
import com.pixeltribe.newssys.newscategory.model.NewsCategoryDTO;
import com.pixeltribe.newssys.newscategory.model.NewsCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
class NewsCategoryController {

    public  final NewsCategoryService newsCategoryService;

    @Autowired
    NewsCategoryController(NewsCategoryService newsCategoryService) {
        this.newsCategoryService = newsCategoryService;
    }

    @GetMapping("allNewsCategories")
    public List<NewsCategory> getAllNewsCategorys() {
        return newsCategoryService.getAllNewsCategories();
    }

    @PostMapping("NewsCategories/add")
    public NewsCategoryDTO addNewsCategory(@RequestBody NewsCategoryDTO newsCategoryDTO) {
        return newsCategoryService.addCategory(newsCategoryDTO);
    }

    @PatchMapping("NewsCatory/update/{id}")
    public NewsCategoryDTO updateNewsCategory(@PathVariable int id, @RequestBody NewsCategoryDTO newsCategoryDTO) {
        newsCategoryDTO.setCategoryId(id);
        return newsCategoryService.updateCategory(newsCategoryDTO);
    }

}
