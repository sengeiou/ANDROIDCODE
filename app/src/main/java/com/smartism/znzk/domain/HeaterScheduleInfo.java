package com.smartism.znzk.domain;

import java.io.Serializable;

/**
 * schedule java bean
 */
public class HeaterScheduleInfo implements Serializable {

    private int id;
    private Long time;
    private int cycle;
    private int duration;
    private int state;
    private int delete;//是否删除，仅下行有效

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public int getCycle() {
        return cycle;
    }

    public void setCycle(int cycle) {
        this.cycle = cycle;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getDelete() {
        return delete;
    }

    public void setDelete(int delete) {
        this.delete = delete;
    }
}