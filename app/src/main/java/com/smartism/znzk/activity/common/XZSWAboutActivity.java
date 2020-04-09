package com.smartism.znzk.activity.common;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.util.Actions;

public class XZSWAboutActivity extends ActivityParentActivity implements OnClickListener {

    private TextView version;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Actions.VersionType.CHANNEL_ANBABAOQUAN.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
            setContentView(R.layout.activity_about_abbq);
            initView();
        }else if(Actions.VersionType.CHANNEL_RUNLONG.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            setContentView(R.layout.activity_about_runlong);
        }else {
            setContentView(R.layout.activity_about_xzsw);
        }

    }

    public void back(View v) {
        finish();
    }


    private void initView() {
        version = (TextView) findViewById(R.id.version);
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version.setText("V" + packageInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
        }
    }
}
