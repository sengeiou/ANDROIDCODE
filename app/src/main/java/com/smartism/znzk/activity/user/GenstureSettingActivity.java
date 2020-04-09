package com.smartism.znzk.activity.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;

import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.view.LockSetupActivity;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.view.SwitchButton.SwitchButton;

public class GenstureSettingActivity extends ActivityParentActivity {

    protected DataCenterSharedPreferences dcsp = null;
    //	CheckSwitchButton btn_toogle_gensture;
    SwitchButton btn_toogle_gensture;
    RelativeLayout rl_resetting_gensture;

    private static final String TAG_MAIN = "MainActivity";
    public static final String LOCK = "lock";
    public static final String LOCK_KEY = "lock_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initValue();
        initEvent();
    }

    private void initView() {
        setContentView(R.layout.activity_gensture_setting);
        btn_toogle_gensture = (SwitchButton) findViewById(R.id.btn_toogle_gensture);

        rl_resetting_gensture = (RelativeLayout) findViewById(R.id.rl_resetting_gensture);

    }

    private void initValue() {
        dcsp = DataCenterSharedPreferences.getInstance(GenstureSettingActivity.this,
                Constant.CONFIG);
        btn_toogle_gensture.setChecked(dcsp.getBoolean(Constant.IS_APP_GENSTURE, false));
        rl_resetting_gensture.setVisibility(dcsp.getBoolean(Constant.IS_APP_GENSTURE, false) ? (View.VISIBLE) : (View.GONE));
    }

    private void initEvent() {
        btn_toogle_gensture.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                dcsp.putBoolean(Constant.IS_APP_GENSTURE, isChecked).commit();
                dcsp.putBoolean(Constant.IS_LOOKS, false).commit();
                // app退到后台时候 Locks为true，解锁重新进来的时候重置
                if (isChecked) {
                    rl_resetting_gensture.setVisibility(View.VISIBLE);
                } else {
                    rl_resetting_gensture.setVisibility(View.GONE);
                }
            }
        });

        rl_resetting_gensture.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(GenstureSettingActivity.this, LockSetupActivity.class));
            }
        });
    }

    public void back(View v) {
        finish();
    }
}
