package com.pixeltribe.membersys.login.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pixeltribe.membersys.login.DTO.MemLoginResult;
import com.pixeltribe.membersys.member.model.MemRepository;
import com.pixeltribe.membersys.member.model.Member;



@Service
public class MemLoginService {

	@Autowired
	private MemRepository memRepository;
	
	public MemLoginResult login(String memAccount, String memPassword) {
		Member member = memRepository.findByMemAccount(memAccount);
		if (member == null) {
			return new MemLoginResult(false, "輸入的帳號不存在");
		}
		if(!member.getMemPassword().equals(memPassword)) {
			return new MemLoginResult(false, "密碼錯誤");
		}
		return new MemLoginResult(true,"登入成功");
	}
}
