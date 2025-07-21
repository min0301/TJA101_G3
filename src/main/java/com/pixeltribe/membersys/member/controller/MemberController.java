package com.pixeltribe.membersys.member.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pixeltribe.membersys.member.dto.MemberGameDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.pixeltribe.membersys.member.dto.MemberAdminDto;
import com.pixeltribe.membersys.member.dto.MemberProfileDto;
import com.pixeltribe.membersys.member.model.MemService;

@RestController
@RequestMapping("/api/members")
public class MemberController {

	@Autowired
	private MemService memService;

	//查詢會員遊戲分數
	@GetMapping("/game-score/{id}")
	public MemberGameDto getMemberGameScoreById(@PathVariable Integer id) {
		return memService.getMemberGameScoreById(id);
	}

	// 查詢所有會員遊戲分數
	@GetMapping("/game-score")
	public List<MemberGameDto> getMemberGameScore() {
		return memService.getMemberGameScore();
	}

	// 更新會員遊戲分數
	@PatchMapping("/update-game-score")
	public MemberGameDto updateMemberGameScore(@RequestBody Map<String, Integer> payload) {
		Integer newScore = payload.get("score");
		Integer id = payload.get("id");
		return memService.updateMemberGameScore(id, newScore);
	}

	// 寄出認證信
	@PostMapping("/forgot-password")
	public Map<String, Object> sendForgotPasswordMail(@RequestBody Map<String, String> payload) {
		return memService.sendForgotPasswordMail(payload.get("email"));
	}

	// 用驗證碼重設密碼(忘記密碼時)
	@PostMapping("/reset-passwordV")
	public Map<String, Object> resetPasswordV(@RequestBody Map<String, String> payload) {
		return memService.resetPasswordByVcode(payload.get("email"), payload.get("password"),
				payload.get("passwordConfirm"), payload.get("Vcode"));
	}

	// 會員中心重設密碼
	@PostMapping("/reset-password")
	public Map<String, Object> resetPassword(@RequestBody Map<String, String> payload) {
		return memService.resetPassword(payload.get("oldPassword"), payload.get("newPassword"),
				payload.get("newPasswordConfirm"), payload.get("id"));
	}

	// 註冊時檢查email重複
	@PostMapping("/check-email")
	public Map<String, Object> registerMailCheck(@RequestBody Map<String, String> payload) {
		return memService.checkEmail(payload.get("email"));
	}
	
	// 註冊時檢查帳號重複
	@PostMapping("/check-account")
	public Map<String, Object> registerAccountCheck(@RequestBody Map<String, String> payload){
		return memService.checkAccount(payload.get("account"));
	}

	// 註冊
	@PostMapping("/register")
	public Map<String, Object> memRegister(@RequestBody Map<String, String> payload) {
		return memService.registerMember(payload);
	}

	// 查詢個人資料，JWT 需驗證
	@GetMapping("/profile/{id}")
	public ResponseEntity<MemberProfileDto> getProfile(@PathVariable Integer id,
			@RequestHeader("Authorization") String authorizationHeader) {
		MemberProfileDto dto = memService.getProfileDtoById(id);
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
			boolean success = memService.updateProfile(id, payload);
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
	
	// 儀錶板顯示會員總數
	@GetMapping("/admin/count")
	public ResponseEntity<Long> getMemberCount() {
        long count = memService.getMemberCount();
        return ResponseEntity.ok(count);
    }
	
	// 顯示被停權會員總數
	@GetMapping("/admin/suspended-count")
	public ResponseEntity<Long> getSuspendedMemberCount() {
	    long count = memService.getSuspendedMemberCount();
	    return ResponseEntity.ok(count);
	}
	
	// 近7天新註冊會員數
	@GetMapping("/admin/newlySign")
	public ResponseEntity<Long> getWeeklyNewMembersCount() {
	    long count = memService.getWeeklyNewMembersCount();
	    return ResponseEntity.ok(count);
	}
	
	// 會員分頁查詢
	@GetMapping("/admin/allMembers")
	public Page<MemberAdminDto> findAllMembers(
			@PageableDefault(size = 5, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
			@RequestParam(required = false) String keyword) {
		return memService.findAllAdminMembers(keyword, pageable);
	}

	// 停權狀態切換
	@PutMapping("/admin/allMembers/status/{id}")
	public ResponseEntity<?> updateMemberStatus(@PathVariable Integer id, @RequestBody Map<String, Object> payload) {
		Object statusObj = payload.get("status");
		if (statusObj == null) {
			return ResponseEntity.badRequest().body("缺少 status");
		}
		Character newStatus = statusObj.toString().charAt(0); // 假設 '1'=正常、'0'=停權
		memService.updateMemberStatus(id, newStatus);
		return ResponseEntity.ok().build();
	}

	// 上傳會員頭像
	@PostMapping("/{id}/avatar")
	public ResponseEntity<?> uploadAvatar(@PathVariable Integer id, @RequestParam("avatar") MultipartFile avatarFile) {
		try {
			memService.updateAvatar(id, avatarFile);
			return ResponseEntity.ok(Map.of("success", true));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("success", false, "message", "頭像上傳失敗"));
		}
	}

}
