package com.pixeltribe.membersys.administrator.model;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AdmRepository extends JpaRepository<Administrator, Integer> {
	
	Administrator findByAdmAccount(String admAccount);
	Administrator findByAdmPassword(String admPassword);
}
