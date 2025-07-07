package com.pixeltribe.membersys.member.model;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

public class MemService {

	@Autowired
	MemRepository repository;

	public void addMem(Member member) {
		repository.save(member);
	}

	public void updateMem(Member member) {
		repository.save(member);
	}

	public Member getOneMem(Integer admNo) {
		Optional<Member> optional = repository.findById(admNo);
		return optional.orElse(null);
	}

	public List<Member> findAll() {
		return repository.findAll();
	}
}
