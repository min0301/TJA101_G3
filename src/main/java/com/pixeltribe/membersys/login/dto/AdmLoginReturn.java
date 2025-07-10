package com.pixeltribe.membersys.login.dto;

public class AdmLoginReturn {
	
	    private boolean success;
	    private String message; // "帳號不存在", "密碼錯誤", "登入成功"
	    private String token;
	    private AdminInfo adminInfo;
	    
	    //Constructor
	    
	    public AdmLoginReturn(boolean success, String message) {
	        this.success = success;
	        this.message = message;
	    }
	    
	    public AdmLoginReturn(boolean success, String message, String token, AdminInfo adminInfo) {
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
		public AdminInfo getAdminInfo() {
			return adminInfo;
		}
		public void setAdminInfo(AdminInfo adminInfo) {
			this.adminInfo = adminInfo;
		}
		
}
