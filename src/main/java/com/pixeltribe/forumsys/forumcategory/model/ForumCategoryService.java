package com.pixeltribe.forumsys.forumcategory.model;

import com.pixeltribe.forumsys.exception.ConflictException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("forumCategoryService")
public class ForumCategoryService {

    private final ForumCategoryRepository forumCategoryRepository;

    public ForumCategoryService(ForumCategoryRepository forumCategoryRepository) {
        this.forumCategoryRepository = forumCategoryRepository;
    }

    @Transactional
    public ForumCategoryDTO add(ForumCategoryUpdateDTO forumCategoryUpdateDTO) {
        forumCategoryRepository.findByCatName(forumCategoryUpdateDTO.getCatName())
                .ifPresent(existingCat -> {
                    throw new ConflictException("Category with name " + existingCat.getCatName() + " already exists");
                });
        ForumCategory forumCategory = new ForumCategory();
        forumCategory.setCatName(forumCategoryUpdateDTO.getCatName());
        forumCategory.setCatDes(forumCategoryUpdateDTO.getCatDes());
        return ForumCategoryDTO.convertToCategoryDTO(forumCategoryRepository.save(forumCategory));
    }


    @Transactional
    public ForumCategoryDTO update(Integer catNo, ForumCategoryUpdateDTO forumCategoryUpdateDTO) {

        forumCategoryRepository.findByCatName(forumCategoryUpdateDTO.getCatName())
                .ifPresent(existingCat -> {
                    throw new ConflictException("Category with name " + existingCat.getCatName() + " already exists");
                });

        ForumCategory forumCategory = forumCategoryRepository.findById(catNo).get();
        ;
        forumCategory.setCatName(forumCategoryUpdateDTO.getCatName());
        forumCategory.setCatDes(forumCategoryUpdateDTO.getCatDes());
        return ForumCategoryDTO.convertToCategoryDTO(forumCategoryRepository.save(forumCategory));
    }


    public ForumCategory getOneForumCategory(Integer catNo) {
        Optional<ForumCategory> optional = forumCategoryRepository.findById(catNo);
        return optional.orElse(null);
    }


    public List<ForumCategoryDTO> getAllForumCategory() {

        List<ForumCategory> forumCategories = forumCategoryRepository.findAll();

        return forumCategories.stream()
                .map(ForumCategoryDTO::convertToCategoryDTO)
                .collect(Collectors.toList());
    }


}


