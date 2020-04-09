package com.smartism.znzk.activity.camera;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.p2p.core.BasePlayBackActivity;
import com.p2p.core.P2PHandler;
import com.p2p.core.P2PView;
import com.smartism.znzk.R;
import com.smartism.znzk.domain.camera.RecordFile;
import com.smartism.znzk.global.AppConfig;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.global.NpcCommon;
import com.smartism.znzk.widget.HeaderView;

public class CameraPlayBackActivity extends BasePlayBackActivity implements View.OnClickListener {

    Button btnPalyback;
    TextView txText;
    RelativeLayout rlP2pview;
    int window_width, window_height;
    private RecordFile recordFile;
    private String deviceId;
    private String devicePwd;
    private Button back_btn;
    private HeaderView hanHeaderView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager manager = getWindowManager();
        window_width = manager.getDefaultDisplay().getWidth();
        window_height = manager.getDefaultDisplay().getHeight();

        Bundle bundle = getIntent().getBundleExtra("recordFile");
        recordFile = (RecordFile) bundle.getSerializable("file");
        deviceId = getIntent().getStringExtra("deviceId");
        devicePwd = getIntent().getStringExtra("devicePwd");

        setContentView(R.layout.activity_play_back);
        btnPalyback = (Button) findViewById(R.id.btn_palyback);
        rlP2pview = (RelativeLayout) findViewById(R.id.rl_p2pview);
        hanHeaderView = (HeaderView) findViewById(R.id.header_img);
        back_btn = (Button) findViewById(R.id.back_btn);
        back_btn.setOnClickListener(this);
        pView = (P2PView) findViewById(R.id.pview);
//        if (!getIntent().getBooleanExtra("finish", false)) {
            regFilter();
        btnPalyback.setOnClickListener(this);
        hanHeaderView.updateImage(deviceId, true, 1);
        this.initScaleView(this, window_width, window_height);
        initp2pView();
//        }
    }

    public void initp2pView() {

        //7是设备类型(技威定义的)
        //LAYOUTTYPE_TOGGEDER 录像回放连接命令和P2P_ACCEPT、P2P_READY、P2P_REJECT等命令在同一界面
        //LAYOUTTYPE_SEPARATION 录像回放连接命令和P2P_ACCEPT、P2P_READY、P2P_REJECT等命令不在同一界面
        this.initP2PView(7, P2PView.LAYOUTTYPE_TOGGEDER);
        pView.halfScreen();
    }

    public void initScaleView(Activity activity, int windowWidth,
                              int windowHeight) {
        pView.setmActivity(activity);
        pView.setScreen_W(windowHeight);
        pView.setScreen_H(windowWidth);
        pView.initScaleView();
    }

    @Override
    protected void onCaptureScreenResult(boolean isSuccess, int prePoint) {

    }

    @Override
    protected void onVideoPTS(long videoPTS) {

    }

    @Override
    protected void onP2PViewSingleTap() {

    }

    boolean isRegister;
    public void regFilter() {
        if (!isRegister){
            isRegister = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction(Constants.P2P.P2P_REJECT);
            filter.addAction(Constants.P2P.P2P_ACCEPT);
            filter.addAction(Constants.P2P.P2P_READY);
            registerReceiver(mReceiver, filter);
        }
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.P2P.P2P_ACCEPT)) {
                int[] type = intent.getIntArrayExtra("type");
                P2PView.type = type[0];
                P2PView.scale = type[1];
                Log.e("dxsTest", "监控数据接收:" + deviceId);
                P2PHandler.getInstance().openAudioAndStartPlaying(2);//打开音频并准备播放，calllType与call时type一致
            } else if (intent.getAction().equals(Constants.P2P.P2P_READY)) {
                Log.e("dxsTest", "监控准备,开始监控" + deviceId);
                pView.sendStartBrod();
//                hander.sendEmptyMessageDelayed(0,3*1000);
                btnPalyback.setVisibility(View.GONE);
            } else if (intent.getAction().equals(Constants.P2P.P2P_REJECT)) {
                hanHeaderView.setVisibility(View.VISIBLE);
                btnPalyback.setVisibility(View.VISIBLE);
//                txText.append("\n 监控挂断");
            } else if (intent.getAction().equals(Constants.P2P.RET_P2PDISPLAY)) {
                Log.e("monitor", "RET_P2PDISPLAY");
                hanHeaderView.setVisibility(View.GONE);
            }
        }
    };

    public void play() {

        String filename = recordFile.getName();
        //录像回放连接
        P2PHandler.getInstance().playbackConnect(deviceId,deviceId,
                devicePwd, filename, recordFile.getPosition(), 0, 0, 896, 896, 0,0,0);
    }

    @Override
    public int getActivityInfo() {
        return 33;
    }

    @Override
    protected void onGoBack() {

    }

    @Override
    protected void onGoFront() {

    }

    @Override
    protected void onExit() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isRegister){
             unregisterReceiver(mReceiver);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.back_btn) {
            finish();
        } else if (v.getId() == R.id.btn_palyback) {
            play();
        }

    }
}
