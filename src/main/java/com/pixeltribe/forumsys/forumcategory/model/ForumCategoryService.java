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

    public ForumCategory getOneForumCategory(Integer catNo) {
        Optional<ForumCategory> optional = forumCategoryRepository.findById(catNo);
        return optional.orElse(null);
    }

    public List<ForumCategoryDTO> getAllForumCategory() {

        List<ForumCategory> forumCategories = forumCategoryRepository.findAll();


        return forumCategories.stream()
                .map(this::convertToCategoryDTO)
                .collect(Collectors.toList());
    }


    private ForumCategoryDTO convertToCategoryDTO(ForumCategory forumCategory) {
        ForumCategoryDTO categoryDTO = new ForumCategoryDTO();
        categoryDTO.setId(forumCategory.getId());
        categoryDTO.setCatName(forumCategory.getCatName());
        categoryDTO.setCatDes(forumCategory.getCatDes());
        categoryDTO.setCatDate(forumCategory.getCatDate());

        return categoryDTO;
    }



}


