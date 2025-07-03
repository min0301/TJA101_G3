package com.pixeltribe.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class LoginInterceptor implements HandlerInterceptor {
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		
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