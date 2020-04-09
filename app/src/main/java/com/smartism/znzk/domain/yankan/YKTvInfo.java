package com.smartism.znzk.domain.yankan;

import java.io.Serializable;

/**
 * Created by win7 on 2017/3/4.
 */

public class YKTvInfo implements Serializable {
    private String key;
    private String keyName;
    private String code;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}