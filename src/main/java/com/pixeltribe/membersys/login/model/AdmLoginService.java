package com.pixeltribe.membersys.login.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pixeltribe.membersys.administrator.dto.AdminBasicDto;
import com.pixeltribe.membersys.administrator.model.AdmRepository;
import com.pixeltribe.membersys.administrator.model.Administrator;
import com.pixeltribe.membersys.login.dto.AdmLoginReturn;
import com.pixeltribe.util.JwtUtil;

@Service
public class AdmLoginService {
	
	@Autowired
	private AdmRepository admRepository;
	
	@Autowired
	private JwtUtil jwtUtil;
	
	public AdmLoginReturn login(String admAccount, String admPassword) {
        Administrator admin = admRepository.findByAdmAccount(admAccount);
        AdminBasicDto adminInfo = new AdminBasicDto(admin.getId(),admin.getAdmAccount(),admin.getAdmName());
        if (admin == null) {
            return new AdmLoginReturn(false, "該管理員帳號不存在");
        }
        if (!admin.getAdmPassword().equals(admPassword)) {
            return new AdmLoginReturn(false, "密碼錯誤");
        }
        String token = jwtUtil.generateAdminToken(admin);
        return new AdmLoginReturn(true, "登入成功", token, adminInfo);
    }
}
