package com.smartism.znzk.activity.device.add;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.device.lib.DeviceManager;
import com.device.lib.TCPHelper;
import com.device.lib.UDPHelper;
import com.device.lib.WiFiTouch;
import com.smartism.znzk.activity.ActivityParentActivity;

/**
 * Created by jiaweili on 16/9/19.
 */
public class BaseActivty extends ActivityParentActivity {

    /***
     * 设备管理器
     */
    protected DeviceManager deviceManager;
    /***
     * wifi控制器
     */
    protected WiFiTouch wifiTouch;

    /***
     * TCP控制器
     */
    protected TCPHelper tcpHelper;
    /**
     * UDP控制器
     */
    protected UDPHelper udpHelper;



    /**
     * Context.
     */
    protected Context mContext;


    /**
     * 双击退出函数
     */
    public static Boolean isExit = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplication();
        deviceManager = DeviceManager.getInstance();
        wifiTouch = WiFiTouch.getInstance();
        tcpHelper = TCPHelper.getInstance();
        udpHelper = UDPHelper.getInstance();
    }


    public boolean checkSDK(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return true;
        }
        return false;
    }

//    public boolean checkJson(Exception e, JsonObject jsonObject) {
//        if (e != null) {
//            Log.e("CheckJson", "checkJson: " + e);
//        } else if (jsonObject.get("success").getAsBoolean()) {
//            return true;
//        }else {
//            ErrorMsg.checkJson(jsonObject, mContext);
//        }
//        return false;
//    }
}
