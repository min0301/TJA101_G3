package com.pixeltribe.util;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import com.pixeltribe.membersys.administrator.model.Administrator;
import com.pixeltribe.membersys.member.model.Member;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtil {
    private final String SECRET_KEY = "PPPPPIIIIIXXXXXEEEEELLLLL_TTTTTRRRRRIIIIIBBBBBEEEEE";

    public String generateAdminToken(Administrator administrator) {
        return Jwts.builder().setSubject(administrator.getAdmAccount()).claim("admId", administrator.getId()).claim("role", administrator.getRole())
                .setIssuedAt(new Date()).setExpiration(Date.from(Instant.now().plus(7, ChronoUnit.DAYS))) // 7天有效
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY.getBytes()).compact();
    }

    public String generateMemberToken(Member member) {
        return Jwts.builder().setSubject(member.getMemAccount()).claim("memId", member.getId()).claim("role", member.getRole())
                .setIssuedAt(new Date()).setExpiration(Date.from(Instant.now().plus(7, ChronoUnit.DAYS))) // 7天有效
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY.getBytes()).compact();
    }

    public String extractUsername(String token) {
//		return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody().getSubject();
        // 【修改點】確保這裡也使用 .getBytes()
        return Jwts.parser().setSigningKey(SECRET_KEY.getBytes()).parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateToken(String token) {
        try {
//			Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            // 【修改點】確保這裡也使用 .getBytes()
            Jwts.parser().setSigningKey(SECRET_KEY.getBytes()).parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public String extractRole(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY.getBytes()).parseClaimsJws(token).getBody().get("role", String.class);
    }

    public String extractToken(HttpServletRequest request) {
        // 優先從 Cookie 抓
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        // 或從 Header 抓（可視情況）
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    // 解碼 JWT
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY.getBytes(StandardCharsets.UTF_8))
                .parseClaimsJws(token)
                .getBody();
    }

    public Integer extractMemberId(String token) {
        Claims claims = extractClaims(token);
        return claims.get("admId", Integer.class); // admId 需在 token payload 裡有
    }

    public Integer extractAdminId(String token) {
        Claims claims = extractClaims(token);
        return claims.get("admId", Integer.class);     // 取的是 admId
    }
    public Integer getAdminIdFromJWT(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) throw new RuntimeException("JWT 不存在");
        return extractMemberId(token);
    }

}
