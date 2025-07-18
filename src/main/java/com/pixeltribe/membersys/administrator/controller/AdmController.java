package com.pixeltribe.membersys.administrator.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pixeltribe.membersys.administrator.model.AdmRepository;
import com.pixeltribe.membersys.administrator.model.AdmService;
import com.pixeltribe.membersys.administrator.model.Administrator;

@RestController
@RequestMapping("/api/adm")
public class AdmController {

	@Autowired
	private AdmRepository admRepository;
	@Autowired
	private AdmService admService;
	
	@PostMapping("/check-email")
	public Map<String, Object> registerMailCheck(@RequestBody Map<String, String> payload) {
		String email = payload.get("email");
		Map<String, Object> result = new HashMap<>();

		// 檢查信箱是否已被註冊
		boolean mailExist = admRepository.existsByAdmAccount(email);
		result.put("exist", mailExist); // true:已註冊 ; false:可使用
		if (mailExist) {
			result.put("message", "此信箱已被註冊");
		} else {
			result.put("message", "✅此信箱可使用");
		}
		return result;
	}
	
	@PostMapping("/register")
	public Map<String, Object> admRegister(@RequestBody Map<String, String> payload) {
		Map<String, Object> result = new HashMap<>();
		
		//從JSON提取資料
		String admAccount = payload.get("admAccount");
		String admName = payload.get("admName");
		String admPassword = payload.get("admPassword");
		
		//設定管理員Role
		String admRole = "ROLE_ADMIN";
		
		//將資料寫入新administrator
		Administrator administrator = new Administrator();
		
		try {
			administrator.setAdmAccount(admAccount);
			administrator.setAdmName(admName);
			administrator.setAdmPassword(admPassword);
			administrator.setRole(admRole);
			//將新administrator存入DB並回傳成功
			administrator = admRepository.save(administrator);
			
			result.put("success", true);
			result.put("message", "註冊成功!");
		} catch (Exception ex) {
			result.put("success", false);
			result.put("message", "管理員註冊失敗: " + ex.getMessage());
		}
		return result;
	}
}
