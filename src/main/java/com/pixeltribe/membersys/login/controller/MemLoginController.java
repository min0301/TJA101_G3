package com.pixeltribe.membersys.login.controller;

import java.nio.file.AccessDeniedException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pixeltribe.membersys.login.dto.MemLoginReturn;
import com.pixeltribe.membersys.login.model.MemLoginService;
import com.pixeltribe.membersys.member.dto.MemberPrivateDTO;
import com.pixeltribe.membersys.member.model.MemService;

@RestController
@RequestMapping("/api/mem")
public class MemLoginController {

	@Autowired
	private MemLoginService memLoginService;

	@PostMapping("/login")
	public MemLoginReturn login(@RequestBody Map<String, String> payload) {
		String memAccount = payload.get("memAccount");
		String memPassword = payload.get("memPassword");
		return memLoginService.login(memAccount, memPassword);
	}
	
	@GetMapping("/private/{id}")
	public MemberPrivateDTO getPrivateProfile(@PathVariable Integer id, @AuthenticationPrincipal MemberPrincipal principal) {
	    if (!id.equals(principal.getMemberId())) {
	        throw new AccessDeniedException("禁止存取他人資料");
	    }
	    return MemService.getPrivateInfo(id);
	}
}
