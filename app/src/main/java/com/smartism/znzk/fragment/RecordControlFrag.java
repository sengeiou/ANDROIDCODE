package com.smartism.znzk.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.p2p.core.P2PHandler;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.camera.MainControlActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.util.camera.T;
import com.smartism.znzk.view.pickerview.TimePickerView;
import com.smartism.znzk.widget.NormalDialog;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RecordControlFrag extends BaseFragment implements OnClickListener, OnTouchListener {
    private Context mContext;
    private Contact contact;
    private boolean isRegFilter = false;
    private TimePickerView timePickerView;
    private TextView bt_set_time;
    private boolean isFrome;//
    RelativeLayout change_record_type, change_record_time;
    LinearLayout record_type_radio, record_time_radio, time_picker, record_type_item;
    ProgressBar progressBar_record_type, progressBar_record_time;
    RadioButton radio_one, radio_two, radio_three;
    RadioButton radio_one_time, radio_two_time, radio_three_time;
    String cur_modify_plan_time;
    int cur_modify_record_type;
    int cur_modify_record_time;

    RelativeLayout change_record, change_pre_record;
    ProgressBar progressBar_record, progressBar_pre_record;
    ImageView record_img, pre_record_img;
    TextView record_text, pre_record_text, time_from, time_to;
    ScrollView scroll_view;
    int recordState;
    int last_record;
    int last_modify_record;
    boolean isOpenPreRecord = false;
    int last_pre_record;
    int last_modify_pre_record;
    boolean isSupportPreRecored = false;
    int type;
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            P2PHandler.getInstance().getSdCardCapacity(idOrIp, contact.contactPassword, MainApplication.GWELL_LOCALAREAIP);
            return false;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

    }

    private String command;
    private String idOrIp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        mContext = getActivity();
        contact = (Contact) getArguments().getSerializable("contact");
        idOrIp = contact.contactId;
        if (contact.ipadressAddress != null) {
            String mark = contact.ipadressAddress.getHostAddress();
            String ip = mark.substring(mark.lastIndexOf(".") + 1, mark.length());
            if (!ip.equals("") && ip != null) {
                idOrIp = ip;
            }
        }
        View view = inflater.inflate(R.layout.fragment_record_control, container, false);
        initComponent(view);
        regFilter();

        showProgress_record_type();
        P2PHandler.getInstance().getNpcSettings(contact.contactId, contact.contactPassword, MainApplication.GWELL_LOCALAREAIP);
        command = createCommand("80", "0", "00");
        P2PHandler.getInstance().getSdCardCapacity(idOrIp, contact.contactPassword, MainApplication.GWELL_LOCALAREAIP);
        return view;
    }

    public String createCommand(String bCommandType, String bOption,
                                String SDCardCounts) {
        return bCommandType + bOption + SDCardCounts;
    }

    public void initComponent(View view) {
        bt_set_time = (TextView) view.findViewById(R.id.bt_set_time);
        time_from = (TextView) view.findViewById(R.id.time_from);
        time_to = (TextView) view.findViewById(R.id.time_to);
        record_type_item = (LinearLayout) view.findViewById(R.id.record_type_item);
        //SD卡
        persen = (TextView) view.findViewById(R.id.sd_persen);
        tv_total_capacity = (TextView) view.findViewById(R.id.total_capacity);
        tv_sd_remainning_capacity = (TextView) view.findViewById(R.id.remainning_capacity);
        tv_usb_total_capacity = (TextView) view.findViewById(R.id.tv_usb_capacity);
        progressBar = (ProgressBar) view.findViewById(R.id.progressbar_sd);
        sd_format = (RelativeLayout) view.findViewById(R.id.sd_format);
        tv_usb_remainning_capacity = (TextView) view.findViewById(R.id.tv_usb_remainning_capacity);
        l_sd_card = (LinearLayout) view.findViewById(R.id.l_sd_card);
        l_usb = (LinearLayout) view.findViewById(R.id.l_usb);
        format_icon = (ImageView) view.findViewById(R.id.format_icon);
        progress_format = (ProgressBar) view.findViewById(R.id.progress_format);
        sd_format.setOnClickListener(this);

        scroll_view = (ScrollView) view.findViewById(R.id.scroll_view);
        change_record_type = (RelativeLayout) view.findViewById(R.id.change_record_type);
        record_type_radio = (LinearLayout) view.findViewById(R.id.record_type_radio);
        progressBar_record_type = (ProgressBar) view.findViewById(R.id.progressBar_record_type);

        radio_one = (RadioButton) view.findViewById(R.id.radio_one);
        radio_two = (RadioButton) view.findViewById(R.id.radio_two);
        radio_three = (RadioButton) view.findViewById(R.id.radio_three);

        change_record_time = (RelativeLayout) view.findViewById(R.id.change_record_time);
        record_time_radio = (LinearLayout) view.findViewById(R.id.record_time_radio);
        progressBar_record_time = (ProgressBar) view.findViewById(R.id.progressBar_record_time);

        radio_one_time = (RadioButton) view.findViewById(R.id.radio_one_time);
        radio_two_time = (RadioButton) view.findViewById(R.id.radio_two_time);
        radio_three_time = (RadioButton) view.findViewById(R.id.radio_three_time);


        time_picker = (LinearLayout) view.findViewById(R.id.time_picker);
        initTimePicker(view);
        bt_set_time.setOnClickListener(this);
        time_from.setOnClickListener(this);
        time_to.setOnClickListener(this);

        radio_one.setOnClickListener(this);
        radio_two.setOnClickListener(this);
        radio_three.setOnClickListener(this);

        radio_one_time.setOnClickListener(this);
        radio_two_time.setOnClickListener(this);
        radio_three_time.setOnClickListener(this);


        change_record = (RelativeLayout) view.findViewById(R.id.change_record);
        record_img = (ImageView) view.findViewById(R.id.record_img);
        record_text = (TextView) view.findViewById(R.id.record_text);
        progressBar_record = (ProgressBar) view.findViewById(R.id.progressBar_record);

        change_pre_record = (RelativeLayout) view.findViewById(R.id.change_pre_record);
        pre_record_text = (TextView) view.findViewById(R.id.pre_record_text);
        pre_record_img = (ImageView) view.findViewById(R.id.pre_record_img);
        progressBar_pre_record = (ProgressBar) view.findViewById(R.id.progressBar_pre_record);
        change_record.setOnClickListener(this);
        change_pre_record.setOnClickListener(this);
        change_pre_record.setClickable(false);

        timePickerView = new TimePickerView(getActivity(), TimePickerView.Type.HOURS_MINS);
        timePickerView.setTime(new Date());
        timePickerView.setCyclic(true);
        timePickerView.setCancelable(true);
        // 时间选择后回调
        timePickerView.setOnTimeSelectListener(new TimePickerView.OnTimeSelectListener() {

            @Override
            public void onTimeSelect(Date date) {
                if (isFrome){
                    time_from.setText(getTime(date));
                }else {
                    time_to.setText(getTime(date));
                }
            }
        });
    }

    private String getTime(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(date);
    }

    public void initTimePicker(View view) {
    }


    TextView tv_total_capacity, tv_sd_remainning_capacity, persen;
    TextView tv_usb_total_capacity, tv_usb_remainning_capacity;
    ProgressBar progressBar;
    LinearLayout l_sd_card, l_usb;
    private ImageView format_icon;
    private ProgressBar progress_format;
    private RelativeLayout sd_format;
    private int SDcardId;
    private int sdId;
    private int usbId;

    public void showSDImg() {
        format_icon.setVisibility(ImageView.VISIBLE);
        progress_format.setVisibility(progress_format.GONE);
        sd_format.setClickable(true);
    }

    public void showSDProgress() {
        format_icon.setVisibility(ImageView.GONE);
        progress_format.setVisibility(progress_format.VISIBLE);
        sd_format.setClickable(false);
    }


    public void regFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.P2P.ACK_RET_GET_NPC_SETTINGS);

        filter.addAction(Constants.P2P.ACK_RET_SET_RECORD_TYPE);
        filter.addAction(Constants.P2P.RET_SET_RECORD_TYPE);
        filter.addAction(Constants.P2P.RET_GET_RECORD_TYPE);

        filter.addAction(Constants.P2P.ACK_RET_SET_RECORD_TIME);
        filter.addAction(Constants.P2P.RET_SET_RECORD_TIME);
        filter.addAction(Constants.P2P.RET_GET_RECORD_TIME);

        filter.addAction(Constants.P2P.ACK_RET_SET_RECORD_PLAN_TIME);
        filter.addAction(Constants.P2P.RET_SET_RECORD_PLAN_TIME);
        filter.addAction(Constants.P2P.RET_GET_RECORD_PLAN_TIME);


        filter.addAction(Constants.P2P.ACK_RET_SET_REMOTE_RECORD);
        filter.addAction(Constants.P2P.RET_SET_REMOTE_RECORD);
        filter.addAction(Constants.P2P.RET_GET_REMOTE_RECORD);
        filter.addAction(Constants.P2P.RET_GET_PRE_RECORD);
        filter.addAction(Constants.P2P.ACK_RET_SET_PRE_RECORD);
        filter.addAction(Constants.P2P.RET_SET_PRE_RECORD);
        //SD卡
        filter.addAction(Constants.P2P.ACK_GET_SD_CARD_CAPACITY);
        filter.addAction(Constants.P2P.RET_GET_SD_CARD_CAPACITY);
        filter.addAction(Constants.P2P.RET_GET_SD_CARD_FORMAT);
        filter.addAction(Constants.P2P.RET_GET_USB_CAPACITY);
        mContext.registerReceiver(mReceiver, filter);
        isRegFilter = true;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            Log.e("BroadcastReceiver_1111", intent.getAction());
            int state = -1;
            //SD卡
            if (intent.getAction().equals(
                    Constants.P2P.RET_GET_SD_CARD_CAPACITY)) {
                int total_capacity = intent.getIntExtra("total_capacity", -1);//总容量
                int remain_capacity = intent.getIntExtra("remain_capacity", -1);//剩余容量
                state = intent.getIntExtra("state", -1);
                SDcardId = intent.getIntExtra("SDcardID", -1);
                String id = Integer.toBinaryString(SDcardId);
                if (total_capacity > 4 || state == 1) {
                    //有内存的时候
                    record_type_radio.setVisibility(View.VISIBLE);
                    record_type_item.setVisibility(View.VISIBLE);
                    sd_format.setVisibility(View.VISIBLE);
                } else {
                    //没内存卡的时候
                    T.show(getActivity(), getResources().getString(R.string.sd_no_exist), Toast.LENGTH_SHORT);
                    record_type_radio.setVisibility(View.GONE);
                    record_type_item.setVisibility(View.GONE);
                    l_sd_card.setVisibility(View.GONE);
                    sd_format.setVisibility(View.GONE);
                }
                while (id.length() < 8) {
                    id = "0" + id;
                }
                char index = id.charAt(3);
                if (state == 1) {
                    progressBar.setMax(total_capacity);
                    progressBar.setProgress(total_capacity-remain_capacity);
                    int ps = 0;
                    if (remain_capacity > 0) {
                        ps = 100 - remain_capacity * 100 / total_capacity;
                    }
                    persen.setText(ps+"%");
                    if (index == '1') {
                        sdId = SDcardId;

                        tv_total_capacity.setText(String
                                .valueOf(total_capacity) + "MB");
                        tv_sd_remainning_capacity.setText(String
                                .valueOf(remain_capacity) + "MB");
                        showSDImg();
                    } else if (index == '0') {
                        usbId = SDcardId;
                        tv_usb_total_capacity.setText(String
                                .valueOf(total_capacity) + "MB");
                        tv_usb_remainning_capacity.setText(String
                                .valueOf(remain_capacity) + "MB");
                    }
                }
            } else if (intent.getAction().equals(
                    Constants.P2P.RET_GET_SD_CARD_FORMAT)) {
                //result - 80：SD卡格式化成功 81:SD卡格式化失败 82：存储卡不存在 103:正在录像，不允许格式化
                int result = intent.getIntExtra("result", -1);
                if (result == Constants.P2P_SET.SD_FORMAT.SD_CARD_SUCCESS) {
                    T.showShort(mContext, R.string.sd_format_success);
                } else if (result == Constants.P2P_SET.SD_FORMAT.SD_CARD_FAIL) {
                    T.showShort(mContext, R.string.sd_format_fail);
                } else if (result == Constants.P2P_SET.SD_FORMAT.SD_NO_EXIST) {
                    T.showShort(mContext, R.string.sd_no_exist);
                } else if (result == Constants.P2P_SET.SD_FORMAT.SD_CAMERA_PLAY) {
                    T.showShort(mContext, R.string.message_record_play);
                }
                showSDImg();
            } else if (intent.getAction().equals(
                    Constants.P2P.RET_GET_USB_CAPACITY)) {
                Log.e("usb", "get usb");
                int total_capacity = intent.getIntExtra("total_capacity", -1);//总容量
                int remain_capacity = intent.getIntExtra("remain_capacity", -1);//剩余容量
                state = intent.getIntExtra("state", -1);
                SDcardId = intent.getIntExtra("SDcardID", -1);
                String id = Integer.toBinaryString(SDcardId);
                Log.e("id", "msga" + id);
                while (id.length() < 8) {
                    id = "0" + id;
                }
                char index = id.charAt(3);
                Log.e("id", "msgb" + id);
                Log.e("id", "msgc" + index);
                if (state == 1) {
                    if (index == '1') {
                        sdId = SDcardId;

                        tv_total_capacity.setText(String
                                .valueOf(total_capacity) + "M");
                        tv_sd_remainning_capacity.setText(String
                                .valueOf(remain_capacity) + "M");
                        showSDImg();
                    } else if (index == '0') {
                        usbId = SDcardId;
                        tv_usb_total_capacity.setText(String
                                .valueOf(total_capacity) + "M");
                        tv_usb_remainning_capacity.setText(String
                                .valueOf(remain_capacity) + "M");
                    }
//                    sd_card_remainning_capacity.setBackgroundResource(R.drawable.tiao_bg_center);
                    l_usb.setVisibility(LinearLayout.VISIBLE);
                }
            } else if (intent.getAction().equals(
                    Constants.P2P.ACK_GET_SD_CARD_CAPACITY)) {
                int result = intent.getIntExtra("result", -1);
                if (result == Constants.P2P_SET.ACK_RESULT.ACK_PWD_ERROR) {

                    Intent i = new Intent();
                    i.setAction(Constants.Action.CONTROL_SETTING_PWD_ERROR);
                    mContext.sendBroadcast(i);

                } else if (result == Constants.P2P_SET.ACK_RESULT.ACK_NET_ERROR) {
                    Log.e("my", "net error resend:get npc time");
                    P2PHandler.getInstance().getSdCardCapacity(idOrIp,
                            contact.contactPassword, MainApplication.GWELL_LOCALAREAIP);
                }
            }

            if (intent.getAction().equals(Constants.P2P.ACK_RET_GET_NPC_SETTINGS)) {
                int result = intent.getIntExtra("result", -1);
                if (result == Constants.P2P_SET.ACK_RESULT.ACK_PWD_ERROR) {
                    Intent i = new Intent();
                    i.setAction(Constants.Action.CONTROL_SETTING_PWD_ERROR);
                    mContext.sendBroadcast(i);
                } else if (result == Constants.P2P_SET.ACK_RESULT.ACK_NET_ERROR) {
                    Log.e("my", "net error resend:get npc settings");
                    P2PHandler.getInstance().getNpcSettings(contact.contactId, contact.contactPassword, MainApplication.GWELL_LOCALAREAIP);
                }
            } else if (intent.getAction().equals(Constants.P2P.RET_GET_RECORD_TYPE)) {
                type = intent.getIntExtra("type", -1);
                updateRecordType(type);
                showRecordType();
            } else if (intent.getAction().equals(Constants.P2P.RET_SET_RECORD_TYPE)) {
                int result = intent.getIntExtra("result", -1);
                if (result == Constants.P2P_SET.RECORD_TYPE_SET.SETTING_SUCCESS) {
                    P2PHandler.getInstance().getNpcSettings(contact.contactId, contact.contactPassword, MainApplication.GWELL_LOCALAREAIP);
                    updateRecordType(cur_modify_record_type);
                    showRecordType();
                    T.showShort(mContext, getString(R.string.record_control_type));
                } else {
                    showRecordType();
                    T.showShort(mContext, R.string.operator_error);
                }
            } else if (intent.getAction().equals(Constants.P2P.RET_GET_RECORD_TIME)) {
                int time = intent.getIntExtra("time", -1);
                if (time == Constants.P2P_SET.RECORD_TIME_SET.RECORD_TIME_ONE_MINUTE) {
                    radio_one_time.setChecked(true);
                } else if (time == Constants.P2P_SET.RECORD_TIME_SET.RECORD_TIME_TWO_MINUTE) {
                    radio_two_time.setChecked(true);
                } else if (time == Constants.P2P_SET.RECORD_TIME_SET.RECORD_TIME_THREE_MINUTE) {
                    radio_three_time.setChecked(true);
                }
                radio_one_time.setEnabled(true);
                radio_two_time.setEnabled(true);
                radio_three_time.setEnabled(true);
                progressBar_record_time.setVisibility(RelativeLayout.GONE);
            } else if (intent.getAction().equals(Constants.P2P.RET_SET_RECORD_TIME)) {
                int result = intent.getIntExtra("result", -1);
                if (result == 0) {
                    if (cur_modify_record_time == Constants.P2P_SET.RECORD_TIME_SET.RECORD_TIME_ONE_MINUTE) {
                        radio_one_time.setChecked(true);
                    } else if (cur_modify_record_time == Constants.P2P_SET.RECORD_TIME_SET.RECORD_TIME_TWO_MINUTE) {
                        radio_two_time.setChecked(true);
                    } else if (cur_modify_record_time == Constants.P2P_SET.RECORD_TIME_SET.RECORD_TIME_THREE_MINUTE) {
                        radio_three_time.setChecked(true);
                    }
                    radio_one_time.setEnabled(true);
                    radio_two_time.setEnabled(true);
                    radio_three_time.setEnabled(true);
                    progressBar_record_time.setVisibility(RelativeLayout.GONE);
                    T.showShort(mContext, getString(R.string.record_control_time));
                } else {
                    radio_one_time.setEnabled(true);
                    radio_two_time.setEnabled(true);
                    radio_three_time.setEnabled(true);
                    progressBar_record_time.setVisibility(RelativeLayout.GONE);
                    T.showShort(mContext, R.string.operator_error);
                }
            } else if (intent.getAction().equals(Constants.P2P.RET_GET_RECORD_PLAN_TIME)) {
                String time = intent.getStringExtra("time");
                Log.e("time", time);
                String startTime1 = time.substring(0, 2);
                String startTime2 = time.substring(3, 5);
                String endTime1 = time.substring(6, 8);
                String endTime2 = time.substring(9, 11);
                if (Integer.parseInt(startTime1) < 10) {
                    startTime1 = time.substring(1, 2);
                }
                if (Integer.parseInt(startTime2) < 10) {
                    startTime2 = time.substring(4, 5);
                }
                if (Integer.parseInt(endTime1) < 10) {
                    endTime1 = time.substring(7, 8);
                }
                if (Integer.parseInt(endTime2) < 10) {
                    endTime2 = time.substring(10, 11);
                }
                Log.e("time", startTime1 + " " + startTime2);
                Log.e("time", endTime1 + " " + endTime2);
                time_from.setText(startTime1 + ":" + startTime2);
                time_to.setText(endTime1 + ":" + endTime2);
            } else if (intent.getAction().equals(Constants.P2P.RET_SET_RECORD_PLAN_TIME)) {
                int result = intent.getIntExtra("result", -1);
                if (result == Constants.P2P_SET.RECORD_PLAN_TIME_SET.SETTING_SUCCESS) {
                    T.showShort(mContext, getString(R.string.record_control_timing));
                } else {
                    T.showShort(mContext, R.string.operator_error);
                }
            } else if (intent.getAction().equals(Constants.P2P.ACK_RET_SET_RECORD_TYPE)) {
                int result = intent.getIntExtra("result", -1);
                if (result == Constants.P2P_SET.ACK_RESULT.ACK_PWD_ERROR) {
                    Intent i = new Intent();
                    i.setAction(Constants.Action.CONTROL_SETTING_PWD_ERROR);
                    mContext.sendBroadcast(i);
                } else if (result == Constants.P2P_SET.ACK_RESULT.ACK_NET_ERROR) {
                    Log.e("my", "net error resend:set npc settings record type");
                    P2PHandler.getInstance().setRecordType(contact.contactId, contact.contactPassword, cur_modify_record_type, MainApplication.GWELL_LOCALAREAIP);
                }
            } else if (intent.getAction().equals(Constants.P2P.ACK_RET_SET_RECORD_TIME)) {
                int result = intent.getIntExtra("result", -1);
                if (result == Constants.P2P_SET.ACK_RESULT.ACK_PWD_ERROR) {
                    Intent i = new Intent();
                    i.setAction(Constants.Action.CONTROL_SETTING_PWD_ERROR);
                    mContext.sendBroadcast(i);
                } else if (result == Constants.P2P_SET.ACK_RESULT.ACK_NET_ERROR) {
                    Log.e("my", "net error resend:set npc settings record time");
                    P2PHandler.getInstance().setRecordType(contact.contactId, contact.contactPassword, cur_modify_record_type, MainApplication.GWELL_LOCALAREAIP);
                }
            } else if (intent.getAction().equals(Constants.P2P.ACK_RET_SET_RECORD_PLAN_TIME)) {
                int result = intent.getIntExtra("result", -1);
                if (result == Constants.P2P_SET.ACK_RESULT.ACK_PWD_ERROR) {
                    Intent i = new Intent();
                    i.setAction(Constants.Action.CONTROL_SETTING_PWD_ERROR);
                    mContext.sendBroadcast(i);
                } else if (result == Constants.P2P_SET.ACK_RESULT.ACK_NET_ERROR) {
                    Log.e("my", "net error resend:set npc settings record plan time");
                    P2PHandler.getInstance().setRecordPlanTime(contact.contactId, contact.contactPassword, cur_modify_plan_time, MainApplication.GWELL_LOCALAREAIP);
                }
            } else if (intent.getAction().equals(Constants.P2P.RET_GET_REMOTE_RECORD)) {
                state = intent.getIntExtra("state", -1);
                progressBar_record.setVisibility(RelativeLayout.GONE);
                record_img.setVisibility(RelativeLayout.VISIBLE);
                updateRecord(state);
            } else if (intent.getAction().equals(Constants.P2P.RET_SET_REMOTE_RECORD)) {
                state = intent.getIntExtra("state", -1);
                P2PHandler.getInstance().getNpcSettings(contact.contactId, contact.contactPassword, MainApplication.GWELL_LOCALAREAIP);
                //updateRecord(state);
            } else if (intent.getAction().equals(Constants.P2P.ACK_RET_SET_REMOTE_RECORD)) {
                int result = intent.getIntExtra("result", -1);
                if (result == Constants.P2P_SET.ACK_RESULT.ACK_PWD_ERROR) {
                    Intent i = new Intent();
                    i.setAction(Constants.Action.CONTROL_SETTING_PWD_ERROR);
                    mContext.sendBroadcast(i);
                } else if (result == Constants.P2P_SET.ACK_RESULT.ACK_NET_ERROR) {
                    Log.e("my", "net error resend:set remote record");
                    P2PHandler.getInstance().setRemoteRecord(contact.contactId, contact.contactPassword, last_modify_record, MainApplication.GWELL_LOCALAREAIP);
                }
            } else if (intent.getAction().equals(Constants.P2P.RET_GET_PRE_RECORD)) {
                state = intent.getIntExtra("state", -1);
                isSupportPreRecored = true;
                change_pre_record.setClickable(true);
                if (type == Constants.P2P_SET.RECORD_TYPE_SET.RECORD_TYPE_ALARM) {
                    change_pre_record.setVisibility(RelativeLayout.VISIBLE);
                }
                if (state == 1) {
                    pre_record_img.setBackgroundResource(R.drawable.zhzj_switch_on);
                    last_pre_record = Constants.P2P_SET.PRE_RECORD_SET.PRE_RECORD_SWITCH_ON;
                } else if (state == 0) {
                    pre_record_img.setBackgroundResource(R.drawable.zhzj_switch_off);
                    last_pre_record = Constants.P2P_SET.PRE_RECORD_SET.PRE_RECORD_SWITCH_OFF;
                }
                showPreRecordImg();
            } else if (intent.getAction().equals(Constants.P2P.ACK_RET_SET_PRE_RECORD)) {
                int result = intent.getIntExtra("state", -1);
                if (result == Constants.P2P_SET.ACK_RESULT.ACK_PWD_ERROR) {
                    Intent i = new Intent();
                    i.setAction(Constants.Action.CONTROL_SETTING_PWD_ERROR);
                    mContext.sendBroadcast(i);
                } else if (result == Constants.P2P_SET.ACK_RESULT.ACK_NET_ERROR) {
                    Log.e("my", "net error resend:set npc settings record type");
                    P2PHandler.getInstance().setPreRecord(contact.contactId, contact.contactPassword, last_modify_pre_record, MainApplication.GWELL_LOCALAREAIP);
                }
            } else if (intent.getAction().equals(Constants.P2P.RET_SET_PRE_RECORD)) {
                int result = intent.getIntExtra("result", -1);
                if (result == 0) {
                    P2PHandler.getInstance().getNpcSettings(contact.contactId, contact.contactPassword, MainApplication.GWELL_LOCALAREAIP);
                    T.showShort(mContext, "设置成功");
                } else if (result == 83) {
                    T.showShort(mContext, R.string.operator_error);
                }
            }
        }
    };

    public void updateRecord(int state) {
        if (state == Constants.P2P_SET.REMOTE_RECORD_SET.RECORD_SWITCH_ON) {
            last_record = Constants.P2P_SET.REMOTE_RECORD_SET.RECORD_SWITCH_ON;
            record_img.setBackgroundResource(R.drawable.zhzj_switch_on);
        } else {
            last_record = Constants.P2P_SET.REMOTE_RECORD_SET.RECORD_SWITCH_OFF;
            record_img.setBackgroundResource(R.drawable.zhzj_switch_off);
        }
    }

    void updateRecordType(int type) {
        if (type == Constants.P2P_SET.RECORD_TYPE_SET.RECORD_TYPE_MANUAL) {
            radio_one.setChecked(true);
            hideRecordTime();
            hidePlanTime();
            showManual();
        } else if (type == Constants.P2P_SET.RECORD_TYPE_SET.RECORD_TYPE_ALARM) {
            radio_two.setChecked(true);
            hidePlanTime();
            hideManual();
            showRecordTime();
        } else if (type == Constants.P2P_SET.RECORD_TYPE_SET.RECORD_TYPE_TIMER) {
            radio_three.setChecked(true);
            hideRecordTime();
            hideManual();
            showPlanTime();
        }
    }

    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub

        switch (view.getId()) {
            case R.id.bt_set_time:
                showProgress_plan_time();
                cur_modify_plan_time = time_from.getText().toString().trim()+"-"+time_to.getText().toString().trim();
                P2PHandler.getInstance().setRecordPlanTime(contact.contactId, contact.contactPassword, cur_modify_plan_time, MainApplication.GWELL_LOCALAREAIP);
                break;
            case R.id.time_from:
                isFrome = true;
                timePickerView.show();
                break;
            case R.id.time_to:
                isFrome = false;
                timePickerView.show();
                break;
            case R.id.sd_format:
                final NormalDialog dialog = new NormalDialog(mContext, mContext
                        .getResources().getString(R.string.sd_formatting), mContext
                        .getResources().getString(R.string.delete_sd_remind),
                        mContext.getResources().getString(R.string.sure),
                        mContext.getResources().getString(R.string.cancel));
                dialog.setOnButtonOkListener(new NormalDialog.OnButtonOkListener() {

                    @Override
                    public void onClick() {
                        // TODO Auto-generated method stub
                        P2PHandler.getInstance().setSdFormat(idOrIp,
                                contact.contactPassword, sdId, MainApplication.GWELL_LOCALAREAIP);

                        mHandler.sendEmptyMessageDelayed(0, 3 * 1000);
                    }
                });
                dialog.setOnButtonCancelListener(new NormalDialog.OnButtonCancelListener() {

                    @Override
                    public void onClick() {
                        // TODO Auto-generated method stub
                        showSDImg();
                        dialog.dismiss();
                    }
                });
                dialog.showNormalDialog();
                dialog.setCanceledOnTouchOutside(false);
                showSDProgress();
                break;
            case R.id.change_record:
                progressBar_record.setVisibility(RelativeLayout.VISIBLE);
                record_img.setVisibility(RelativeLayout.GONE);
                if (last_record == Constants.P2P_SET.REMOTE_RECORD_SET.RECORD_SWITCH_ON) {
                    last_modify_record = Constants.P2P_SET.REMOTE_RECORD_SET.RECORD_SWITCH_OFF;
                    P2PHandler.getInstance().setRemoteRecord(contact.contactId, contact.contactPassword, last_modify_record, MainApplication.GWELL_LOCALAREAIP);
                } else {
                    last_modify_record = Constants.P2P_SET.REMOTE_RECORD_SET.RECORD_SWITCH_ON;
                    P2PHandler.getInstance().setRemoteRecord(contact.contactId, contact.contactPassword, last_modify_record, MainApplication.GWELL_LOCALAREAIP);
                }
                break;
            case R.id.radio_one:
                progressBar_record_type.setVisibility(RelativeLayout.VISIBLE);
                radio_one.setEnabled(false);
                radio_two.setEnabled(false);
                radio_three.setEnabled(false);
                cur_modify_record_type = Constants.P2P_SET.RECORD_TYPE_SET.RECORD_TYPE_MANUAL;
                showRecordSwitchProgress();
                P2PHandler.getInstance().setRecordType(contact.contactId, contact.contactPassword, cur_modify_record_type, MainApplication.GWELL_LOCALAREAIP);
                break;
            case R.id.radio_two:
                progressBar_record_type.setVisibility(RelativeLayout.VISIBLE);
                radio_one.setEnabled(false);
                radio_two.setEnabled(false);
                radio_three.setEnabled(false);
                cur_modify_record_type = Constants.P2P_SET.RECORD_TYPE_SET.RECORD_TYPE_ALARM;
                showPreRecordProgress();
                P2PHandler.getInstance().setRecordType(contact.contactId, contact.contactPassword, cur_modify_record_type, MainApplication.GWELL_LOCALAREAIP);
                break;
            case R.id.radio_three:
                radio_one.setEnabled(false);
                radio_two.setEnabled(false);
                radio_three.setEnabled(false);
                progressBar_record_type.setVisibility(RelativeLayout.VISIBLE);
                cur_modify_record_type = Constants.P2P_SET.RECORD_TYPE_SET.RECORD_TYPE_TIMER;
                P2PHandler.getInstance().setRecordType(contact.contactId, contact.contactPassword, cur_modify_record_type, MainApplication.GWELL_LOCALAREAIP);
                break;
            case R.id.radio_one_time:
                progressBar_record_time.setVisibility(RelativeLayout.VISIBLE);
                radio_one_time.setEnabled(false);
                radio_two_time.setEnabled(false);
                radio_three_time.setEnabled(false);
                cur_modify_record_time = Constants.P2P_SET.RECORD_TIME_SET.RECORD_TIME_ONE_MINUTE;
                P2PHandler.getInstance().setRecordTime(contact.contactId, contact.contactPassword, cur_modify_record_time, MainApplication.GWELL_LOCALAREAIP);
                break;
            case R.id.radio_two_time:
                progressBar_record_time.setVisibility(RelativeLayout.VISIBLE);
                radio_one_time.setEnabled(false);
                radio_two_time.setEnabled(false);
                radio_three_time.setEnabled(false);
                cur_modify_record_time = Constants.P2P_SET.RECORD_TIME_SET.RECORD_TIME_TWO_MINUTE;
                P2PHandler.getInstance().setRecordTime(contact.contactId, contact.contactPassword, cur_modify_record_time, MainApplication.GWELL_LOCALAREAIP);
                break;
            case R.id.radio_three_time:
                progressBar_record_time.setVisibility(RelativeLayout.VISIBLE);
                radio_one_time.setEnabled(false);
                radio_two_time.setEnabled(false);
                radio_three_time.setEnabled(false);
                cur_modify_record_time = Constants.P2P_SET.RECORD_TIME_SET.RECORD_TIME_THREE_MINUTE;
                P2PHandler.getInstance().setRecordTime(contact.contactId, contact.contactPassword, cur_modify_record_time, MainApplication.GWELL_LOCALAREAIP);
                break;
//            case R.id.change_plan_time:
//                showProgress_plan_time();
//
////                cur_modify_plan_time = Utils.convertPlanTime(hour_from.getCurrentItem(), minute_from.getCurrentItem(), hour_to.getCurrentItem(), minute_to.getCurrentItem());
//                P2PHandler.getInstance().setRecordPlanTime(contact.contactId, contact.contactPassword, cur_modify_plan_time);
//                break;
            case R.id.change_pre_record:
//			1.开启
                showPreRecordProgress();
                if (last_pre_record == Constants.P2P_SET.PRE_RECORD_SET.PRE_RECORD_SWITCH_ON) {
                    last_modify_pre_record = Constants.P2P_SET.PRE_RECORD_SET.PRE_RECORD_SWITCH_OFF;
                    P2PHandler.getInstance().setPreRecord(contact.contactId, contact.contactPassword, last_modify_pre_record, MainApplication.GWELL_LOCALAREAIP);
                    isOpenPreRecord = false;
                } else if (last_pre_record == Constants.P2P_SET.PRE_RECORD_SET.PRE_RECORD_SWITCH_OFF) {
                    last_modify_pre_record = Constants.P2P_SET.PRE_RECORD_SET.PRE_RECORD_SWITCH_ON;
                    P2PHandler.getInstance().setPreRecord(contact.contactId, contact.contactPassword, last_modify_pre_record, MainApplication.GWELL_LOCALAREAIP);
                    isOpenPreRecord = true;
                }
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
        Intent it = new Intent();
        it.setAction(Constants.Action.CONTROL_BACK);
        mContext.sendBroadcast(it);
    }

    public void showRecordType() {
        change_record_type.setBackgroundResource(R.drawable.tiao_bg_up);
        progressBar_record_type.setVisibility(RelativeLayout.GONE);
        radio_one.setEnabled(true);
        radio_two.setEnabled(true);
        radio_three.setEnabled(true);
    }

    public void showProgress_record_type() {
        change_record_type.setBackgroundResource(R.drawable.tiao_bg_single);
        progressBar_record_type.setVisibility(RelativeLayout.VISIBLE);
        record_type_radio.setVisibility(RelativeLayout.GONE);
        sd_format.setVisibility(View.GONE);
    }

    public void showRecordTime() {
        change_record_time.setVisibility(RelativeLayout.VISIBLE);
        record_time_radio.setVisibility(RelativeLayout.VISIBLE);
        progressBar_record_time.setVisibility(RelativeLayout.GONE);
        if (isSupportPreRecored == true) {
            change_pre_record.setVisibility(RelativeLayout.VISIBLE);
        } else {
            change_pre_record.setVisibility(RelativeLayout.GONE);
        }
    }

    public void showProgress_record_time() {
        change_record_time.setVisibility(RelativeLayout.VISIBLE);
        record_time_radio.setVisibility(RelativeLayout.VISIBLE);
        progressBar_record_time.setVisibility(RelativeLayout.VISIBLE);
    }

    public void showPlanTime() {
        change_pre_record.setVisibility(RelativeLayout.GONE);
        change_record.setVisibility(RelativeLayout.GONE);
        time_picker.setVisibility(RelativeLayout.VISIBLE);

    }

    public void showProgress_plan_time() {
        time_picker.setVisibility(RelativeLayout.VISIBLE);
    }

    public void showManual() {
        change_record.setVisibility(RelativeLayout.VISIBLE);
        change_pre_record.setVisibility(RelativeLayout.GONE);
    }

    public void showPreRecordProgress() {
        progressBar_pre_record.setVisibility(ProgressBar.VISIBLE);
        pre_record_img.setVisibility(ImageView.GONE);
    }

    public void showPreRecordImg() {
        progressBar_pre_record.setVisibility(ProgressBar.GONE);
        pre_record_img.setVisibility(ImageView.VISIBLE);
    }

    public void hideRecordTime() {
        change_record_time.setVisibility(RelativeLayout.GONE);
        record_time_radio.setVisibility(RelativeLayout.GONE);
    }

    public void hidePlanTime() {
        time_picker.setVisibility(RelativeLayout.GONE);
    }

    public void hideManual() {
        change_record.setVisibility(RelativeLayout.GONE);
    }

    public void showRecordSwitchProgress() {
        progressBar_record.setVisibility(RelativeLayout.VISIBLE);
        record_img.setVisibility(RelativeLayout.GONE);
    }

    public void showRecordSwitchImg() {
        progressBar_record.setVisibility(RelativeLayout.GONE);
        record_img.setVisibility(RelativeLayout.VISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent it = new Intent();
        it.setAction(Constants.Action.CONTROL_BACK);
        mContext.sendBroadcast(it);
    }


    @Override
    public boolean onTouch(View arg0, MotionEvent event) {
        // TODO Auto-generated method stub
        if (event.getAction() == MotionEvent.ACTION_UP) {
            scroll_view.requestDisallowInterceptTouchEvent(false);
        } else {
            scroll_view.requestDisallowInterceptTouchEvent(true);
        }
        return false;
    }
}
