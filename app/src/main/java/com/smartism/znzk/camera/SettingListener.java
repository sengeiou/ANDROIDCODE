package com.smartism.znzk.camera;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.p2p.core.P2PHandler;
import com.p2p.core.P2PInterface.ISetting;
import com.p2p.core.P2PValue;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.db.camera.DataManager;
import com.smartism.znzk.db.camera.SysMessage;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.global.FList;
import com.smartism.znzk.global.NpcCommon;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by dansesshou on 16/11/30.
 * 设备设置返回结果回调,分为ACK回调和结果回调
 * ACK回调可以判断这条命令是否正确发送到设备端(回调方法名前有ACK前缀)
 * 结果回调则是业务数据,根据硬件接口说明解析即可
 * update by 王建 on 2019-10-27
 */

public class SettingListener implements ISetting {
    String TAG = "jiwei-SettingListener";
    private static boolean isAlarming = false;
    private static String MonitorDeviceID = "";

    public static void setAlarm(boolean isAlarm) {
        SettingListener.isAlarming = isAlarm;
    }

    public static void setMonitorID(String id) {
        SettingListener.MonitorDeviceID = id;
    }

    @Override
    public void ACK_vRetSetDeviceTime(int msgId, int result) {
        // 设置设备时间ACK回调
        Log.e(TAG, "ACK_vRetSetDeviceTime:" + result);
        Intent i = new Intent();
        i.putExtra("result", result);
        i.setAction(Constants.P2P.ACK_RET_SET_TIME);
        MainApplication.app.sendBroadcast(i);
    }

