package com.pixeltribe.membersys.member.model;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pixeltribe.membersys.login.model.MemForgetPasswordService;
import com.pixeltribe.membersys.member.dto.MemberProfileDto;

@Service
public class MemService {

    @Autowired
    private MemRepository memrepository;

    @Autowired
    private MemForgetPasswordService memForgetPasswordService;

    // 新增
    public void addMem(Member member) {
        memrepository.save(member);
    }

    // 修改
    public void updateMem(Member member) {
        memrepository.save(member);
    }

    // 依ID查單一會員
    public Member getOneMem(Integer memNo) {
        Optional<Member> optional = memrepository.findById(memNo);
        return optional.orElse(null);
    }

    // 查詢所有會員
    public List<Member> findAll() {
        return memrepository.findAll();
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
    public Map<String, Object> resetPasswordByVcode(String email, String password, String passwordConfirm, String vcode) {
        Map<String, Object> result = new HashMap<>();
        if (email == null || password == null || passwordConfirm == null || vcode == null) {
            result.put("success", false);
            result.put("message", "請確認輸入的密碼及驗證碼");
            return result;
        }
        Member member = memrepository.findByMemEmail(email);
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
        memrepository.save(member);
        result.put("success", true);
        result.put("message", "密碼已更新，請重新登入");
        return result;
    }

    // 會員中心重設密碼
    public Map<String, Object> resetPassword(String oldPassword, String newPassword, String newPasswordConfirm, String Id) {
        Map<String, Object> result = new HashMap<>();
        Member member = memrepository.findById(Integer.parseInt(Id)).orElse(null);
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
        memrepository.save(member);
        result.put("success", true);
        result.put("message", "密碼已更新");
        return result;
    }

    // 註冊時檢查信箱是否存在
    public Map<String, Object> checkEmail(String email) {
        Map<String, Object> result = new HashMap<>();
        boolean mailExist = memrepository.existsByMemEmail(email);
        result.put("exist", mailExist);
        if (mailExist) {
            result.put("message", "此信箱已被註冊");
        } else {
            result.put("message", "✅此信箱可使用");
        }
        return result;
    }

    // 註冊會員
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
            char memStatus = 1;
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
            memrepository.save(member);
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
        Optional<Member> opt = memrepository.findById(id);
        if (opt.isEmpty()) return null;
        Member member = opt.get();

        MemberProfileDto dto = new MemberProfileDto();
        dto.setMemName(member.getMemName());
        dto.setMemBirthday(member.getMemBirthday());
        dto.setMemAccount(member.getMemAccount());
        dto.setMemNickName(member.getMemNickName());
        dto.setMemEmail(member.getMemEmail());
        dto.setMemAddr(member.getMemAddr());
        dto.setMemPhone(member.getMemPhone());

        return dto;
    }

	public boolean updateProfile(Integer id, Map<String, String> payload) {
		Optional<Member> opt = memrepository.findById(id);
		if (opt.isEmpty()) return false;
		Member member = opt.get();
		
		//更新欄位
		member.setMemName(payload.get("memName"));
		member.setMemNickName(payload.get("memNickName"));
		member.setMemEmail(payload.get("memEmail"));
		member.setMemAddr(payload.get("memAddr"));
		member.setMemPhone(payload.get("memPhone"));
		memrepository.save(member);
		
		return true;
	}
}
