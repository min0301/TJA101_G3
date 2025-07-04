package com.pixeltribe.membersys.login.model;

import java.security.SecureRandom;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.pixeltribe.membersys.member.model.MemRepository;
import com.pixeltribe.membersys.member.model.Member;

@Service
public class MemForgetPasswordService {

    @Autowired
    private MemRepository memberRepository; // 你的 JPA repository

    @Autowired
    private JavaMailSender mailSender;

    // 1. 產生驗證碼並更新資料庫
    public boolean sendEmailAuthCode(String memEmail) {
        Member member = memberRepository.findByMemEmail(memEmail);
        if (member == null) return false; // 查無此帳號

        String code = generateRandomCode(12);
        Instant now = Instant.now();
        member.setMemEmailAuth(code);
        member.setSendAuthEmailTime(now);
        memberRepository.save(member);

        // 2. 發信
        String subject = "Pixel Tribe 驗證信";
        String text = String.format(
            "Hello Pixi!! \n\n您的驗證碼為：%s\n\n請於10分鐘內在驗證頁面輸入此驗證碼完成認證。\n\nPixelTribe 團隊敬上", code);
        sendSimpleMail(memEmail, subject, text);

        return true;
    }

    private void sendSimpleMail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    private static String generateRandomCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
