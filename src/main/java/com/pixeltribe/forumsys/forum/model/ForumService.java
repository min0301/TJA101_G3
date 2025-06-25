package com.pixeltribe.forumsys.forum.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ForumService {

    @Autowired
    ForumRepository forumRepository;

    public void add(ForumVO forumVO) {
        forumRepository.save(forumVO);
    }

    public void update(ForumVO forumVO) {
        forumRepository.save(forumVO);
    }

    public void delete(ForumVO forumVO) {
            forumRepository.deleteById(forumVO);
    }

    public ForumVO getOneForum(Integer forNO) {
        Optional<ForumVO> optional = forumRepository.findById(forNO);
        return optional.orElse(null);
    }



}
