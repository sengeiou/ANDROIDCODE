package com.smartism.znzk.activity.common;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.alert.AudioSettingActivity;
import com.smartism.znzk.activity.user.GenstureInitActivity;
import com.smartism.znzk.activity.user.GenstureSettingActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;

public class SettingActivity extends ActivityParentActivity {
    private TextView tip_unit, tip_sort, tv_gensture_status;
    private LinearLayout rl_setting_gensture, rl_setting_devices_audio, rl_setting_message_audio, rl_notice_center, rl_temprature;
    private long zhuji_id;
    private ZhujiInfo zhuji;
    private ImageView temp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        zhuji_id = getIntent().getLongExtra("zhuji_Id", 0);
        zhuji = DatabaseOperator.getInstance(SettingActivity.this).queryDeviceZhuJiInfo(zhuji_id);
        rl_temprature = (LinearLayout) findViewById(R.id.rl_temprature);
        if (Actions.VersionType.CHANNEL_ZHZJ.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                || Actions.VersionType.CHANNEL_CHUANGAN.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                || Actions.VersionType.CHANNEL_UCTECH.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
            rl_temprature.setVisibility(View.VISIBLE);
        }
        tip_unit = (TextView) findViewById(R.id.tips_shoutemperature);
        tip_sort = (TextView) findViewById(R.id.tips_dlistsort);
        rl_setting_gensture = (LinearLayout) findViewById(R.id.rl_setting_gensture);
        if (MainApplication.app.getAppGlobalConfig().isSupportGestures()) {
            rl_setting_gensture.setVisibility(View.VISIBLE);
        }
        rl_setting_devices_audio = (LinearLayout) findViewById(R.id.rl_setting_devices_audio);
        rl_setting_message_audio = (LinearLayout) findViewById(R.id.rl_setting_message_audio);
        tv_gensture_status = (TextView) findViewById(R.id.tv_gensture_status);
        rl_notice_center = (LinearLayout) findViewById(R.id.rl_notice_center);
        if (zhuji != null && zhuji.isAdmin()) {
            if (zhuji.getAc() > 0)
                rl_notice_center.setVisibility(View.VISIBLE);
        }

        temp = (ImageView) findViewById(R.id.temp);
        if (dcsp.getString(Constant.SHOW_TEMPERATURE_UNIT, "ssd").equals("ssd")) {
//            tip_unit.setText("℃");
            temp.setImageResource(R.drawable.zhzj_sz_c);

        } else if (dcsp.getString(Constant.SHOW_TEMPERATURE_UNIT, "ssd").equals("hsd")) {
//            tip_unit.setText("℉");
            temp.setImageResource(R.drawable.zhzj_sz_f);
        }


        temp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dcsp.getString(Constant.SHOW_TEMPERATURE_UNIT, "ssd").equals("ssd")) {
                    temp.setImageResource(R.drawable.zhzj_sz_f);
                    dcsp.putString(Constant.SHOW_TEMPERATURE_UNIT, "hsd").commit();
                } else {
                    dcsp.putString(Constant.SHOW_TEMPERATURE_UNIT, "ssd").commit();
                    temp.setImageResource(R.drawable.zhzj_sz_c);
                }
            }
        });


        // 跳转到手势密码打开关闭界面
        rl_setting_gensture.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                String patternString = dcsp.getString(Constant.CODE_GENSTURE, "");
                if (TextUtils.isEmpty(patternString)) {
                    startActivity(new Intent(getApplicationContext(), GenstureInitActivity.class));
                } else {
                    startActivity(new Intent(getApplicationContext(), GenstureSettingActivity.class));
                }
            }
        });


        // 主机状态开关
//        btn_toogle_online_status.setCheckedImmediatelyNoEvent(dcsp.getBoolean(Constant.IS_SERVER_STATUS_NOTIFY, false));
//        btn_toogle_online_status.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//
//                dcsp.putBoolean(Constant.IS_SERVER_STATUS_NOTIFY, isChecked).commit();
//            }
//        });


        //设置设备提示音
        rl_setting_devices_audio.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("mode", "device");
                intent.setClass(SettingActivity.this, AudioSettingActivity.class);
                startActivity(intent);
            }
        });

        //设置短信提示音
        rl_setting_message_audio.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("mode", "message");
                intent.setClass(SettingActivity.this, AudioSettingActivity.class);
                startActivity(intent);
            }
        });
        initData();
    }

    private void initData() {
    }

    /**
     * 设置温度显示单位
     *
     * @param v
     */
    public void setTemperatureShowType(View v) {
        new AlertDialog.Builder(SettingActivity.this).setTitle(getString(R.string.setting_activity_select_unit))
                .setSingleChoiceItems(new String[]{"℃", "℉"},
                        dcsp.getString(Constant.SHOW_TEMPERATURE_UNIT, "ssd").equals("ssd") ? 0 : 1,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    tip_unit.setText("℃");
                                    dcsp.putString(Constant.SHOW_TEMPERATURE_UNIT, "ssd").commit();
                                } else if (which == 1) {
                                    tip_unit.setText("℉");
                                    dcsp.putString(Constant.SHOW_TEMPERATURE_UNIT, "hsd").commit();
                                }
                                Intent intent = new Intent();
                                intent.setAction(Actions.REFRESH_DEVICES_LIST); // 发送一个广播刷新页面
                                SettingActivity.this.sendBroadcast(intent);
                                dialog.dismiss();
                            }
                        })
                .setNegativeButton(getString(R.string.setting_activity_cancel), null).show();
    }


    /**
     * 报警中心设置
     *
     * @param view
     */
    public void setNotice(View view) {
        Intent intent = new Intent();
        intent.setClass(this, NoticeCenterActivity.class);
        intent.putExtra("zhuji_id", zhuji_id);
        startActivity(intent);
    }


    /**
     * 设置设备列表排序方式
     *
     * @param v
     */
    public void setDeviceSortShowType(View v) {

        startActivity(new Intent(this, DeviceSortStyleActivity.class));
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (dcsp.getString(Constant.SHOW_DLISTSORT, "zhineng").equals("zhineng")) {
            tip_sort.setText(getString(R.string.setting_activity_zhineng));
        } else {
            tip_sort.setText(getString(R.string.setting_activity_addsort));
        }

        String patternString = dcsp.getString(Constant.CODE_GENSTURE, "");
        if (TextUtils.isEmpty(patternString)  /*patternString == null*/) {

            runOnUiThread(new Runnable() {
                public void run() {
                    tv_gensture_status.setText(R.string.gesture_unInit);
                }
            });
        } else {
            dcsp = DataCenterSharedPreferences.getInstance(SettingActivity.this,
                    Constant.CONFIG);
            runOnUiThread(new Runnable() {
                public void run() {

                    //tv_gensture_status.setText(dcsp.getBoolean(Constant.IS_APP_GENSTURE, false) ? (R。string。gensture_open) : (R。string。gensture_off));

                    if (dcsp.getBoolean(Constant.IS_APP_GENSTURE, false)) {
                        tv_gensture_status.setText(R.string.gensture_open);
                    } else {
                        tv_gensture_status.setText(R.string.gensture_off);
                    }
                }
            });
        }

    }

    public void back(View v) {
        finish();
    }

}
