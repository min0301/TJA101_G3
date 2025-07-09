package com.pixeltribe.membersys.member.model;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemRepository extends JpaRepository<Member, Integer> {
	
	Member findByMemAccount (String memAccount);
	Member findByMemPassword(String memPassword);
	Member findByMemEmail(String memEmail);
	boolean existsByMemEmail(String memEmail);
}
