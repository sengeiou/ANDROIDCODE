package com.smartism.znzk.util;

/*
* 工具类，没有成员变量，方法之间不需要数据共享,可以进行单元测试，只不过不好mock
* */
public class DecimalUtils {

    /*
     * 检查用户的输入的么一个字符是否符合正则表达式,不匹配则替换成空字符
     * 并且返回处理过的字符
     * */
    public static  String checkUserInput(String input,String regex){
        String result = input ;
        for(int i=0;i<result.length();i++){
            Character ch = result.charAt(i);
            if(!ch.toString().matches(regex)){
                result = result.replace(ch.toString(),"");//用空字符替换掉
                i--;
            }
        }
        return result;
    }


    //将符合regex的替换成空字符
    public static String handleMatcheString(String input,String regex){
        String result = input ;
        //将空格和-处理成空字符
        for(int i=0;i<result.length();i++){
            Character ch = result.charAt(i);
            if(ch.toString().matches(regex)){
                result = result.replace(ch.toString(),"");//用空字符替换掉
                i--;
            }
        }

        return  result ;
    }
    /*
     * 由于功能中常常以字符串的形式传十六进制过来，每一个字节都有它的含义,整数
     *
     * @param dest :代表十六进制的字符串
     * @param from 取的起始位置
     * @param to  取的结束位置(不包括)
     * @param 返回值 返回-1，表示字符串不合法,其余结果。但是需要注意Java中的整型都是有符号的，因此当数比较大时，可能会出现NumberFormatter异常。
     * */
    public static long getBytesInString(String dest,int from,int to){
        int length = dest.length();//获取个数，两位占一个字节，高位在左，低位在右
        //验证字符串是不是合法的十六进制字符串，不然做无用功,通过正则验证,记得单元测试的依赖隔离性
        if(!checkUserInput(dest,"[a-fA-F\\d]").equals(dest)){
            //进来了说明，有些不合法字符被替换成空字符
            return  -1;
        }
        //再来验证from to的合法性,有符号类型，最大支持一次性解析8位十六进制
        if(from<0||from>=to||(to-from)>8||to>length){
            return -1 ;
        }
        /*
         * 取出字节
         * 思路，首先直接获取到from到to之间的子字符串,在进行解析
         * */
        String sunDest = dest.substring(from,to);
        //以int类型作为模板,第二参数表示采用的进制
        return Long.parseLong(sunDest,16);
    }
}
