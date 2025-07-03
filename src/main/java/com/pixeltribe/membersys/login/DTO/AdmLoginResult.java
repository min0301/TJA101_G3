package com.pixeltribe.membersys.login.DTO;

public class AdmLoginResult {
	
	    private boolean success;
	    private String message; // "帳號不存在", "密碼錯誤", "登入成功"
	    
	    //constructor
	    public AdmLoginResult(boolean success, String message) {
	        this.success = success;
	        this.message = message;
	    }
	    // ... Getter Setter
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
	
}
