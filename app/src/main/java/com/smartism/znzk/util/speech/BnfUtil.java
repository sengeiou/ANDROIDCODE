package com.smartism.znzk.util.speech;

import android.content.Context;
import android.content.SharedPreferences;

import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.domain.DeviceInfo;

import java.util.List;

/**
 * Created by Administrator on 2017/1/5.
 */

public class BnfUtil {
    public ActivityParentActivity context = null;
    private static BnfUtil bnfUtil = null;

    public BnfUtil(ActivityParentActivity activity) {
        this.context = activity;
    }

    public BnfUtil() {

    }

    public void getTip(String str, List<DeviceInfo> deviceInfos) {
        if (str == null || "".equals(str)) return;
        String[] strs = getValue();
        if (strs == null) {
            save("", "");
            strs = getValue();
        }
        String[] heads = strs[0].split("\\|");
        String[] foods = strs[1].split("\\|");
        String hStr = checkStr(heads, str);
        if (hStr == null) {
            return;
        }
        String fStr = checkStr(foods, str.substring(hStr.length()));
        if (hStr == null) {
            return;
        }
        String ss = str.substring((hStr + fStr).length());
        String name = checkDeviceName(deviceInfos, ss);
        if (name == null) {
            return;
        }
        if (ss.length()-1==name.length()){
            return;
        }

    }

    private String checkDeviceName(List<DeviceInfo> deviceInfos, String spStr) {
        for (DeviceInfo deviceInfo : deviceInfos) {
            String name = deviceInfo.getName();
            if (name.equals(spStr.substring(0, name.length()))) {
                return name;
            }
        }
        return null;
    }

    private String checkStr(String[] strs, String spStr) {
        for (String head : strs) {
            String headStr = spStr.substring(0, head.length());
            if (head.equals(headStr)) {
                return head;
            }
        }
        return null;
    }


    //    获取
    private String[] getValue() {
        String[] strs = new String[2];
        SharedPreferences sp = context.getSharedPreferences("speech_sp", Context.MODE_PRIVATE);
        strs[0] = sp.getString("speech_head", null);
        strs[1] = sp.getString("speech_body", null);
        if (strs[0] == null || strs[1] == null) return null;
        return strs;
    }


    //保存语音命令的文件
    private void save(String head, String body) {
        SharedPreferences sp = context.getSharedPreferences("speech_sp", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("speech_head", "我要|我想");
        editor.putString("speech_body", "打开|关闭");
        editor.commit();
    }

    public static BnfUtil getInstant(Context context, ActivityParentActivity activity) {

        if (bnfUtil == null) {
            synchronized (BnfUtil.class) {
                if (bnfUtil == null) {
                    bnfUtil = new BnfUtil(activity);
                }
            }
        }
        return bnfUtil;
    }


}
