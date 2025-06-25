package com.pixeltribe.forumsys.forumcategory.model;

import java.util.List;


public interface ForumCategoryDAO_interface {
    public void insert(ForumCategoryVO forumCategoryVO);
    public void update(ForumCategoryVO forumCategoryVO);
    public void delete(Integer catNo);
    public ForumCategoryVO findByPrimaryKey(Integer catNo);
    public List<ForumCategoryVO> getAll();
    //萬用複合查詢(傳入參數型態Map)(回傳 List)
//  public List<EmpVO> getAll(Map<String, String[]> map); 
}
