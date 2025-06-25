package com.pixeltribe.forumsys.forumcategory.model;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import util.HibernateUtil;

public class ForumCategoryHibernateDAO implements ForumCategoryDAO_interface {
	
	private SessionFactory factory; 
	
	public ForumCategoryHibernateDAO() {
		factory = HibernateUtil.getSessionFactory();
	}
	
	private Session getSession() {
		return factory.getCurrentSession();
	}

	@Override
	public void insert(ForumCategoryVO entity) {
		getSession().persist(entity);
	}

	@Override
	public void update(ForumCategoryVO entity) {
		getSession().merge(entity);
	}

	@Override
	public void delete(Integer catNo) {
		ForumCategoryVO forumCategory = getSession().get(ForumCategoryVO.class, catNo);
		if (forumCategory != null) {
			getSession().remove(forumCategory);
		}
		
	}

	@Override
	public ForumCategoryVO findByPrimaryKey(Integer catNo) {
		return getSession().find(ForumCategoryVO.class, catNo);
	}

	@Override
	public List<ForumCategoryVO> getAll() {
		return getSession().createQuery("from ForumCategoryVO", ForumCategoryVO.class).getResultList();
	}
	
	

}
