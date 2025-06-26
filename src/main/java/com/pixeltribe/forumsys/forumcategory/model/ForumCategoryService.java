package com.pixeltribe.forumsys.forumcategory.model;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@Service
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

}
