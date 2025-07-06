package com.pixeltribe.forumsys.message.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ForumMesRepository extends JpaRepository <ForumMes, Integer> {

    public List<ForumMes> findByPostNo_Id(Integer postNo);

}
