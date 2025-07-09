package com.pixeltribe.newssys.newscategory.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NewsCategoryRepository extends JpaRepository<NewsCategory, Integer> {
    /** 新增時檢查 */
    boolean existsByNcatNameIgnoreCase(String ncatName);

    /** 更新時檢查，排除自己 */
    boolean existsByNcatNameIgnoreCaseAndIdNot(String ncatName, Integer id);

}
