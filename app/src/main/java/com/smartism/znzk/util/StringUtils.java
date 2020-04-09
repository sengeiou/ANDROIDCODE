package com.smartism.znzk.util;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * 字符串处理的通用方法
 * <p>
 *
 * @author Lien Li
 * @version 1.00
 */
public class StringUtils {

    /**
     * 字符串编码
     */
    public static final String ENCODING_UTF8 = "utf-8";

    /**
     * 返回一个StringBuffer对象
     *
     * @return 返回StringBuffer对象, 长度50
     */
    public static StringBuffer getBuffer() {
        return new StringBuffer(50);
    }

    /**
     * 返回一个指定长度的StringBuffer对象
     *
     * @param length 指定长度
     * @return 返回StringBuffer对象
     */
    public static StringBuffer getBuffer(int length) {
        return new StringBuffer(length);
    }

    /**
     * 判断字符串是否为null或者为空
     *
     * @param str 传入的字符串
     * @return boolean true or false
     */
    public static boolean isEmpty(String str) {
        if (str == null || str == "" || str.trim().equals(""))
            return true;
        return false;
    }

    /**
     * 判断字符串是否为0或者为空
     *
     * @param str 传入的字符串
     * @return boolean true or false
     */
    public static boolean isNumEmpty(String str) {
        if (str == null || str == "" || str.trim().equals("")
                || str.trim().equals("0"))
            return true;
        return false;
    }

