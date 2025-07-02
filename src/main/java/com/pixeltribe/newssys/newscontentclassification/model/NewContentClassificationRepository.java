package com.pixeltribe.newssys.newscontentclassification.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewContentClassificationRepository extends JpaRepository<NewContentClassification, Integer> {

}
