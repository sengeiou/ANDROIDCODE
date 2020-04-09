package com.smartism.znzk.domain;

import java.io.Serializable;

/**
 * schedule java bean
 */
public class HeaterScheduleCommandInfo implements Serializable {

    private Long time;
    private int onOrOff;

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public int getOnOrOff() {
        return onOrOff;
    }

    public void setOnOrOff(int onOrOff) {
        this.onOrOff = onOrOff;
    }
}