package com.pixeltribe.forumsys.forum.model;

import com.pixeltribe.forumsys.model.ForumDAO_interface;
import com.pixeltribe.forumsys.model.ForumJDBCDAO;
import com.pixeltribe.forumsys.model.ForumVO;

import java.util.List;

public class ForumServiceold {
	
	private ForumDAO_interface dao;
	
	public ForumServiceold() {
		dao = new ForumJDBCDAO();
	}

	public com.pixeltribe.forumsys.model.ForumVO addForum(String forName, Integer catNo, String forDes) {
		com.pixeltribe.forumsys.model.ForumVO forumVO = new com.pixeltribe.forumsys.model.ForumVO();
		forumVO.setForName(forName);
		forumVO.setCatNo(catNo);
		forumVO.setForDes(forDes);
		dao.insert(forumVO);
		
		return forumVO;
		
	}
	
	public com.pixeltribe.forumsys.model.ForumVO updateForum(Integer forNo, String forName, Integer catNo, String forDes, Integer fchatStatus) {
		com.pixeltribe.forumsys.model.ForumVO forumVO = new com.pixeltribe.forumsys.model.ForumVO();
		forumVO.setForNo(forNo);
		forumVO.setForName(forName);
		forumVO.setCatNo(catNo);
		forumVO.setForDes(forDes);
		forumVO.setFchatStatus(fchatStatus);
		dao.update(forumVO);
		return forumVO;
	}
	
	public void deleteForum(Integer forNO) {
		dao.delete(forNO);
	}
	public com.pixeltribe.forumsys.model.ForumVO getOneForum(Integer forNO) {
		return dao.findByPrimaryKey(forNO);
	}

	public List<ForumVO> getAll() {
		return dao.getAll();
	}
	
	
}
