/* 
 * 文件名：MD5Encrypt.java
 * 版权：Copyright 2009-2010 KOOLSEE MediaNet. Co. Ltd. All Rights Reserved. 
 * 描述： 实现MD5的加密算法
 * 创建人：王利民
 * 修改时间：
 * 跟踪单号：
 * 修改单号：
 * 修改内容：
 */
package com.smartism.znzk.util.yaokan;

import java.security.MessageDigest;

public class Encrypt {

    private final static String[] hexDigits = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9","a", "b", "c", "d", "e", "f"};

    public static String encryptStr(String inputString) {
        return encodeByMD5(inputString);
    }

    public static String encryptSpecial(String input){
    	String md5  = encryptStr(input);
    	String result = "";
    	for(int i = 0 ; i< 5 ; i++){
    		int index = (int) Math.pow(2, i+1) ;
    		result  = result + md5.charAt(index-1);
    	}
    	return result ;
    }
    
    private static String encodeByMD5(String originString) {
        if (originString != null) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] results = md.digest(originString.getBytes());
                String resultString = byteArrayToHexString(results);
                return resultString;
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return "";
    }

    private static String byteArrayToHexString(byte[] b) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            resultSb.append(byteToHexString(b[i]));
        }
        return resultSb.toString();
    }

    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0) {
            n = 256 + n;
        }
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }
}
