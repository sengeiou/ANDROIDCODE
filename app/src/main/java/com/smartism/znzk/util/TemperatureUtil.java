package com.smartism.znzk.util;

/**
 * 华氏度和摄氏度互转工具类
 */
public class TemperatureUtil {

    public static double ssdToFsd(float ssd){
        return ssd * 1.8 + 32;
    }

    public static double fsdTossd(float fsd){
        return (fsd - 32) / 1.8;
    }

}
