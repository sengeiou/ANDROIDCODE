package com.smartism.znzk.domain;

/**
 * Created by win7 on 2017/11/28.
 */

import java.io.Serializable;

public class MedicHabitInfo implements Serializable {
    private long id;
    private int type;
    private String name;
    private int startTime;
    private int endTime;
    private boolean valid;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public void setName(String name) {

        this.name = name;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

//        public int getValid() {
//            return valid;
//        }
//
//        public void setValid(int valid) {
//            this.valid = valid;
//        }
}

