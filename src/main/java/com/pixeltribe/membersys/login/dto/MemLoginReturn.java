package com.pixeltribe.membersys.login.dto;

import com.pixeltribe.membersys.member.dto.MemberBasicDto;

public class MemLoginReturn {

    private boolean success;
    private String message; // "帳號不存在", "密碼錯誤", "登入成功"
    private String token;
    private MemberBasicDto memberInfo;
    private Character Status;
    //Constructor
    
    public MemLoginReturn(boolean success, String message) {
    	this.success = success;
    	this.message = message;
    }
    
    public MemLoginReturn(boolean success, String message, String token, MemberBasicDto memberInfo) {
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

	public MemberBasicDto getMemberInfo() {
		return memberInfo;
	}

	public void setMemberInfo(MemberBasicDto memberInfo) {
		this.memberInfo = memberInfo;
	}

	public Character getStatus() {
		return Status;
	}

	public void setStatus(Character status) {
		Status = status;
	}
	
}
