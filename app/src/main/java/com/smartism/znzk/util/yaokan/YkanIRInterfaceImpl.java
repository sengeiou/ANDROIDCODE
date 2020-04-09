package com.smartism.znzk.util.yaokan;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.smartism.znzk.R;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.domain.yankan.BrandResult;
import com.smartism.znzk.domain.yankan.DeviceTypeResult;
import com.smartism.znzk.domain.yankan.MatchRemoteControlResult;
import com.smartism.znzk.domain.yankan.YKTvInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class YkanIRInterfaceImpl implements YkanIRInterface {

    private String deviceId;
    private String appId;
    private Gson gson = new Gson();
    private Context ctx;
    private String domain = "http://api.yaokongyun.cn";
    // private String url_prefix =  domain + "/open/m.php?";
    private String url_prefix =  domain + "/open/m2.php?";
    private HttpUtil httpUtil;

    public static final int NET_SUCCEES_GETDEVICETYPE = -1;
    public static final int NET_SUCCEES_GETBRANDSBYTYPE = 1;//品牌列表
    public static final int NET_SUCCEES_GETREMOTEMATCHED = 2;//型号列表
    public static final int NET_SUCCEES_GETREMOTEDETAILS = 3;//下载码
    public static final int NET_SUCCEES_REGIS = 4;//


    public YkanIRInterfaceImpl() {
        appId = MainApplication.app.getAppGlobalConfig().getYaokanAppid();
        deviceId = MainApplication.app.getAppGlobalConfig().getYaoKanDeviceId();
//        deviceId = "pingkoko8888888";
        httpUtil = new HttpUtil(appId, deviceId);
    }

    private String getPostUrl(String url_sufx) {
        return url_prefix + url_sufx;
    }

    @Override
    public void registerDevice(Handler handler) {
        String func = "c=r";
        String url = getPostUrl(func);
        String result = httpUtil.postMethod(url, null);
        String msg = "";
        int code = -1;
        int code1 = -1;
        int flag = 0;
        Message m = handler.obtainMessage(NET_SUCCEES_REGIS);
        JSONObject jsonObject = null;
        if (!Utility.isEmpty(result)) {
            try {
                jsonObject = new JSONObject(result);
                if (jsonObject.isNull("ret_code")) {
                    code = jsonObject.getInt("ret_code");
                }
                if (!jsonObject.isNull("code")) {
                    code1 = jsonObject.getInt("code");
                }
                if (code == 1 || code == 10013 || code == 10011 || code == 10005 || code1 == 10013 || code1 == 10005) {
                    flag = -1;
                } else {
                    if (code == -1) {
                        flag = code1;
                    } else {
                        flag = code;
                    }
                }


            } catch (JSONException e) {
                try {
                    if (jsonObject.isNull("error")) {
                        msg = jsonObject.getString("error");
                    }
                    Log.d("Ykan regis msg", "error:" + msg);
                } catch (JSONException e1) {
                    Log.d("Ykan regis", "error:" + e1.getMessage());
                }
            }
        }
        m.arg1 = flag;
        m.obj = msg;
        handler.sendMessage(m);
    }

    /**
     * 获取类型
     */
    @Override
    public void getDeviceType(Handler handler) {
        String func = "c=t";
        String url = getPostUrl(func);
        String result = httpUtil.postMethod(url, null);
        Log.e("impl", result);
        DeviceTypeResult obj = gson.fromJson(result, DeviceTypeResult.class);
        Message m = handler.obtainMessage(NET_SUCCEES_GETDEVICETYPE);
        m.obj = obj;
        handler.sendMessage(m);
    }

    /**
     * 获取单个类型下面的所有品牌
     */
    public void getBrandsByType(int type, Handler handler) {
        String func = "c=f";
        String url = getPostUrl(func);
        List<String> params = new ArrayList<String>();
        params.add("t=" + type);
        String result = httpUtil.postMethod(url, params);
        BrandResult obj = gson.fromJson(result, BrandResult.class);

        Message m = handler.obtainMessage(NET_SUCCEES_GETBRANDSBYTYPE);
        m.obj = obj;
        //Log.e("aaa", "下载的结果是：：NET" + obj.toString().substring(1, 100) + ".....");
        handler.sendMessage(m);
    }

    /**
     * 获取特点类型 特定品牌下面的所有遥控器列表===
     */
    public MatchRemoteControlResult getRemoteMatched(int type, int bid, Handler handler) {
        // 定义Key值
        String func = "c=s";
        String url = getPostUrl(func);
        List<String> params = new ArrayList<String>();
        params.add("bid=" + bid);
        params.add("t=" + type);
        params.add("model=" + "");
        params.add("v=" + "1");
        params.add("zip=" + 0);
        String result = httpUtil.postMethod(url, params).replace("short", "shortCode");
//		String result = httpUtil.postMethod(url, params);
        MatchRemoteControlResult obj = gson.fromJson(result, MatchRemoteControlResult.class);
        Message m = handler.obtainMessage(NET_SUCCEES_GETREMOTEMATCHED);
        m.obj = obj;
        handler.sendMessage(m);
        return obj;
    }

    /**
     * 获取某个遥控器对应的详情码库
     */
    public void getRemoteDetailsHashMap(Context context,String rId, String tname, Handler handler) {
        // 定义Key值
        String func = "c=d";
        // 获取JsonData对象
        String url = getPostUrl(func);
        List<String> params = new ArrayList<String>();
        params.add("r=" + rId);
        params.add("zip=" + 0);
        String result = httpUtil.postMethod(url, params);
        HashMap<String, Object> map = new HashMap<>();
//        HashMap<String, YKTvInfo> tvRcCommand = new HashMap<>();

        try {
            if (result.startsWith("{")) {
                com.alibaba.fastjson.JSONObject object = JSON.parseObject(result);
                if (object.containsKey("error")) {
                    map.put("error", object.getString("error"));
                }
            } else if (result.startsWith("[")) {
                com.alibaba.fastjson.JSONArray array = com.alibaba.fastjson.JSONArray.parseArray(result);
                com.alibaba.fastjson.JSONObject object1 = array.getJSONObject(0);
                String rc_command = object1.getString("rc_command");
                com.alibaba.fastjson.JSONObject cmd = com.alibaba.fastjson.JSONObject.parseObject(rc_command);
                int version = object1.getIntValue("v");
                if (tname.equals(context.getString(R.string.hwzf_kt)) && version == 1) {
                    map.put("ar16", Trim(cmd.getString("ar16")));
                    map.put("ar17", Trim(cmd.getString("ar17")));
                    map.put("ar18", Trim(cmd.getString("ar18")));
                    map.put("ar19", Trim(cmd.getString("ar19")));
                    map.put("ar20", Trim(cmd.getString("ar20")));
                    map.put("ar21", Trim(cmd.getString("ar21")));
                    map.put("ar22", Trim(cmd.getString("ar22")));
                    map.put("ar23", Trim(cmd.getString("ar23")));
                    map.put("ar24", Trim(cmd.getString("ar24")));
                    map.put("ar25", Trim(cmd.getString("ar25")));
                    map.put("ar26", Trim(cmd.getString("ar26")));
                    map.put("ar27", Trim(cmd.getString("ar27")));
                    map.put("ar28", Trim(cmd.getString("ar28")));
                    map.put("ar29", Trim(cmd.getString("ar29")));
                    map.put("ar30", Trim(cmd.getString("ar30")));

                    map.put("ah16", Trim(cmd.getString("ah16")));
                    map.put("ah17", Trim(cmd.getString("ah17")));
                    map.put("ah18", Trim(cmd.getString("ah18")));
                    map.put("ah19", Trim(cmd.getString("ah19")));
                    map.put("ah20", Trim(cmd.getString("ah20")));
                    map.put("ah21", Trim(cmd.getString("ah21")));
                    map.put("ah22", Trim(cmd.getString("ah22")));
                    map.put("ah23", Trim(cmd.getString("ah23")));
                    map.put("ah24", Trim(cmd.getString("ah24")));
                    map.put("ah25", Trim(cmd.getString("ah25")));
                    map.put("ah26", Trim(cmd.getString("ah26")));
                    map.put("ah27", Trim(cmd.getString("ah27")));
                    map.put("ah28", Trim(cmd.getString("ah28")));
                    map.put("ah29", Trim(cmd.getString("ah29")));
                    map.put("ah30", Trim(cmd.getString("ah30")));

                    map.put("on", Trim(cmd.getString("on")));
                    map.put("off", Trim(cmd.getString("off")));
                    if (!TextUtils.isEmpty(cmd.getString("aw"))) {
                        map.put("aw", Trim(cmd.getString("aw")));
                    }
                    if (!TextUtils.isEmpty(cmd.getString("ad"))) {
                        map.put("ad", Trim(cmd.getString("ad")));
                    }
                    if (!TextUtils.isEmpty(cmd.getString("aa"))) {
                        map.put("aa", Trim(cmd.getString("aa")));
                    }
                } else if (tname.equals(context.getString(R.string.hwzf_fan_fan)) && version == 1) {
                    if (!TextUtils.isEmpty(cmd.getString("mode"))) {
                        map.put("mode", Trim(cmd.getString("mode")));
                    }
                    if (!TextUtils.isEmpty(cmd.getString("power"))) {
                        map.put("power", Trim(cmd.getString("power")));
                    }
                    if (!TextUtils.isEmpty(cmd.getString("oscillation"))) {
                        map.put("oscillation", Trim(cmd.getString("oscillation")));
                    }
                    if (!TextUtils.isEmpty(cmd.getString("poweroff"))) {
                        map.put("poweroff", Trim(cmd.getString("poweroff")));
                    }
                    if (!TextUtils.isEmpty(cmd.getString("timer"))) {
                        map.put("timer", Trim(cmd.getString("timer")));
                    }
                    if (!TextUtils.isEmpty(cmd.getString("fanspeed"))) {
                        map.put("fanspeed", Trim(cmd.getString("fanspeed")));
                    }
                    if (!TextUtils.isEmpty(cmd.getString("fanspeed+"))) {
                        map.put("fanspeed+", Trim(cmd.getString("fanspeed+")));
                    }
                    if (!TextUtils.isEmpty(cmd.getString("fanspeed-"))) {
                        map.put("fanspeed-", Trim(cmd.getString("fanspeed-")));
                    }
                    if (!TextUtils.isEmpty(cmd.getString("timer+"))) {
                        map.put("timer+", Trim(cmd.getString("timer+")));
                    }
                    if (!TextUtils.isEmpty(cmd.getString("timer-"))) {
                        map.put("timer-", Trim(cmd.getString("timer-")));
                    }
                    if (!TextUtils.isEmpty(cmd.getString("lamp"))) {
                        map.put("lamp", Trim(cmd.getString("lamp")));
                    }
                    if (!TextUtils.isEmpty(cmd.getString("cool"))) {
                        map.put("cool", Trim(cmd.getString("cool")));
                    }
                } else if (tname.equals(context.getString(R.string.hwzf_tv_tv)) && version == 1) {
                    YKTvInfo tvInfo;
                    Iterator iterator = cmd.keySet().iterator();
                    while (iterator.hasNext()) {
                        tvInfo = new YKTvInfo();
                        String key = (String) iterator.next();
                        com.alibaba.fastjson.JSONObject value = cmd.getJSONObject(key);
                        tvInfo.setKeyName(value.getString("kn"));
                        tvInfo.setCode(value.getString("src"));
                        tvInfo.setKey(key);
                        map.put(key, tvInfo);
                    }
                } else if (tname.equals(context.getString(R.string.hwzf_tvbox_tvbox)) && version == 1) {
                    YKTvInfo tvInfo;
                    Iterator iterator = cmd.keySet().iterator();
                    while (iterator.hasNext()) {
                        tvInfo = new YKTvInfo();
                        String key = (String) iterator.next();
                        com.alibaba.fastjson.JSONObject value = cmd.getJSONObject(key);
                        tvInfo.setKeyName(value.getString("kn"));
                        tvInfo.setCode(value.getString("src"));
                        tvInfo.setKey(key);
                        map.put(key, tvInfo);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Message m = handler.obtainMessage(NET_SUCCEES_GETREMOTEDETAILS);
        m.obj = map;
        handler.sendMessage(m);
    }


    // 去掉short短码部分 {"short":"","src":"1,38000,341,172,22,0,23"}
    public Object Trim(String cmd) {
        JSONObject obj1;
        String src = null;
        try {
            obj1 = new JSONObject(cmd);
            src = obj1.getString("src");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return src;
    }
}
