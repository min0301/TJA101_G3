package com.pixeltribe.membersys.administrator.dto;

public class AdminBasicDto {
	
	private Integer Id;
	private String admAccount;
	private String admName;
	private String role;
	
	//Constructor
	public AdminBasicDto() {
		
	}
	
	public AdminBasicDto(Integer Id, String admAccount, String admName, String role) {
		this.Id = Id;
		this.admAccount = admAccount;
		this.admName = admName;
		this.role = role;
	}
	
	//Getter & Setter
	
	public Integer getId() {
		return Id;
	}

	public void setId(Integer id) {
		Id = id;
	}

	public String getAdmAccount() {
		return admAccount;
	}

	public void setAdmAccount(String admAccount) {
		this.admAccount = admAccount;
	}

	public String getAdmName() {
		return admName;
	}

	public void setAdmName(String admName) {
		this.admName = admName;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
	
		
}


