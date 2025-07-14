package com.pixeltribe.forumsys.postcollect.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PostCollectRepository extends JpaRepository<PostCollect, Integer> {
    List<PostCollect> findByMember_Id(Integer Id);
}
