package com.pixeltribe.membersys.login.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pixeltribe.membersys.login.DTO.MemLoginResult;
import com.pixeltribe.membersys.login.model.MemLoginService;



@RestController
@RequestMapping("/api/mem")
public class MemLoginController {
	
	@Autowired
	private MemLoginService memLoginService;
	
	@PostMapping("/login")
	public Map<String, Object> login(@RequestBody Map<String, String> payload) {
		String memAccount = payload.get("memAccount");
		String memPassword = payload.get("memPassword");
		
		Map<String, Object> result = new HashMap<>();
		MemLoginResult loginResult = memLoginService.login(memAccount, memPassword);
		result.put("success", loginResult.isSuccess());
		result.put("message", loginResult.getMessage());
		return result;
	}
}
