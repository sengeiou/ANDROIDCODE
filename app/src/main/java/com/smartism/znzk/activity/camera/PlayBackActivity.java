package com.smartism.znzk.activity.camera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.p2p.core.BasePlayBackActivity;
import com.p2p.core.P2PHandler;
import com.p2p.core.P2PView;
import com.smartism.znzk.R;
import com.smartism.znzk.camera.P2PConnect;
import com.smartism.znzk.camera.utils.PhoneWatcher;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.util.camera.T;

import java.util.ArrayList;


public class PlayBackActivity extends BasePlayBackActivity implements OnClickListener, OnTouchListener, OnSeekBarChangeListener {
    private int mCurrentVolume, mMaxVolume;
    private AudioManager mAudioManager = null;
    private RelativeLayout control_bottom, title;
    private boolean isControlShow = true;
    private boolean mIsCloseVoice = false;
    private ImageView stopVoice, previous, pause, next;
    private Context mContext;
    private SeekBar seekbar;
    boolean isPause = false;
    boolean isRegFilter = false;
    private TextView nowTime, totalTime;

    boolean isScroll = false;
    boolean isReject = false;
    private PhoneWatcher mPhoneWatcher;
    private ArrayList<String> list;
    private String fileName;
    private int position;
    private ImageView back;
    private TextView name;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.p2p_playback);
        fileName = getIntent().getStringExtra("fileName");
        list = getIntent().getStringArrayListExtra("list");
        position = getIntent().getIntExtra("position", 0);
        mContext = this;
        initComponent();
        regFilter();
        startWatcher();
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        }
        mCurrentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

    }


    @Override
    public void onHomePressed() {
        // TODO Auto-generated method stub
        super.onHomePressed();
        reject();
    }


    private void startWatcher() {
        mPhoneWatcher = new PhoneWatcher(mContext);
        mPhoneWatcher.setOnCommingCallListener(new PhoneWatcher.OnCommingCallListener() {

            @Override
            public void onCommingCall() {
                // TODO Auto-generated method stub
                reject();
            }

        });
        mPhoneWatcher.startWatcher();
    }



    private void initComponent() {
        title = (RelativeLayout) findViewById(R.id.title);
        back = (ImageView) findViewById(R.id.back);
        name = (TextView) findViewById(R.id.name);
        pView = (P2PView) findViewById(R.id.pView);
        this.initP2PView(7, P2PView.LAYOUTTYPE_SEPARATION);
        control_bottom = (RelativeLayout) findViewById(R.id.control_bottom);
        stopVoice = (ImageView) findViewById(R.id.close_voice);
        previous = (ImageView) findViewById(R.id.previous);
        pause = (ImageView) findViewById(R.id.pause);
        next = (ImageView) findViewById(R.id.next);
        seekbar = (SeekBar) findViewById(R.id.seek_bar);
        nowTime = (TextView) findViewById(R.id.nowTime);
        totalTime = (TextView) findViewById(R.id.totalTime);

        stopVoice.setOnClickListener(this);
        control_bottom.setOnTouchListener(this);
        previous.setOnClickListener(this);
        pause.setOnClickListener(this);
        next.setOnClickListener(this);
        seekbar.setOnSeekBarChangeListener(this);
        back.setOnClickListener(this);
        name.setText(fileName);
    }

    private void regFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.P2P.P2P_REJECT);
        filter.addAction(Constants.P2P.PLAYBACK_CHANGE_SEEK);
        filter.addAction(Constants.P2P.PLAYBACK_CHANGE_STATE);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mReceiver, filter);
        isRegFilter = true;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.P2P.P2P_REJECT)) {
                reject();
            } else if (intent.getAction().equals(Constants.P2P.PLAYBACK_CHANGE_SEEK)) {
                if (!isScroll) {
                    int max = intent.getIntExtra("max", 0);
                    Log.e("max_time", "max_time=" + max);
                    int current = intent.getIntExtra("current", 0);
                    seekbar.setMax(max);
                    seekbar.setProgress(current);
                    nowTime.setText(convertTime(current));
                    totalTime.setText(convertTime(max));
                }
            } else if (intent.getAction().equals(Constants.P2P.PLAYBACK_CHANGE_STATE)) {
                int state = intent.getIntExtra("state", 0);
                switch (state) {
                    case 0:
                        isPause = true;
                        pause.setImageResource(R.drawable.playing_start);
                        break;
                    case 1:
                        isPause = true;
                        pause.setImageResource(R.drawable.playing_start);
                        break;
                    case 2:
                        isPause = false;
                        pause.setImageResource(R.drawable.playing_pause);
                        break;
                }
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                reject();
            }

        }
    };

    public void reject() {
        if (!isReject) {
            isReject = true;
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        reject();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
            mCurrentVolume++;
            if (mCurrentVolume > mMaxVolume) {
                mCurrentVolume = mMaxVolume;
            }

            if (mCurrentVolume != 0) {
                mIsCloseVoice = false;
                stopVoice.setImageResource(R.drawable.btn_playback_voice);
            }
            return false;
        } else if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
            mCurrentVolume--;
            if (mCurrentVolume < 0) {
                mCurrentVolume = 0;
            }

            if (mCurrentVolume == 0) {
                mIsCloseVoice = true;
                stopVoice.setImageResource(R.drawable.btn_playback_voice_s);
            }

            return false;
        }


        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onDestroy() {
        if (mAudioManager != null) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mCurrentVolume, 0);
        }
        if (isRegFilter) {
            mContext.unregisterReceiver(mReceiver);
            isRegFilter = false;
        }
        if (null != mPhoneWatcher) {
            mPhoneWatcher.stopWatcher();
        }
        P2PHandler.getInstance().finish();
        super.onDestroy();
    }

    @Override
    protected void onCaptureScreenResult(boolean isSuccess, int prePoint) {

    }

    @Override
    protected void onVideoPTS(long videoPTS) {

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.back:
                reject();
                break;
            case R.id.close_voice:
                if (mIsCloseVoice) {
                    mIsCloseVoice = false;
                    stopVoice.setImageResource(R.drawable.btn_playback_voice);
                    if (mCurrentVolume == 0) {
                        mCurrentVolume = 1;
                    }
                    if (mAudioManager != null) {
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mCurrentVolume, 0);
                    }
                } else {
                    mIsCloseVoice = true;
                    stopVoice.setImageResource(R.drawable.btn_playback_voice_s);
                    if (mAudioManager != null) {
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                    }
                }
                break;
            case R.id.pause:
                if (isPause) {
                    this.startPlayBack();
                } else {
                    this.pausePlayBack();
                }
                break;
            case R.id.previous:
                position = position - 1;
                if (list != null && !list.isEmpty()) {
                    if (position < 0) {
                        position = position + 1;
                        T.showShort(mContext, R.string.no_previous_file);
                    } else {
                        if (!isPause) {
                            this.pausePlayBack();
                        }
                        if (!this.previous(fileName,0)) {
                            T.showShort(mContext, R.string.no_previous_file);
                        }
                        fileName = list.get(position);
                    }
                } else {
                    T.showShort(mContext, R.string.no_previous_file);
                }
                break;
            case R.id.next:
                position = position + 1;
                if (list != null && !list.isEmpty()) {
                    if (position >= list.size()) {
                        position = position - 1;
                        T.showShort(mContext, R.string.no_next_file);
                    } else {
                        if (!isPause) {
                            this.pausePlayBack();
                        }
                        fileName  = list.get(position);
                        if (!this.next(fileName,0)) {
                            T.showShort(mContext, R.string.no_next_file);
                        }
                    }
                } else {
                    T.showShort(mContext, R.string.no_next_file);
                }

                break;
        }

        name.setText("name:" + fileName);
    }


    public void changeControl() {
        if (isControlShow) {
            isControlShow = false;
            Animation anim2 = AnimationUtils.loadAnimation(this, R.anim.slide_out_top);
            anim2.setDuration(300);
            control_bottom.startAnimation(anim2);
            control_bottom.setVisibility(RelativeLayout.GONE);

            title.setVisibility(RelativeLayout.GONE);
        } else {
            isControlShow = true;
            control_bottom.setVisibility(RelativeLayout.VISIBLE);
            Animation anim2 = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);
            anim2.setDuration(300);
            control_bottom.startAnimation(anim2);

            title.setVisibility(RelativeLayout.VISIBLE);
        }
    }

    @Override
    public boolean onTouch(View arg0, MotionEvent event) {
        // TODO Auto-generated method stub
        switch (arg0.getId()) {
            case R.id.control_bottom:
                return true;
        }
        return false;
    }

    @Override
    public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
        // TODO Auto-generated method stub

        Log.e("playback", "onProgressChanged arg1:" + arg1 + " arg2:" + arg2);
        nowTime.setText(convertTime(arg1));
    }

    @Override
    public void onStartTrackingTouch(SeekBar arg0) {
        // TODO Auto-generated method stub
        isScroll = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar arg0) {
        // TODO Auto-generated method stub
        this.jump(arg0.getProgress());
        isScroll = false;
    }

    public String convertTime(int time) {
        int hour = time / (60 * 60);
        int minute = time / (60) - hour * 60;
        int second = time - hour * 60 * 60 - minute * 60;

        String hour_str = hour + "";
        String minute_str = minute + "";
        String second_str = second + "";
        if (minute < 10) {
            minute_str = "0" + minute;
        }
        if (second < 10) {
            second_str = "0" + second;
        }

        return hour_str + ":" + minute_str + ":" + second_str;
    }


    @Override
    public int getActivityInfo() {
        // TODO Auto-generated method stub
        return Constants.ActivityInfo.ACTIVITY_PLAYBACKACTIVITY;
    }


    @Override
    protected void onP2PViewSingleTap() {
        // TODO Auto-generated method stub
        changeControl();
    }


    @Override
    protected void onGoBack() {
        // TODO Auto-generated method stub
    }


    @Override
    protected void onGoFront() {
        // TODO Auto-generated method stub
    }


    @Override
    protected void onExit() {
        // TODO Auto-generated method stub
    }

    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
//	            Toast.makeText(getApplicationContext(),R.string.Press_again_exit, Toast.LENGTH_SHORT).show();        
                T.showShort(mContext, R.string.Press_again_exit);
                exitTime = System.currentTimeMillis();
            } else {
                reject();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
