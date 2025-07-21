package com.pixeltribe.membersys.member.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.pixeltribe.membersys.member.dto.MemberGameDto;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.pixeltribe.membersys.login.model.MemForgetPasswordService;
import com.pixeltribe.membersys.member.dto.MemberAdminDto;
import com.pixeltribe.membersys.member.dto.MemberProfileDto;

@Service
public class MemService {

	@Autowired
	private MemRepository memRepository;

	@Autowired
	private MemForgetPasswordService memForgetPasswordService;

	// 新增
	@Transactional
	public void addMem(Member member) {
		memRepository.save(member);
	}

	// 修改
	@Transactional
	public void updateMem(Member member) {
		memRepository.save(member);
	}

	// 依ID查單一會員
	public Member getOneMem(Integer memNo) {
		Optional<Member> optional = memRepository.findById(memNo);
		return optional.orElse(null);
	}

	// 查詢所有會員
	public List<Member> findAll() {
		return memRepository.findAll();
	}

	// 查詢會員總數
	public long getMemberCount() {
		return memRepository.count();
	}
	
    // 查詢被停權會員數
    public long getSuspendedMemberCount() {
        return memRepository.countByMemStatus('2');
    }

    // 取得近7天新註冊會員數
    public long getWeeklyNewMembersCount() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return memRepository.countByMemCreateAfter(sevenDaysAgo);
    }
        
	// 發送驗證信
	public Map<String, Object> sendForgotPasswordMail(String email) {
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
		} else {
			result.put("success", true);
			result.put("message", "已寄出驗證信，請至信箱查收!!");
		}
		return result;
	}

	// 忘記密碼用驗證碼重設密碼
	@Transactional
	public Map<String, Object> resetPasswordByVcode(String email, String password, String passwordConfirm,
			String vcode) {
		Map<String, Object> result = new HashMap<>();
		if (email == null || password == null || passwordConfirm == null || vcode == null) {
			result.put("success", false);
			result.put("message", "請確認輸入的密碼及驗證碼");
			return result;
		}
		Member member = memRepository.findByMemEmail(email);
		if (member == null) {
			result.put("success", false);
			result.put("message", "查無此會員");
			return result;
		}
		String dbVcode = member.getMemEmailAuth();
		if (!password.equals(passwordConfirm)) {
			result.put("success", false);
			result.put("message", "請確認兩次密碼輸入是否一致");
			return result;
		}
		if (dbVcode == null || !dbVcode.equals(vcode)) {
			result.put("success", false);
			result.put("message", "驗證碼錯誤");
			return result;
		}
		Instant sendTime = member.getSendAuthEmailTime();
		if (sendTime == null || Duration.between(sendTime, Instant.now()).toMinutes() > 5) {
			result.put("success", false);
			result.put("message", "為了保護使用者帳號安全，驗證碼具有效期限，請重新申請");
			return result;
		}
		member.setMemPassword(password);
		member.setMemEmailAuth(null);
		member.setSendAuthEmailTime(null);
		memRepository.save(member);
		result.put("success", true);
		result.put("message", "密碼已更新，請重新登入");
		return result;
	}

	// 會員中心重設密碼
	@Transactional
	public Map<String, Object> resetPassword(String oldPassword, String newPassword, String newPasswordConfirm,
			String Id) {
		Map<String, Object> result = new HashMap<>();
		Member member = memRepository.findById(Integer.parseInt(Id)).orElse(null);
		if (member == null) {
			result.put("success", false);
			result.put("message", "查無此會員");
			return result;
		}
		String dbPassword = member.getMemPassword();
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
		memRepository.save(member);
		result.put("success", true);
		result.put("message", "密碼已更新");
		return result;
	}

	// 註冊時檢查信箱是否存在
	public Map<String, Object> checkEmail(String email) {
		Map<String, Object> result = new HashMap<>();
		boolean mailExist = memRepository.existsByMemEmail(email);
		result.put("exist", mailExist);
		if (mailExist) {
			result.put("message", "❌信箱已被註冊");
		} else {
			result.put("message", "		✅ +1");
		}
		return result;
	}

	// 註冊時檢查帳號是否存在
	public Map<String, Object> checkAccount(String account) {
		Map<String, Object> result = new HashMap<>();
		boolean accountExist = memRepository.existsByMemAccount(account);
		result.put("exist", accountExist);
		if (accountExist) {
			result.put("message", "❌帳號名稱已被註冊");
		} else {
			result.put("message", "		✅ +1");
		}
		return result;
	}

	// 註冊會員
	@Transactional
	public Map<String, Object> registerMember(Map<String, String> payload) {
		Map<String, Object> result = new HashMap<>();
		try {
			String memAccount = payload.get("memAccount");
			String memEmail = payload.get("memEmail");
			String memName = payload.get("memName");
			String memNickName = payload.get("memNickName");
			String memBirthday = payload.get("memBirthday");
			String memAddr = payload.get("memAddr");
			String memPhone = payload.get("memPhone");
			String memPassword = payload.get("memPassword");
			char memStatus = '1';
			String role = "ROLE_USER";
			Member member = new Member();
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
			memRepository.save(member);
			result.put("success", true);
			result.put("message", "歡迎加入, pixi!");
		} catch (Exception ex) {
			result.put("success", false);
			result.put("message", "註冊失敗: " + ex.getMessage());
		}
		return result;
	}

	// 用id查會員並提取個人資料頁面所需資料
	public MemberProfileDto getProfileDtoById(Integer id) {
		Optional<Member> opt = memRepository.findById(id);
		if (opt.isEmpty())
			return null;
		Member member = opt.get();

		MemberProfileDto dto = new MemberProfileDto();
		dto.setId(member.getId());
		dto.setMemName(member.getMemName());
		dto.setMemBirthday(member.getMemBirthday());
		dto.setMemAccount(member.getMemAccount());
		dto.setMemNickName(member.getMemNickName());
		dto.setMemEmail(member.getMemEmail());
		dto.setMemAddr(member.getMemAddr());
		dto.setMemPhone(member.getMemPhone());
		dto.setMemIconData(member.getMemIconData());
		return dto;
	}

	@Transactional
	public boolean updateProfile(Integer id, Map<String, String> payload) {
		Optional<Member> opt = memRepository.findById(id);
		if (opt.isEmpty())
			return false;
		Member member = opt.get();

		// 更新欄位
		member.setMemName(payload.get("memName"));
		member.setMemNickName(payload.get("memNickName"));
		member.setMemEmail(payload.get("memEmail"));
		member.setMemAddr(payload.get("memAddr"));
		member.setMemPhone(payload.get("memPhone"));
		String iconData = payload.get("memIconData");
		if (iconData != null) {
			member.setMemIconData(iconData);
		}
		memRepository.save(member);

		return true;
	}

	// 分頁查詢會員
	public Page<MemberAdminDto> findAllAdminMembers(String keyword, Pageable pageable) {
		if (keyword == null || keyword.trim().isEmpty()) {
			// 沒有 keyword 時，回傳全部分頁
			return memRepository.findAll(pageable)
					.map(member -> new MemberAdminDto(member.getId(), member.getMemName(), member.getMemNickName(),
							member.getMemAccount(), member.getMemEmail(), member.getMemAddr(), member.getMemPhone(),
							member.getMemBirthday(), member.getMemCreate(), member.getMemStatus()));
		} else {
			// 有 keyword 時，進行條件搜尋
			return memRepository.findByKeyword(keyword, pageable)
					.map(member -> new MemberAdminDto(member.getId(), member.getMemName(), member.getMemNickName(),
							member.getMemAccount(), member.getMemEmail(), member.getMemAddr(), member.getMemPhone(),
							member.getMemBirthday(), member.getMemCreate(), member.getMemStatus()));
		}
	}

	// 停權狀態切換
	@Transactional
	public void updateMemberStatus(Integer id, Character status) {
		Member member = memRepository.findById(id).orElseThrow(() -> new RuntimeException("會員不存在"));
		member.setMemStatus(status);
		memRepository.save(member);
	}

	public List<MemberGameDto> getMemberGameScore() {
		List<MemberGameDto> memberGameScores = memRepository.findMemberGameScores();
		if (memberGameScores == null || memberGameScores.isEmpty()) {
			return List.of(); // 返回空列表而不是 null
		}
		return memberGameScores;
	}

	public MemberGameDto updateMemberGameScore(Integer id, Integer newScore) {
		Member m = memRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("會員不存在"));

		Integer old = m.getPoint();
		if (old == null || newScore > old) {
			m.setPoint(newScore);
		}
		memRepository.save(m);

		return new MemberGameDto(m.getId(), m.getMemName(), m.getMemNickName(), m.getPoint());
	}

	public MemberGameDto getMemberGameScoreById(Integer id) {
		Member member = memRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("會員不存在"));
		return new MemberGameDto(member.getId(), member.getMemName(), member.getMemNickName(), member.getPoint());
	}

	// 存會員頭像進DB
	public void updateAvatar(Integer memberId, MultipartFile avatarFile) throws IOException {
		Member member = memRepository.findById(memberId).orElseThrow(() -> new RuntimeException("找不到會員"));

		// 取得副檔名
		String ext = StringUtils.getFilenameExtension(avatarFile.getOriginalFilename());
		String filename = "mem" + memberId + "." + ext;

		// 設定儲存資料夾
		String rootDir = System.getProperty("user.dir");
		Path uploadDir = Paths.get(rootDir, "./uploads/memberAvatar");
		if (!Files.exists(uploadDir))
			Files.createDirectories(uploadDir);

		Path filePath = uploadDir.resolve(filename);
		avatarFile.transferTo(filePath.toFile());

		// 刪除舊頭像
		String oldFile = member.getMemIconData();
		if (oldFile != null && !oldFile.isBlank() && !oldFile.equals(filename)) {
			Path oldPath = uploadDir.resolve(oldFile);
			Files.deleteIfExists(oldPath);
		}

		// 存檔名進DB
		member.setMemIconData(filename);
		memRepository.save(member);
	}

}
