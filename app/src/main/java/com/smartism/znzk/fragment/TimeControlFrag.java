package com.smartism.znzk.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.p2p.core.P2PHandler;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.camera.MainControlActivity;
import com.smartism.znzk.adapter.camera.DateNumericAdapter;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.thread.DelayThread;
import com.smartism.znzk.util.camera.T;
import com.smartism.znzk.view.pickerview.TimePickerView;
import com.smartism.znzk.widget.wheel.OnWheelScrollListener;
import com.smartism.znzk.widget.wheel.WheelView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Administrator on 2017/3/11.
 */

public class TimeControlFrag extends BaseFragment implements View.OnClickListener {
    private Context mContext;
    private Contact contact;
    private boolean isRegFilter = false;
    private WheelView w_urban;
    private Button setting_sure;
    private TimePickerView timePickerView;

    RelativeLayout setting_device_time, setting_urban_title;
    TextView deviec_time;
//    ProgressBar progressBar;

    String cur_modify_time;
    int current_urban;
    Button bt_set_timezone;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        mContext = getActivity();
        contact = (Contact) getArguments().getSerializable("contact");
        View view = inflater.inflate(R.layout.fragment_time_control, container, false);
        initComponent(view);
        regFilter();
        P2PHandler.getInstance().getDeviceTime(contact.contactId, contact.contactPassword, MainApplication.GWELL_LOCALAREAIP);
        P2PHandler.getInstance().getNpcSettings(contact.contactId, contact.contactPassword, MainApplication.GWELL_LOCALAREAIP);
        return view;
    }

    public void initComponent(View view) {
        Calendar calendar = Calendar.getInstance();
        setting_device_time = (RelativeLayout) view.findViewById(R.id.setting_device_time);
        setting_device_time.setOnClickListener(this);
        setting_sure = (Button) view.findViewById(R.id.sure);
        setting_sure.setOnClickListener(this);
        deviec_time = (TextView) view.findViewById(R.id.deviec_time);
        deviec_time.setText(getTime(new Date()));
        //时区选择器初始化
        w_urban = (WheelView) view.findViewById(R.id.w_urban);
        w_urban.setViewAdapter(new DateNumericAdapter(mContext, -11, 12));
        w_urban.setCyclic(true);
        bt_set_timezone = (Button) view.findViewById(R.id.bt_set_timezone);
        bt_set_timezone.setOnClickListener(this);
        setting_urban_title = (RelativeLayout) view.findViewById(R.id.setting_urban_title);

        timePickerView = new TimePickerView(getActivity(), TimePickerView.Type.ALL);
        timePickerView.setTime(new Date());
        timePickerView.setCyclic(true);
        timePickerView.setCancelable(true);
        // 时间选择后回调
        timePickerView.setOnTimeSelectListener(new TimePickerView.OnTimeSelectListener() {

            @Override
            public void onTimeSelect(Date date) {
                deviec_time.setText(getTime(date));
//                sure.setEnabled(true);
            }
        });
    }
    private String getTime(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return format.format(date);
    }

    private boolean wheelScrolled = false;

    OnWheelScrollListener scrolledListener = new OnWheelScrollListener() {
        public void onScrollingStarted(WheelView wheel) {
            wheelScrolled = true;
        }

        public void onScrollingFinished(WheelView wheel) {
            wheelScrolled = false;
        }
    };


    public void regFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.P2P.ACK_RET_SET_TIME);
        filter.addAction(Constants.P2P.ACK_RET_GET_TIME);
        filter.addAction(Constants.P2P.RET_SET_TIME);
        filter.addAction(Constants.P2P.RET_GET_TIME);
        filter.addAction(Constants.P2P.RET_GET_TIME_ZONE);
        filter.addAction(Constants.P2P.ACK_RET_SET_TIME_ZONE);
        mContext.registerReceiver(mReceiver, filter);
        isRegFilter = true;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (intent.getAction().equals(Constants.P2P.RET_GET_TIME)) {
                String mtime = intent.getStringExtra("time");
                deviec_time.setText(mtime);
//                progressBar.setVisibility(RelativeLayout.GONE);
//                sure.setEnabled(true);
            } else if (intent.getAction().equals(Constants.P2P.RET_SET_TIME)) {
                int result = intent.getIntExtra("result", -1);
                if (result == Constants.P2P_SET.DEVICE_TIME_SET.SETTING_SUCCESS) {
//                    progressBar.setVisibility(RelativeLayout.GONE);
//                    sure.setEnabled(true);
                    T.showShort(mContext, getString(R.string.fragment_time_setsuccess));
                } else {
//                    progressBar.setVisibility(RelativeLayout.GONE);
//                    sure.setEnabled(true);
                    T.showShort(mContext, getString(R.string.fragment_time_setfailure));
                }
            } else if (intent.getAction().equals(Constants.P2P.ACK_RET_GET_TIME)) {
                int result = intent.getIntExtra("result", -1);
                if (result == Constants.P2P_SET.ACK_RESULT.ACK_PWD_ERROR) {

                    Intent i = new Intent();
                    i.setAction(Constants.Action.CONTROL_SETTING_PWD_ERROR);
                    mContext.sendBroadcast(i);

                } else if (result == Constants.P2P_SET.ACK_RESULT.ACK_NET_ERROR) {
                    P2PHandler.getInstance().getDeviceTime(contact.contactId, contact.contactPassword, MainApplication.GWELL_LOCALAREAIP);
                }
            } else if (intent.getAction().equals(Constants.P2P.ACK_RET_SET_TIME)) {

                int result = intent.getIntExtra("result", -1);
                if (result == Constants.P2P_SET.ACK_RESULT.ACK_PWD_ERROR) {
                    Intent i = new Intent();
                    i.setAction(Constants.Action.CONTROL_SETTING_PWD_ERROR);
                    mContext.sendBroadcast(i);
                } else if (result == Constants.P2P_SET.ACK_RESULT.ACK_NET_ERROR) {
                    Log.e("my", "net error resend:set npc time");
                    P2PHandler.getInstance().setDeviceTime(contact.contactId, contact.contactPassword, cur_modify_time, MainApplication.GWELL_LOCALAREAIP);
                }

            } else if (intent.getAction().equals(Constants.P2P.RET_GET_TIME_ZONE)) {
                int timezone = intent.getIntExtra("state", -1);
                if (timezone != -1) {
                    setting_urban_title.setVisibility(RelativeLayout.VISIBLE);
                }
                w_urban.setCurrentItem(timezone);
            } else if (intent.getAction().equals(Constants.P2P.ACK_RET_SET_TIME_ZONE)) {
                int state = intent.getIntExtra("state", -1);
                if (state == Constants.P2P_SET.ACK_RESULT.ACK_SUCCESS) {
                    T.showShort(mContext, getString(R.string.fragment_time_success));
                    P2PHandler.getInstance().getDeviceTime(contact.contactId, contact.contactPassword, MainApplication.GWELL_LOCALAREAIP);
                } else if (state == Constants.P2P_SET.ACK_RESULT.ACK_NET_ERROR) {
                    P2PHandler.getInstance().setTimeZone(contact.contactId, contact.contactPassword, current_urban, MainApplication.GWELL_LOCALAREAIP);
                }
            }
        }
    };

    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        switch (view.getId()) {
            case R.id.sure:
//                progressBar.setVisibility(RelativeLayout.VISIBLE);
//                sure.setEnabled(false);
                new DelayThread(Constants.SettingConfig.SETTING_CLICK_TIME_DELAY, new DelayThread.OnRunListener() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        cur_modify_time = deviec_time.getText().toString().trim();
                        P2PHandler.getInstance().setDeviceTime(contact.contactId, contact.contactPassword, cur_modify_time, MainApplication.GWELL_LOCALAREAIP);
                    }
                }).start();
                break;
            case R.id.bt_set_timezone:
                current_urban = w_urban.getCurrentItem();
                P2PHandler.getInstance().setTimeZone(contact.contactId, contact.contactPassword, current_urban, MainApplication.GWELL_LOCALAREAIP);
                break;
            case R.id.setting_device_time:
                timePickerView.show();
                break;
        }
    }

    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        super.onDestroyView();
        if (isRegFilter) {
            mContext.unregisterReceiver(mReceiver);
            isRegFilter = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent it = new Intent();
        it.setAction(Constants.Action.CONTROL_BACK);
        mContext.sendBroadcast(it);
    }

}

