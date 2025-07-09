package com.pixeltribe.membersys.member.controller;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pixeltribe.membersys.login.model.MemForgetPasswordService;
import com.pixeltribe.membersys.member.model.MemRepository;
import com.pixeltribe.membersys.member.model.Member;

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
			result.put("message", "請確認輸入的密碼及驗證碼");
			return result;
		}

		if (!password.equals(passwordConfirm)) {
			result.put("success", false);
			result.put("message", "請確認兩次密碼輸入是否一致");
			return result;
		}

		// 將前端送出的驗證碼和DB中的驗證碼進行比對
		if (dbVcode == null || !dbVcode.equals(Vcode)) {
			result.put("success", false);
			result.put("message", "驗證碼錯誤");
			return result;
		}

		// 檢查驗證碼是否過期(5mins)
		Instant sendTime = member.getSendAuthEmailTime();
		if (sendTime == null || Duration.between(sendTime, Instant.now()).toMinutes() > 5) {
			result.put("success", false);
			result.put("message", "為了保護使用者帳號安全，驗證碼具有效期限，請重新申請");
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

	@PostMapping("/reset-password")
	public Map<String, Object> resetPassword(@RequestBody Map<String, String> payload) {

		String oldPassword = payload.get("oldPassword");
		String newPassword = payload.get("newPassword");
		String newPasswordConfirm = payload.get("newPasswordConfirm");
		Member member = memrepository.findByMemPassword(oldPassword);
		String dbPassword = member.getMemPassword();
		Map<String, Object> result = new HashMap<>();

		if (!oldPassword.equals(dbPassword)) {
			result.put("success", false);
			result.put("message", "請確認原密碼無誤");
			return result;
		}

		if (!newPassword.equals(newPasswordConfirm)) {
			result.put("success", false);
			result.put("message", "請確認兩次密碼輸入相符");
			return result;
		}

		member.setMemPassword(newPassword);
		memrepository.save(member);
		result.put("success", true);
		result.put("message", "密碼已更新");
		return result;
	}

	@PostMapping("/check-email")
	public Map<String, Object> registerMailCheck(@RequestBody Map<String, String> payload) {
		String email = payload.get("email");
		Map<String, Object> result = new HashMap<>();

		// 檢查信箱是否已被註冊
		boolean mailExist = memrepository.existsByMemEmail(email);
		result.put("exist", mailExist); // true:已註冊 ; false:可使用
		if (mailExist) {
			result.put("message", "此信箱已被註冊");
		} else {
			result.put("message", "✅此信箱可使用");
		}
		return result;
	}

	@PostMapping("/register")
	public Map<String, Object> memRegister(@RequestBody Map<String, String> payload) {
		Map<String, Object> result = new HashMap<>();
		// 從JSON提取資料
		String memAccount = payload.get("memAccount");
		String memEmail = payload.get("memEmail");
		String memName = payload.get("memName");
		String memNickName = payload.get("memNickName");
		String memBirthday = payload.get("memBirthday");
		String memAddr = payload.get("memAddr");
		String memPhone = payload.get("memPhone");
		String memPassword = payload.get("memPassword");
		
		//設定會員狀態及Role
		char memStatus = 1;
		String role = "ROLE_USER";

		// 將資料寫入新member
		Member member = new Member();
		
		try {
			member.setMemAccount(memAccount);
			member.setMemEmail(memEmail);
			member.setMemName(memName);
			member.setMemNickName(memNickName);
			member.setMemBirthday(LocalDate.parse(memBirthday));
			member.setMemAddr(memAddr);
			member.setMemPhone(memPhone);
			member.setMemPassword(memPassword);
			
			member.setMemStatus(memStatus);
			member.setRole(role);

			// 將新member存入DB
			member = memrepository.save(member);

			// 回傳成功
			result.put("success", true);
			result.put("message", "歡迎加入, pixi!");
			return result;
		} catch (Exception ex) {
			// 捕捉例外狀況，回傳失敗
			result.put("success", false);
			result.put("message", "註冊失敗: " + ex.getMessage());
		}
		return result;
	}
}