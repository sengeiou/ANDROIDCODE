package com.smartism.znzk.util;

/**
 * Created by win7 on 2016/12/20.
 */

public class NativeUtils {
    static {
        System.loadLibrary("jujiangsuo_jp");
    }

    //java调C中的方法都需要用native声明且方法名必须和c的方法名一样
//    public native int getTime(int id,int time);
//    public native String getTimee();

    public native long getSecrct(int id,int time);

//    public static void main(String[] args) {
//        NativeUtils test = new NativeUtils();
//        test.getTime(0xee552a5a,0x32101205);
//        System.out.println("int     --> " +  test.getTime(0xee552a5a,0x32101205));
//    }
}
