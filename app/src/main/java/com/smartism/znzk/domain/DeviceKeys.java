package com.smartism.znzk.domain;

/**
 * Created by Administrator on 2016/10/17.
 */
public class DeviceKeys {
    private long deviceId;
    private String keyName;
    private String keyIco;
    private String keyCommand;
    private int keySort;
    private int keyWhere;
    private boolean keySState; //指令是否支持状态回传

    public boolean isKeySState() {
        return keySState;
    }

    public void setKeySState(boolean keySState) {
        this.keySState = keySState;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public String getKeyIco() {
        return keyIco;
    }

    public void setKeyIco(String keyIco) {
        this.keyIco = keyIco;
    }

    public String getKeyCommand() {
        return keyCommand;
    }

    public void setKeyCommand(String keyCommand) {
        this.keyCommand = keyCommand;
    }

    public int getKeySort() {
        return keySort;
    }

    public void setKeySort(int keySort) {
        this.keySort = keySort;
    }

    public int getKeyWhere() {
        return keyWhere;
    }

    public void setKeyWhere(int keyWhere) {
        this.keyWhere = keyWhere;
    }

    @Override
    public String toString() {
        return "DeviceKeys{" +
                "deviceId=" + deviceId +
                ", keyName='" + keyName + '\'' +
                ", keyIco='" + keyIco + '\'' +
                ", keyCommand='" + keyCommand + '\'' +
                ", keySort=" + keySort +
                ", keyWhere=" + keyWhere +
                '}';
    }

    public final static String TABLE_NAME = "devices_key";
    public final static String COLUMN_DEVICEKEYS_ID = "d_id";
    public final static String COLUMN_DEVICEKEYS_NAME = "key_name";
    public final static String COLUMN_DEVICEKEYS_ICO = "key_ico";
    public final static String COLUMN_DEVICEKEYS_COMMAND = "key_command";
    public final static String COLUMN_DEVICEKEYS_SORT = "key_sort";
    public final static String COLUMN_DEVICEKEYS_WHERE = "key_where";
    public final static String COLUMN_DEVICEKEYS_SSTATE = "key_sstate";
}
