package com.smartism.znzk.domain.camera;

import java.io.Serializable;

public class Account implements Serializable{
	public String three_number;
	public String email;
	public String phone;
	public int sessionId;
	public int sessionId2;
	public int rCode1;
	public int rCode2;
	public String accessKey;
	public String countryCode;
	public String password;
	public Account() {

	}

	public Account(String three_number, String email, String phone,
				   int sessionId,int sessionId2, int code1, int code2, String countryCode,String password) {
		this.three_number = three_number;
		this.email = email;
		this.phone = phone;
		this.sessionId = sessionId;
		this.sessionId2 = sessionId2;
		this.rCode1 = code1;
		this.rCode2 = code2;
		this.countryCode = countryCode;
		this.password = password;

	}
}
