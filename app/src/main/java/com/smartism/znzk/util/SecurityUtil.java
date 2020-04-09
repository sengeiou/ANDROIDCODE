package com.smartism.znzk.util;

import android.util.Base64;

import java.security.Key;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class SecurityUtil {

	

	/**
	 * 加密方法
	 * 
	 * @param content
	 * @param
	 * @return 加密成功，则返回加密串，否则返回null
	 */
	public static String crypt(String content, String key) {
		String result = null;
		try {
			Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
			Key k = new SecretKeySpec(key.getBytes(), 0, 16, "AES");
			c.init(Cipher.ENCRYPT_MODE, k);
			result = Base64.encodeToString(c.doFinal(content.getBytes()),Base64.DEFAULT);
		} catch (Exception e) {
			result = null;
		}
		return result;

	}

	/**
	 * 加密方法 并转出16进制字符串
	 *
	 * @param content
	 * @param key
	 * @return 加密成功，则返回加密串，否则返回null
	 */
	public static String cryptToHexString(String content, String key) throws Exception{
		Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
		Key k = new SecretKeySpec(key.getBytes(), 0, 16, "AES");
		c.init(Cipher.ENCRYPT_MODE, k);
		return PacketUtil.byteArrayToHexString(c.doFinal(content.getBytes("UTF-8")));
	}
	
	/**
	 * 解密方法
	 * 
	 * @param code
	 * @param
	 * @return 加密成功，则返回结果串，否则返回null
	 */
	public static String decrypt(String code, String key) {
		String result = null;
		try {
			Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
			Key k = new SecretKeySpec(key.getBytes(), 0, 16, "AES");
			c.init(Cipher.DECRYPT_MODE, k);
			result = new String(c.doFinal(Base64.decode(code, Base64.DEFAULT)));
		} catch (Exception e) {
			e.printStackTrace();
			result = null;
		}
		return result;
	}

	/**
	 * 16进制字符串 解密方法
	 *
	 * @param code
	 * @param key
	 * @return 加密成功，则返回结果串，否则返回null
	 */
	public static String decryptHexStringToString(String code, String key) throws Exception{
		Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
		Key k = new SecretKeySpec(key.getBytes(), 0, 16, "AES");
		c.init(Cipher.DECRYPT_MODE, k);
		return new String(c.doFinal(PacketUtil.HexString2Bytes(code)),"UTF-8");
	}
	
	/**
	 * 加密方法
	 * 
	 * @param content
	 * @param
	 * @return 加密成功，则返回加密串，否则返回null
	 */
	public static byte[] crypt(byte[] content, String key) {
		byte[] result = {};
		try {
			Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
			Key k = new SecretKeySpec(key.getBytes(), 0, 16, "AES");
			c.init(Cipher.ENCRYPT_MODE, k);
			result = c.doFinal(content);
		} catch (Exception e) {
			result = null;
		}
		return result;

	}

	/**
	 * 解密方法
	 * 
	 * @param code
	 * @param
	 * @return 加密成功，则返回结果串，否则返回null
	 */
	public static byte[] decrypt(byte[] code, String key) {
		byte[] result = {};
		try {
			Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
			Key k = new SecretKeySpec(key.getBytes(), 0, 16, "AES");
			c.init(Cipher.DECRYPT_MODE, k);
			result = c.doFinal(code);
		} catch (Exception e) {
			e.printStackTrace();
			result = null;
		}
		return result;
	}
	
	public final static String MD5(String s) {
        char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};       
        try {
            byte[] btInput = s.getBytes();
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
	/**
	 * 生成签名
	 * @param v 参数   uid 用户id   code 登录code    n 随机字符串
	 * @return
	 */
	public static String createSign(String v,long uid,String code,String n){
		StringBuilder builder = new StringBuilder();
		if (v!=null && !"".equals(v)) {
			builder.append(v);
			builder.append("&");
		}
		builder.append(n);
		builder.append("&");
		builder.append(DataCenterSharedPreferences.Constant.KEY_HTTP);
		if (uid!=0) {
			builder.append("&");
			builder.append(uid);
			builder.append("&");
			builder.append(code);
		}
		return MD5(builder.toString()).toUpperCase();
	}

	/**
	 * 生成签名 新的方式
	 * @param v 参数   uid 用户id   code 登录code    n 随机字符串
	 * @return
	 */
	public static String createSign(String v,String appid,String appSecret,String code,String n){
		StringBuilder builder = new StringBuilder();
		if (v!=null && !"".equals(v)) {
			builder.append(v);
			builder.append("&");
		}
		builder.append(n);
		builder.append("&");
		builder.append(appid);
		builder.append("&");
		builder.append(appSecret);
		if (code!=null && code.length() > 0) {
			builder.append("&");
			builder.append(code);
		}
		return MD5(builder.toString()).toUpperCase();
	}

	/**
	 * efud的商城加密算法
	 * @return
	 */
	public static String createEFUDShopSign(String v){
		StringBuilder builder = new StringBuilder();
		builder.append("Q3G2As8AL06x");
		builder.append(v);
		builder.append("TFv4uUKdBdTEM48zN68wzarp");
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < builder.length(); i++) {
			result.append((char) (((int)builder.charAt(i))+1));
		}
		return result.toString();
	}
}
