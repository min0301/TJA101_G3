package com.pixeltribe.util;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

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
		return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody().getSubject();
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
			return true;
		} catch (JwtException e) {
			return false;
		}
	}

}
