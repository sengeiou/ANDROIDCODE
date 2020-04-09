package com.smartism.znzk.domain;

import java.io.Serializable;
import java.util.List;

public class FoundInfo implements Serializable{
    @Override
    public String toString() {
        return "FoundInfo{" +
                "tip=" + tip +
                ", appId=" + appId +
                ", id=" + id +
                ", masterId='" + masterId + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", type=" + type +
                ", controlInfos=" + controlInfos +
                ", triggerInfos=" + triggerInfos +
                '}';
    }

    /**
	 * 
	 */
	private static final long serialVersionUID = 8382070207308303248L;
	/**
     * appId : 38
     * controlInfos : [{"command":"0","deviceId":"1064","id":505,"sceneId":424,"type":0},{"command":"1","deviceId":"1108","id":506,"sceneId":424,"type":0},{"command":"3","deviceId":"1109","id":507,"sceneId":424,"type":0}]
     * id : 424
     * masterId : FF00000001
     * name : ding
     * status : 1
     * triggerInfos : [{"cycle":"10000000","id":39,"sceneId":424,"time":690,"triggerSceneId":0,"type":1}]
     * type : 1
     */
	private long tip;
    private long appId;
    private long id;
    private String masterId;
    private String name;
    private int status;
    private int type;
    /**
     * command : 0
     * deviceId : 1064
     * id : 505
     * sceneId : 424
     * type : 0
     */

    private List<ControlInfosEntity> controlInfos;
    /**
     * cycle : 10000000
     * id : 39
     * sceneId : 424
     * time : 690
     * triggerSceneId : 0
     * type : 1
     */
    
    private List<TriggerInfosEntity> triggerInfos;

    public long getTip() {
        return tip;
    }

    public void setTip(long tip) {
        this.tip = tip;
    }

    public long getAppId() {
        return appId;
    }

    public void setAppId(long appId) {
        this.appId = appId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMasterId() {
        return masterId;
    }

    public void setMasterId(String masterId) {
        this.masterId = masterId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public List<ControlInfosEntity> getControlInfos() {
        return controlInfos;
    }

    public void setControlInfos(List<ControlInfosEntity> controlInfos) {
        this.controlInfos = controlInfos;
    }

    public List<TriggerInfosEntity> getTriggerInfos() {
        return triggerInfos;
    }

    public void setTriggerInfos(List<TriggerInfosEntity> triggerInfos) {
        this.triggerInfos = triggerInfos;
    }

    public class ControlInfosEntity implements Serializable{
        /**
		 * 
		 */
		private static final long serialVersionUID = 7271760114151132894L;
		private String command;
        private String deviceId;
        private long id;
        private long sceneId;
        private String commandName;
        private int type;

        public String getCommandName() {
            return commandName;
        }

        public void setCommandName(String commandName) {
            this.commandName = commandName;
        }

        public String getCommand() {
            return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public long getSceneId() {
            return sceneId;
        }

        public void setSceneId(long sceneId) {
            this.sceneId = sceneId;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return "ControlInfosEntity{" +
                    "command='" + command + '\'' +
                    ", deviceId='" + deviceId + '\'' +
                    ", id=" + id +
                    ", sceneId=" + sceneId +
                    ", type=" + type +
                    '}';
        }
    }

    public class TriggerInfosEntity implements Serializable {
        /**
		 * 
		 */
		private static final long serialVersionUID = -2005384477397653101L;
		private String cycle;
        private long id;
        private long sceneId;
        private int time;
        private long triggerSceneId;
        private int type;
        private String command;
        private long device;
        private String commandName;

        public String getCommandName() {
            return commandName;
        }

        public void setCommandName(String commandName) {
            this.commandName = commandName;
        }

        public String getCycle() {
            return cycle;
        }

        public void setCycle(String cycle) {
            this.cycle = cycle;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public long getSceneId() {
            return sceneId;
        }

        public void setSceneId(long sceneId) {
            this.sceneId = sceneId;
        }

        public int getTime() {
            return time;
        }

        public void setTime(int time) {
            this.time = time;
        }

        public long getTriggerSceneId() {
            return triggerSceneId;
        }

        public void setTriggerSceneId(long triggerSceneId) {
            this.triggerSceneId = triggerSceneId;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getCommand() {
            return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }

        public long getDevice() {
            return device;
        }

        public void setDevice(long device) {
            this.device = device;
        }

        @Override
        public String toString() {
            return "TriggerInfosEntity{" +
                    "cycle='" + cycle + '\'' +
                    ", id=" + id +
                    ", sceneId=" + sceneId +
                    ", time=" + time +
                    ", triggerSceneId=" + triggerSceneId +
                    ", type=" + type +
                    ", command='" + command + '\'' +
                    ", device=" + device +
                    ", commandName='" + commandName + '\'' +
                    '}';
        }
    }


}
