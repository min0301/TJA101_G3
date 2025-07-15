package com.pixeltribe.membersys.security;

import com.pixeltribe.membersys.member.model.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class MemberDetails implements UserDetails {

    private final Member member; // 持有從資料庫查出來的 Member 物件

    public MemberDetails(Member member) {
        this.member = member;
    }

    // 【核心】提供一個方法讓外部可以拿到你的會員 ID
    public Integer getMemberId() {
        return this.member.getId();
    }

    public String getRole() {
        return "ROLE_USER";
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 從 Member 物件中取得角色字串，並包裝成 GrantedAuthority 物件
        // SimpleGrantedAuthority 是 Spring Security 提供的標準實作
        return Collections.singletonList(new SimpleGrantedAuthority(member.getRole()));
    }

    @Override
    public String getPassword() {
        // 回傳會員的密碼
        return this.member.getMemPassword();
    }

    @Override
    public String getUsername() {
        // 回傳會員的帳號
        return this.member.getMemAccount();
    }

    // --- 以下是帳號狀態的方法，先給預設值 true 即可 ---

    @Override
    public boolean isAccountNonExpired() {
        return true; // 帳號是否未過期
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // 帳號是否未鎖定
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 憑證是否未過期
    }

    @Override
    public boolean isEnabled() {
        return true; // 帳號是否啟用
    }


}
