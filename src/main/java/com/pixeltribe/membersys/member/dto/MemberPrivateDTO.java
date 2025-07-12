package com.pixeltribe.membersys.member.dto;

import java.time.LocalDate;

public class MemberPrivateDTO {
	
	private String memAddr;
	private String memPhone;
	private LocalDate memBirthday;
	
	// Constructor
	public MemberPrivateDTO () {
		
	}
	
	
	// Getter & Setter
	
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

}