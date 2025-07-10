package com.pixeltribe.membersys.login.dto;

public class MemLoginReturn {

    private boolean success;
    private String message; // "帳號不存在", "密碼錯誤", "登入成功"
    private String token;
    private MemberInfo memberInfo;
    
    //Constructor
    
    public MemLoginReturn(boolean success, String message) {
    	this.success = success;
    	this.message = message;
    }
    
    public MemLoginReturn(boolean success, String message, String token, MemberInfo memberInfo) {
		super();
		this.success = success;
		this.message = message;
		this.token = token;
		this.memberInfo = memberInfo;
	}

	//Setter & Getter

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

	public MemberInfo getMemberInfo() {
		return memberInfo;
	}

	public void setMemberInfo(MemberInfo memberInfo) {
		this.memberInfo = memberInfo;
	}
    
    
}
