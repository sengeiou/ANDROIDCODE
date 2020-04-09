package com.smartism.znzk.activity.device.add;


import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.smartism.znzk.R;
import com.smartism.znzk.zhicheng.activities.MZBaseActivity;
/*
* 短信配网
* */
public class GSMDistributionNetworkActivity extends MZBaseActivity implements View.OnClickListener {

    public static final int SMS_DISTRIBUTION_FLAG = 0x343 ;

    private TextView mNoFindTextView;
    private Button mSendSmsBtn,mNextBtn;
    private EditText mNumberEdit ;
    private String mSsid,mPassword;
    private int mNetId ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState==null){
            mSsid = getIntent().getStringExtra("ssid");
            mPassword = getIntent().getStringExtra("password");
            mNetId = getIntent().getIntExtra("net_id",-1);
        }else{
            mSsid = savedInstanceState.getString("ssid");
            mPassword = savedInstanceState.getString("password");
            mNetId = savedInstanceState.getInt("net_id");
        }
        setTitle(getResources().getString(R.string.zhuji_perwang_sms_title));
        bindView();
        bindEvent();
        bindData();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("ssid",mSsid);
        outState.putString("password",mPassword);
        outState.putInt("net_id",mNetId);
        super.onSaveInstanceState(outState);
    }

    private void bindView(){
        mNoFindTextView = findViewById(R.id.no_find_tv);
        mNumberEdit = findViewById(R.id.gsm_number_edit);
        mSendSmsBtn = findViewById(R.id.send_sms_btn);
        mNextBtn = findViewById(R.id.next_btn);
    }
    private void bindEvent(){
        mNoFindTextView.setOnClickListener(this);
        mNextBtn.setOnClickListener(this);
        mSendSmsBtn.setOnClickListener(this);
    }

    private void bindData(){
        mSendSmsBtn.setEnabled(false);
        mNoFindTextView.setText(Html.fromHtml(getString(R.string.gsmdna_sms_no_find_tip)));
        mNumberEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //电话号码11位
                if(s.length()==11){
                    mSendSmsBtn.setEnabled(true);
                    mSendSmsBtn.setBackgroundResource(R.drawable.zhzj_default_button);
                }else{
                    mSendSmsBtn.setEnabled(false);
                    mSendSmsBtn.setBackgroundColor(Color.GRAY);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public int setLayoutId() {
        return R.layout.activity_gsmdistribution_network;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()){
            case R.id.no_find_tv:
                //没有看到蓝灯长亮
                intent.setClass(getApplicationContext(),AddZhujiByGsmFailureActivity.class);
                startActivity(intent);
                break ;
            case R.id.send_sms_btn:
                //发送短信
                StringBuilder sb = new StringBuilder();
                sb.append("WiFi:")
                        .append("\"")
                        .append(mSsid)
                        .append("\"")
                        .append(",")
                        .append("\"")
                        .append(mPassword)
                        .append("\"");
                intent.setAction(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("smsto:"+mNumberEdit.getText().toString()));
                intent.putExtra("sms_body",sb.toString());
                startActivity(intent);
                break ;
            case R.id.next_btn:
                //搜索主机
                intent.setClass(getApplicationContext(),ConnectActivity.class);
                intent.putExtra("peiwang_flag",SMS_DISTRIBUTION_FLAG);
                startActivity(intent);
                break ;
        }
    }
}
