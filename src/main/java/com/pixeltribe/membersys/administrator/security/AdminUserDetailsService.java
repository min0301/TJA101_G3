package com.pixeltribe.membersys.administrator.security;

import com.pixeltribe.membersys.administrator.model.AdmRepository;
import com.pixeltribe.membersys.administrator.model.Administrator;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
class AdminUserDetailsService implements UserDetailsService {

    private final AdmRepository adminRepo;

    public AdminUserDetailsService(AdmRepository adminRepo) {
        this.adminRepo = adminRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Administrator admin = adminRepo.findByAdmAccount(username);
        if (admin == null) {
            throw new UsernameNotFoundException("找不到此管理員: " + username);
        }
        return new AdminDetails(admin);
    }
}
