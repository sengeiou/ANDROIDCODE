package com.smartism.znzk.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.just.agentweb.AgentWeb;
import com.smartism.znzk.R;
import com.smartism.znzk.util.LanguageUtil;

public class DisclaimerActivity extends AppCompatActivity {
    private AgentWeb mAgentWeb;
    private LinearLayout mAgentWebLayout;
    String url = null;
    private String mTitleStr;
    private TextView mTitleTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disclaimer);
        if (savedInstanceState == null) {
            mTitleStr = getIntent().getStringExtra("title");
            url = getIntent().getStringExtra("url");
        } else {
            mTitleStr = savedInstanceState.getString("title");
            url = savedInstanceState.getString("url");
        }
        mAgentWebLayout = (LinearLayout) findViewById(R.id.main_webview_layout);
        mTitleTv = findViewById(R.id.title_tv);
        if (!TextUtils.isEmpty(mTitleStr)) {
            mTitleTv.setText(mTitleStr);
        }
        if (TextUtils.isEmpty(url)) {
            if (LanguageUtil.isZh(this)) {
                url = "file:///android_asset/new_text_cn.html";
            }else {
                url = "file:///android_asset/new_text_en.html";
            }
        }

        mAgentWeb = AgentWeb.with(this)
                .setAgentWebParent(mAgentWebLayout, new LinearLayout.LayoutParams(-1, -1))
                .useDefaultIndicator()
                .interceptUnkownUrl()
                .createAgentWeb()
                .ready()
                .go(url);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("url", url);
        super.onSaveInstanceState(outState);
    }

    public void back(View v) {
        finish();
    }
}
