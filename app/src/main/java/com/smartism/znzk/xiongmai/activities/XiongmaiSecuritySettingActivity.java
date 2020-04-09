package com.smartism.znzk.xiongmai.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.smartism.znzk.R;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.camera.CameraInfo;
import com.smartism.znzk.zhicheng.activities.MZBaseActivity;

public class XiongmaiSecuritySettingActivity extends MZBaseActivity implements View.OnClickListener{

    private LinearLayout mPasswordSettingLayout ;

    private long mDeviceId ;
    private CameraInfo mCameraInfo ;
    private String sn ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState==null){
            mDeviceId = getIntent().getLongExtra("device_id",0);
            mCameraInfo = (CameraInfo) getIntent().getSerializableExtra("camera_info");
            sn = getIntent().getStringExtra("sn");
        }else{
            mDeviceId = savedInstanceState.getLong("device_id");
            mCameraInfo = (CameraInfo) savedInstanceState.getSerializable("camera_info");
            sn = savedInstanceState.getString("sn");
        }
        setTitle(getString(R.string.security_set));
        initView();
        initEvent();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("sn",sn);
        outState.putSerializable("camera_info",mCameraInfo);
        outState.putLong("device_id",mDeviceId);
        super.onSaveInstanceState(outState);
    }

    private void initView(){
        mPasswordSettingLayout = findViewById(R.id.ll_password_setting);
    }

    private void initEvent(){
        mPasswordSettingLayout.setOnClickListener(this);
    }

    @Override
    public int setLayoutId() {
        return R.layout.activity_xiongmai_security_setting;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ll_password_setting:
                Intent intent =new Intent();
                intent.setClass(this,XiongmaiPasswordModifyActivity.class);
                intent.putExtra("camera_info",mCameraInfo);
                intent.putExtra("device_id",mDeviceId);
                startActivity(intent);
                break ;
        }
    }
}
