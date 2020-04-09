package com.smartism.znzk.domain.camera;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;



public class Email {
	String[] UIEmail = new String[] { "@163.com", "@qq.com", "@sina.com",
			"@yahoo.com", "@gmail.com", "@189.cn", "@hotmail.com" };
	String[] SMTPadrress = new String[] { "smtp.163.com", "smtp.qq.com",
			"smtp.sina.com.cn", "smtp.mail.yahoo.com",
			"173.194.193.108,173.194.67.108,smtp.gmail.com", "smtp.189.cn",
			"smtp.live.com" };
	int[] port = new int[] { 25, 25, 25, 587, 465, 25, 587 };
	public static String subject = "Attention: alarm";
	public static String countent = "Dear User,\n Please check the attached picture for more information.";

	private static Email email;

	private Email() {
		super();
	}

	public static Email getInstence() {
		if (email == null) {
			email = new Email();
		}
		return email;
	}

	/**
	 * 获得支持的邮箱列表
	 * 
	 * @return
	 */
	public List<String> getUIEmailList() {
		List<String> list = new ArrayList<String>();
		for (int i = 0, count = UIEmail.length; i < count; i++) {
			list.add(UIEmail[i]);
		}
		return list;
	}

	public String[] getUIEmailLists() {
		return UIEmail;
	}

	/**
	 * 邮箱后缀获得其他信息
	 * 
	 * @param uiEmail
	 * @return
	 */
	public String[] getEmailMessage(String uiEmail) {
		String[] emailMessage = new String[5];
		try {
			emailMessage[3] = new String(subject.getBytes(), "UTF-8");
			emailMessage[4] = new String(countent.getBytes(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i = 0, count = UIEmail.length; i < count; i++) {
			if (UIEmail[i].equals(uiEmail)) {
				emailMessage[0] = uiEmail;
				emailMessage[1] = SMTPadrress[i];
				emailMessage[2] = String.valueOf(port[i]);
				break;
			}
		}
		return emailMessage;
	}

	public byte getEncryptByPort(int port) {
		if (port == 25) {
			return 0;
		} else if (port == 465) {
			return 1;
		} else if (port == 587) {
			return 2;
		} else {
			return -1;
		}
	}

	/**
	 * 是否支持此类邮箱
	 * 
	 * @param uiEmail
	 * @return
	 */
	public boolean isSurportThisSMTP(String uiEmail) {
		for (int i = 0, count = UIEmail.length; i < count; i++) {
			if (UIEmail[i].equals(uiEmail)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 服务地址获得展示序列号
	 * 
	 * @param SMTPadrresss
	 * @return
	 */
	public int getUIEmailPosition(String SMTPadrresss) {
		for (int i = 0, count = SMTPadrress.length; i < count; i++) {
			if (SMTPadrress[i].equals(SMTPadrresss)) {
				return i;
			}
		}
		return UIEmail.length - 1;
	}

	public String getUIEmail(String smtp) {
		for (int i = 0, count = SMTPadrress.length; i < count; i++) {
			if (SMTPadrress[i].equals(smtp)) {
				return UIEmail[i];
			}
		}
		return UIEmail[0];
	}

}
