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

	    // 1. 查無此帳號
	    if (member == null) {
	        return new MemLoginReturn(false, "查無此帳號");
	    }

	    // 2. 密碼錯誤
	    if (!member.getMemPassword().equals(memPassword)) {
	        return new MemLoginReturn(false, "密碼錯誤");
	    }

	    // 3. 停權
	    if (member.getMemStatus().equals('2')) {
	        return new MemLoginReturn(false, "此帳號已被停權");
	    }

	    // 4. 正常登入流程
	    MemberBasicDto memberInfo = new MemberBasicDto(
	        member.getId(),
	        member.getMemAccount(),
	        member.getMemNickName(),
	        member.getMemName(),
	        member.getMemEmail(),
	        member.getRole(),
	        member.getMemIconData()
	    );
	    String token = jwtUtil.generateMemberToken(member);

	    return new MemLoginReturn(true, "登入成功", token, memberInfo);
	}
}
