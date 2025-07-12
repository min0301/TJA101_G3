package com.pixeltribe.membersys.login.dto;

import com.pixeltribe.membersys.administrator.dto.AdminBasicDto;

public class AdmLoginReturn {
	
	    private boolean success;
	    private String message; // "帳號不存在", "密碼錯誤", "登入成功"
	    private String token;
	    private AdminBasicDto adminInfo;
	    
	    // Constructor
	    public AdmLoginReturn(boolean success, String message) {
	        this.success = success;
	        this.message = message;
	    }
	    
	    public AdmLoginReturn(boolean success, String message, String token, AdminBasicDto adminInfo) {
	    	super();
	    	this.success = success;
	    	this.message = message;
	    	this.token = token;
	    	this.adminInfo = adminInfo;
	    }
	    // Getter & Setter
	    
		public boolean isSuccess() {
			return success;
		}
		public void setSuccess(boolean success) {
			this.success = success;
		}
		public String getMessage() {
			return message;
		}
		public void setMessage(String message) {
			this.message = message;
		}
		public String getToken() {
			return token;
		}
		public void setToken(String token) {
			this.token = token;
		}
		public AdminBasicDto getAdminInfo() {
			return adminInfo;
		}
		public void setAdminInfo(AdminBasicDto adminInfo) {
			this.adminInfo = adminInfo;
		}
		
}
