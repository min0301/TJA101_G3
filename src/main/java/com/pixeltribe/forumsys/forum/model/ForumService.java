package com.pixeltribe.forumsys.forum.model;

import com.pixeltribe.forumsys.forumcategory.model.ForumCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ForumService {

    @Autowired
    ForumRepository forumRepository;

    public void add(Forum forum) {
        forumRepository.save(forum);
    }

    public void update(Forum forum) {
        forumRepository.save(forum);
    }

    public void delete(Forum forum) {
            forumRepository.deleteById(forum.getId());
    }

    public Forum getOneForum(Integer forNO) {
        Optional<Forum> optional = forumRepository.findById(forNO);
        return optional.orElse(null);
    }

    public List<Forum> getAllForums() {
        return forumRepository.findAll();
    }


}
