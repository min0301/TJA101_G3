package com.pixeltribe.membersys.member.controller;

import com.pixeltribe.membersys.member.dto.MemberProfileDto;
import com.pixeltribe.membersys.member.model.MemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/members")
public class MemberController {

	@Autowired
	private MemService memberService;
	
	// 寄出認證信
	@PostMapping("/forgot-password")
	public Map<String, Object> sendForgotPasswordMail(@RequestBody Map<String, String> payload) {
		return memberService.sendForgotPasswordMail(payload.get("email"));
	}
	
	// 用驗證碼重設密碼(忘記密碼時)
	@PostMapping("/reset-passwordV")
	public Map<String, Object> resetPasswordV(@RequestBody Map<String, String> payload) {
		return memberService.resetPasswordByVcode(payload.get("email"), payload.get("password"),
				payload.get("passwordConfirm"), payload.get("Vcode"));
	}
	
	// 會員中心重設密碼
	@PostMapping("/reset-password")
	public Map<String, Object> resetPassword(@RequestBody Map<String, String> payload) {
		return memberService.resetPassword(payload.get("oldPassword"), payload.get("newPassword"),
				payload.get("newPasswordConfirm"), payload.get("id"));
	}
	
	// 註冊時檢查email重複
	@PostMapping("/check-email")
	public Map<String, Object> registerMailCheck(@RequestBody Map<String, String> payload) {
		return memberService.checkEmail(payload.get("email"));
	}
	
	// 註冊
	@PostMapping("/register")
	public Map<String, Object> memRegister(@RequestBody Map<String, String> payload) {
		return memberService.registerMember(payload);
	}

	// 查詢個人資料，JWT 需驗證
	@GetMapping("/profile/{id}")
	public ResponseEntity<MemberProfileDto> getProfile(@PathVariable Integer id,
			@RequestHeader("Authorization") String authorizationHeader) {
		MemberProfileDto dto = memberService.getProfileDtoById(id);
		if (dto == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(dto);
	}
}
