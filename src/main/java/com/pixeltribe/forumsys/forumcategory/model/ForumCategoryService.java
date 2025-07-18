package com.pixeltribe.forumsys.forumcategory.model;

import com.pixeltribe.forumsys.exception.ConflictException;
import com.pixeltribe.forumsys.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
                    throw new ConflictException("類別名稱：" + existingCat.getCatName() + " 已經存在");
                });
        ForumCategory forumCategory = new ForumCategory();
        forumCategory.setCatName(forumCategoryUpdateDTO.getCatName());
        forumCategory.setCatDes(forumCategoryUpdateDTO.getCatDes());
        return ForumCategoryDTO.convertToCategoryDTO(forumCategoryRepository.save(forumCategory));
    }


    @Transactional
    public ForumCategoryDTO update(Integer catNo, ForumCategoryUpdateDTO forumCategoryUpdateDTO) {

        ForumCategory forumCategory = forumCategoryRepository.findById(catNo)
                .orElseThrow(() -> new ResourceNotFoundException("找不到討論區編號: " + catNo));

        forumCategoryRepository.findByCatName(forumCategoryUpdateDTO.getCatName())
                .filter(existingCat -> !existingCat.getId().equals(catNo))
                .ifPresent(existingCat -> {
                    throw new ConflictException("類別名稱：" + existingCat.getCatName() + " 已經存在");
                });


        forumCategory.setCatName(forumCategoryUpdateDTO.getCatName());
        forumCategory.setCatDes(forumCategoryUpdateDTO.getCatDes());
        return ForumCategoryDTO.convertToCategoryDTO(forumCategoryRepository.save(forumCategory));
    }


    public ForumCategory getOneForumCategory(Integer catNo) {
        Optional<ForumCategory> optional = forumCategoryRepository.findById(catNo);
        return forumCategoryRepository.findById(catNo)
                .orElseThrow(() -> new ResourceNotFoundException("找不到類別編號: " + catNo));
    }


    public List<ForumCategoryDTO> getAllForumCategory() {

        List<ForumCategory> forumCategories = forumCategoryRepository.findAll();

        return forumCategories.stream()
                .map(ForumCategoryDTO::convertToCategoryDTO)
                .toList();
    }


}


