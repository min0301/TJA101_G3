package com.pixeltribe.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.pixeltribe.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class LoginInterceptor implements HandlerInterceptor {
	
	@Autowired
    private JwtUtil jwtUtil;  // 購物車部分需要

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 加入如果是購物車API而且有JWT token，直接通過
    	if (request.getRequestURI().startsWith("/api/cart/")) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                // 有JWT token，讓Spring Security處理，直接通過攔截器
            	
            	// 從 JWT 中提取會員ID
                try {
                    String token = authHeader.substring(7);
                    if (jwtUtil.validateToken(token)) {
                        Integer memberId = jwtUtil.extractMemberIdFromMemberToken(token);
                        request.setAttribute("currentId", memberId);  // 設定會員ID
                    }
                } catch (Exception e) {
                    // JWT 解析失敗，繼續到 Session 檢查
                }
            	
                return true;
            }
        }
    	

    	
    	//取得Session (登入時的設定)
        HttpSession session = request.getSession(false);

        // 檢查是否有登入會員
        if (session == null || session.getAttribute("id") == null) {
            //如果未登入，回傳錯誤訊息提示
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("請先登入會員");
            return false;  //停止執行
        }

        // 已經登入的會員，把會員id(id)透過request丟給Controller使用
        Integer id = (Integer) session.getAttribute("id");
        request.setAttribute("currentId", id);

        return true;  //沒問題，繼續執行Controller

    }

}