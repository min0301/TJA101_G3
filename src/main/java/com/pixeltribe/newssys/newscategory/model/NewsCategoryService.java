package com.pixeltribe.newssys.newscategory.model;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NewsCategoryService {
    public final NewsCategoryRepository newsCategoryRepository;

    @Autowired
    NewsCategoryService(NewsCategoryRepository newsCategoryRepository) {
        this.newsCategoryRepository = newsCategoryRepository;
    }

    @Transactional(readOnly = true)
    public List<NewsCategory> getAllNewsCategories() {
        return newsCategoryRepository.findAll();
    }

    @Transactional
    public NewsCategoryDTO addCategory(@Valid NewsCategoryDTO newsCategoryDTO) {

        String categoryName = newsCategoryDTO.getCategoryName().trim();

        if (newsCategoryRepository.existsByNcatNameIgnoreCase(categoryName)
        ) {
            throw new IllegalArgumentException("Category: " + categoryName + ". Already exists!");
        }

        NewsCategory newsCategory = new NewsCategory();
        newsCategory.setNcatName(categoryName);

        newsCategoryRepository.save(newsCategory);
        return newsCategoryDTO;
    }

    @Transactional
    public NewsCategoryDTO updateCategory(NewsCategoryDTO newsCategoryDTO) {
        NewsCategory newsCategory = newsCategoryRepository.findById(newsCategoryDTO.getCategoryId()).orElseThrow(() -> new EntityNotFoundException("CategoryId: " + newsCategoryDTO.getCategoryId()));

        String newName = newsCategoryDTO.getCategoryName().trim();

        if (newsCategoryRepository.existsByNcatNameIgnoreCase(newName)){
            throw new IllegalArgumentException("Category: " + newName + ". Already exists!");
        }

        if(newName.equalsIgnoreCase(newsCategory.getNcatName())){
            return newsCategoryDTO;
        }
        newsCategory.setNcatName(newName);

        return newsCategoryDTO;
    }
}
