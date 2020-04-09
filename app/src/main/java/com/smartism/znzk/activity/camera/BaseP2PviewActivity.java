package com.smartism.znzk.activity.camera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.p2p.core.BaseCoreActivity;
import com.p2p.core.GestureDetector;
import com.p2p.core.MediaPlayer;
import com.p2p.core.P2PHandler;
import com.p2p.core.P2PView;
import com.p2p.core.global.P2PConstants;
//import com.p2p.core.pano.PanoManager;
import com.p2p.core.utils.MyUtils;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.util.StringUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;


/**
 * Created by USER on 2016/12/15.
 */

public abstract class BaseP2PviewActivity extends BaseCoreActivity implements MediaPlayer.ICapture, MediaPlayer.IVideoPTS {
    public P2PView pView;
    public static int mVideoFrameRate = 15;  //视频帧速度
    private boolean isBaseRegFilter = false;
    private int PrePoint = -1;
    public boolean bFlagPanorama = false;
    private Disposable disposable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!StringUtils.isEmpty(MainApplication.app.getAppGlobalConfig().getAPPID())) {
            baseRegFilter();
            MediaPlayer.getInstance().setCaptureListener(this);
            MediaPlayer.getInstance().setVideoPTSListener(this);
            String mac = MyUtils.getLocalMacAddress(this);
            String imei = MyUtils.getIMEI(this); //IMEI:国际移动设备识别码的缩写
            MediaPlayer.native_init_hardMessage(mac, imei);
        }
    }

    public void baseRegFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(P2PConstants.P2P_WINDOW.Action.P2P_WINDOW_READY_TO_START);
        this.registerReceiver(baseReceiver, filter);
        isBaseRegFilter = true;
    }

    /**
     * 初始化P2PView
     *
     * @param type       宽高类型
     * @param layoutType 布局类型默认分离使用（call与P2Pview分开）
     */

    public void initP2PView(int type, int layoutType, GestureDetector.SimpleOnGestureListener listener) {
        if (pView != null) {
            pView.setLayoutType(layoutType);
        }
        pView.setCallBack();
        pView.setGestureDetector(new GestureDetector(this, listener, null, true));
        pView.setDeviceType(type);
    }

    private BroadcastReceiver baseReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (intent.getAction().equals(P2PConstants.P2P_WINDOW.Action.P2P_WINDOW_READY_TO_START)) {
                final MediaPlayer mPlayer = MediaPlayer.getInstance();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MediaPlayer.nativeInit(mPlayer);
                        try {
                            mPlayer.setDisplay(pView);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mPlayer.start(mVideoFrameRate);
                    }
                }).start();
            }
        }
    };

    @Override
    public void vCaptureResult(final int result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (result == 1) {
                    onCaptureScreenResult(true, PrePoint);
                } else {
                    onCaptureScreenResult(false, PrePoint);
                }
            }
        });
    }

    /**
     * 开始获取码率,建议在视频开始渲染的时候调用
     */
    public void StartGetBitRate() {
        //TODO 这里会不会有多次订阅的影响，取消订阅只取消了最后一个
        Disposable disposable = Observable.interval(1, TimeUnit.SECONDS)
                .map(new Function<Long, Object>() {
                    @Override
                    public Object apply(Long aLong) throws Exception {
                        return P2PHandler.getInstance().getAvBytesPerSec();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object integer) throws Exception {
                        onAvBytesPerSec((Integer) integer);
                    }
                });
    }

    @Override
    public void onDestroy() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        super.onDestroy();
        if (isBaseRegFilter) {
            this.unregisterReceiver(baseReceiver);
            isBaseRegFilter = false;
        }
    }

    /**
     * -1是普通截图，0~4是预置位截图
     *
     * @param prePoint
     */
    public void captureScreen(int prePoint) {
        this.PrePoint = prePoint;
        onPreCapture(1, prePoint);
        try {
            MediaPlayer.getInstance()._CaptureScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置是否全景
     *
     * @param subType
     */
    public void setPanorama(int subType) {
        boolean isparam = false;
        if (pView != null) {
            pView.setPanorama(subType);
            isparam = pView.isPanorama();
        }
        try {
            MediaPlayer.getInstance()._setPanorama(isparam);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void vVideoPTS(long videoPTS) {
        // TODO: 2017/9/6 底层已经有全景判断，这里不再限制
        //PTS（Presentation Time Stamp）：即显示时间戳，这个时间戳用来告诉播放器该在什么时候显示这一帧的数据。
        onVideoPTS(videoPTS);
    }

    @Override
    public void vSendRendNotify(int MsgType, int MsgAction) {
        if (pView != null && pView.isPanorama()) {
            pView.FilpAction(MsgType, MsgAction);
        }

    }

    protected abstract void onCaptureScreenResult(boolean isSuccess, int prePoint);

    protected abstract void onVideoPTS(long videoPTS);

    /**
     * 码率回调接口
     *
     * @param videoPTS byte/s
     */
    protected abstract void onAvBytesPerSec(int videoPTS);

    public void onPreCapture(int mark, int prepoint) {

    }

    @Override
    protected int onPreFinshByLoginAnother() {
        //被踢飞之后关闭Activity之前的回调
        return 0;
    }

    //通过旋转角度获取方向
    public int getOrientationByRotation(int rotation) {
        int orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        if (((rotation >= 0) && (rotation <= 45)) || (rotation > 315)) {
            orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        } else if ((rotation > 45) && (rotation <= 135)) {
            orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
        } else if ((rotation > 135) && (rotation <= 225)) {
            orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
        } else if ((rotation > 225) && (rotation <= 315)) {
            orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } else {
            orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
        return orientation;
    }

    /**
     * 开始播放线程，由于新全景不再使用FFmpeg解码，所以在Call时已经将取流类型置为Stream
     * 调用此方法将不会开启视频渲染线程，仅开启录音线程
     * 普通设备监控禁止调用此方法
     */
    public void starPlay(){
        MediaPlayer.getInstance().start(mVideoFrameRate);
    }

    /**
     * Unity全景截图
     * @param prePoint 截图标记
     * @param path 截图路径与名称
     */
    public void captureScreenUnity(int prePoint,String path){
        this.PrePoint=prePoint;
//        PanoManager.getInstance().saveTexture(path);
    }
}