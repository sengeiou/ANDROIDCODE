package com.smartism.znzk.xiongmai.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartism.znzk.R;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.domain.camera.CameraInfo;
import com.smartism.znzk.xiongmai.activities.XMDeviceMoreSettingsActivity;
import com.smartism.znzk.xiongmai.activities.XMDeviceRecordListActivity;
import com.smartism.znzk.xiongmai.activities.XMDeviceSetupRecord;
import com.smartism.znzk.xiongmai.activities.XMDeviceSetupStorage;
import com.smartism.znzk.xiongmai.activities.XMLocalRecordFilesActivity;
import com.smartism.znzk.xiongmai.activities.XMScreenshotDisplayActivity;
import com.smartism.znzk.xiongmai.activities.XiongMaiSetupAlarmActivity;
import com.smartism.znzk.xiongmai.activities.XiongmaiAboutDeviceActivity;
import com.smartism.znzk.xiongmai.activities.XiongmaiSecuritySettingActivity;
import com.smartism.znzk.zhicheng.tasks.LoadZhujiAndDeviceTask;

public class XMSettingMainFragment extends Fragment implements View.OnClickListener {

    public interface XMSettingMainTiaoZhuang {
        void changeFragment(int id);
    }

    XMSettingMainTiaoZhuang mChangeFragment;
    RelativeLayout time_control, video_control, screen_shot, security_control, recode_control, recodeback_control, rl_phone_record_file, rl_screenshot_from_phone, rl_about_device,
            alarm_control, net_control, check_device_update, sd_card_control, more_settings_control;
    TextView base_setting_text;
    private CameraInfo mCameraInfo;
    private long mDeviceId;

    //不需要什么参数，就通过构造器创建对象
    public XMSettingMainFragment() {
    }

    public static XMSettingMainFragment getInstance(Bundle bundle) {
        XMSettingMainFragment xmSettingMainFragment = new XMSettingMainFragment();
        xmSettingMainFragment.setArguments(bundle);
        return xmSettingMainFragment;
    }


    String device_sn;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() instanceof XMSettingMainTiaoZhuang) {
            mChangeFragment = (XMSettingMainTiaoZhuang) getActivity();
            device_sn = getArguments().getString("sn");
            mDeviceId = getArguments().getLong("device_id");
            mCameraInfo = (CameraInfo) getArguments().getSerializable("camera_info");
        } else {
            throw new IllegalArgumentException("Activity must implements XMSettingMainTiaoZhuang interface");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_control_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        time_control = view.findViewById(R.id.time_control);
        video_control = view.findViewById(R.id.video_control);
        screen_shot = view.findViewById(R.id.screen_shot);
        security_control = view.findViewById(R.id.security_control);
        recode_control = view.findViewById(R.id.recode_control);
        recodeback_control = view.findViewById(R.id.recodeback_control);
        alarm_control = view.findViewById(R.id.alarm_control);
        net_control = view.findViewById(R.id.net_control);
        check_device_update = view.findViewById(R.id.check_device_update);
        base_setting_text = view.findViewById(R.id.base_setting_text);
        sd_card_control = view.findViewById(R.id.sd_card_control);//存储设置
        rl_phone_record_file = view.findViewById(R.id.rl_phone_record_file);//手机录像文件设置
        rl_screenshot_from_phone = view.findViewById(R.id.rl_screenshot_from_phone);//查看截图
        rl_about_device = view.findViewById(R.id.rl_about_device);//关于设备
        more_settings_control = view.findViewById(R.id.more_settings_control);//高级设置


        //默认隐藏的先显示
        more_settings_control.setVisibility(View.VISIBLE);

        base_setting_text.setText(getResources().getString(R.string.base_setting));

        //先隐藏其它设置
        time_control.setVisibility(View.GONE);
        screen_shot.setVisibility(View.GONE);
        security_control.setVisibility(View.GONE);
        alarm_control.setVisibility(View.GONE);
        net_control.setVisibility(View.GONE);
        check_device_update.setVisibility(View.GONE);

        //事件处理
        video_control.setOnClickListener(this);
        sd_card_control.setOnClickListener(this);
        recode_control.setOnClickListener(this);
        recodeback_control.setOnClickListener(this);
        rl_screenshot_from_phone.setOnClickListener(this);
        alarm_control.setOnClickListener(this);
        rl_phone_record_file.setOnClickListener(this);
        rl_about_device.setOnClickListener(this);
        security_control.setOnClickListener(this);
        more_settings_control.setOnClickListener(this);

        //if(MainApplication.app.getAppGlobalConfig().isDebug()){
        alarm_control.setVisibility(View.VISIBLE);
        rl_phone_record_file.setVisibility(View.VISIBLE);
        rl_screenshot_from_phone.setVisibility(View.VISIBLE);
        rl_about_device.setVisibility(View.VISIBLE);

        //}

        new LoadZhujiAndDeviceTask().queryZhujiInfoByZhuji(mDeviceId, new LoadZhujiAndDeviceTask.ILoadResult<ZhujiInfo>() {
            @Override
            public void loadResult(ZhujiInfo result) {
                if (result.isAdmin()) {
                    //管理员显示安全设置
                    security_control.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.rl_about_device:
                intent.setClass(getActivity(), XiongmaiAboutDeviceActivity.class);
                intent.putExtra("sn", device_sn);
                startActivity(intent);
                break;
            case R.id.alarm_control:
                intent.setClass(getActivity(), XiongMaiSetupAlarmActivity.class);
                intent.putExtra("sn", device_sn);
                startActivity(intent);
                break;
            case R.id.rl_phone_record_file:
                intent.setClass(getContext(), XMLocalRecordFilesActivity.class);
                intent.putExtra("sn", device_sn);
                startActivity(intent);
                break;
            case R.id.video_control:
                mChangeFragment.changeFragment(R.id.video_control);
                break;
            case R.id.sd_card_control:
                intent.setClass(getActivity(), XMDeviceSetupStorage.class);
                intent.putExtra("sn", device_sn);
                startActivity(intent);
                break;
            case R.id.recode_control:
                intent.setClass(getActivity(), XMDeviceSetupRecord.class);
                intent.putExtra("sn", device_sn);
                startActivity(intent);
                break;
            case R.id.recodeback_control:
                intent.setClass(getActivity(), XMDeviceRecordListActivity.class);
                intent.putExtra("sn", device_sn);
                startActivity(intent);
                break;
            case R.id.rl_screenshot_from_phone:
                intent.setClass(getActivity(), XMScreenshotDisplayActivity.class);
                intent.putExtra("sn", device_sn);
                startActivity(intent);
                break;
            case R.id.security_control:
                intent.setClass(getActivity(), XiongmaiSecuritySettingActivity.class);
                intent.putExtra("sn", device_sn);
                intent.putExtra("camera_info", mCameraInfo);
                intent.putExtra("device_id", mDeviceId);
                startActivity(intent);
                break;
            case R.id.more_settings_control:
                intent.setClass(getActivity(), XMDeviceMoreSettingsActivity.class);
                intent.putExtra("sn", device_sn);
                startActivity(intent);
                break;
        }
    }
}