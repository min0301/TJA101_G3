package com.pixeltribe.membersys.administrator.model;

import java.util.Optional;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("admService")
public class AdmService {

	@Autowired
	AdmRepository repository;

	@Autowired
	private SessionFactory sessionFactory;

	public void addAdm(Administrator administrator) {
		repository.save(administrator);
	}
	
	public void updateAdm(Administrator administrator) {
		repository.save(administrator);
	}
	
	public void getOneAdm(Integer admNo) {
		Optional<Administrator> optional = repository.findById(admNo);
		return optional.orElse(null);
	}
}
