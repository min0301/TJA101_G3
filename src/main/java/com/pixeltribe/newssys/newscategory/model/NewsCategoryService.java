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
    private final NewsCategoryRepository newsCategoryRepository;

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

        NewsCategory saved = newsCategoryRepository.save(newsCategory);

        NewsCategoryDTO returnedNewsCategoryDTO = new NewsCategoryDTO();
        returnedNewsCategoryDTO.setCategoryId(saved.getId());
        returnedNewsCategoryDTO.setCategoryName(saved.getNcatName());

        return returnedNewsCategoryDTO;
    }

    @Transactional
    public NewsCategoryDTO updateCategory(@Valid NewsCategoryDTO newsCategoryDTO) {
        NewsCategory newsCategory = newsCategoryRepository.findById(newsCategoryDTO.getCategoryId()).orElseThrow(
                () -> new EntityNotFoundException("CategoryId: " + newsCategoryDTO.getCategoryId()));

        String newName = newsCategoryDTO.getCategoryName().trim();

        if (newsCategoryRepository.existsByNcatNameIgnoreCaseAndIdNot(newName, newsCategory.getId())) {
            throw new IllegalArgumentException("Category: " + newName + ". Already exists!");
        }

        if (!newName.equals(newsCategory.getNcatName())) {

            newsCategory.setNcatName(newName);
        }

        NewsCategory saved = newsCategoryRepository.save(newsCategory);

        NewsCategoryDTO returnedNewsCategoryDTO = new NewsCategoryDTO();
        returnedNewsCategoryDTO.setCategoryId(saved.getId());
        returnedNewsCategoryDTO.setCategoryName(saved.getNcatName());

        return returnedNewsCategoryDTO;
    }
}