package com.pixeltribe.membersys.login.dto;

public class MemberInfo {
	
	private Integer Id;
	private String memAccount;
	private String memNickName;
	private String memName;
	private String memEmail;
	
	//Constructor
	public MemberInfo() {}
	
	public MemberInfo(Integer Id, String memAccount, String memNickName, String memName, String memEmail) {
		this.Id = Id;
		this.memAccount = memAccount;
		this.memNickName = memNickName;
		this.memName = memName;
		this.memEmail = memEmail;
	}

	//Getter & Setter
	public Integer getId() {
		return Id;
	}

	public void setId(Integer id) {
		Id = id;
	}
	
	public String getMemAccount() {
		return memAccount;
	}

	public void setMemAccount(String memAccount) {
		this.memAccount = memAccount;
	}
	public String getMemNickName() {
		return memNickName;
	}
	public void setMemNickName(String memNickName) {
		this.memNickName = memNickName;
	}
	public String getMemName() {
		return memName;
	}
	public void setMemName(String memName) {
		this.memName = memName;
	}
	public String getMemEmail() {
		return memEmail;
	}
	public void setMemEmail(String memEmail) {
		this.memEmail = memEmail;
	}
	
}
