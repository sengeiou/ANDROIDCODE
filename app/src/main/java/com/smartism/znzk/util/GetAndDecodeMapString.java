package com.smartism.znzk.util;

import com.smartism.znzk.domain.yankan.YKTvInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class GetAndDecodeMapString {


    public HashMap<String, String> getMap(String command) {
        HashMap<String, String> itemMap = new HashMap<>();

        try {
            JSONArray array = new JSONArray(command);
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                String keys;
                keys = object.keys().next().toString();
                String value = object.getString(keys);
                itemMap.put(keys, value);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return itemMap;
    }





    public String getTvCode(HashMap<String, Object> datas, int type) {
        com.alibaba.fastjson.JSONArray array = new com.alibaba.fastjson.JSONArray();
        com.alibaba.fastjson.JSONObject object = null;
        com.alibaba.fastjson.JSONObject object1 = null;
        YKTvInfo info = null;
        Iterator iterator = datas.keySet().iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            object = new com.alibaba.fastjson.JSONObject();
            if (type == 1) {
                object.put(key, datas.get(key));
            } else {
                info = (YKTvInfo) datas.get(key);
                object1 = new com.alibaba.fastjson.JSONObject();
                object1.put("kn", info.getKeyName());
                object1.put("src", info.getCode());
                object.put(key, object1);
            }
            array.add(object);
        }
        return array.toJSONString();
    }

    public HashMap<String, YKTvInfo> getTvCodeMap(String command) {
        YKTvInfo tvInfo;
        HashMap<String, YKTvInfo> itemMap = new HashMap<>();
        String key = "";
        com.alibaba.fastjson.JSONArray array = com.alibaba.fastjson.JSONArray.parseArray(command);
        com.alibaba.fastjson.JSONObject object;
        com.alibaba.fastjson.JSONObject object1;
        for (int i = 0; i < array.size(); i++) {
            object = array.getJSONObject(i);
            tvInfo = new YKTvInfo();
            Iterator iterator = object.keySet().iterator();
            while (iterator.hasNext()) {
                key = (String) iterator.next();
                tvInfo.setKey(key);
                object1 = object.getJSONObject(key);
                tvInfo.setKeyName(object1.getString("kn"));
                tvInfo.setCode(object1.getString("src"));
            }
            itemMap.put(key, tvInfo);
        }
        return itemMap;
    }

    public List<YKTvInfo> getYkTvList(String command) {
        List<YKTvInfo> ykTvInfos = new ArrayList<>();
        YKTvInfo tvInfo;
        String key = "";
        com.alibaba.fastjson.JSONArray array = com.alibaba.fastjson.JSONArray.parseArray(command);
        com.alibaba.fastjson.JSONObject object;
        com.alibaba.fastjson.JSONObject object1;
        for (int i = 0; i < array.size(); i++) {
            object = array.getJSONObject(i);
            tvInfo = new YKTvInfo();
            Iterator iterator = object.keySet().iterator();
            while (iterator.hasNext()) {
                key = (String) iterator.next();
                tvInfo.setKey(key);
                object1 = object.getJSONObject(key);
                tvInfo.setKeyName(object1.getString("kn"));
                tvInfo.setCode(object1.getString("src"));
            }
            ykTvInfos.add(tvInfo);
        }
        return ykTvInfos;
    }
    public HashMap<String,YKTvInfo> getYkTvMap(String command) {
        HashMap<String,YKTvInfo> ykTvInfos = new HashMap<>();
        YKTvInfo tvInfo;
        String key = "";
        com.alibaba.fastjson.JSONArray array = com.alibaba.fastjson.JSONArray.parseArray(command);
        com.alibaba.fastjson.JSONObject object;
        com.alibaba.fastjson.JSONObject object1;
        for (int i = 0; i < array.size(); i++) {
            object = array.getJSONObject(i);
            tvInfo = new YKTvInfo();
            Iterator iterator = object.keySet().iterator();
            while (iterator.hasNext()) {
                key = (String) iterator.next();
                tvInfo.setKey(key);
                object1 = object.getJSONObject(key);
                tvInfo.setKeyName(object1.getString("kn"));
                tvInfo.setCode(object1.getString("src"));
            }
            ykTvInfos.put(key,tvInfo);
        }
        return ykTvInfos;
    }
}
