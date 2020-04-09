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

public class WSDTimeSetActivity extends ActivityParentActivity implements View.OnClickListener {

    private RelativeLayout rl_0, rl_5, rl_15, rl_30, rl_60, rl_120, rl_240;
    private ImageViewCheckable iv_0, iv_5, iv_15, iv_30, iv_60, iv_120, iv_240;
    public final int dHandler_timeout = 10;
    private ZhujiInfo zhujiInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wsd_set_time);
        initView();

        zhujiInfo = (ZhujiInfo) getIntent().getSerializableExtra("zhuji");
        tag = getIntent().getIntExtra("time", 60);
        initData();
    }

    private void initData() {
        if (tag == 0) {
            iv_0.setChecked(true);
        } else if (tag == 5) {
            iv_5.setChecked(true);
        } else if (tag == 15) {
            iv_15.setChecked(true);
        } else if (tag == 30) {
            iv_30.setChecked(true);
        } else if (tag == 60) {
            iv_60.setChecked(true);
        } else if (tag == 120) {
            iv_120.setChecked(true);
        } else if (tag == 240) {
            iv_240.setChecked(true);
        }
    }

    private void initView() {
        iv_0 = (ImageViewCheckable) findViewById(R.id.iv_0);
        iv_5 = (ImageViewCheckable) findViewById(R.id.iv_5);
        iv_15 = (ImageViewCheckable) findViewById(R.id.iv_15);
        iv_30 = (ImageViewCheckable) findViewById(R.id.iv_30);
        iv_60 = (ImageViewCheckable) findViewById(R.id.iv_60);
        iv_120 = (ImageViewCheckable) findViewById(R.id.iv_120);
        iv_240 = (ImageViewCheckable) findViewById(R.id.iv_240);

        rl_0 = (RelativeLayout) findViewById(R.id.rl_0);
        rl_5 = (RelativeLayout) findViewById(R.id.rl_5);
        rl_15 = (RelativeLayout) findViewById(R.id.rl_15);
        rl_30 = (RelativeLayout) findViewById(R.id.rl_30);
        rl_60 = (RelativeLayout) findViewById(R.id.rl_60);
        rl_120 = (RelativeLayout) findViewById(R.id.rl_120);
        rl_240 = (RelativeLayout) findViewById(R.id.rl_240);


        rl_0.setOnClickListener(this);
        rl_5.setOnClickListener(this);
        rl_15.setOnClickListener(this);
        rl_30.setOnClickListener(this);
        rl_60.setOnClickListener(this);
        rl_120.setOnClickListener(this);
        rl_240.setOnClickListener(this);

//        iv_0.setOnClickListener(this);
//        iv_5.setOnClickListener(this);
//        iv_15.setOnClickListener(this);
//        iv_30.setOnClickListener(this);
//        iv_60.setOnClickListener(this);
//        iv_120.setOnClickListener(this);
//        iv_240.setOnClickListener(this);

        views = new ImageViewCheckable[]{iv_0, iv_5, iv_15, iv_30, iv_60, iv_120, iv_240};
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
                object.put("vkey", "interval_wsdj");
                object.put("value", time + "");
                array.add(object);
                pJsonObject.put("vkeys", array);

                String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/d/p/set", pJsonObject, WSDTimeSetActivity.this);
                if ("-3".equals(result)) {
                    if (mHandler.hasMessages(dHandler_timeout)) {
                        mHandler.removeMessages(dHandler_timeout);
                    }
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(WSDTimeSetActivity.this, getString(R.string.net_error_nodata),
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
                            Toast.makeText(WSDTimeSetActivity.this, getString(R.string.device_not_getdata),
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
                            Toast.makeText(WSDTimeSetActivity.this, getString(R.string.success),
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
                            Toast.makeText(WSDTimeSetActivity.this, getString(R.string.net_error),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    private int tag = 60;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_0:
                setTime(iv_0);
                tag = 0;
                break;
            case R.id.rl_5:
                setTime(iv_5);
                tag = 5;
                break;
            case R.id.rl_15:
                setTime(iv_15);
                tag = 15;
                break;
            case R.id.rl_30:
                setTime(iv_30);
                tag = 30;
                break;
            case R.id.rl_60:
                setTime(iv_60);
                tag = 60;
                break;
            case R.id.rl_120:
                setTime(iv_120);
                tag = 120;
                break;
            case R.id.rl_240:
                setTime(iv_240);
                tag = 240;
                break;
        }
    }

    ImageViewCheckable[] views;

    private void setTime(ImageViewCheckable view) {
//        int[] ids =new int[]{R.id.iv_0,R.id.iv_5};
        for (int i = 0; i < views.length; i++) {
            if (view == views[i]) {
                views[i].setChecked(true);
            } else {
                views[i].setChecked(false);
            }
        }

    }
}
