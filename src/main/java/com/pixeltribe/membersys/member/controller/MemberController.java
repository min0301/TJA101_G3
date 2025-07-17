package com.pixeltribe.membersys.member.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pixeltribe.membersys.member.dto.MemberProfileDto;
import com.pixeltribe.membersys.member.model.MemRepository;
import com.pixeltribe.membersys.member.model.MemService;
import com.pixeltribe.membersys.member.model.Member;

@RestController
@RequestMapping("/api/members")
public class MemberController {

	@Autowired
	private MemService memberService;
	@Autowired
	private MemRepository memRepository;

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

	// 編輯個人資料
	@PostMapping("/editProfile/{id}")
	public Map<String, Object> updateProfile(@PathVariable Integer id, @RequestBody Map<String, String> payload) {

		Map<String, Object> result = new HashMap<>();
		try {
			boolean success = memberService.updateProfile(id, payload);
			if (success) {
				result.put("success", true);
				result.put("message", "會員資料已更新");
			} else {
				result.put("success", false);
				result.put("message", "找不到會員");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			result.put("success", false);
			result.put("message", "更新失敗: " + ex.getMessage());
		}
		return result;
	}

	@GetMapping("/admin/allMembers")
	public Page<Member> findAllMembers(Pageable pageable) {
	    return memRepository.findAll(pageable);
	}
}
