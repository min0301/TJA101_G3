package com.pixeltribe.membersys.member.controller;

import com.pixeltribe.membersys.member.model.MemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    @Autowired
    private MemService memberService;

    @PostMapping("/forgot-password")
    public Map<String, Object> sendForgotPasswordMail(@RequestBody Map<String, String> payload) {
        return memberService.sendForgotPasswordMail(payload.get("email"));
    }

    @PostMapping("/reset-passwordV")
    public Map<String, Object> resetPasswordV(@RequestBody Map<String, String> payload) {
        return memberService.resetPasswordByVcode(
                payload.get("email"),
                payload.get("password"),
                payload.get("passwordConfirm"),
                payload.get("Vcode")
        );
    }

    @PostMapping("/reset-password")
    public Map<String, Object> resetPassword(@RequestBody Map<String, String> payload) {
        return memberService.resetPassword(
                payload.get("oldPassword"),
                payload.get("newPassword"),
                payload.get("newPasswordConfirm")
        );
    }

    @PostMapping("/check-email")
    public Map<String, Object> registerMailCheck(@RequestBody Map<String, String> payload) {
        return memberService.checkEmail(payload.get("email"));
    }

    @PostMapping("/register")
    public Map<String, Object> memRegister(@RequestBody Map<String, String> payload) {
        return memberService.registerMember(payload);
    }
}
