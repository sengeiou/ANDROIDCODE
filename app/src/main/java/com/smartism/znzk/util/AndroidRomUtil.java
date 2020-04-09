package com.smartism.znzk.util;

import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * Created with IntelliJ IDEA.
 * **********************************
 * User: zs
 * Date: 2016年 07月 26日
 * Time: 下午2:01
 *
 * @QQ : 1234567890
 * **********************************
 */
public class AndroidRomUtil {

    public static final String TAG = AndroidRomUtil.class.getSimpleName();
    private static final String KEY_EMUI_VERSION_CODE = "ro.build.version.emui";
    private static final String KEY_BUILD_VERSION_CODE = "ro.build.display.id";
    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";

    /**
     * 华为 rom 版本
     * @return
     */
    public static String getEmuiVersion() {
        String emuiVerion = "";
        Class<?>[] clsArray = new Class<?>[]{String.class};
        Object[] objArray = new Object[]{KEY_EMUI_VERSION_CODE};
        try {
            Class<?> SystemPropertiesClass = Class
                    .forName("android.os.SystemProperties");
            Method get = SystemPropertiesClass.getDeclaredMethod("get",
                    clsArray);
            String version = (String) get.invoke(SystemPropertiesClass,
                    objArray);
            Log.d(TAG, "get EMUI version is:" + version);
            if (!TextUtils.isEmpty(version)) {
                return version;
            }
        } catch (ClassNotFoundException e) {
            Log.e(TAG, " getEmuiVersion wrong, ClassNotFoundException");
        } catch (LinkageError e) {
            Log.e(TAG, " getEmuiVersion wrong, LinkageError");
        } catch (NoSuchMethodException e) {
            Log.e(TAG, " getEmuiVersion wrong, NoSuchMethodException");
        } catch (NullPointerException e) {
            Log.e(TAG, " getEmuiVersion wrong, NullPointerException");
        } catch (Exception e) {
            Log.e(TAG, " getEmuiVersion wrong");
        }
        return emuiVerion;
    }

    /**
     * 小米rom
     *
     * @return
     */
    public static boolean isMIUI() {
        return !StringUtils.isEmpty(getSystemProperty(KEY_MIUI_VERSION_CODE, ""))
                || !StringUtils.isEmpty(getSystemProperty(KEY_MIUI_VERSION_NAME, ""))
                || !StringUtils.isEmpty(getSystemProperty(KEY_MIUI_INTERNAL_STORAGE, ""));
    }


    // 判断是魅族操作系统
    public static boolean isMeizuFlymeOS() {
        return getMeizuFlymeOSFlag().toLowerCase().contains("flyme");
    }

    /**
     * 获取魅族系统操作版本标识
     */
    public static String getMeizuFlymeOSFlag() {
        return getSystemProperty(KEY_BUILD_VERSION_CODE, "");
    }

    private static String getSystemProperty(String key, String defaultValue) {
        try {
            Class<?> clz = Class.forName("android.os.SystemProperties");
            Method get = clz.getMethod("get", String.class, String.class);
            return (String)get.invoke(clz, key, defaultValue);
        } catch (Exception e) {
            Log.e(TAG, " getSystemProperties error");
        }
        return defaultValue;
    }

}
