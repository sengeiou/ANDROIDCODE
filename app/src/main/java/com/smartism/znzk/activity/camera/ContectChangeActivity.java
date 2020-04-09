package com.smartism.znzk.activity.camera;

import android.app.ActivityGroup;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

import com.jwkj.soundwave.SoundWaveManager;
import com.smartism.znzk.R;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.domain.camera.LocalDevice;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.view.zbarscan.ScanCaptureActivity;

import java.util.List;

/**
 * 设置不同的content来展示不同的页面/添加设备
 *
 * @author 2016年08月05日 update 王建
 */
@SuppressWarnings("deprecation")
public class ContectChangeActivity extends ActivityGroup {
    private TabHost mTabHost;
    private ImageView back;
    private String device;
    private int i;//3、技威，5、v380
    private int isCameraList;//判断是否是返回摄像头列表还是返回设备列表 0、返回设备列表，1、返回摄像头列表
    private ImageView scand;
    private boolean isFirstRefresh = true;
    private List<LocalDevice> localDevices;

    @Override
    protected void onCreate(Bundle arg0) {
        // TODO Auto-generated method stub
        super.onCreate(arg0);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_change_context);
        boolean isSuccess = SoundWaveManager.init(this);//初始化声波配置

        scand = (ImageView) findViewById(R.id.add_camera_scand);
        i = getIntent().getIntExtra("int", 0);
        isCameraList = getIntent().getIntExtra("isCameraList", 0);

        back = (ImageView) findViewById(R.id.back_btn);
        back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // 设置TabHost
        initTabs();
        if (Actions.VersionType.CHANNEL_JUJIANG.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
            scand.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
//                    if (localDevices!=null&&!localDevices.isEmpty()){
                    Intent intent = new Intent();
                    intent.setClass(ContectChangeActivity.this, ScanCaptureActivity.class);
                    intent.putExtra("int", i);
                    intent.putExtra("isCameraList", isCameraList);
                    startActivity(intent);
                    finish();
                }
            });
//            regFilter();

        } else {
            scand.setVisibility(View.GONE);

        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
        isFirstRefresh = true;
    }

    private void initTabs() {
        mTabHost = (TabHost) findViewById(R.id.tabhost);

        mTabHost.setup(this.getLocalActivityManager());
        // 添加日志列表的tab,注意下面的setContent中的代码.是这个需求实现的关键
        Intent intent = new Intent();
        intent.setClass(this, RadarAddActivity.class);
        intent.putExtra("int", i);
        intent.putExtra("isCameraList", isCameraList);
        mTabHost.addTab(mTabHost
                .newTabSpec("tab_log")
                .setIndicator(getString(R.string.radar_add),
                        getResources().getDrawable(R.drawable.zhzj_icom_radar))
                .setContent(new Intent(intent)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));

        // 添加应用设置的tab,注意下面的setContent中的代码.是这个需求实现的关键
        final Intent in = new Intent();
        in.setClass(this, AddContactActivity.class);
        intent.putExtra("int", i);
        intent.putExtra("isCameraList", isCameraList);
        mTabHost.addTab(mTabHost
                .newTabSpec("tab_setting")
                .setIndicator(getString(R.string.manually_add),
                        getResources().getDrawable(R.drawable.zhzj_icon_manually_n))
                .setContent(new Intent(in)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
        mTabHost.setCurrentTab(0);
        updateTabBackground(mTabHost);
        mTabHost.setOnTabChangedListener(new OnTabChangeListener() {

            @Override
            public void onTabChanged(String tabId) {
                updateTabBackground(mTabHost);
                if (tabId.equals("tab_log")) {

                }
            }
        });

    }

    public void updateTabBackground(final TabHost tabHost) {
        tabHost.getTabWidget().getChildAt(0).setBackgroundColor(Color.WHITE);
        tabHost.getTabWidget().getChildAt(1).setBackgroundColor(Color.WHITE);
        TextView tv0 = (TextView) tabHost.getTabWidget().getChildAt(0).findViewById(android.R.id.title);
        ImageView img0 = (ImageView) tabHost.getTabWidget().getChildAt(0).findViewById(android.R.id.icon);
        TextView tv1 = (TextView) tabHost.getTabWidget().getChildAt(1).findViewById(android.R.id.title);
        ImageView img1 = (ImageView) tabHost.getTabWidget().getChildAt(1).findViewById(android.R.id.icon);
        if (tabHost.getCurrentTab() == 0) {
            tv0.setTextColor(getResources().getColor(R.color.tabhost_title_color_choice_y));
            img0.setImageDrawable(getResources().getDrawable(R.drawable.zhzj_icom_radar));
            tv1.setTextColor(getResources().getColor(R.color.tabhost_title_color_choice_n));
            img1.setImageDrawable(getResources().getDrawable(R.drawable.zhzj_icon_manually_n));

        } else {
            tv0.setTextColor(getResources().getColor(R.color.tabhost_title_color_choice_n));
            img0.setImageDrawable(getResources().getDrawable(R.drawable.zhzj_icom_radar_n));
            tv1.setTextColor(getResources().getColor(R.color.tabhost_title_color_choice_y));
            img1.setImageDrawable(getResources().getDrawable(R.drawable.zhzj_icon_manually));
        }
    }

    boolean isRegFilter = false;

    @Override
    protected void onDestroy() {
        SoundWaveManager.onDestroy(this);
        super.onDestroy();
    }
}
