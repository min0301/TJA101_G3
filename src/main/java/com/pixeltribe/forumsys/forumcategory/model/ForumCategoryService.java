package com.pixeltribe.forumsys.forumcategory.model;

import java.util.List;


public class ForumCategoryService {
	
	private ForumCategoryDAO_interface dao;
	
	public ForumCategoryService() {
		dao =new ForumCategoryHibernateDAO();
	}
	
	public ForumCategoryVO addForumCategoryVO(String catName, String catDes) {
		ForumCategoryVO forumCategoryVO = new ForumCategoryVO();
		forumCategoryVO.setCatName(catName);
		forumCategoryVO.setCatDes(catDes);
		dao.insert(forumCategoryVO);
		return forumCategoryVO;
	}
	
	
	public ForumCategoryVO update(Integer catNo,String catName, String catDes) {
		ForumCategoryVO forumCategoryVO = new ForumCategoryVO();
		forumCategoryVO.setCatNo(catNo);
		forumCategoryVO.setCatName(catName);
		forumCategoryVO.setCatDes(catDes);
		dao.update(forumCategoryVO);
		return forumCategoryVO;
	}
	
	public void delete(Integer catNo) {
		dao.delete(catNo);
	}
	
	public ForumCategoryVO findByPrimaryKey(Integer catNo) {
		return dao.findByPrimaryKey(catNo);
	}
	
	public List<ForumCategoryVO> getAll() {
		return dao.getAll();
	}
}
