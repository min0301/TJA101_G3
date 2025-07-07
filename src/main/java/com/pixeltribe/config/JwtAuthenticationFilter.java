package com.pixeltribe.config;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.pixeltribe.membersys.administrator.model.AdmService;
import com.pixeltribe.membersys.member.model.MemService;
import com.pixeltribe.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final MemService memberService;
	private final AdmService admService;

	public JwtAuthenticationFilter(JwtUtil jwtUtil, MemService memberService, AdmService admService) {
		this.jwtUtil = jwtUtil;
		this.memberService = memberService;
		this.admService = admService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
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
			if (jwtUtil.validateToken(token)) {
				// 可選：可進一步查詢會員資料
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username,
						null, List.of());
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		}
		// 3. 通過驗證才往下
		filterChain.doFilter(request, response);
	}
}
