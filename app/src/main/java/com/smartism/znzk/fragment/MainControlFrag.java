package com.smartism.znzk.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.p2p.core.P2PHandler;
import com.p2p.core.P2PValue;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.camera.DeviceUpdateActivity;
import com.smartism.znzk.activity.camera.MainControlActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.activity.camera.PlayBackListActivity;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.util.camera.T;
import com.smartism.znzk.widget.NormalDialog;
import com.smartism.znzk.widget.NormalDialog.OnButtonOkListener;

public class MainControlFrag extends BaseFragment implements OnClickListener {
    RelativeLayout time_contrl, remote_control, alarm_control, video_control, security_control,
            chekc_device_update, recodeback_control, language_control, modify_wifipwd_control, ap_statechange, screen_shot, recode_control,net_control;
    private Context mContext;
    private Contact mContact;
    private int connectType = 0;
    boolean isRegFilter = false;
    NormalDialog dialog;
    private TextView tvAPmodeChange;
    private String device;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }

    public MainControlFrag() {
        super();
        // TODO Auto-generated constructor stub
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        mContext = getActivity();
        mContact = (Contact) getArguments().getSerializable("contact");
        connectType = getArguments().getInt("connectType");
        device = getArguments().getString("device");
        View view = inflater.inflate(R.layout.fragment_control_main, container,
                false);
        initComponent(view);
        changeViewVisible();
        if (!isRegFilter){
            regFilter();
        }
        initData();
        return view;
    }
    public void  changeViewVisible(){
        if (MainApplication.app.getAppGlobalConfig().isCameraTimeSetting()) time_contrl.setVisibility(View.VISIBLE);//显示时间设置
        if (MainApplication.app.getAppGlobalConfig().isCameraMediaSetting()) video_control.setVisibility(View.VISIBLE);//媒体设置
        if (MainApplication.app.getAppGlobalConfig().isCameraSnapshotSetting()) screen_shot.setVisibility(View.VISIBLE);//截图
        if (MainApplication.app.getAppGlobalConfig().isCameraSecuritySetting()) security_control.setVisibility(View.VISIBLE);//安全设置
        if (MainApplication.app.getAppGlobalConfig().isCameraRecordSetting()) recode_control.setVisibility(View.VISIBLE);//录像设置
        if (MainApplication.app.getAppGlobalConfig().isCameraVedioFile()) recodeback_control.setVisibility(View.VISIBLE);//录像文件
        if (MainApplication.app.getAppGlobalConfig().isCameraNetWorkSetting()) net_control.setVisibility(View.VISIBLE);//网络
        if (MainApplication.app.getAppGlobalConfig().isCameraUpDate()) chekc_device_update.setVisibility(View.VISIBLE);//显示时间设置

    }
    public void initComponent(View view) {
        recode_control = (RelativeLayout) view.findViewById(R.id.recode_control);
        screen_shot = (RelativeLayout) view.findViewById(R.id.screen_shot);
        time_contrl = (RelativeLayout) view.findViewById(R.id.time_control);
        remote_control = (RelativeLayout) view
                .findViewById(R.id.remote_control);
        alarm_control = (RelativeLayout) view.findViewById(R.id.alarm_control);
        video_control = (RelativeLayout) view.findViewById(R.id.video_control);
        security_control = (RelativeLayout) view
                .findViewById(R.id.security_control);
        chekc_device_update = (RelativeLayout) view
                .findViewById(R.id.check_device_update);
        recodeback_control = (RelativeLayout) view
                .findViewById(R.id.recodeback_control);
        language_control = (RelativeLayout) view
                .findViewById(R.id.language_control);
        modify_wifipwd_control = (RelativeLayout) view.
                findViewById(R.id.modify_wifipwd_control);
        net_control = (RelativeLayout) view.
                findViewById(R.id.net_control);
        ap_statechange = (RelativeLayout) view.findViewById(R.id.ap_statechange);
        tvAPmodeChange = (TextView) view.findViewById(R.id.tx_apmodecange);
        if (device != null && device.equals("v380")) {
            video_control.setVisibility(View.GONE);
            recode_control.setVisibility(View.VISIBLE);
        }
        if (MainApplication.app.getAppGlobalConfig().isShowRecode()) {
            recode_control.setVisibility(View.VISIBLE);
        }
        recode_control.setOnClickListener(this);
        screen_shot.setOnClickListener(this);
        security_control.setOnClickListener(this);
        video_control.setOnClickListener(this);
        time_contrl.setOnClickListener(this);
        remote_control.setOnClickListener(this);
        alarm_control.setOnClickListener(this);
        chekc_device_update.setOnClickListener(this);
        recodeback_control.setOnClickListener(this);
        language_control.setOnClickListener(this);
        modify_wifipwd_control.setOnClickListener(this);
        net_control.setOnClickListener(this);
        ap_statechange.setOnClickListener(this);
        if (mContact != null) {
            modifyFeatures(view);
        }

        //隐藏存储设置
        view.findViewById(R.id.sd_card_control).setVisibility(View.GONE);
    }

    private void initData() {
        if (mContact != null) {
            P2PHandler.getInstance().getNpcSettings(mContact.getContactId(), mContact.contactPassword,MainApplication.GWELL_LOCALAREAIP);
        }
    }


    private void modifyFeatures(View view) {
        if (mContact.contactType == P2PValue.DeviceType.PHONE) {
            view.findViewById(R.id.control_main_frame).setVisibility(
                    RelativeLayout.GONE);
        } else if (mContact.contactType == P2PValue.DeviceType.NPC) {
            chekc_device_update.setVisibility(RelativeLayout.GONE);

        } else {
            //chekc_device_update.setVisibility(RelativeLayout.VISIBLE);
        }
    }

    public void regFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.P2P.RET_GET_LANGUEGE);
        filter.addAction(Constants.P2P.RET_DEVICE_NOT_SUPPORT);
        filter.addAction(Constants.P2P.RET_SET_AP_MODE);
        filter.addAction(Constants.P2P.RET_AP_MODESURPPORT);
        filter.addAction(Constants.Action.REFRESH_CONTANTS);
        mContext.registerReceiver(br, filter);
        isRegFilter = true;
    }

    BroadcastReceiver br = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            // TODO Auto-generated method stub
            if (intent.getAction().equals(Constants.Action.REFRESH_CONTANTS)) {
                Contact c = (Contact) intent.getSerializableExtra("contact");
                if (null != c) {
                    mContact = c;
                }
            } else if (intent.getAction().equals(Constants.P2P.RET_GET_LANGUEGE)) {
                int languegecount = intent.getIntExtra("languegecount", -1);
                int curlanguege = intent.getIntExtra("curlanguege", -1);
                int[] langueges = intent.getIntArrayExtra("langueges");
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                    Intent go_language_control = new Intent();
                    go_language_control
                            .setAction(Constants.Action.REPLACE_LANGUAGE_CONTROL);
                    go_language_control.putExtra("isEnforce", true);
                    go_language_control
                            .putExtra("languegecount", languegecount);
                    go_language_control.putExtra("curlanguege", curlanguege);
                    go_language_control.putExtra("langueges", langueges);
                    mContext.sendBroadcast(go_language_control);
                }
            } else if (intent.getAction().equals(
                    Constants.P2P.RET_DEVICE_NOT_SUPPORT)) {
                if (dialog != null) {
                    dialog.dismiss();
                    T.showShort(mContext, R.string.not_support);
                }
            } else if (intent.getAction().equals(Constants.P2P.RET_AP_MODESURPPORT)) {
                int result = intent.getIntExtra("result", 0);
                String id = intent.getStringExtra("id");
                if (id == null || mContact == null) {
                    return;
                }
                if (mContact != null && id.equals(mContact.getContactId())) {
                    if (result == 0) {//不支持AP模式
                        ap_statechange.setVisibility(View.GONE);
                    } else if (result == 1) {//支持AP模式,不处于AP模式
                        ap_statechange.setVisibility(View.VISIBLE);
                        tvAPmodeChange.setText(R.string.ap_modecahnge_line);
                    } else if (result == 2) {//支持AP模式，处于AP模式
                        ap_statechange.setVisibility(View.VISIBLE);
                        tvAPmodeChange.setText(R.string.ap_modecahnge_ap);
                    }
                    ap_statechange.setTag(result);
                    //暂时不用单机模式
                    ap_statechange.setVisibility(View.GONE);
                }
            } else if (intent.getAction().equals(Constants.P2P.RET_SET_AP_MODE)) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                ((MainControlActivity) mContext).finish();
            }
        }
    };


    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        switch (view.getId()) {
            case R.id.recode_control:
                Intent go_recode_control = new Intent();
                go_recode_control
                        .setAction(Constants.Action.REPLACE_RECORD_CONTROL);
                go_recode_control.putExtra("isEnforce", true);
                mContext.sendBroadcast(go_recode_control);
                break;
            case R.id.remote_control:
                Intent go_remote_control = new Intent();
                go_remote_control
                        .setAction(Constants.Action.REPLACE_REMOTE_CONTROL);
                go_remote_control.putExtra("isEnforce", true);
                mContext.sendBroadcast(go_remote_control);
                break;
            case R.id.time_control:
                Intent go_time_control = new Intent();
                go_time_control.setAction(Constants.Action.REPLACE_SETTING_TIME);
                go_time_control.putExtra("isEnforce", true);
                mContext.sendBroadcast(go_time_control);
                break;
            case R.id.alarm_control:
                Intent go_alarm_control = new Intent();
                go_alarm_control.setAction(Constants.Action.REPLACE_ALARM_CONTROL);
                go_alarm_control.putExtra("isEnforce", true);
                mContext.sendBroadcast(go_alarm_control);
                break;
            case R.id.video_control:
                Intent go_video_control = new Intent();
                go_video_control.setAction(Constants.Action.REPLACE_VIDEO_CONTROL);
                go_video_control.putExtra("isEnforce", true);
                mContext.sendBroadcast(go_video_control);
                break;
            case R.id.security_control:
                Intent go_security_control = new Intent();
                go_security_control
                        .setAction(Constants.Action.REPLACE_SECURITY_CONTROL);
                go_security_control.putExtra("isEnforce", true);
                mContext.sendBroadcast(go_security_control);
                break;
            case R.id.check_device_update:
                Intent check_update = new Intent(mContext,
                        DeviceUpdateActivity.class);
                check_update.putExtra("contact", mContact);
                mContext.startActivity(check_update);
                break;
            case R.id.recodeback_control:
                //录像列表
//                Intent play_back_camera = new Intent();
//                play_back_camera.setAction(Constants.Action.FRAG_PLAYBACK_CAMERALIST);
//                play_back_camera.putExtra("isEnforce", true);
//                mContext.sendBroadcast(play_back_camera);

                Intent play_back_camera = new Intent();
                play_back_camera.setClass(getActivity(), PlayBackListActivity.class);
                play_back_camera.putExtra("contact", mContact);
                mContext.startActivity(play_back_camera);
                break;
            case R.id.screen_shot:

                Intent screen_shot = new Intent();
                screen_shot
                        .setAction(Constants.Action.REPLACE_UTILS_CONTROL);
                screen_shot.putExtra("isEnforce", true);
                mContext.sendBroadcast(screen_shot);
                break;
            case R.id.language_control:
                P2PHandler.getInstance().getDeviceLanguage(mContact.contactId,
                        mContact.contactPassword,MainApplication.GWELL_LOCALAREAIP);
                dialog = new NormalDialog(mContext);
                dialog.setStyle(NormalDialog.DIALOG_STYLE_LOADING);
                dialog.showDialog();
                break;
            case R.id.modify_wifipwd_control:
                Intent go_modify_wifipwd = new Intent();
                go_modify_wifipwd.setAction(Constants.Action.REPLACE_MODIFY_WIFI_PWD_CONTROL);
                go_modify_wifipwd.putExtra("isEnforce", true);
                mContext.sendBroadcast(go_modify_wifipwd);
                break;
            case R.id.net_control:
                //网络设置
                Intent go_net_control = new Intent();
                go_net_control.setAction(Constants.Action.REPLACE_NET_CONTROL);
                go_net_control.putExtra("isEnforce", true);
                mContext.sendBroadcast(go_net_control);
                break;
            case R.id.ap_statechange:
                int resuly = (Integer) view.getTag();
                dialog = new NormalDialog(mContext);
                if (resuly == 1) {//支持AP模式,不处于AP模式
                    dialog.setTitle(R.string.ap_modecahnge_line);
                } else if (resuly == 2) {//支持AP模式，处于AP模式
                    dialog.setTitle(R.string.ap_modecahnge_ap);
                }
                dialog.setContentStr(R.string.ap_modecahnge);
                dialog.setbtnStr2(R.string.cancel);
                dialog.setbtnStr1(R.string.sure);
                dialog.setStyle(NormalDialog.DIALOG_STYLE_NORMAL);
                dialog.showDialog();
                dialog.setOnButtonOkListener(new OnButtonOkListener() {

                    @Override
                    public void onClick() {
                        P2PHandler.getInstance().setAPModeChange(mContact.getContactId(), mContact.contactPassword, 1,MainApplication.GWELL_LOCALAREAIP);
                        dialog.showLoadingDialog2();
                    }
                });
                //查看返回结果，再跳转
                break;
        }
    }

    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        super.onDestroyView();
        if (isRegFilter) {
            isRegFilter = false;
            mContext.unregisterReceiver(br);
        }
    }
}
