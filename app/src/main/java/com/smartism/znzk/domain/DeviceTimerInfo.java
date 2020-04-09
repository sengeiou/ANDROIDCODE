package com.smartism.znzk.domain;

import java.io.Serializable;
import java.util.List;

/**
 * Created by win7 on 2017/2/23.
 */

public class DeviceTimerInfo implements Serializable {

    private long id;
    private long did;
    private int status;
    private int type;
    private String cycle;
    private int time;

    public String getCycle() {
        return cycle;
    }

    public void setCycle(String cycle) {
        this.cycle = cycle;
    }

    private List<DeviceControlInfo> controlInfos;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getDid() {
        return did;
    }

    public void setDid(long did) {
        this.did = did;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }


    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public List<DeviceControlInfo> getControlInfos() {
        return controlInfos;
    }

    public void setControlInfos(List<DeviceControlInfo> controlInfos) {
        this.controlInfos = controlInfos;
    }

  public   class DeviceControlInfo implements Serializable {
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

        public long getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(long deviceId) {
            this.deviceId = deviceId;
        }

        public int getCommand() {
            return command;
        }

        public void setCommand(int command) {
            this.command = command;
        }

        public String getCommandName() {
            return commandName;
        }

        public void setCommandName(String commandName) {
            this.commandName = commandName;
        }

        private long id;
        private int type;
        private long deviceId;
        private int command;
        private String commandName;

    }
}