    /**
     * 将一个字符串进行UTF8编码后返回
     *
     * @param data 传入的字符串
     * @return String
     */
    public static String encode(String data) {
        try {
            return URLEncoder.encode(data, ENCODING_UTF8);
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    /**
     * 将字符串转移成整数
     *
     * @param num 传入的字符串
     * @return int
     */
    public static int toInt(String num) {
        try {
            return Integer.parseInt(num);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 将字符串转移成长整数
     *
     * @param num 传入的字符串
     * @return long
     */
    public static long toLong(String num) {
        try {
            return Long.parseLong(num);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 将字符串转移成浮点数
     *
     * @param num 传入的字符串
     * @return long
     */
    public static float toFloat(String num) {
        try {
            return Float.parseFloat(num);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 将字符串转移成布尔数
     *
     * @param num 传入的字符串
     * @return boolean
     */
    public static boolean toBoolean(String num) {
        if (isEmpty(num))
            return false;
        if (num.equals("true"))
            return true;
        return false;
    }

    /**
     * sql特殊字符转义
     *
     * @param keyWord 关键字
     * @return 转义后的字符串
     */
    public static String sqliteEscape(String keyWord) {
        keyWord = keyWord.replace("/", "//");
        keyWord = keyWord.replace("'", "''");
        keyWord = keyWord.replace("[", "/[");
        keyWord = keyWord.replace("]", "/]");
        keyWord = keyWord.replace("%", "/%");
        keyWord = keyWord.replace("&", "/&");
        keyWord = keyWord.replace("_", "/_");
        keyWord = keyWord.replace("(", "/(");
        keyWord = keyWord.replace(")", "/)");
        return keyWord;
    }

    /**
     * sql特殊字符反转义
     *
     * @param keyWord 关键字
     * @return 反转义后的字符串
     */
    public static String sqliteUnEscape(String keyWord) {
        keyWord = keyWord.replace("//", "/");
        keyWord = keyWord.replace("''", "'");
        keyWord = keyWord.replace("/[", "[");
        keyWord = keyWord.replace("/]", "]");
        keyWord = keyWord.replace("/%", "%");
        keyWord = keyWord.replace("/&", "&");
        keyWord = keyWord.replace("/_", "_");
        keyWord = keyWord.replace("/(", "(");
        keyWord = keyWord.replace("/)", ")");
        return keyWord;
    }

    /**
     * 格式一个日期
     *
     * @param longDate 需要格式日期的长整数的字符串形式
     * @param format   格式化参数
     * @return 格式化后的日期
     */
    public static String getStrDate(String longDate, String format) {
        if (isEmpty(longDate))
            return "";
        long time = Long.parseLong(longDate);
        Date date = new Date(time);
        return getStrDate(date, format);
    }

    /**
     * 格式一个日期
     *
     * @param time   需要格式日期的长整数
     * @param format 格式化参数
     * @return 格式化后的日期
     */
    public static String getStrDate(long time, String format) {
        Date date = new Date(time);
        return getStrDate(date, format);
    }

    /**
     * 返回当前日期的格式化表示
     *
     * @param date    指定格式化的日期
     * @param formate 格式化参数
     * @return 格式化后的日期
     */
    public static String getStrDate(Date date, String formate) {
        SimpleDateFormat dd = new SimpleDateFormat(formate);
        return dd.format(date);
    }

    /**
     * 返回当前日期的格式化（yyyy-MM-dd）表示
     *
     * @return 格式化的字符串
     */
    public static String getStrDate() {
        SimpleDateFormat dd = new SimpleDateFormat("yyyy-MM-dd");
        return dd.format(new Date());
    }

    /**
     * 返回指定个数的随机数字串
     *
     * @param num 随机数的位数
     * @return String
     */
    public static String getRandomStr(int num) {
        StringBuffer temp = new StringBuffer();
        Random rand = new Random();
        for (int i = 0; i < num; i++) {
            temp.append(rand.nextInt(10));
        }
        return temp.toString();
    }


    /**
     * 修剪空格</br> by Anter 2016年1月11日09:47:31</br> Ex: "48 65 6c 6c 6f" ->
     * "48656c6c6f" 根据发过来的数据 由第七个开始
     *
     * @param strTrim
     * @return
     */
    public static String spaceTrim(String strTrim) {

        String result = "";

        for (int i = 0; i < (strTrim.length() - 2) / 3; i++) {

            result += strTrim.split(" ")[i];
            if (i == ((strTrim.length() - 2) / 3 - 1)) {
                result += strTrim.split(" ")[i + 1];
            }
        }

        return result;
    }

    public static String deleleSpaceTrim(String spaceStr){
        StringBuilder text  = new StringBuilder();
        spaceStr = text.append(spaceStr.replace(" ","")).toString();
        return spaceStr;
    }


    public static String digitstoTens(int digits) {


        String Tens = digits+"";

        if (digits < 10 && digits >= 0) {
            StringBuilder sb = new StringBuilder();
            Tens=  sb.append('0').append(Tens).toString();
        }

        return Tens;
    }

    /**
     * 十六进制字符串to字符字符串</br>
     * <p>
     * by Anter 2016年1月11日09:25:18</br>
     * <p>
     * Ex: "48656c6c6f" -> "Hello"
     *
     * @param strToConvert 十六进制字符串(不含空格)
     * @return 字符类型字符串
     */
    public static String hexStrToStr(String strToConvert) {
        byte[] baKeyword = new byte[strToConvert.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(
                        strToConvert.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            strToConvert = new String(baKeyword, "utf-8");// UTF-16le:Not
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return strToConvert;
    }

    /**
     * 字符字符串to十六进制字符串</br>
     * <p>
     * by Anter 2016年1月11日09:25:18</br>
     * <p>
     * Ex: "Hello" -> "48656c6c6f"
     *
     * @param strToConvert 十六进制字符串
     * @return 字符类型字符串
     */
    public static String strToHexStr(String strToConvert) {
        String str = "";
        for (int i = 0; i < strToConvert.length(); i++) {
            int ch = (int) strToConvert.charAt(i);
            String s4 = Integer.toHexString(ch);
            str = str + s4;
        }
        return str;
    }

    /****
     * Convert byte[] to hex string.这里我们可以将byte转换成int，然后利用Integer.toHexString(int)
     * 来转换成16进制字符串
     *
     * @param src
     * @return
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }


    /***
     * 是否为手机号码
     * @param mobiles
     * @return
     */
    public static boolean isPhoneNumber(String mobiles) {
        Pattern p = null;
        Matcher m = null;
        boolean b = false;
        p = Pattern.compile("^[1][3,4,5,8][0-9]{9}$"); // 验证手机号
        m = p.matcher(mobiles);
        b = m.matches();
        return b;
    }

    private static String hexString = "0123456789abcdef";

    public static String decode(String bytes) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length() / 2);
        //将每2位16进制整数组装成一个字节
        for (int i = 0; i < bytes.length(); i += 2)
            baos.write((hexString.indexOf(bytes.charAt(i)) << 4 | hexString.indexOf(bytes.charAt(i + 1))));
        return new String(baos.toByteArray());
    }

    public static byte[] hexStringToBytes(String hex) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(hex.length() / 2);
        //将每2位16进制整数组装成一个字节
        for (int i = 0; i < hex.length(); i += 2)
            baos.write((hexString.indexOf(hex.charAt(i)) << 4 | hexString.indexOf(hex.charAt(i + 1))));
        return baos.toByteArray();
    }

}
