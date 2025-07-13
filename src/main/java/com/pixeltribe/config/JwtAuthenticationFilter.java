package com.pixeltribe.config;

import com.pixeltribe.util.JwtUtil;
import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    //	private final MemService memberService;
    //	private final AdmService admService;
    private final UserDetailsService userDetailsService;

    //	public JwtAuthenticationFilter(JwtUtil jwtUtil, MemService memberService, AdmService admService) {
//		this.jwtUtil = jwtUtil;
//		this.memberService = memberService;
//		this.admService = admService;
//	}
    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        // 1. 取得 header 裡的 token
        String header = request.getHeader("Authorization");
        String token = null;
        String username = null;

        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7); // 取出 Bearer 後的內容
            username = jwtUtil.extractUsername(token); // 從 token 解析帳號
        }

        // 2. 判斷 token 合法
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            //載入完整的 UserDetails
            // 說明：這是最核心的修正。我們不再只滿足於從 Token 拿到 username 字串，
            // 而是利用 UserDetailsService 去資料庫把包含所有資訊的 MemberDetails 物件撈出來。
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(token)) {
                // 可選：可進一步查詢會員資料
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

            }
        }
        // 3. 通過驗證才往下
        filterChain.doFilter(request, response);
    }
}
