package com.pixeltribe.membersys.security;

import com.pixeltribe.membersys.member.model.MemRepository;
import com.pixeltribe.membersys.member.model.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MemberUserDetailsService implements UserDetailsService {

    @Autowired
    private MemRepository memRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 【調整點】直接呼叫現有的方法，它會回傳 Member 或 null
        Member member = memRepository.findByMemAccount(username);

        // 【調整點】手動檢查回傳結果是否為 null
        if (member == null) {
            // 如果找不到會員，就拋出 Spring Security 定義的例外
            throw new UsernameNotFoundException("找不到此用戶: " + username);
        }

        // 如果找到了，後續的邏輯完全不變
        return new MemberDetails(member);
    }

}
