package com.pixeltribe.membersys.member.dto;

public class MemberBasicDto {
	
	private Integer Id;
	private String memAccount;
	private String memNickName;
	private String memName;
	private String memEmail;
	private String role;
	
	//Constructor
	public MemberBasicDto() {
		
	}
	
	public MemberBasicDto(Integer Id, String memAccount, String memNickName, String memName, String memEmail, String role) {
		this.Id = Id;
		this.memAccount = memAccount;
		this.memNickName = memNickName;
		this.memName = memName;
		this.memEmail = memEmail;
		this.role = role;
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

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
		
}
