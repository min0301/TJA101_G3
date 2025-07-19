package com.pixeltribe.membersys.member.dto;

import java.time.LocalDate;

public class MemberProfileDto {
	
	private Integer id;
	private String memName;
	private LocalDate memBirthday;
	private String memAccount;
	private String memNickName;
	private String memEmail;
	private String memAddr;
	private String memPhone;
	private String memIconData;

	// Constructor
	public MemberProfileDto() {

	}

	// Getter & Setter

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getMemName() {
		return memName;
	}
	public void setMemName(String memName) {
		this.memName = memName;
	}

	public LocalDate getMemBirthday() {
		return memBirthday;
	}

	public void setMemBirthday(LocalDate memBirthday) {
		this.memBirthday = memBirthday;
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

	public String getMemEmail() {
		return memEmail;
	}

	public void setMemEmail(String memEmail) {
		this.memEmail = memEmail;
	}

	public String getMemAddr() {
		return memAddr;
	}

	public void setMemAddr(String memAddr) {
		this.memAddr = memAddr;
	}

	public String getMemPhone() {
		return memPhone;
	}

	public void setMemPhone(String memPhone) {
		this.memPhone = memPhone;
	}

	public String getMemIconData() {
		return memIconData;
	}

	public void setMemIconData(String memIconData) {
		this.memIconData = memIconData;
	}

}