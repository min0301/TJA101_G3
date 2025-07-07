package com.pixeltribe.membersys.login.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pixeltribe.membersys.login.dto.AdmLoginResult;
import com.pixeltribe.membersys.login.model.AdmLoginService;


@RestController
@RequestMapping("/api/adm")
public class AdmLoginController {

	@Autowired
	private  AdmLoginService admLoginService;
	

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> payload) {
        String admAccount = payload.get("admAccount");
        String admPassword = payload.get("admPassword");

        Map<String, Object> result = new HashMap<>();
        AdmLoginResult loginResult = admLoginService.login(admAccount, admPassword);
        result.put("success", loginResult.isSuccess());
        result.put("message", loginResult.getMessage());
        return result;
    }
}
