package com.pixeltribe.membersys.member.dto;

import java.time.Instant;
import java.time.LocalDate;

public class MemberAdminDto {

	private Integer id;
	private String memName;
	private String memNickName;
	private String memAccount;
	private String memEmail;
	private String memAddr;
	private String memPhone;
	private LocalDate memBirthday;
	private Instant memCreate;
	private Character memStatus;

	// Constructor
	public MemberAdminDto(Integer id, String memName, String memNickName, String memAccount, String memEmail, String memAddr,
			String memPhone, LocalDate memBirthday, Instant memCreate, Character memStatus) {
		this.id = id;
		this.memName = memName;
		this.memNickName = memNickName;
		this.memAccount = memAccount;
		this.memEmail = memEmail;
		this.memAddr = memAddr;
		this.memPhone = memPhone;
		this.memBirthday = memBirthday;
		this.memCreate = memCreate;
		this.memStatus = memStatus;
	}
	
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

	public String getMemNickName() {
		return memNickName;
	}

	public void setMemNickName(String memNickName) {
		this.memNickName = memNickName;
	}

	public String getMemAccount() {
		return memAccount;
	}

	public void setMemAccount(String memAccount) {
		this.memAccount = memAccount;
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

	public LocalDate getMemBirthday() {
		return memBirthday;
	}

	public void setMemBirthday(LocalDate memBirthday) {
		this.memBirthday = memBirthday;
	}

	public Instant getMemCreate() {
		return memCreate;
	}

	public void setMemCreate(Instant memCreate) {
		this.memCreate = memCreate;
	}

	public Character getMemStatus() {
		return memStatus;
	}

	public void setMemStatus(Character memStatus) {
		this.memStatus = memStatus;
	}
}
