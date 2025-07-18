package com.pixeltribe.membersys.login.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pixeltribe.membersys.login.dto.MemLoginReturn;
import com.pixeltribe.membersys.member.dto.MemberBasicDto;
import com.pixeltribe.membersys.member.model.MemRepository;
import com.pixeltribe.membersys.member.model.Member;
import com.pixeltribe.util.JwtUtil;



@Service
public class MemLoginService {

	@Autowired
	private MemRepository memRepository;
	
	@Autowired
	private JwtUtil jwtUtil;
	
	public MemLoginReturn login(String memAccount, String memPassword) {
		
		Member member = memRepository.findByMemAccount(memAccount);
		MemberBasicDto memberInfo = new MemberBasicDto(member.getId(),member.getMemAccount(),member.getMemNickName(),member.getMemName(),member.getMemEmail(),member.getRole());
		
		if (member == null) {
			return new MemLoginReturn(false, "輸入的帳號不存在");
		}
		if(!member.getMemPassword().equals(memPassword)) {
			return new MemLoginReturn(false, "密碼錯誤");
		}
		String token = jwtUtil.generateMemberToken(member);
		return new MemLoginReturn(true,"登入成功", token, memberInfo);
	}
}
