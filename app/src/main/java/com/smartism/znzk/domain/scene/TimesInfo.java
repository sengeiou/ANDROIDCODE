package com.smartism.znzk.domain.scene;

/**
 * Created by Administrator on 2017/6/4.
 */

public class TimesInfo {
    String name;
    String value;
    boolean flag = false;

    public TimesInfo(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public TimesInfo(String name, String value, boolean flag) {
        this.name = name;
        this.value = value;
        this.flag = flag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }
}
