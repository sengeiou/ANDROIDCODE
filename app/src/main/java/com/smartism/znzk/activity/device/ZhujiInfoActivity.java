package com.smartism.znzk.activity.device;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.OwenerInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.CollectionsUtils;
import com.smartism.znzk.util.DateUtil;

import java.util.List;

/**
 * Created by win7 on 2017/6/4.
 */

public class ZhujiInfoActivity extends ActivityParentActivity {
    private TextView tv_outtime, tv_mac, tv_name, tv_address, tv_zhuji_name, tv_zhuji_status, iv_sim, iv_battery, iv_power, iv_wan, tv_owner,iv_battery_percent;
    private ZhujiInfo zhuji;
    private TextView mTitleTv ;
    private OwenerInfo owenerInf;
    private LinearLayout rl_servicetime,ll_iv_sim,ll_iv_battery,ll_iv_power,ll_iv_wan,ll_battery_percent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zhuji_info);
        DeviceInfo deviceInfo = (DeviceInfo) getIntent().getSerializableExtra("device");
        zhuji = DatabaseOperator.getInstance(this).queryDeviceZhuJiInfo(deviceInfo.getId());
        owenerInf = (OwenerInfo) getIntent().getSerializableExtra("owenerInf");
        initView();

        //埃利恩
        if(zhuji!=null&&(zhuji.getMasterid().contains("FF3B")||DeviceInfo.CaMenu.nbyg.value().equals(zhuji.getCa()))){
            if(DeviceInfo.CaMenu.nbyg.value().equals(zhuji.getCa())){
                mTitleTv.setText(getString(R.string.deviceinfo_activity_singleproduct_info,getString(R.string.deviceinfo_activity_singleproduct_nbyg)));
            }
            List<CommandInfo> commandInfos = DatabaseOperator.getInstance().queryAllCommands(zhuji.getId());
            if (commandInfos.size()>0){
                for (CommandInfo c : commandInfos) {
                    if (CommandInfo.CommandTypeEnum.battery.value().equals(c.getCtype())){
                        ll_battery_percent.setVisibility(View.VISIBLE);
                        iv_battery_percent.setText(c.getCommand()+"%");
                        break ;
                    }
                  }
                }
        }
    }

    private void initView() {
        mTitleTv = findViewById(R.id.tv_title);
        tv_outtime = (TextView) findViewById(R.id.iv_device_outtime);
        rl_servicetime = (LinearLayout) findViewById(R.id.rl_servicetime);
//        if (MainApplication.app.getAppGlobalConfig().getVersion().equals(Actions.VersionType.CHANNEL_ZHILIDE)
//                || MainApplication.app.getAppGlobalConfig().getVersion().equals(Actions.VersionType.CHANNEL_ZHZJ)){
//            rl_servicetime.setVisibility(View.GONE);
//        }
//        tv_zhuji_name = (TextView) findViewById(R.id.tv_zhuji_name);
//        tv_zhuji_status = (TextView) findViewById(R.id.tv_zhuji_status);
        tv_name = (TextView) findViewById(R.id.tv_device_name);
        tv_mac = (TextView) findViewById(R.id.iv_device_mac);
        tv_address = (TextView) findViewById(R.id.tv_device_address);
        tv_name.setText(zhuji.getName());
        tv_mac.setText(zhuji.getMasterid());
        if (owenerInf != null) {
            tv_outtime.setText(DateUtil.formatUnixTime(owenerInf.getServiceTime()));
        }
        tv_address.setText(zhuji.getWhere());
        iv_battery = (TextView) findViewById(R.id.iv_battery);
        iv_power = (TextView) findViewById(R.id.iv_power);
        iv_sim = (TextView) findViewById(R.id.iv_sim);
        iv_wan = (TextView) findViewById(R.id.iv_wan);
        ll_iv_sim = (LinearLayout) findViewById(R.id.ll_iv_sim);
        ll_iv_wan = (LinearLayout) findViewById(R.id.ll_iv_wan);
        ll_iv_battery = (LinearLayout) findViewById(R.id.ll_iv_battery);
        ll_iv_power = (LinearLayout) findViewById(R.id.ll_iv_power);
        ll_battery_percent = findViewById(R.id.ll_battery_percent);
        iv_battery_percent = findViewById(R.id.iv_battery_percent);
        if (zhuji == null) {
            return;
        }
//        if (zhuji.getGsm() == 1){
//            ll_iv_sim.setVisibility(View.VISIBLE);
//            ll_iv_wan.setVisibility(View.VISIBLE);
//            ll_iv_battery.setVisibility(View.VISIBLE);
//            ll_iv_power.setVisibility(View.VISIBLE);
//        }
        if(!Actions.VersionType.CHANNEL_AIERFUDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                && !Actions.VersionType.CHANNEL_UHOME.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            List<CommandInfo> commandInfos = DatabaseOperator.getInstance().queryAllCommands(zhuji.getId());
            if (!CollectionsUtils.isEmpty(commandInfos)) {
                for (CommandInfo c : commandInfos) {
                    if ("17".equals(c.getCtype())) {
                        ll_iv_sim.setVisibility(View.VISIBLE);
                    } else if ("18".equals(c.getCtype())) {
                        ll_iv_battery.setVisibility(View.VISIBLE);
                    } else if ("19".equals(c.getCtype())) {
                        ll_iv_wan.setVisibility(View.VISIBLE);
                    } else if ("20".equals(c.getCtype())) {
                        ll_iv_power.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
        if (zhuji.getSimStatus() == 0) {
            iv_sim.setText(getString(R.string.deviceslist_zhuji_sim_no));
        } else if (zhuji.getSimStatus() == 1) {
            iv_sim.setText(getString(R.string.deviceslist_zhuji_sim_normal));
        } else if (zhuji.getSimStatus() == 2) {
            iv_sim.setText(getString(R.string.deviceslist_zhuji_sim_poor));
        }
        if (zhuji.getBatteryStatus() == 0) {
            iv_battery.setText(getString(R.string.deviceslist_zhuji_battery_normal));
        } else if (zhuji.getBatteryStatus() == 1) {
            iv_battery.setText(getString(R.string.deviceslist_zhuji_battery_less));
        }
        if (zhuji.getPowerStatus() == 0) {
            iv_power.setText(getString(R.string.deviceslist_zhuji_power_220v));
        } else if (zhuji.getPowerStatus() == 1) {
            iv_power.setText(getString(R.string.deviceslist_zhuji_power_battery));
        }
        if (zhuji.getWanType() == 0) {
            iv_wan.setText(getString(R.string.deviceslist_zhuji_wan_lain));
        } else if (zhuji.getWanType() == 1) {
            iv_wan.setText(getString(R.string.deviceslist_zhuji_wan_wifi));
        } else if (zhuji.getWanType() == 2) {
            iv_wan.setText(getString(R.string.deviceslist_zhuji_wan_gsm));
        }
    }

    public void back(View v) {
        finish();
    }
}
