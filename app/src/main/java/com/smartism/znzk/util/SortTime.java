package com.smartism.znzk.util;

import com.alibaba.fastjson.JSONObject;

import java.util.Comparator;

/**
 * Created by win7 on 2017/4/19.
 */

public class SortTime implements Comparator {
    private String t;
    public SortTime(String t){
        this.t = t;
    }

    @Override
    public int compare(Object lhs, Object rhs) {
        JSONObject object1 = (JSONObject) lhs;
        JSONObject object2 = (JSONObject) rhs;

//        if (object1.getString("t") != null) {
//            return object1.getString("t").compareTo(object2.getString("t"));
//        } else if (object1.getLong("t") != null) {
//            if (object1.getLongValue("t") - object2.getLongValue("t") < 0) {
//                return -1;
//            } else {
//                return 1;
//            }
//        }
        return object2.getString(t).compareTo(object1.getString(t));
    }
}
