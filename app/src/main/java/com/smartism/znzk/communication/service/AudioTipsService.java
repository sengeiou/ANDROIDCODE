package com.smartism.znzk.communication.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;

import com.smartism.znzk.R;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.CollectionsUtils;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;

import java.util.List;

/**
 * 播放震动和来电铃音的服务
 *
 * @author Administrator
 */
public class AudioTipsService extends Service {
    // 震动
    private Vibrator vibrator;
    // 播放声音
    private MediaPlayer mMediaPlayer;
    // 设置音量 暂时去掉。8.0以上有权限问题。这个修改声音大小也不是很友好。
//    private AudioManager mAudioManager;
//    private int currentAudio;
    private String devId;
    private ZhujiInfo zhujiInfo;
    private DeviceInfo deviceInfo;


    @Override
    public void onCreate() {
        super.onCreate();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        startVibrator();
    }

    @Override
    public IBinder onBind(Intent intent) {
        devId = intent.getStringExtra("devId");
        try {
            deviceInfo = DatabaseOperator.getInstance(AudioTipsService.this).queryDeviceInfo(Long.parseLong(devId));
            if (deviceInfo==null){
                zhujiInfo = DatabaseOperator.getInstance().queryDeviceZhuJiInfo(Long.parseLong(devId));
            }
        } catch (Exception e) {
            Log.e("AudioTip", "解析异常");
        }
        startMusic();
        return null;
    }

    @Override
    public void onDestroy() {
        vibrator.cancel();
        vibrator = null;
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer = null;
        }
//        if (Actions.VersionType.CHANNEL_JKD.equals(((MainApplication) getApplication()).getAppGlobalConfig().getVersion())) {
//            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentAudio,
//                    AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
//        } else {
//            if (!Actions.VersionType.CHANNEL_LILESI.equals(((MainApplication) getApplication()).getAppGlobalConfig().getVersion())) {
//                mAudioManager.setStreamVolume(AudioManager.STREAM_RING, currentAudio,
//                        AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
//            }
//        }
//        mAudioManager = null;
        super.onDestroy();
    }

    /**
     * 震动
     */
    private void startVibrator() {
        long[] pattern = {1000, 1000};// 停止 开启
        vibrator.vibrate(pattern, 0);// 重复两次上面的pattern
        // -1表示只振动一次，非-1表示从pattern的指定下标开始重复振动
    }

    /**
     * 播放音乐
     * JKD：使用ring但是聲音要開刀最大
     * LILESI：使用鈴聲，聲音和原來一樣
     */
    private void startMusic() {
        if (Actions.VersionType.CHANNEL_JKD.equals(((MainApplication) getApplication()).getAppGlobalConfig().getVersion())
                || Actions.VersionType.CHANNEL_HTZN.equals(((MainApplication) getApplication()).getAppGlobalConfig().getVersion())) {
            playRing();
        } else {
            mMediaPlayer = new MediaPlayer();
            try {
                //鈴聲的音量
//                int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
//                currentAudio = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
//                if (!Actions.VersionType.CHANNEL_LILESI.equals(((MainApplication) getApplication()).getAppGlobalConfig().getVersion())) {
//                    mAudioManager.setStreamVolume(AudioManager.STREAM_RING, max, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
//                }
                SharedPreferences sp = getApplicationContext().getSharedPreferences(Constant.SP_NAME, Context.MODE_MULTI_PROCESS);
                String default_path = sp.getString(Constant.PATH_ALARM_SONG, "");
                // 单个设备路径
                String ID = "" + devId;
                String devIdpath = sp.getString(ID + Constant.PATH_ALARM_SONG, "");
                String play_path = null;
                // 如果都没有设置
                if (!TextUtils.isEmpty(devIdpath)) {
                    play_path = devIdpath;
                } else if (!TextUtils.isEmpty(default_path)) {
                    play_path = default_path;
                }
            //    if (deviceInfo!=null&&(deviceInfo.getCa().equals(DeviceInfo.CaMenu.menling.value())|| CommandInfo.SpecialEnum.doorbell.value() == deviceInfo.getLastCommandSpecial())) {
                if (deviceInfo!=null&&(deviceInfo.getCa().equals(DeviceInfo.CaMenu.menling.value())||
                        CommandInfo.SpecialEnum.doorbell.value() ==DatabaseOperator.getInstance().queryLastCommand(deviceInfo.getId()).getSpecial() )) {
                    String uriString = "android.resource://" + getPackageName() + "/" + R.raw.doorbell;
                    Uri setDataSourceuri = Uri.parse(uriString);
                    mMediaPlayer.setDataSource(this, setDataSourceuri);
                }else if (zhujiInfo!=null) {
                    CommandInfo commandInfos= DatabaseOperator.getInstance().queryLastCommand(zhujiInfo.getId());
                    if((zhujiInfo.getCa().equals(DeviceInfo.CaMenu.menling.value())|| (commandInfos != null && CommandInfo.SpecialEnum.doorbell.value() == commandInfos.getSpecial())))
                    {
                        String uriString = "android.resource://" + getPackageName() + "/" + R.raw.doorbell;
                        Uri setDataSourceuri = Uri.parse(uriString);
                        mMediaPlayer.setDataSource(this, setDataSourceuri);
                    }else{
                        String uriString = "android.resource://" + getPackageName() + "/" + R.raw.ring;
                        Uri setDataSourceuri = Uri.parse(uriString);
                        mMediaPlayer.setDataSource(this, setDataSourceuri);
//                        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
//                        mMediaPlayer.setDataSource(this, alert);
                    }
                } else if (!TextUtils.isEmpty(play_path)) {
                    mMediaPlayer.setDataSource(play_path);
                } else {
                    String uriString = "android.resource://" + getPackageName() + "/" + R.raw.ring;
                    Uri setDataSourceuri = Uri.parse(uriString);
                    mMediaPlayer.setDataSource(this, setDataSourceuri);
//                    Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);默认声音
//                    mMediaPlayer.setDataSource(this, alert);
                }

                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
                mMediaPlayer.setLooping(true);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            } catch (Exception e) {
                Log.e("AudioTips", "播放声音错误", e);
                playRing();
            }
        }
    }

    /**
     * 播放 ring.war
     */
    private void playRing(){
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        mMediaPlayer = null;
        mMediaPlayer = MediaPlayer.create(this, R.raw.ring);//重新设置要播放的音频
        try {
            //音樂的音量
//            int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//            currentAudio = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, max, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            mMediaPlayer.setLooping(true);
            mMediaPlayer.start();
        } catch (Exception e) {
            Log.e("AudioTips", "播放声音错误", e);
        }
    }
}
