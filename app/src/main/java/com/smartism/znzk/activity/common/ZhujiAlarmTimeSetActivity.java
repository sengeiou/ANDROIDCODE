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
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.view.ImageViewCheckable;

import java.util.Map;

import static com.smartism.znzk.domain.ZhujiInfo.GNSetNameMenu.batteryMargin;

/**
 * Created by win7 on 2017/6/20.
 */

public class ZhujiAlarmTimeSetActivity extends ActivityParentActivity implements View.OnClickListener {

    private RelativeLayout rl_0,rl_1, rl_5, rl_10, rl_30, rl_2, rl_20;
    private ImageViewCheckable iv_0,iv_1, iv_5, iv_10, iv_30, iv_2, iv_20;
    public final int dHandler_timeout = 10;
    private ZhujiInfo zhujiInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zhuji_alarm_set_time);
        initView();

        zhujiInfo = (ZhujiInfo) getIntent().getSerializableExtra("zhuji");
        tag = getIntent().getIntExtra("time", 5);
        initData();
    }

    private void initData() {
        Map<String, String> setInfos = DatabaseOperator.getInstance().queryZhujiSets(zhujiInfo.getId());
        if("1".equals(setInfos.get(batteryMargin.value()))){
            rl_0.setVisibility(View.VISIBLE);
        }
        if(tag==0){
            iv_0.setChecked(true);
        }else if (tag == 1) {
            iv_1.setChecked(true);
        } else if (tag == 5) {
            iv_5.setChecked(true);
        } else if (tag == 10) {
            iv_10.setChecked(true);
        } else if (tag == 30) {
            iv_30.setChecked(true);
        } else if (tag == 2) {
            iv_2.setChecked(true);
        } else if (tag == 20) {
            iv_20.setChecked(true);
        }
    }

    private void initView() {
        iv_0 = findViewById(R.id.iv_0);
        iv_1 = (ImageViewCheckable) findViewById(R.id.iv_1);
        iv_5 = (ImageViewCheckable) findViewById(R.id.iv_5);
        iv_10 = (ImageViewCheckable) findViewById(R.id.iv_10);
        iv_30 = (ImageViewCheckable) findViewById(R.id.iv_30);
        iv_2 = (ImageViewCheckable) findViewById(R.id.iv_2);
        iv_20 = (ImageViewCheckable) findViewById(R.id.iv_20);

        rl_0 = findViewById(R.id.rl_0);
        rl_1 = (RelativeLayout) findViewById(R.id.rl_1);
        rl_5 = (RelativeLayout) findViewById(R.id.rl_5);
        rl_10 = (RelativeLayout) findViewById(R.id.rl_10);
        rl_30 = (RelativeLayout) findViewById(R.id.rl_30);
        rl_2 = (RelativeLayout) findViewById(R.id.rl_2);
        rl_20 = (RelativeLayout) findViewById(R.id.rl_20);


        rl_0.setOnClickListener(this);
        rl_1.setOnClickListener(this);
        rl_5.setOnClickListener(this);
        rl_10.setOnClickListener(this);
        rl_30.setOnClickListener(this);
        rl_2.setOnClickListener(this);
        rl_20.setOnClickListener(this);

//        iv_1.setOnClickListener(this);
//        iv_5.setOnClickListener(this);
//        iv_10.setOnClickListener(this);
//        iv_30.setOnClickListener(this);
//        iv_2.setOnClickListener(this);
//        iv_20.setOnClickListener(this);
//        iv_240.setOnClickListener(this);

        views = new ImageViewCheckable[]{iv_0,iv_1, iv_5, iv_10, iv_30, iv_2, iv_20};
    }

    public void sure(View v) {
//        setAcceptTime(tag);
        setZhujiAlarmTime(tag);
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

    public void setZhujiAlarmTime(final int time) {
        showInProgress(getString(R.string.loading), false, true);
        mHandler.sendEmptyMessageDelayed(dHandler_timeout, 10 * 1000);
        JavaThreadPool.getInstance().excute(new Runnable() {
            @Override
            public void run() {
                String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                JSONObject object = new JSONObject();
                object.put("did", zhujiInfo.getId());
                JSONArray array = new JSONArray();
                JSONObject o = new JSONObject();
                o.put("vkey", CommandInfo.CommandTypeEnum.setZhujiAlarmTime.value());
                o.put("value", time);
                array.add(o);
                object.put("vkeys", array);
                String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/d/p/set", object,
                        ZhujiAlarmTimeSetActivity.this);
                if (result != null && result.equals("0")) {
                    if (mHandler.hasMessages(dHandler_timeout)) {
                        mHandler.removeMessages(dHandler_timeout);
                    }
                    mHandler.post(new Runnable() {
                        //
                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(ZhujiAlarmTimeSetActivity.this, getString(R.string.success),
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
                            Toast.makeText(ZhujiAlarmTimeSetActivity.this, getString(R.string.net_error),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }


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

                String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/u/p/set", pJsonObject, ZhujiAlarmTimeSetActivity.this);
                if ("-3".equals(result)) {
                    if (mHandler.hasMessages(dHandler_timeout)) {
                        mHandler.removeMessages(dHandler_timeout);
                    }
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(ZhujiAlarmTimeSetActivity.this, getString(R.string.net_error_nodata),
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
                            Toast.makeText(ZhujiAlarmTimeSetActivity.this, getString(R.string.device_not_getdata),
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
                            Toast.makeText(ZhujiAlarmTimeSetActivity.this, getString(R.string.success),
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
                            Toast.makeText(ZhujiAlarmTimeSetActivity.this, getString(R.string.net_error),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    private int tag = 30;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_0:
                setTime(iv_0);
                tag = 0;
                break ;
            case R.id.rl_1:
                setTime(iv_1);
                tag = 1;
                break;
            case R.id.rl_5:
                setTime(iv_5);
                tag = 5;
                break;
            case R.id.rl_10:
                setTime(iv_10);
                tag = 10;
                break;
            case R.id.rl_30:
                setTime(iv_30);
                tag = 30;
                break;
            case R.id.rl_2:
                setTime(iv_2);
                tag = 2;
                break;
            case R.id.rl_20:
                setTime(iv_20);
                tag = 20;
                break;
            default:
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
