package com.pixeltribe.membersys.login.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pixeltribe.membersys.login.dto.AdmLoginReturn;
import com.pixeltribe.membersys.login.model.AdmLoginService;

@RestController
@RequestMapping("/api/adm")
public class AdmLoginController {

	@Autowired
	private  AdmLoginService admLoginService;
	
    @PostMapping("/login")
    public AdmLoginReturn login(@RequestBody Map<String, String> payload) {
        String admAccount = payload.get("admAccount");
        String admPassword = payload.get("admPassword");
        return admLoginService.login(admAccount, admPassword);
    }
}
