package com.pixeltribe.membersys.login.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.pixeltribe.membersys.administrator.model.AdmRepository;
import com.pixeltribe.membersys.administrator.model.Administrator;
import com.pixeltribe.membersys.login.dto.AdmLoginResult;

@Service
public class AdmLoginService {
	
	@Autowired
	private AdmRepository admRepository;
	
	public AdmLoginResult login(String admAccount, String admPassword) {
        Administrator admin = admRepository.findByAdmAccount(admAccount);
        if (admin == null) {
            return new AdmLoginResult(false, "該管理員帳號不存在");
        }
        if (!admin.getAdmPassword().equals(admPassword)) {
            return new AdmLoginResult(false, "密碼錯誤");
        }
        return new AdmLoginResult(true, "登入成功");
    }
}
