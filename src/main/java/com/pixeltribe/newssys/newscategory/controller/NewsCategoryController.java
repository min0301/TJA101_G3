package com.pixeltribe.newssys.newscategory.controller;

import com.pixeltribe.newssys.newscategory.model.NewsCategory;
import com.pixeltribe.newssys.newscategory.model.NewsCategoryDTO;
import com.pixeltribe.newssys.newscategory.model.NewsCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class NewsCategoryController {

    private final NewsCategoryService newsCategoryService;

    NewsCategoryController(NewsCategoryService newsCategoryService) {
        this.newsCategoryService = newsCategoryService;
    }

    @GetMapping("allNewsCategories")
    @Operation(summary = "獲得所有新聞分類")
    public List<NewsCategory> getAllNewsCategories() {
        return newsCategoryService.getAllNewsCategories();
    }

    @GetMapping("NewsCategory/{id}")
    @Operation(summary = "獲得單一新聞分類")
    public NewsCategoryDTO getNewsCategoryById(@PathVariable int id) {
        return newsCategoryService.getNewsCategoryById(id);
    }

    @PostMapping("NewsCategories/add")
    @Operation(summary = "新增新聞分類")
    public NewsCategoryDTO addNewsCategory(@RequestBody NewsCategoryDTO newsCategoryDTO) {
        return newsCategoryService.addCategory(newsCategoryDTO);
    }

    @PatchMapping("NewsCategory/update/{id}")
    @Operation(summary = "修改新聞分類")
    public NewsCategoryDTO updateNewsCategory(@PathVariable int id, @RequestBody NewsCategoryDTO newsCategoryDTO) {
        newsCategoryDTO.setCategoryId(id);
        return newsCategoryService.updateCategory(newsCategoryDTO);
    }

}
