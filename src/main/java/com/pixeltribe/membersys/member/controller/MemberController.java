package com.pixeltribe.membersys.member.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.pixeltribe.membersys.login.model.MemForgetPasswordService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/mem")
public class MemberController {

	@Autowired
	private MemForgetPasswordService MemForgetPasswordService;

	@PostMapping("/forgot-password")
	public Map<String, Object> sendForgotPasswordMail(@RequestBody Map<String, String> payload) {
		String email = payload.get("email");
		Map<String, Object> result = new HashMap<>();
		if (email == null || email.isBlank()) {
			result.put("success", false);
			result.put("message", "請輸入Email");
			return result;
		}
		boolean ok = MemForgetPasswordService.sendEmailAuthCode(email);
		if (!ok) {
			result.put("success", false);
			result.put("message", "查無此帳號Email");
			return result;
		} else {
			result.put("success", true);
			result.put("message", "已寄出驗證信，請至信箱查收!!");
			return result;
		}
	}
}
