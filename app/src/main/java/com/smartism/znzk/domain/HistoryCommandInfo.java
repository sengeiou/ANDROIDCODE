package com.smartism.znzk.domain;

import java.io.Serializable;

/**
 * Created by win7 on 2017/5/30.
 */

public class HistoryCommandInfo implements Serializable {
    private String opreator;
    private String date;
    private String command;

    public String getCommandShidu() {
        return commandShidu;
    }

    public void setCommandShidu(String commandShidu) {
        this.commandShidu = commandShidu;
    }

    private String commandShidu;

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    private String dayOfWeek;

    public String getOpreator() {
        return opreator;
    }

    public void setOpreator(String opreator) {
        this.opreator = opreator;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}