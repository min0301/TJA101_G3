package com.pixeltribe.membersys.administrator.dto;

public class AdminBasicDto {
	
	private Integer Id;
	private String admAccount;
	private String admName;
	
	//Constructor
	public AdminBasicDto() {
		
	}
	
	public AdminBasicDto(Integer Id, String admAccount, String admName) {
		this.Id = Id;
		this.admAccount = admAccount;
		this.admName = admName;
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
	
}