    @Override
    public void ACK_vRetGetDeviceTime(int msgId, int result) {
        // 获取设备时间ACK回调
        Log.e(TAG, "ACK_vRetGetDeviceTime:" + result);
        Intent i = new Intent();
        i.putExtra("result", result);
        i.setAction(Constants.P2P.ACK_RET_GET_TIME);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void ACK_vRetGetNpcSettings(String contactId, int msgId, int result) {
        Log.e(TAG, "ACK_vRetGetNpcSettings:" + result);
        Intent i = new Intent();
        i.putExtra("result", result);
        i.setAction(Constants.P2P.ACK_RET_GET_NPC_SETTINGS);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void ACK_vRetSetRemoteDefence(String contactId, int msgId, int result) {
        Log.e(TAG, "ACK_vRetSetRemoteDefence:" + result);
        Log.e("remote_defence", "ACK_vRetSetRemoteDefence--" + "contactId=" + contactId + "---" + "result=" + result);
        if (result == Constants.P2P_SET.ACK_RESULT.ACK_SUCCESS) {
            Contact contact = FList.getInstance().isContact(contactId);
            if (null != contact) {
                P2PHandler.getInstance().getNpcSettings(contact.contactId,
                        contact.contactPassword,0);
            }

        } else if (result == Constants.P2P_SET.ACK_RESULT.ACK_NET_ERROR) {
            FList.getInstance().setDefenceState(contactId,
                    Constants.DefenceState.DEFENCE_STATE_WARNING_NET);
            Intent i = new Intent();
            i.putExtra("state",
                    Constants.DefenceState.DEFENCE_STATE_WARNING_NET);
            i.putExtra("contactId", contactId);
            i.setAction(Constants.P2P.RET_GET_REMOTE_DEFENCE);
            MainApplication.app.sendBroadcast(i);

        } else if (result == Constants.P2P_SET.ACK_RESULT.ACK_PWD_ERROR) {
            FList.getInstance().setDefenceState(contactId,
                    Constants.DefenceState.DEFENCE_STATE_WARNING_PWD);
            Intent i = new Intent();
            i.putExtra("state",
                    Constants.DefenceState.DEFENCE_STATE_WARNING_PWD);
            i.putExtra("contactId", contactId);
            i.setAction(Constants.P2P.RET_GET_REMOTE_DEFENCE);
            MainApplication.app.sendBroadcast(i);
        } else if (result == Constants.P2P_SET.ACK_RESULT.ACK_INSUFFICIENT_PERMISSIONS) {
            FList.getInstance().setDefenceState(contactId,
                    Constants.DefenceState.DEFENCE_NO_PERMISSION);
        }


    }

    @Override
    public void ACK_vRetSetRemoteRecord(int msgId, int result) {
        Log.e(TAG, "ACK_vRetSetRemoteRecord:" + result);
        Intent i = new Intent();
        i.putExtra("result", result);
        i.setAction(Constants.P2P.ACK_RET_SET_REMOTE_RECORD);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void ACK_vRetSetNpcSettingsVideoFormat(int msgId, int result) {
        Log.e(TAG, "ACK_vRetSetNpcSettingsVideoFormat:" + result);
        Intent i = new Intent();
        i.putExtra("result", result);
        i.setAction(Constants.P2P.ACK_RET_SET_VIDEO_FORMAT);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void ACK_vRetSetNpcSettingsVideoVolume(int msgId, int result) {
        Log.e(TAG, "ACK_vRetSetNpcSettingsVideoVolume:" + result);
        Intent i = new Intent();
        i.setAction(Constants.P2P.ACK_RET_SET_VIDEO_VOLUME);
        i.putExtra("result", result);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void ACK_vRetSetNpcSettingsBuzzer(int msgId, String s, int result) {
        Intent i = new Intent();
        i.putExtra("result", result);
        i.setAction(Constants.P2P.ACK_RET_SET_BUZZER);
        Log.e("报警", "开始报警7");
        MainApplication.app.sendBroadcast(i);
    }

    @Override
    public void ACK_vRetSetNpcSettingsMotion(int msgId, String s, int result) {
        Log.e(TAG, "ACK_vRetSetNpcSettingsMotion:" + result);
        Intent i = new Intent();
        i.putExtra("result", result);
        i.setAction(Constants.P2P.ACK_RET_SET_MOTION);
        Log.e("报警", "开始报警5");
        MainApplication.app.sendBroadcast(i);
    }

    @Override
    public void ACK_vRetSetNpcSettingsRecordType(int msgId, int result) {
        Log.e(TAG, "ACK_vRetSetNpcSettingsRecordType:" + result);
        Intent i = new Intent();
        i.putExtra("result", result);
        i.setAction(Constants.P2P.ACK_RET_SET_RECORD_TYPE);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void ACK_vRetSetNpcSettingsRecordTime(int msgId, int result) {
        Log.e(TAG, "ACK_vRetSetNpcSettingsRecordTime:" + result);
        Intent i = new Intent();
        i.putExtra("result", result);
        i.setAction(Constants.P2P.ACK_RET_SET_RECORD_TIME);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void ACK_vRetSetNpcSettingsRecordPlanTime(int msgId, int result) {
        Log.e(TAG, "ACK_vRetSetNpcSettingsRecordPlanTime:" + result);
        Intent i = new Intent();
        i.putExtra("result", result);
        i.setAction(Constants.P2P.ACK_RET_SET_RECORD_PLAN_TIME);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void ACK_vRetSetNpcSettingsNetType(int msgId, int result) {
        Log.e(TAG, "ACK_vRetSetNpcSettingsNetType:" + result);
        Intent i = new Intent();
        i.setAction(Constants.P2P.ACK_RET_SET_NET_TYPE);
        i.putExtra("result", result);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void ACK_vRetSetAlarmEmail(int msgId, int result) {
        Log.e(TAG, "ACK_vRetSetAlarmEmail:" + result);
        Intent i = new Intent();
        i.putExtra("result", result);
        i.setAction(Constants.P2P.ACK_RET_SET_ALARM_EMAIL);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void ACK_vRetGetAlarmEmail(int msgId, String s, int result) {
        Log.e(TAG, "ACK_vRetGetAlarmEmail:" + result);
        Intent i = new Intent();
        i.putExtra("result", result);
        i.setAction(Constants.P2P.ACK_RET_GET_ALARM_EMAIL);
        MainApplication.app.sendBroadcast(i);
    }

    @Override
    public void ACK_vRetSetAlarmBindId(int srcID, String s, int result) {
        Log.e(TAG, "ACK_vRetSetAlarmBindId:" + result);
        Intent i = new Intent();
        i.putExtra("result", result);
        i.putExtra("srcID", String.valueOf(srcID));
        i.setAction(Constants.P2P.ACK_RET_SET_BIND_ALARM_ID);
        MainApplication.app.sendBroadcast(i);
    }

    @Override
    public void ACK_vRetGetAlarmBindId(int srcID, int result) {
        Log.e(TAG, "ACK_vRetGetAlarmBindId:" + result);
        Intent i = new Intent();
        i.putExtra("result", result);
        i.setAction(Constants.P2P.ACK_RET_GET_BIND_ALARM_ID);
        i.putExtra("srcID", String.valueOf(srcID));
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void ACK_vRetSetInitPassword(int msgId, int result) {
        Log.e(TAG, "ACK_vRetSetInitPassword:" + result);
        Intent i = new Intent();
        i.setAction(Constants.P2P.ACK_RET_SET_INIT_PASSWORD);
        i.putExtra("result", result);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void ACK_vRetSetDevicePassword(int msgId, int result) {
        Log.e(TAG, "ACK_vRetSetDevicePassword:" + result);
        Intent i = new Intent();
        i.setAction(Constants.P2P.ACK_RET_SET_DEVICE_PASSWORD);
        i.putExtra("result", result);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void ACK_vRetCheckDevicePassword(int msgId, int result, String deviceId) {
        Log.e(TAG, "ACK_vRetCheckDevicePassword:" + result);
        // if(result==Constants.P2P_SET.ACK_RESULT.ACK_INSUFFICIENT_PERMISSIONS){
        // FList.getInstance().setDefenceState(threeNum, state)
        // }
        Intent i = new Intent();
        i.setAction(Constants.P2P.ACK_RET_CHECK_PASSWORD);
        i.putExtra("result", result);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void ACK_vRetSetWifi(int msgId, int result) {
        Log.e(TAG, "ACK_vRetSetWifi:" + result);
        Intent i = new Intent();
        i.putExtra("result", result);
        i.setAction(Constants.P2P.ACK_RET_SET_WIFI);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void ACK_vRetGetWifiList(int msgId, int result) {
        Log.e(TAG, "ACK_vRetGetWifiList:" + result);
        Intent i = new Intent();
        i.putExtra("result", result);
        i.setAction(Constants.P2P.ACK_RET_GET_WIFI);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void ACK_vRetSetDefenceArea(int msgId, int result) {
        Log.e(TAG, "ACK_vRetSetDefenceArea:" + result);
        Intent i = new Intent();
        i.putExtra("result", result);
        i.setAction(Constants.P2P.ACK_RET_SET_DEFENCE_AREA);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void ACK_vRetGetDefenceArea(int msgId, int result) {
        Log.e(TAG, "ACK_vRetGetDefenceArea:" + result);
        Intent i = new Intent();
        i.putExtra("result", result);
        i.setAction(Constants.P2P.ACK_RET_GET_DEFENCE_AREA);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void ACK_vRetGetRecordFileList(int msgId, int result) {
        Log.e(TAG, "ACK_vRetGetRecordFileList:" + result);
        Intent i = new Intent();
        i.putExtra("result", result);
        i.setAction(Constants.P2P.ACK_RET_GET_PLAYBACK_FILES);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void ACK_vRetMessage(int msgId, int result) {
        Intent i = new Intent();
        i.setAction(Constants.Action.RECEIVE_MSG);
        i.putExtra("msgFlag", msgId + "");
        i.putExtra("result", result);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void ACK_vRetCustomCmd(int msgId, int result) {
        Log.e("dxsTest", "ACK_vRetCustomCmd:" + msgId + "result:" + result);


    }

    @Override
    public void ACK_vRetGetDeviceVersion(int deviceId, int msgId, int result) {
        Log.e(TAG, "ACK_vRetGetDeviceVersion:" + result);
        Intent i = new Intent();
        i.putExtra("result", result);
        i.setAction(Constants.P2P.ACK_RET_GET_DEVICE_INFO);
        MainApplication.app.sendBroadcast(i);
    }

    @Override
    public void ACK_vRetCheckDeviceUpdate(int msgId, int result) {
        Log.e(TAG, "ACK_vRetCheckDeviceUpdate:" + result);
        Intent i = new Intent();
        i.putExtra("result", result);
        i.setAction(Constants.P2P.ACK_RET_CHECK_DEVICE_UPDATE);
        MainApplication.app.sendBroadcast(i);
    }

    @Override
    public void ACK_vRetDoDeviceUpdate(int msgId, int result) {
        Log.e(TAG, "ACK_vRetDoDeviceUpdate:" + result);
        Intent i = new Intent();
        i.putExtra("result", result);
        i.setAction(Constants.P2P.ACK_RET_DO_DEVICE_UPDATE);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void ACK_vRetCancelDeviceUpdate(int msgId, int result) {
        Log.e(TAG, "ACK_vRetCancelDeviceUpdate:" + result);
        Intent i = new Intent();
        i.putExtra("result", result);
        i.setAction(Constants.P2P.ACK_RET_CANCEL_DEVICE_UPDATE);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void ACK_vRetClearDefenceAreaState(int msgId, int result) {
        Log.e(TAG, "ACK_vRetClearDefenceAreaState:" + result);
        Intent i = new Intent();
        i.putExtra("result", result);
        i.setAction(Constants.P2P.ACK_RET_CLEAR_DEFENCE_AREA);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void ACK_vRetGetDefenceStates(String contactId, int msgId, int result) {
        Log.e(TAG, "ACK_vRetGetDefenceStates:" + result);
        Log.e("defence", "contactId=" + contactId + "result=" + result);
        if (result == Constants.P2P_SET.ACK_RESULT.ACK_NET_ERROR) {
            FList.getInstance().setDefenceState(contactId,
                    Constants.DefenceState.DEFENCE_STATE_WARNING_NET);
            Intent i = new Intent();
            i.putExtra("state",
                    Constants.DefenceState.DEFENCE_STATE_WARNING_NET);
            i.putExtra("contactId", contactId);
            i.setAction(Constants.P2P.RET_GET_REMOTE_DEFENCE);
            MainApplication.app.sendBroadcast(i);
        } else if (result == Constants.P2P_SET.ACK_RESULT.ACK_PWD_ERROR) {
            FList.getInstance().setDefenceState(contactId,
                    Constants.DefenceState.DEFENCE_STATE_WARNING_PWD);
            Intent i = new Intent();
            i.putExtra("state",
                    Constants.DefenceState.DEFENCE_STATE_WARNING_PWD);
            i.putExtra("contactId", contactId);
            i.setAction(Constants.P2P.RET_GET_REMOTE_DEFENCE);
            Log.e("报警", "开始报警0");
            MainApplication.app.sendBroadcast(i);
        } else if (result == Constants.P2P_SET.ACK_RESULT.ACK_INSUFFICIENT_PERMISSIONS) {
            FList.getInstance().setDefenceState(contactId,
                    Constants.DefenceState.DEFENCE_NO_PERMISSION);
        }
        Intent ack_Defence = new Intent();
        ack_Defence.setAction(Constants.P2P.ACK_GET_REMOTE_DEFENCE);
        ack_Defence.putExtra("contactId", contactId);
        ack_Defence.putExtra("result", result);
        MainApplication.app.sendBroadcast(ack_Defence);


    }

    @Override
    public void ACK_vRetSetImageReverse(int msgId, int result) {
        Intent i = new Intent();
        i.setAction(Constants.P2P.ACK_VRET_SET_IMAGEREVERSE);
        i.putExtra("result", result);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void ACK_vRetSetInfraredSwitch(int msgId, int result) {
        Intent i = new Intent();
        i.setAction(Constants.P2P.ACK_RET_SET_INFRARED_SWITCH);
        i.putExtra("result", result);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void ACK_vRetSetWiredAlarmInput(int msgId, int state) {
        Intent i = new Intent();
        i.setAction(Constants.P2P.ACK_RET_SET_WIRED_ALARM_INPUT);
        i.putExtra("state", state);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void ACK_vRetSetWiredAlarmOut(int msgId, int state) {
        Intent i = new Intent();
        i.setAction(Constants.P2P.ACK_RET_SET_WIRED_ALARM_OUT);
        i.putExtra("state", state);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void ACK_vRetSetAutomaticUpgrade(int msgId, int state) {
        Intent i = new Intent();
        i.setAction(Constants.P2P.ACK_RET_SET_AUTOMATIC_UPGRADE);
        i.putExtra("state", state);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void ACK_VRetSetVisitorDevicePassword(int msgId, int state) {
        Log.i("dxssetting", "state-->" + state);
        Intent i = new Intent();
        i.setAction(Constants.P2P.ACK_RET_SET_VISITOR_DEVICE_PASSWORD);
        i.putExtra("state", state);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void ACK_vRetSetTimeZone(int msgId, int state) {
        Intent i = new Intent();
        i.setAction(Constants.P2P.ACK_RET_SET_TIME_ZONE);
        i.putExtra("state", state);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void ACK_vRetGetSDCard(int msgId, int state) {
        Intent i = new Intent();
        i.setAction(Constants.P2P.ACK_GET_SD_CARD_CAPACITY);
        i.putExtra("result", state);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void ACK_vRetSdFormat(int msgId, int state) {
        Intent i = new Intent();
        i.setAction(Constants.P2P.ACK_GET_SD_CARD_FORMAT);
        i.putExtra("result", state);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void ACK_vRetSetGPIO(int msgId, int state) {


    }

    @Override
    public void ACK_vRetSetGPIO1_0(int msgId, int state) {


    }

    @Override
    public void ACK_vRetSetPreRecord(int msgId, int state) {
        Intent i = new Intent();
        i.setAction(Constants.P2P.ACK_RET_SET_PRE_RECORD);
        i.putExtra("state", state);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void ACK_vRetGetSensorSwitchs(int msgId, int state) {
        Intent i = new Intent();
        i.setAction(Constants.P2P.ACK_RET_GET_SENSOR_SWITCH);
        i.putExtra("result", state);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void ACK_vRetSetSensorSwitchs(int msgId, int state) {
        Intent i = new Intent();
        i.setAction(Constants.P2P.ACK_RET_SET_SENSOR_SWITCH);
        i.putExtra("result", state);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void ACK_vRetGetAlarmCenter(int msgId, int state) {


    }

    @Override
    public void ACK_vRetSetAlarmCenter(int msgId, int state) {


    }

    @Override
    public void ACK_VRetGetNvrIpcList(int msgId, int state) {
        Intent i = new Intent();
        i.setAction(Constants.P2P.ACK_GET_NVR_IPC_LIST);
        i.putExtra("state", state);
        MainApplication.app.sendBroadcast(i);
        Log.e("ACK_VRetGetNvrIpcList", "state=" + state);


    }

    @Override
    public void ACK_VRetGetNvrInfo(String deviceId, int msgId, int state) {


    }

    @Override
    public void ACK_OpenDoor(int msgId, int state) {


    }

    @Override
    public void ACK_vRetGetFTPInfo(int msgId, int state) {


    }

    @Override
    public void ACK_vRetGetPIRLight(int msgId, int state) {


    }

    @Override
    public void ACK_vRetSetPIRLight(int msgId, int state) {


    }

    @Override
    public void ACK_vRetGetDefenceWorkGroup(int msgId, int state) {


    }

    @Override
    public void ACK_VRetGetPresetPos(int msgId, int state) {


    }

    @Override
    public void ACK_VRetSetKeepClient(String contactId, int msgId, int state) {


    }

    @Override
    public void ACK_VRetGetLed(String contactId, int msgId, int state) {


    }

    @Override
    public void ACK_VRetSetLed(String contactId, int msgId, int state) {


    }

    @Override
    public void ACK_vRetSetNpcSettingsMotionSens(int msgId, String s, int result) {

    }

    @Override
    public void ACK_vRetGetVideoQuality(int msgId, int result) {


    }

    @Override
    public void ACK_vRetSetVideoQuality(int msgId, int result) {


    }

    @Override
    public void ACK_vRetGetApIsWifiSetting(String contactId, int msgId, int result) {


    }

    @Override
    public void ACK_vRetSetApStaWifiInfo(String contactId, int msgId, int result) {


    }

    @Override
    public void ACK_vRetSetLockInfo(String contactId, int msgId, int result) {

    }

    @Override
    public void ACK_vRetGetLockInfo(String contactId, int msgId, int result) {

    }

    @Override
    public void ACK_vRetSetLEDStatus(String contactId, int msgId, int result) {


    }

    @Override
    public void ACK_vRetSetApStart(String contactId, int msgId, int result) {

    }


    @Override
    public void vRetGetRemoteDefenceResult(String contactId, int state) {
        Log.i(TAG, "vRetGetRemoteDefenceResult:" + state);
        if (state == Constants.P2P_SET.REMOTE_DEFENCE_SET.ALARM_SWITCH_ON) {
            FList.getInstance().setDefenceState(contactId,
                    Constants.DefenceState.DEFENCE_STATE_ON);
        } else {
            FList.getInstance().setDefenceState(contactId,
                    Constants.DefenceState.DEFENCE_STATE_OFF);
        }

        Intent defence = new Intent();
        defence.setAction(Constants.P2P.RET_GET_REMOTE_DEFENCE);
        defence.putExtra("state", state);
        defence.putExtra("contactId", contactId);
        MainApplication.app.sendBroadcast(defence);


    }

    @Override
    public void vRetGetRemoteRecordResult(int contactId, int result) {
        Log.e(TAG, "vRetGetRemoteRecordResult:" + result);
        Intent record = new Intent();
        record.setAction(Constants.P2P.RET_GET_REMOTE_RECORD);
        record.putExtra("contactId", contactId);
        record.putExtra("state", result);
        MainApplication.app.sendBroadcast(record);
    }

    @Override
    public void vRetGetBuzzerResult(int state) {
        Log.e(TAG, "vRetGetBuzzerResult:" + state);
        Intent buzzer = new Intent();
        buzzer.setAction(Constants.P2P.RET_GET_BUZZER);
        buzzer.putExtra("buzzerState", state);
        Log.e("报警", "开始报警8");
        MainApplication.app.sendBroadcast(buzzer);
    }

    @Override
    public void vRetGetMotionResult(int contactId, int state) {
        Log.e(TAG, "vRetGetMotionResult:" + state);
        Intent motion = new Intent();
        motion.setAction(Constants.P2P.RET_GET_MOTION);
        motion.putExtra("contactId", contactId);
        motion.putExtra("motionState", state);
        Log.e("报警", "开始报警6");
        MainApplication.app.sendBroadcast(motion);
    }

    @Override
    public void vRetGetVideoFormatResult(int type) {
        Log.e(TAG, "vRetGetVideoFormatResult:" + type);
        Intent format_type = new Intent();
        format_type.setAction(Constants.P2P.RET_GET_VIDEO_FORMAT);
        format_type.putExtra("type", type);
        MainApplication.app.sendBroadcast(format_type);


    }

    @Override
    public void vRetGetRecordTypeResult(int type) {
        Log.e(TAG, "vRetGetRecordTypeResult:" + type);
        Intent record_type = new Intent();
        record_type.setAction(Constants.P2P.RET_GET_RECORD_TYPE);
        record_type.putExtra("type", type);
        MainApplication.app.sendBroadcast(record_type);


    }

    @Override
    public void vRetGetRecordTimeResult(int time) {
        Log.e(TAG, "vRetGetRecordTimeResult:" + time);
        Intent record_time = new Intent();
        record_time.setAction(Constants.P2P.RET_GET_RECORD_TIME);
        record_time.putExtra("time", time);
        MainApplication.app.sendBroadcast(record_time);


    }

    @Override
    public void vRetGetNetTypeResult(int type) {
        Log.e(TAG, "vRetGetNetTypeResult:" + type);
        Intent net_type = new Intent();
        net_type.setAction(Constants.P2P.RET_GET_NET_TYPE);
        net_type.putExtra("type", type);
        MainApplication.app.sendBroadcast(net_type);


    }

    @Override
    public void vRetGetVideoVolumeResult(int value) {
        Log.e(TAG, "vRetGetVideoVolumeResult:" + value);
        Intent volume = new Intent();
        volume.setAction(Constants.P2P.RET_GET_VIDEO_VOLUME);
        volume.putExtra("value", value);
        MainApplication.app.sendBroadcast(volume);


    }

    @Override
    public void vRetGetRecordPlanTimeResult(String time) {
        Log.e(TAG, "vRetGetRecordPlanTimeResult:" + time);
        Intent plan_time = new Intent();
        plan_time.setAction(Constants.P2P.RET_GET_RECORD_PLAN_TIME);
        plan_time.putExtra("time", time);
        MainApplication.app.sendBroadcast(plan_time);


    }

    @Override
    public void vRetGetImageReverseResult(int type) {
        Intent i = new Intent();
        i.setAction(Constants.P2P.RET_GET_IMAGE_REVERSE);
        i.putExtra("type", type);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void vRetGetInfraredSwitch(int deviceId, int state) {
        Intent i = new Intent();
        i.setAction(Constants.P2P.RET_GET_INFRARED_SWITCH);
        i.putExtra("state", state);
        MainApplication.app.sendBroadcast(i);
    }

    @Override
    public void vRetGetWiredAlarmInput(int state) {
        Intent i = new Intent();
        i.setAction(Constants.P2P.RET_GET_WIRED_ALARM_INPUT);
        i.putExtra("state", state);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void vRetGetWiredAlarmOut(int state) {
        Intent i = new Intent();
        i.setAction(Constants.P2P.RET_GET_WIRED_ALARM_OUT);
        i.putExtra("state", state);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void vRetGetAutomaticUpgrade(int state) {
        Intent i = new Intent();
        i.setAction(Constants.P2P.RET_GET_AUTOMATIC_UPGRAD);
        i.putExtra("state", state);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void vRetGetTimeZone(int state) {
        Intent i = new Intent();
        i.setAction(Constants.P2P.RET_GET_TIME_ZONE);
        i.putExtra("state", state);
        MainApplication.app.sendBroadcast(i);
        Log.e("leleTest", "getTimeZone=" + state);


    }

    @Override
    public void vRetGetAudioDeviceType(int type) {
        Intent i = new Intent();
        i.setAction(Constants.P2P.RET_GET_AUDIO_DEVICE_TYPE);
        i.putExtra("type", type);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void vRetGetPreRecord(int state) {
        Intent i = new Intent();
        i.setAction(Constants.P2P.RET_GET_PRE_RECORD);
        i.putExtra("state", state);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void vRetGetSensorSwitchs(int result, ArrayList<int[]> data) {
        Intent i = new Intent();
        i.setAction(Constants.P2P.ACK_RET_GET_SENSOR_SWITCH);
        i.putExtra("result", result);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void vRetSetRemoteDefenceResult(String contactId, int result) {
        Log.e(TAG, "vRetSetRemoteDefenceResult:" + result);
        Intent defence = new Intent();
        defence.setAction(Constants.P2P.RET_SET_REMOTE_DEFENCE);
        defence.putExtra("state", result);
        defence.putExtra("contactId", contactId);
        MainApplication.app.sendBroadcast(defence);


    }

    @Override
    public void vRetSetRemoteRecordResult(int result) {
        Log.e(TAG, "vRetSetRemoteRecordResult:" + result);
        Intent record = new Intent();
        record.setAction(Constants.P2P.RET_SET_REMOTE_RECORD);
        record.putExtra("result", result);
        MainApplication.app.sendBroadcast(record);


    }

    @Override
    public void vRetSetBuzzerResult(int result) {
        Log.e(TAG, "vRetSetBuzzerResult:" + result);
        Intent i = new Intent();
        i.setAction(Constants.P2P.RET_SET_BUZZER);
        i.putExtra("result", result);
        Log.e("报警", "开始报警4");
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void vRetSetMotionResult(int contactId, int result) {
        Log.e(TAG, "vRetSetMotionResult:" + result);
        Intent i = new Intent();
        i.setAction(Constants.P2P.RET_SET_MOTION);
        i.putExtra("result", result);
        i.putExtra("contactId", contactId);
        Log.e("报警", "开始报警3");
        MainApplication.app.sendBroadcast(i);
    }

    @Override
    public void vRetSetVideoFormatResult(int result) {
        Log.e(TAG, "vRetSetVideoFormatResult:" + result);
        Intent i = new Intent();
        i.setAction(Constants.P2P.RET_SET_VIDEO_FORMAT);
        i.putExtra("result", result);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void vRetSetRecordTypeResult(int result) {
        Log.e(TAG, "vRetSetRecordTypeResult:" + result);
        Intent i = new Intent();
        i.setAction(Constants.P2P.RET_SET_RECORD_TYPE);
        i.putExtra("result", result);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void vRetSetRecordTimeResult(int result) {
        Log.e(TAG, "vRetSetRecordTimeResult:" + result);
        Intent i = new Intent();
        i.setAction(Constants.P2P.RET_SET_RECORD_TIME);
        i.putExtra("result", result);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void vRetSetNetTypeResult(int result) {
        Log.e(TAG, "vRetSetNetTypeResult:" + result);
        Intent i = new Intent();
        i.setAction(Constants.P2P.RET_SET_NET_TYPE);
        i.putExtra("result", result);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void vRetSetVolumeResult(int result) {
        Log.e(TAG, "vRetSetVolumeResult:" + result);
        Intent i = new Intent();
        i.setAction(Constants.P2P.RET_SET_VIDEO_VOLUME);
        i.putExtra("result", result);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void vRetSetRecordPlanTimeResult(int result) {
        Log.e(TAG, "vRetSetRecordPlanTimeResult:" + result);
        Intent i = new Intent();
        i.setAction(Constants.P2P.RET_SET_RECORD_PLAN_TIME);
        i.putExtra("result", result);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void vRetSetDeviceTimeResult(int result) {
        Log.e(TAG, "vRetSetDeviceTimeResult:" + result);
        Intent i = new Intent();
        i.putExtra("result", result);
        i.setAction(Constants.P2P.RET_SET_TIME);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void vRetGetDeviceTimeResult(String time) {
        Log.e(TAG, "vRetGetDeviceTimeResult:" + time);
        Intent i = new Intent();
        i.setAction(Constants.P2P.RET_GET_TIME);
        i.putExtra("time", time);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void vRetAlarmEmailResult(int result, String email) {
        Log.e("option", "vRetAlarmEmailResult:" + result + ":" + email);
        byte option = (byte) (result & (1 << 0));
        byte ooo = (byte) ((result << 1) & (0x1));
        Log.e("option", "option:" + option + "ooo-->" + ooo);
        if (option == 1) {
            Intent i = new Intent();
            i.setAction(Constants.P2P.RET_GET_ALARM_EMAIL);
            i.putExtra("email", email);
            i.putExtra("result", result);
            MainApplication.app.sendBroadcast(i);
        } else {
            Intent i = new Intent();
            i.putExtra("result", result);
            i.setAction(Constants.P2P.RET_SET_ALARM_EMAIL);
            MainApplication.app.sendBroadcast(i);
        }


    }

    @Override
    public void vRetAlarmEmailResultWithSMTP(int result, String email, int smtpport, byte Entry, String[] SmptMessage, byte reserve) {
        if ((result & (1 << 0)) == 1) {
            Intent i = new Intent();
            i.setAction(Constants.P2P.RET_GET_ALARM_EMAIL_WITHSMTP);
            i.putExtra("contectid", SmptMessage[5]);
            i.putExtra("result", result);
            i.putExtra("email", email);
            i.putExtra("smtpport", smtpport);
            i.putExtra("SmptMessage", SmptMessage);
            i.putExtra("encrypt", (int) Entry);
            i.putExtra("isSupport", (int) reserve);
            MainApplication.app.sendBroadcast(i);
        } else {
            Intent i = new Intent();
            i.putExtra("result", result);
            i.setAction(Constants.P2P.RET_SET_ALARM_EMAIL);
            MainApplication.app.sendBroadcast(i);
        }


    }

    @Override
    public void vRetWifiResult(int result, int currentId, int count, int[] types, int[] strengths, String[] names) {
        Log.e(TAG, "vRetWifiResult:" + result + ":" + currentId);
        if (result == 1) {
            Intent i = new Intent();
            i.setAction(Constants.P2P.RET_GET_WIFI);
            i.putExtra("iCurrentId", currentId);
            i.putExtra("iCount", count);
            i.putExtra("iType", types);
            i.putExtra("iStrength", strengths);
            i.putExtra("names", names);
            MainApplication.app.sendBroadcast(i);
        } else {
            Intent i = new Intent();
            i.putExtra("result", result);
            i.setAction(Constants.P2P.RET_SET_WIFI);
            MainApplication.app.sendBroadcast(i);
        }


    }

    @Override
    public void vRetDefenceAreaResult(int result, ArrayList<int[]> data, int group, int item) {
        Log.e(TAG, "vRetDefenceAreaResult:" + result);
        if (result == 1) {
            Intent i = new Intent();
            i.setAction(Constants.P2P.RET_GET_DEFENCE_AREA);
            i.putExtra("data", data);
            MainApplication.app.sendBroadcast(i);
        } else {
            Intent i = new Intent();
            i.putExtra("result", result);
            i.setAction(Constants.P2P.RET_SET_DEFENCE_AREA);
            i.putExtra("group", group);
            i.putExtra("item", item);
            MainApplication.app.sendBroadcast(i);
        }


    }

    @Override
    public void vRetBindAlarmIdResult(int srcID, int result, int maxCount, String[] data) {
        Log.e(TAG, "vRetBindAlarmIdResult:" + result);
        if (result == 1) {
            Intent alarmId = new Intent();
            alarmId.setAction(Constants.P2P.RET_GET_BIND_ALARM_ID);
            alarmId.putExtra("data", data);
            alarmId.putExtra("max_count", maxCount);
            alarmId.putExtra("srcID", String.valueOf(srcID));
            MainApplication.app.sendBroadcast(alarmId);
        } else {
            Intent i = new Intent();
            i.putExtra("result", result);
            i.setAction(Constants.P2P.RET_SET_BIND_ALARM_ID);
            i.putExtra("max_count", maxCount);
            i.putExtra("srcID", String.valueOf(srcID));
            MainApplication.app.sendBroadcast(i);
        }


    }

    @Override
    public void vRetSetInitPasswordResult(int result) {


    }

    @Override
    public void vRetSetDevicePasswordResult(int result) {
        Log.e(TAG, "vRetSetDevicePasswordResult:" + result);
        Intent i = new Intent();
        i.setAction(Constants.P2P.RET_SET_DEVICE_PASSWORD);
        i.putExtra("result", result);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void vRetGetFriendStatus(int count, String[] contactIDs, int[] status, int[] types, boolean isFinish) {

        FList flist = FList.getInstance();
        for (int i = 0; i < count; i++) {
            flist.setState(contactIDs[i], status[i]);
            if (contactIDs[i].charAt(0) == '0') {
                flist.setType(contactIDs[i], P2PValue.DeviceType.PHONE);
            } else {
                if (status[i] == Constants.DeviceState.ONLINE) {
                    flist.setType(contactIDs[i], types[i]);
                }
            }
        }
        // TODO Auto-generated method stub
        FList.getInstance().sort();
        FList.getInstance().getDefenceState();
        Intent friends = new Intent();
        friends.setAction(Constants.Action.GET_FRIENDS_STATE);
        Bundle bundle = new Bundle();
        bundle.putStringArray("contactIDs", contactIDs);
        bundle.putIntArray("status", status);
        friends.putExtras(bundle);
        MainApplication.app.sendBroadcast(friends);
    }

    @Override
    public void vRetGetRecordFiles(String[] names, byte option0, byte option1) {
        for (String str : names) {
            Log.e("RecordFiles", "RecordFiles:" + str);
        }
        Intent i = new Intent();
        i.setAction(Constants.P2P.RET_GET_PLAYBACK_FILES);
        i.putExtra("recordList", names);
        MainApplication.app.sendBroadcast(i);
    }

    @Override
    public void vRetMessage(String contactId, String msg) {
        Intent i = new Intent();
        i.setAction(Constants.Action.RECEIVE_MSG);
        i.putExtra("msgFlag", contactId + "");
        i.putExtra("result", msg);
        MainApplication.app.sendBroadcast(i);
    }

    @Override
    public void vRetSysMessage(String msg) {
        SysMessage sysMessage = new SysMessage();
        sysMessage.activeUser = NpcCommon.mThreeNum;
        sysMessage.msg = msg;
        sysMessage.msg_time = String.valueOf(System.currentTimeMillis());
        sysMessage.msgState = SysMessage.MESSAGE_STATE_NO_READ;
        sysMessage.msgType = SysMessage.MESSAGE_TYPE_ADMIN;
        DataManager.insertSysMessage(MainApplication.app, sysMessage);
        Intent k = new Intent();
        k.setAction(Constants.Action.RECEIVE_SYS_MSG);
        MainApplication.app.sendBroadcast(k);
    }

    @Override
    public void vRetCustomCmd(int contactId, int len, byte[] cmd) {

        // TODO Auto-generated method stub

//		if (len < 11) {
//
//			return;
//		}
//		String id = String.valueOf(contactId);
//		String v_call = String.valueOf(cmd).substring(0, 11);
//
//		if (cmd.equals("anerfa:disconnect")) {
//			Intent i = new Intent();
//			i.setAction(Constants.P2P.RET_CUSTOM_CMD_DISCONNECT);
//			i.putExtra("contactId", contactId);
//			MainApplication.app.sendBroadcast(i);
//			return;
//		}
//		AlarmRecord alarmRecord = new AlarmRecord();
//		alarmRecord.alarmTime = String.valueOf(System.currentTimeMillis());
//		alarmRecord.deviceId = id;
//		alarmRecord.alarmType = 13;
//		alarmRecord.activeUser = NpcCommon.mThreeNum;
//		alarmRecord.group = -1;
//		alarmRecord.item = -1;
//		DataManager.insertAlarmRecord(P2PConnect.mContext, alarmRecord);
//		Intent i = new Intent();
//		i.setAction(Constants.Action.REFRESH_ALARM_RECORD);
//		P2PConnect.mContext.sendBroadcast(i);
//		long time = SharedPreferencesManager.getInstance().getIgnoreAlarmTime(
//				MainActivity.mContext);
//		int time_interval = SharedPreferencesManager.getInstance()
//				.getAlarmTimeInterval(MainActivity.mContext);
//		if ((System.currentTimeMillis() - time) < (1000 * time_interval)) {
//			return;
//		}
//		if (v_call.equals("anerfa:call")) {
//			if (MonitorDeviceID.equals("")) {// 没在监控
//				Log.i("dxsalarmmessage", "没在监控" + id + "MonitorDeviceID-->"
//						+ MonitorDeviceID);
//				if (!isAlarming) {
//					Intent it = new Intent();
//					it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//					it.setClass(P2PConnect.mContext, DoorBellNewActivity.class);
//					it.putExtra("contactId", id);
//					P2PConnect.mContext.startActivity(it);
//					Log.e("cus_cmd", "-----");
//				}
//			} else if (MonitorDeviceID.equals(id)) {// 正在监控此设备
//				Log.i("dxsalarmmessage", "正在监控此设备" + id + "MonitorDeviceID-->"
//						+ MonitorDeviceID);
//				// 不推送
//			} else {// 正在监控但不是此设备
//				// 监控页面弹窗
//				Log.i("dxsalarmmessage", "正在监控但不是此设备" + id
//						+ "MonitorDeviceID-->" + MonitorDeviceID);
//				Intent k = new Intent();
//				k.setAction(Constants.Action.MONITOR_NEWDEVICEALARMING);
//				k.putExtra("messagetype", 2);// 1是报警，2是透传门铃
//				k.putExtra("contactId", id);
//				MainApplication.app.sendBroadcast(k);
//			}
//		}

    }

    @Override
    public void vRetGetDeviceVersion(int result, String cur_version, int iUbootVersion, int iKernelVersion, int iRootfsVersion, int i4) {
        Log.e(TAG, "ACK_vRetGetDeviceVersion:" + result);
        Intent i = new Intent();
        i.putExtra("result", result);
        i.putExtra("cur_version", cur_version);
        i.putExtra("iUbootVersion", iUbootVersion);
        i.putExtra("iKernelVersion", iKernelVersion);
        i.putExtra("iRootfsVersion", iRootfsVersion);
        i.setAction(Constants.P2P.RET_GET_DEVICE_INFO);
        MainApplication.app.sendBroadcast(i);
    }

    @Override
    public void vRetCheckDeviceUpdate(String contactId, int result, String cur_version, String upg_version) {
        Log.e(TAG, "vRetCheckDeviceUpdate:" + result);
        Intent i = new Intent();
        i.putExtra("result", result);
        i.putExtra("cur_version", cur_version);
        i.putExtra("upg_version", upg_version);
        i.setAction(Constants.P2P.RET_CHECK_DEVICE_UPDATE);
        MainApplication.app.sendBroadcast(i);

    }

    @Override
    public void vRetDoDeviceUpdate(String contactId, int result, int value) {
        Log.e(TAG, "ACK_vRetDoDeviceUpdate:" + result);
        Intent i = new Intent();
        i.putExtra("result", result);
        i.setAction(Constants.P2P.RET_DO_DEVICE_UPDATE);
        MainApplication.app.sendBroadcast(i);

    }

    @Override
    public void vRetCancelDeviceUpdate(int result) {
        Log.e(TAG, "ACK_vRetCancelDeviceUpdate:" + result);
        Intent i = new Intent();
        i.putExtra("result", result);
        i.setAction(Constants.P2P.RET_CANCEL_DEVICE_UPDATE);
        MainApplication.app.sendBroadcast(i);

    }

    @Override
    public void vRetDeviceNotSupport(String deviceId) {
        Intent i = new Intent();
        i.setAction(Constants.P2P.RET_DEVICE_NOT_SUPPORT);
        i.putExtra("deviceId",deviceId);
        MainApplication.app.sendBroadcast(i);
    }

    @Override
    public void vRetClearDefenceAreaState(int result) {
        Log.e(TAG, "ACK_vRetClearDefenceAreaState:" + result);
        Intent i = new Intent();
        i.putExtra("result", result);
        i.setAction(Constants.P2P.ACK_RET_CLEAR_DEFENCE_AREA);
        MainApplication.app.sendBroadcast(i);

    }

    @Override
    public void vRetSetImageReverse(int result) {
        Intent i = new Intent();
        i.setAction(Constants.P2P.ACK_VRET_SET_IMAGEREVERSE);
        i.putExtra("result", result);
        MainApplication.app.sendBroadcast(i);

    }

    @Override
    public void vRetSetInfraredSwitch(int result) {
        Intent i = new Intent();
        i.setAction(Constants.P2P.ACK_RET_SET_INFRARED_SWITCH);
        i.putExtra("result", result);
        MainApplication.app.sendBroadcast(i);

    }

    @Override
    public void vRetSetWiredAlarmInput(int state) {
        Intent i = new Intent();
        i.setAction(Constants.P2P.ACK_RET_SET_WIRED_ALARM_INPUT);
        i.putExtra("state", state);
        MainApplication.app.sendBroadcast(i);

    }

    @Override
    public void vRetSetWiredAlarmOut(int state) {
        Intent i = new Intent();
        i.setAction(Constants.P2P.ACK_RET_SET_WIRED_ALARM_OUT);
        i.putExtra("state", state);
        MainApplication.app.sendBroadcast(i);

    }

    @Override
    public void vRetSetAutomaticUpgrade(int state) {
        Intent i = new Intent();
        i.setAction(Constants.P2P.ACK_RET_SET_AUTOMATIC_UPGRADE);
        i.putExtra("state", state);
        MainApplication.app.sendBroadcast(i);

    }

    @Override
    public void vRetSetVisitorDevicePassword(int result) {
        Log.i("dxssetting", "state-->" + result);
        Intent i = new Intent();
        i.setAction(Constants.P2P.ACK_RET_SET_VISITOR_DEVICE_PASSWORD);
        i.putExtra("state", result);
        MainApplication.app.sendBroadcast(i);

    }

    @Override
    public void vRetSetTimeZone(int result) {
        Intent i = new Intent();
        i.setAction(Constants.P2P.ACK_RET_SET_TIME_ZONE);
        i.putExtra("state", result);
        MainApplication.app.sendBroadcast(i);

    }

    /**
     * @param result1  SD卡内存
     * @param result2  内存剩余
     * @param SDcardID
     * @param state    是否有sd卡 0.无 1.有
     */
    @Override
    public void vRetGetSdCard(int result1, int result2, int SDcardID, int state) {
        Log.e("vRetGetSdCard", result1 + "---" + result2 + "---" + SDcardID + "---" + state);
        Intent i = new Intent();
        i.setAction(Constants.P2P.RET_GET_SD_CARD_CAPACITY);
        i.putExtra("total_capacity", result1);
        i.putExtra("remain_capacity", result2);
        i.putExtra("SDcardID", SDcardID);
        i.putExtra("state", state);
        MainApplication.app.sendBroadcast(i);

    }

    @Override
    public void VRetGetUsb(int result1, int result2, int SDcardID, int state) {
        Intent i = new Intent();
        i.setAction(Constants.P2P.RET_GET_USB_CAPACITY);
        i.putExtra("total_capacity", result1);
        i.putExtra("remain_capacity", result2);
        i.putExtra("SDcardID", SDcardID);
        i.putExtra("state", state);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void vRetSdFormat(int result) {
        Intent i = new Intent();
        i.setAction(Constants.P2P.RET_GET_SD_CARD_FORMAT);
        i.putExtra("result", result);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void vRetSetGPIO(int result) {


    }

    @Override
    public void vRetSetPreRecord(int result) {
        Intent i = new Intent();
        i.setAction(Constants.P2P.RET_SET_PRE_RECORD);
        i.putExtra("result", result);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void vRetSetSensorSwitchs(int result) {
        Intent i = new Intent();
        i.setAction(Constants.P2P.ACK_RET_SET_SENSOR_SWITCH);
        i.putExtra("result", result);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void vRecvSetLAMPStatus(String deviceId, int result) {
        Intent i = new Intent();
        i.setAction(Constants.P2P.SET_LAMP_STATUS);
        i.putExtra("result", result);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void vACK_RecvSetLAMPStatus(int result, int value) {
        Intent i = new Intent();
        i.setAction(Constants.P2P.ACK_SET_LAMP_STATUS);
        i.putExtra("result", result);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void vRecvGetLAMPStatus(String deviceId, int result) {
        Intent i = new Intent();
        i.setAction(Constants.P2P.GET_LAMP_STATUS);
        i.putExtra("result", result);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void vRetPresetMotorPos(byte[] result) {


    }

    @Override
    public void vRetHxstPresetMotorPos(byte[] result) {

    }

    @Override
    public void vRetDefenceSwitchStatus(int result) {


    }

    @Override
    public void vRetDefenceSwitchStatusResult(byte[] result) {


    }

    @Override
    public void vRetAlarmPresetMotorPos(byte[] result) {


    }

    @Override
    public void vRetIpConfig(byte[] result) {


    }

    @Override
    public void vRetGetAlarmCenter(int result, int state, String ipdress, int port, String userId) {


    }

    @Override
    public void vRetSetAlarmCenter(int result) {


    }

    @Override
    public void vRetDeviceNotSupportAlarmCenter() {


    }

    @Override
    public void vRetNPCVistorPwd(int pwd) {
        Intent i = new Intent();
        i.putExtra("visitorpwd", pwd);
        i.setAction(Constants.P2P.RET_GET_VISTOR_PASSWORD);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void vRetDeleteDeviceAlarmID(String deviceId, int result, int result1) {
        Intent i = new Intent();
        i.putExtra("deleteResult", result);
        i.putExtra("result1", result1);
        i.putExtra("deviceId", deviceId);
        i.setAction(Constants.P2P.DELETE_BINDALARM_ID);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void vRetDeviceLanguege(int result, int languegecount, int curlanguege, int[] langueges) {
        if (result == 1) {
            Intent i = new Intent();
            i.putExtra("languegecount", languegecount);
            i.putExtra("curlanguege", curlanguege);
            i.putExtra("langueges", langueges);
            i.setAction(Constants.P2P.RET_GET_LANGUEGE);
            MainApplication.app.sendBroadcast(i);
        } else {
            Intent i = new Intent();
            i.putExtra("result", result);
            i.setAction(Constants.P2P.RET_SET_LANGUEGE);
            MainApplication.app.sendBroadcast(i);
        }


    }

    @Override
    public void vRetFocusZoom(String deviceId, int result) {
        Log.e(TAG, "vRetFocusZoom:" + result);
        Log.e("vRetFocusZoom", "vRetFocusZoom:" + result);
        Intent i = new Intent();
        i.putExtra("result", result);
        i.setAction(Constants.P2P.RET_GET_FOCUS_ZOOM);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void vRetGetAllarmImage(int id, String filename, int errorCode) {
        Intent i = new Intent();
        i.putExtra("id", id);
        i.putExtra("filename", filename);
        i.putExtra("errorCode", errorCode);
        i.setAction(Constants.P2P.RET_GET_ALLARMIMAGE);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void vRetFishEyeData(int iSrcID, byte[] data, int datasize) {
        Intent i = new Intent();
        i.putExtra("iSrcID", iSrcID);
        i.putExtra("boption", data[2]);
        i.putExtra("data", data);
        Log.e("vRetFishEyeData", "iSrcID-->" + iSrcID + "--data-->" + Arrays.toString(data));
        switch (data[1]) {
            case 2:
                i.setAction(Constants.P2P.RET_SET_IPC_WORKMODE);
                break;
            case 4:
                i.setAction(Constants.P2P.RET_SET_SENSER_WORKMODE);
                break;
            case 6:
                i.setAction(Constants.P2P.RET_SET_SCHEDULE_WORKMODE);
                break;
            case 8:
                i.setAction(Constants.P2P.RET_DELETE_SCHEDULE);
                break;
            case 10:
                i.setAction(Constants.P2P.RET_GET_CURRENT_WORKMODE);
                break;
            case 12:
                i.setAction(Constants.P2P.RET_GET_SENSOR_WORKMODE);
                break;
            case 14:
                i.setAction(Constants.P2P.RET_GET_SCHEDULE_WORKMODE);
                break;
            case 16:
                i.setAction(Constants.P2P.RET_SET_ALLSENSER_SWITCH);
                break;
            case 18:
                i.setAction(Constants.P2P.RET_GET_ALLSENSER_SWITCH);
                break;
            case 20:
                i.setAction(Constants.P2P.RET_SET_LOWVOL_TIMEINTERVAL);//暂时不处理
                break;
            case 22:
                i.setAction(Constants.P2P.RET_GET_LOWVOL_TIMEINTERVAL);//暂时不处理
                break;
            case 24:
                i.setAction(Constants.P2P.RET_DELETE_ONE_CONTROLER);
                break;
            case 26:
                i.setAction(Constants.P2P.RET_DELETE_ONE_SENSOR);
                break;
            case 28:
                //修改遥控器名字返回,但不带修改后的名字
                i.setAction(Constants.P2P.RET_CHANGE_CONTROLER_NAME);
                break;
            case 30:
                //修改传感器名字返回,但不带修改后的名字
                i.setAction(Constants.P2P.RET_CHANGE_SENSOR_NAME);
                break;
            case 32:
                i.setAction(Constants.P2P.RET_INTO_LEARN_STATE);
                break;
            case 34:
                i.setAction(Constants.P2P.RET_TURN_SENSOR);
                break;
            case 36:
                //分享时管理员返回
                i.setAction(Constants.P2P.RET_SHARE_TO_MEMBER);
                break;
            case 37:
                //分享时用户收到的信息
                i.setAction(Constants.P2P.RET_GOT_SHARE);
                break;
            case 39:
                i.setAction(Constants.P2P.RET_DEV_RECV_MEMBER_FEEDBACK);
                break;
            case 41:
                i.setAction(Constants.P2P.RET_ADMIN_DELETE_ONE_MEMBER);
                break;
            case 43:
                i.setAction(Constants.P2P.RET_DELETE_DEV);
                break;
            case 45:
                i.setAction(Constants.P2P.RET_GET_MEMBER_LIST);
                break;
            case 47:
                i.setAction(Constants.P2P.RET_SET_ONE_SPECIAL_ALARM);
                break;
            case 49:
                i.setAction(Constants.P2P.RET_GET_ALL_SPECIAL_ALARM);
                break;
            case 51:
                i.setAction(Constants.P2P.RET_GET_LAMPSTATE);
                break;
            case 53:
                i.setAction(Constants.P2P.RET_KEEP_CLIENT);
                break;
        }
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void vRetGetNvrIpcList(String contactId, String[] date, int number) {
        Intent i = new Intent();
        i.setAction(Constants.P2P.ACK_GET_NVR_IPC_LIST);
        i.putExtra("state", number);
        MainApplication.app.sendBroadcast(i);

    }

    @Override
    public void vRetSetWifiMode(String id, int result) {
        Intent i = new Intent();
        i.putExtra("id", id);
        i.putExtra("result", result);
        i.setAction(Constants.P2P.RET_SET_AP_MODE);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void vRetAPModeSurpport(String id, int result) {
        Intent i = new Intent();
        i.putExtra("id", id);
        i.putExtra("result", result);
        i.setAction(Constants.P2P.RET_AP_MODESURPPORT);
        MainApplication.app.sendBroadcast(i);


    }

    @Override
    public void vRetDeviceType(String id, int mainType, int subType) {


    }

    @Override
    public void vRetNVRInfo(int iSrcID, byte[] data, int datasize) {


    }

    @Override
    public void vRetGetFocusZoom(String deviceId, int result, int value) {


    }

    @Override
    public void vRetSetFocusZoom(String deviceId, int result, int value) {


    }

    @Override
    public void vRetSetGPIO(String contactid, int result) {


    }

    @Override
    public void vRetGetGPIO(String contactid, int result, int bValueNs) {


    }

    @Override
    public void vRetGetDefenceWorkGroup(String contactid, byte[] data) {


    }

    @Override
    public void vRetSetDefenceWorkGroup(String contactid, byte[] data) {


    }

    @Override
    public void vRetFTPConfigInfo(String contactid, byte[] data) {


    }

    @Override
    public void vRetGPIOStatus(String contactid, byte[] Level) {


    }

    @Override
    public void vRecvSetGPIOStatus(String contactid, byte[] data) {


    }

    @Override
    public void vRetGetDefenceSwitch(int value) {


    }

    @Override
    public void vRetSetDefenceSwitch(int result) {


    }

    @Override
    public void vRetDefenceAreaName(String contactid, byte[] data) {
        Intent i = new Intent();
        i.setAction(Constants.P2P.RET_GET_DEFENCE_AREA_NAME);
        i.putExtra("contactid", contactid);
        i.putExtra("data", data);
        MainApplication.app.sendBroadcast(i);
    }

    @Override
    public void vRetGetPIRLightControl(int value) {


    }

    @Override
    public void vRetFishInfo(String contactid, byte[] data) {


    }

    @Override
    public void vRetGetAutoSnapshotSwitch(int value) {


    }

    @Override
    public void vRetSetAutoSnapshotSwitch(int result) {


    }

    @Override
    public void vRecvGetPrepointSurpporte(String deviceId, int result) {


    }

    @Override
    public void vRetGetMotionSensResult(int value) {


    }

    @Override
    public void vRetSetMotionSensResult(int iResult) {


    }

    @Override
    public void vRetVideoQuality(String contactId, byte[] data) {


    }

    /**
     * Index服务器返回设备信息（区别于P2P服务器返回数据，存在兼容标记）
     *
     * @param count          设备信息数量
     * @param contactIDs     设备ID
     * @param IdProtery      设备属性 &0x1==1（最低位为1）则支持Index服务器
     * @param status         设备在线状态 0:离线 1:在线
     * @param DevTypes       设备类型
     * @param SubType        设备子类型（需支持Index服务器）
     * @param DefenceState   设备布撤防状态（需支持Index服务器）
     * @param bRequestResult Index请求结果标记  非0时正常  为0时需要重新请求P2P服务器
     */
    @Override
    public void vRetGetIndexFriendStatus(int count, String[] contactIDs, int[] IdProtery, int[] status, int[] DevTypes, int[] SubType, int[] DefenceState, byte bRequestResult, long[] defenceFlag, int[][] ints5, int[][] ints6, int[] ints7, short[] shorts) {
        FList flist = FList.getInstance();
        for (int i = 0; i < count; i++) {
            flist.setState(contactIDs[i], status[i]);
            if (contactIDs[i].charAt(0) == '0') {
                flist.setType(contactIDs[i], P2PValue.DeviceType.PHONE);
            } else {
                if (status[i] == Constants.DeviceState.ONLINE) {
                    flist.setType(contactIDs[i], DevTypes[i]);
                }
            }
        }
        // TODO Auto-generated method stub
        FList.getInstance().sort();
        FList.getInstance().getDefenceState();
        Intent friends = new Intent();
        friends.setAction(Constants.Action.GET_FRIENDS_STATE);
        Bundle bundle = new Bundle();
        bundle.putStringArray("contactIDs", contactIDs);
        bundle.putIntArray("status", status);
        friends.putExtras(bundle);
        MainApplication.app.sendBroadcast(friends);
    }

//    @Override
//    public void vRetGetIndexFriendStatus(int count, String[] contactIDs, int[] IdProtery, int[] status, int[] DevTypes, int[] SubType, int[] DefenceState, byte bRequestResult, long[] defenceFlag) {
//    }

    @Override
    public void vRetGetIRLEDResult(int value) {


    }

    @Override
    public void vRetSetIRLEDResult(String contactId, int iResult) {


    }

    @Override
    public void vRetGetApIsWifiSetting(String contactId, byte[] data) {


    }

    @Override
    public void vRetSetApStaWifiInfo(String contactId, byte[] data) {


    }

    @Override
    public void vRetGetLEDResult(String contactId, int iResult) {


    }

    @Override
    public void vRetSetLEDResult(String contactId, int iResult) {


    }

    @Override
    public void vRetSetApStart(String contactId, int iResult) {

    }

    @Override
    public void vRetLockInfo(String contactId, byte[] data) {

    }

    @Override
    public void vRetGroupMessage(String groupName, int srcId, int ReciveTime, byte[] msg, int msgSize, int MesgType) {

    }

    @Override
    public void vRetGroupMessageAck(String groupName, int srcId, int Error) {

    }

    @Override
    public void vRetOfflineGroupMessage(String groupName, int srcId, int ReciveTime, byte[] msg, int msgSize, int MesgType) {

    }

    @Override
    public void vRetGroupMessageOver() {

    }

    @Override
    public void vRetLoginAnother(int LoginStatus) {

    }

    @Override
    public void vRetDefenceFrag(String deviceId, int frag) {

    }

    @Override
    public void ACK_vResult(String deviceId, int result) {

    }

    @Override
    public void vRetGetRTSPResult(String deviceId, int result) {

    }

    @Override
    public void vRetSetRTSPResult(String deviceId, int result) {

    }

    @Override
    public void vRetRTSPType_M3(String deviceId, int result) {

    }

    @Override
    public void vRetSetRTSPPWD(String deviceId, int result) {

    }

    @Override
    public void ACK_vRetSetUpdateId(String ipFour, int msgId, int result) {

    }

    @Override
    public void vRetGetLockState(String deviceId, int result) {

    }

    @Override
    public void vRetGetDeviceIPInfo(String contactId, byte[] data) {

    }

    @Override
    public void vRetRetGarageLightStatus(String s, byte[] bytes) {

    }

    @Override
    public void vRetSetWhiteLight(String deviceId, int iResult) {

    }

    @Override
    public void vRetSetWhiteLightSchedule(String deviceId, int iResult) {

    }

    @Override
    public void vRetGetWhiteLightState(String s, int i) {

    }

    @Override
    public void vRetWhiteLightScheduleTimeSetting(String s, byte[] data) {

    }

    @Override
    public void vRetGetWhiteLightSupport(String s, int i) {

    }

    @Override
    public void vRetSupport443DoorBell(String s, int i) {

    }

    @Override
    public void vRetNPCSettings(int iSrcID, int iCount, int[] iSettingID, int[] iValue) {

    }

    @Override
    public void vRetSystemMsgNotify(long msgId, int msgType, byte[] msg, int msgSize) {

    }

    @Override
    public void vRetGetSupportSetVisitorUnlock(String s, int i) {

    }

    @Override
    public void vRetGetObjectTracking(String s, int i) {

    }

    @Override
    public void vRetSetVisitorUnlock(String s, int i) {

    }

    @Override
    public void vRetSetObjectTracking(String s, int i) {

    }

    @Override
    public void ACK_vRetSetVisitorUnlock(String s, int i) {

    }

    @Override
    public void ACK_vRetGetGarageLight(String s, int i) {

    }

    @Override
    public void ACK_vRetSetGarageLight(String s, int i) {

    }

    @Override
    public void vRetAuthManageMsgNotify(long l, byte b, byte[] bytes, int i) {

    }

    @Override
    public void ACK_vRetSetObjectTracking(String s, int i) {

    }

    @Override
    public void vRetGetScheduleDefence(String s, int i) {

    }

    @Override
    public void vRetGetSupportAddSensor(String s, int i) {

    }

    @Override
    public void vRetGetAlarmInterval(String s, int i) {

    }

    @Override
    public void vRetSetScheduleDefence(String s, int i) {

    }

    @Override
    public void vRetSetAlarmInterval(String s, int i) {

    }

    @Override
    public void vRetGuardPlan(String s, byte[] bytes) {

    }

    @Override
    public void ACK_vRetSetGuardPlan(String s, int i) {

    }

    @Override
    public void vRetGuardSetting(String s, byte[] bytes) {

    }

    @Override
    public void vRetGetHaveRecordDays(String s, byte[] bytes) {

    }

    @Override
    public void ACK_vRetGuardSetting(String s, int i) {

    }

    @Override
    public void vRetFgP2PNotifyAppUpdate(int i, byte[] bytes, int i1) {

    }

    @Override
    public void ACK_vRetWarmLightSetting(String s, int i) {

    }

    @Override
    public void vRetWarmLightSetting(String s, int i) {

    }

    @Override
    public void vRetWhiteLightSchedule(String s, int i) {

    }

    @Override
    public void vRetGetNightColorSupport(String s, int i) {

    }

    @Override
    public void ACK_vRetSetNightColorSupport(String s, int i) {

    }

    @Override
    public void vRetGetDownloadVideoQuality(String s, int i) {

    }

    @Override
    public void ACK_vRetSetDownloadVideoQuality(String s, int i) {

    }

    @Override
    public void vRetGetBodyDetection(String s, int i) {

    }

    @Override
    public void ACK_vRetSetBodyDetection(String s, int i) {

    }

    @Override
    public void vRetRedBlueAlarmLightSetting(String s, int i) {

    }

    @Override
    public void vRetSpecialAlarmSoundSetting(String s, int i) {

    }

    @Override
    public void vRetWhiteAlarmLightSetting(String s, int i) {

    }

    @Override
    public void vRetSetRedBlueAlarmLight(String s, int i) {

    }

    @Override
    public void vRetSetSpecialAlarmSound(String s, int i) {

    }

    @Override
    public void vRetSetWhiteAlarmLight(String s, int i) {

    }

    @Override
    public void ACK_vRetSetSpecialAlarmSound(String s, int i) {

    }

    @Override
    public void ACK_vRetSetRedBlueAlarmLight(String s, int i) {

    }

    @Override
    public void ACK_vRetSetWhiteAlarmLight(String s, int i) {

    }


}
