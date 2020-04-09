package com.smartism.znzk.activity.common;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.view.ImageViewCheckable;

/**
 * Created by win7 on 2017/6/20.
 */

public class CLDTimeSetActivity extends ActivityParentActivity implements View.OnClickListener {

    private RelativeLayout rl_0,rl_1, rl_5, rl_2, rl_3, rl_4, rl_6,rl_7,rl_8,rl_9,rl_10,rl_11;
    private ImageViewCheckable iv_0,iv_1, iv_5, iv_2, iv_3, iv_4, iv_6,iv_7,iv_8,iv_9,iv_10,iv_11;
    public final int dHandler_timeout = 10;
    private ZhujiInfo zhujiInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cld_set_time);
        initView();

        zhujiInfo = (ZhujiInfo) getIntent().getSerializableExtra("zhuji");
        tag = getIntent().getIntExtra("time", 0);
        initData();
    }

    private void initData() {
        if (tag == 0) {
            iv_0.setChecked(true);
        }else if (tag == 15) {
            iv_1.setChecked(true);
        } else if (tag == 75) {
            iv_5.setChecked(true);
        } else if (tag == 30) {
            iv_2.setChecked(true);
        } else if (tag == 45) {
            iv_3.setChecked(true);
        } else if (tag == 60) {
            iv_4.setChecked(true);
        } else if (tag == 5) {
            iv_6.setChecked(true);
        }else if(tag==90){
            iv_7.setChecked(true);
        }else if(tag==105){
            iv_8.setChecked(true);
        }else if(tag==120){
            iv_9.setChecked(true);
        }else if(tag==135){
            iv_10.setChecked(true);
        }else if(tag==150){
            iv_11.setChecked(true);
        }
    }

    private void initView() {
        iv_0 = (ImageViewCheckable) findViewById(R.id.iv_0);
        iv_1 = (ImageViewCheckable) findViewById(R.id.iv_1);
        iv_5 = (ImageViewCheckable) findViewById(R.id.iv_5);
        iv_2 = (ImageViewCheckable) findViewById(R.id.iv_2);
        iv_3 = (ImageViewCheckable) findViewById(R.id.iv_3);
        iv_4 = (ImageViewCheckable) findViewById(R.id.iv_4);
        iv_6 = (ImageViewCheckable) findViewById(R.id.iv_6);
        iv_7 = (ImageViewCheckable) findViewById(R.id.iv_7);
        iv_8 = (ImageViewCheckable) findViewById(R.id.iv_8);
        iv_9 = (ImageViewCheckable) findViewById(R.id.iv_9);
        iv_10 = (ImageViewCheckable) findViewById(R.id.iv_10);
        iv_11 = (ImageViewCheckable) findViewById(R.id.iv_11);

        rl_0 = (RelativeLayout) findViewById(R.id.rl_0);
        rl_1 = (RelativeLayout) findViewById(R.id.rl_1);
        rl_5 = (RelativeLayout) findViewById(R.id.rl_5);
        rl_2 = (RelativeLayout) findViewById(R.id.rl_2);
        rl_3 = (RelativeLayout) findViewById(R.id.rl_3);
        rl_4 = (RelativeLayout) findViewById(R.id.rl_4);
        rl_6 = (RelativeLayout) findViewById(R.id.rl_6);
        rl_7 = (RelativeLayout) findViewById(R.id.rl_7);
        rl_8 = (RelativeLayout) findViewById(R.id.rl_8);
        rl_9 = (RelativeLayout) findViewById(R.id.rl_9);
        rl_10 = (RelativeLayout) findViewById(R.id.rl_10);
        rl_11 = (RelativeLayout) findViewById(R.id.rl_11);


        rl_0.setOnClickListener(this);
        rl_1.setOnClickListener(this);
        rl_5.setOnClickListener(this);
        rl_2.setOnClickListener(this);
        rl_3.setOnClickListener(this);
        rl_4.setOnClickListener(this);
        rl_6.setOnClickListener(this);
        rl_7.setOnClickListener(this);
        rl_8.setOnClickListener(this);
        rl_9.setOnClickListener(this);
        rl_10.setOnClickListener(this);
        rl_11.setOnClickListener(this);


        views = new ImageViewCheckable[]{iv_0,iv_1, iv_5, iv_2, iv_3, iv_4, iv_6,iv_7, iv_8, iv_9, iv_10,iv_11};
    }

    public void sure(View v) {
        setAcceptTime(tag);
    }

    public void back(View v) {
        finish();
    }

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case dHandler_timeout:
                    cancelInProgress();
                    Toast.makeText(getApplicationContext(), getString(R.string.timeout), Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            return false;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);

    private void setAcceptTime(final int time) {
        showInProgress(getString(R.string.loading), false, true);
        mHandler.sendEmptyMessageDelayed(dHandler_timeout, 10 * 1000);
        JavaThreadPool.getInstance().excute(new Runnable() {
            @Override
            public void run() {
                String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                JSONObject pJsonObject = new JSONObject();
                pJsonObject.put("did", zhujiInfo != null ? zhujiInfo.getId() : 0);
                JSONArray array = new JSONArray();
                JSONObject object = new JSONObject();
                object.put("vkey", "108");
                object.put("value", time + "");
                array.add(object);
                pJsonObject.put("vkeys", array);

                String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/d/p/set", pJsonObject, CLDTimeSetActivity.this);
                if ("-3".equals(result)) {
                    if (mHandler.hasMessages(dHandler_timeout)) {
                        mHandler.removeMessages(dHandler_timeout);
                    }
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(CLDTimeSetActivity.this, getString(R.string.net_error_nodata),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-5".equals(result)) {
                    if (mHandler.hasMessages(dHandler_timeout)) {
                        mHandler.removeMessages(dHandler_timeout);
                    }
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(CLDTimeSetActivity.this, getString(R.string.device_not_getdata),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("0".equals(result)) {
                    if (mHandler.hasMessages(dHandler_timeout)) {
                        mHandler.removeMessages(dHandler_timeout);
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(CLDTimeSetActivity.this, getString(R.string.success),
                                    Toast.LENGTH_LONG).show();
                            Intent in = getIntent();
                            in.putExtra("time", tag + "");
                            setResult(10, in);
                            finish();
                        }
                    });

                } else {
                    if (mHandler.hasMessages(dHandler_timeout)) {
                        mHandler.removeMessages(dHandler_timeout);
                    }
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(CLDTimeSetActivity.this, getString(R.string.net_error),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    private int tag = 0;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_0:
                setTime(iv_0);
                tag = 0;
                break;
            case R.id.rl_1:
                setTime(iv_1);
                tag = 15;
                break;
            case R.id.rl_5:
                setTime(iv_5);
                tag = 75;
                break;
            case R.id.rl_2:
                setTime(iv_2);
                tag = 30;
                break;
            case R.id.rl_3:
                setTime(iv_3);
                tag = 45;
                break;
            case R.id.rl_4:
                setTime(iv_4);
                tag = 60;
                break;
            case R.id.rl_6:
                setTime(iv_6);
                tag = 5;
                break;

            case R.id.rl_7:
                setTime(iv_7);
                tag = 90;
                break;
            case R.id.rl_8:
                setTime(iv_8);
                tag = 105;
                break;
            case R.id.rl_9:
                setTime(iv_9);
                tag = 120;
                break;
            case R.id.rl_10:
                setTime(iv_10);
                tag = 135;
                break;
            case R.id.rl_11:
                setTime(iv_11);
                tag = 150;
                break;
        }
    }

    ImageViewCheckable[] views;

    private void setTime(ImageViewCheckable view) {
//        int[] ids =new int[]{R.id.iv_1,R.id.iv_5};
        for (int i = 0; i < views.length; i++) {
            if (view == views[i]) {
                views[i].setChecked(true);
            } else {
                views[i].setChecked(false);
            }
        }

    }
}
