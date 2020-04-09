package com.smartism.znzk.activity.common;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.CommonWebViewActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.util.Actions.VersionType;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.LanguageUtil;

public class AboutActivity extends ActivityParentActivity implements OnClickListener {
    private TextView name, version, about_copyright, about_url;
    private ImageView logo;
    private int a = 0;
    private LinearLayout about_layout1, about_layout2, about_layout3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        initView();
    }

    public void back(View v) {
        finish();
    }


    private void initView() {
        about_url = (TextView) findViewById(R.id.about_url);
        name = (TextView) findViewById(R.id.about_appname);
        name.setText(getApplicationInfo().labelRes);
        version = (TextView) findViewById(R.id.about_version);
        about_copyright = (TextView) findViewById(R.id.about_copyright);
        logo = (ImageView) findViewById(R.id.imageView1);
        if (VersionType.CHANNEL_ZHZJ.equals(getJdmApplication().getAppGlobalConfig().getVersion())) {
            about_copyright.setVisibility(View.VISIBLE);
        } else if (MainApplication.app.getAppGlobalConfig().getVersion().equals(VersionType.CHANNEL_AIERFUDE)) {
            version.setVisibility(View.GONE);
            about_copyright.setVisibility(View.GONE);
            about_url.setVisibility(View.GONE);
            name.setVisibility(View.GONE);
        } else {
            about_copyright.setVisibility(View.GONE);
            if (VersionType.CHANNEL_JUJIANG.equals(getJdmApplication().getAppGlobalConfig().getVersion())) {
                logo.setOnClickListener(this);
                if (LanguageUtil.isZh(this)) {
                    about_url.setText(Html.fromHtml("<u>" + "www.x-house.net" + "</u>"));
                } else {
                    about_url.setText(Html.fromHtml("<u>" + "www.x-house.net" + "</u>"));
                }
            }

        }
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version.setText("V" + packageInfo.versionName);
        } catch (NameNotFoundException e) {

        }
        about_url.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.about_url:
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), CommonWebViewActivity.class);
                startActivity(intent);
                break;
            case R.id.imageView1:
                a++;
                if (a > 3) {
                    if (dcsp.getBoolean(DataCenterSharedPreferences.Constant.IS_SUPORT_STU, false)) {
                        if (a > 5) {
                            displayToast(String.format(getString(R.string.activity_about_closesuportstu), 10 - a));
                        } else {
                            displayToast(getString(R.string.activity_about_suportstu_open));
                        }
                    } else {
                        displayToast(String.format(getString(R.string.activity_about_suportstu), 8 - a));
                    }
                }
                if (dcsp.getBoolean(DataCenterSharedPreferences.Constant.IS_SUPORT_STU, false)) {
                    if (10 - a <= 0) {
                        displayToast(getString(R.string.activity_about_suportstu_close));
                        dcsp.putBoolean(DataCenterSharedPreferences.Constant.IS_SUPORT_STU, false).commit();
                        a = 4;
                    }
                } else {
                    if (8 - a <= 0) {
                        displayToast(getString(R.string.activity_about_suportstu_open));
                        dcsp.putBoolean(DataCenterSharedPreferences.Constant.IS_SUPORT_STU, true).commit();
                        a = 4;
                    }
                }
                break;
            default:
                break;
        }
    }

//    public void openWeb(String url) {
//        Intent intent = new Intent();
//        intent.setAction("android.intent.action.VIEW");
//        Uri content_url = Uri.parse(url);
//        intent.setData(content_url);
//        startActivity(intent);
//    }

    Toast mytoast = null;

    public void displayToast(String str) {
        if (mytoast == null)
            mytoast = Toast.makeText(this, str, Toast.LENGTH_SHORT);
        else
            mytoast.setText(str);
        mytoast.show();
    }
}
