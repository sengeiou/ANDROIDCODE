package com.smartism.znzk.zhicheng.activities;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.smartism.znzk.R;

/*
* autho mz
* */
public class ZCInfraredModeSelectionActivity extends MZBaseActivity implements View.OnClickListener{

    Button telligentBtn,addTypeBtn ;
    long deviceId  ,zhujiID;
    String deviceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState==null){
            deviceId = getIntent().getLongExtra("did",0);
            deviceName = getIntent().getStringExtra("deviceName");
            zhujiID = getIntent().getLongExtra("zhujiID",0);

        }else{
            deviceId = savedInstanceState.getLong("did");
            deviceName = savedInstanceState.getString("deviceName");
            zhujiID = savedInstanceState.getLong("zhujiID");
        }
        setTitle(deviceName);
        telligentBtn = findViewById(R.id.telligent_btn);
        addTypeBtn = findViewById(R.id.type_add_btn);

        telligentBtn.setOnClickListener(this);
        addTypeBtn.setOnClickListener(this);


    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong("did",deviceId);
        outState.putString("deviceName",deviceName);
        outState.putLong("zhujiID",zhujiID);
        super.onSaveInstanceState(outState);
    }

    @Override
    public int setLayoutId() {
        return R.layout.activity_zc_infrared_mode_selection_layout;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        intent.putExtra("did",deviceId);
        intent.putExtra("zhujiID",zhujiID);
        intent.putExtra("deviceName",deviceName);
        switch (v.getId()){
            case R.id.telligent_btn:
                intent.setClass(this,ZCSmartMatchActivity.class);
                break;
            case R.id.type_add_btn:
                intent.setClass(this,ZCBrandDisplayActivity.class);
                break;
        }
        startActivity(intent);

    }
}
