package com.pixeltribe.membersys.administrator.model;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("admService")
public class AdmService {

	@Autowired
	AdmRepository repository;

	public void addAdm(Administrator administrator) {
		repository.save(administrator);
	}
	
	public void updateAdm(Administrator administrator) {
		repository.save(administrator);
	}
	
	public Administrator getOneAdm(Integer admNo) {
		Optional<Administrator> optional = repository.findById(admNo);
		return optional.orElse(null);
	}
	
	public List<Administrator> findAll(){
		return repository.findAll();
	}
}
