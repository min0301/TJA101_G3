package com.pixeltribe.membersys.member.controller;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.pixeltribe.membersys.login.model.MemForgetPasswordService;
import com.pixeltribe.membersys.member.model.MemRepository;
import com.pixeltribe.membersys.member.model.Member;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/members")
public class MemberController {
	
	@Autowired
	private MemRepository memrepository;
	
	@Autowired
	private MemForgetPasswordService memForgetPasswordService;

//	@PostMapping("/{id}")
//	public String update(@Valid Member member, BindingResult result, ModelMap model,
//			@RequestParam("upFiles") MultipartFile[] parts) throws IOException {
//
//		return "";
//	}

	@PostMapping("/forgot-password")
	public Map<String, Object> sendForgotPasswordMail(@RequestBody Map<String, String> payload) {
		String email = payload.get("email");
		Map<String, Object> result = new HashMap<>();
		if (email == null || email.isBlank()) {
			result.put("success", false);
			result.put("message", "請輸入Email");
			return result;
		}
		boolean ok = memForgetPasswordService.sendEmailAuthCode(email);
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

	@PostMapping("/reset-passwordV")
	public Map<String, Object> resetPasswordV(@RequestBody Map<String, String> payload) {
		String password = payload.get("password");
		String passwordConfirm = payload.get("passwordConfirm");
		String Vcode = payload.get("Vcode");
		String email = payload.get("email");
		Member member = memrepository.findByMemEmail(email);
		String dbVcode = member.getMemEmailAuth();
		Map<String, Object> result = new HashMap<>();
		
		// 基本輸入檢查
		if (email == null || password == null || passwordConfirm == null || Vcode == null) {
			result.put("success", false);
			result.put("message", "資料不完整");
			return result;
		}

		if (!password.equals(passwordConfirm)) {
			result.put("success", false);
			result.put("message", "請確認兩次密碼輸入是否一致");
			return result;
		}
		
		//將前端送出的驗證碼和DB中的驗證碼進行比對
	    if (dbVcode == null || !dbVcode.equals(Vcode)) {
	        result.put("success", false);
	        result.put("message", "驗證碼錯誤");
	        return result;
	    }
	    
	    //檢查驗證碼是否過期(5mins)
	    Instant sendTime = member.getSendAuthEmailTime();
	    if (sendTime == null || Duration.between(sendTime, Instant.now()).toMinutes() > 5) {
	        result.put("success", false);
	        result.put("message", "驗證碼已過期，請重新申請");
	        return result;
	    }
	    
	    member.setMemPassword(password);
	    member.setMemEmailAuth(null);
	    member.setSendAuthEmailTime(null);
	    memrepository.save(member);
	    result.put("success", true);
	    result.put("message", "密碼已更新，請重新登入");
	    return result;
	}
}
