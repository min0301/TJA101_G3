package com.pixeltribe.config;

import com.pixeltribe.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;


    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }


    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        // 1. 取得 header 裡的 token
        String header = request.getHeader("Authorization");

//         【診斷日誌 1】檢查請求路徑和 Header
        log.debug("處理請求: {}", request.getRequestURI());
        log.debug("Authorization Header: {}", header);

        // 檢查 Header 是否為 null，或格式是否不以 "Bearer " 開頭
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = header.substring(7); // 取出 Bearer 後的內容
        String username = null;

        // 【【【新增的 try-catch 區塊，捕獲所有 JWT 相關的異常】】】
        try {
            username = jwtUtil.extractUsername(token); // 從 token 解析帳號

            // 【診斷日誌 2】檢查從 Token 中解析出的使用者名稱
            log.debug("從token中提取的使用者名稱: {}", username);


            // 2. 判斷 token 合法
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                //載入完整的 UserDetails
                // 說明：這是最核心的修正。我們不再只滿足於從 Token 拿到 username 字串，
                // 而是利用 UserDetailsService 去資料庫把包含所有資訊的 MemberDetails 物件撈出來。
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // 【【【核心修改點：加入日誌來追蹤 validateToken 的結果】】】
                boolean isTokenValid = jwtUtil.validateToken(token); // 先將結果存到變數中
                // 【診斷日誌 3】印出 Token 驗證結果
                log.debug("token 對使用者是否有效 '{}'? {}", username, String.valueOf(isTokenValid));


                if (isTokenValid) {

                    // 【診斷日誌 4】如果驗證成功，印出訊息
                    log.info("Token 有效。正在 SecurityContext 中設定身份驗證。");

                    // 可選：可進一步查詢會員資料
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                } else {
                    // 【診斷日誌 5】如果驗證失敗，印出訊息
                    log.debug("Token 驗證失敗。不會設定身份驗證。");
                }

            }
        } catch (ExpiredJwtException e) {
            log.warn("JWT Token 已過期", e);
        } catch (SignatureException e) {
            log.warn("JWT Token 簽名無效", e);
        } catch (MalformedJwtException e) {
            log.warn("JWT Token 格式錯誤", e);
        } catch (Exception e) {
            // 捕獲其他所有未知異常，記錄為錯誤(Error)級別
            log.error("JWT Token 驗證時發生未知錯誤", e);
        }

        // 3. 通過驗證才往下
        filterChain.doFilter(request, response);
    }
}
