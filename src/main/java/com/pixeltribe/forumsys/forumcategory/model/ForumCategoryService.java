package com.pixeltribe.forumsys.forumcategory.model;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("forumCategoryService")
public class ForumCategoryService {

    @Autowired
    ForumCategoryRepository forumCategoryRepository;


    public void add(ForumCategory forumCategory) {
        forumCategoryRepository.save(forumCategory);
    }
    public void update(ForumCategory forumCategory) {

        forumCategoryRepository.save(forumCategory);
    }

    public void delete(ForumCategory forumCategory) {

        forumCategoryRepository.delete(forumCategory);
    }

    public ForumCategory getOneForumCategory(Integer catNo) {
        Optional<ForumCategory> optional = forumCategoryRepository.findById(catNo);
        return optional.orElse(null);
    }


    public List<ForumCategoryDetailDTO> getAllForumCategoriesWithForums() {

        // 1. 從資料庫取得原始的 Entity 列表
        List<ForumCategory> forumCategories = forumCategoryRepository.findAll();
        // 2. 使用 Stream API 將 List<ForumCategory> 轉換為 List<ForumCategoryDetailDTO>
        return forumCategories.stream()
                .map(this::convertToCategoryDetailDTO)
                .collect(Collectors.toList());
    }

    private ForumCategoryDetailDTO convertToCategoryDetailDTO(ForumCategory forumCategory) {
        ForumCategoryDetailDTO categoryDTO = new ForumCategoryDetailDTO();
        // 建立外層 DTO
        categoryDTO.setId(forumCategory.getId());
        categoryDTO.setCatName(forumCategory.getCatName());
        categoryDTO.setCatDes(forumCategory.getCatDes());
        categoryDTO.setCatDate(forumCategory.getCatDate());

        // 處理內層的 Forum 列表
        List<ForumSummaryDTO> forumDTOs = (forumCategory.getForums() == null) ? Collections.emptyList() :
                forumCategory.getForums().stream()
                        .map(forum -> { // 對每個 forum Entity 進行轉換
                            ForumSummaryDTO forumDTO = new ForumSummaryDTO();
                            forumDTO.setId(forum.getId());
                            forumDTO.setForName(forum.getForName());
                            forumDTO.setForDes(forum.getForDes());
                            return forumDTO;
                        })
                        .collect(Collectors.toList());

        categoryDTO.setForums(forumDTOs);

        return categoryDTO;
    }





}


