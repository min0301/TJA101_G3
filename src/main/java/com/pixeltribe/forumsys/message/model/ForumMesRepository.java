package com.pixeltribe.forumsys.message.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ForumMesRepository extends JpaRepository<ForumMes, Integer> {

    List<ForumMes> findByPostNo_Id(Integer postNo);

    List<ForumMes> findByMesStatus(Character status);

}
