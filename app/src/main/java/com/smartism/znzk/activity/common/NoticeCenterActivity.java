package com.smartism.znzk.activity.common;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.CollectionsUtils;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.WeakRefHandler;

import org.apache.commons.lang.StringUtils;

/**
 * Created by win7 on 2016/10/28.
 */
public class NoticeCenterActivity extends ActivityParentActivity {

    private static final int NOTICE_STATUS = 1;
    private EditText et_port, et_ip_address, et_user_id,et_ac_number;
    private TextView status;
    private LinearLayout ly_noticecenter_unumber,ly_noticecenter_acid,ly_noticecenter_status,ly_noticecenter_ip,ly_noticecenter_port;
    private ZhujiInfo zhujiInfo;
    private Context mContext;
    private Button updateBtn;

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case NOTICE_STATUS:
                    if (msg.obj != null) {
                        cancelInProgress();
                        JSONObject object = (JSONObject) msg.obj;

                        status.setText(object.getIntValue("status") == 1 ? getString(R.string.activity_on_status) : getString(R.string.activity_off_status));
                        et_ip_address.setText(object.getString("ip"));
                        et_user_id.setText(object.getString("number"));
                        et_port.setText(object.getString("port"));

                    }
                    break;
            }
            return false;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_center);
        mContext = this;
        initView();
        initData();
    }

    private void initData() {
        zhujiInfo = DatabaseOperator.getInstance(this).queryDeviceZhuJiInfo(getIntent().getLongExtra("zhuji_id",0));
        if(zhujiInfo != null && zhujiInfo.getAc() == 3){//哈烁报警中心
            ly_noticecenter_unumber.setVisibility(View.GONE);
            ly_noticecenter_status.setVisibility(View.GONE);
            ly_noticecenter_ip.setVisibility(View.GONE);
            ly_noticecenter_port.setVisibility(View.GONE);
            ly_noticecenter_acid.setVisibility(View.VISIBLE);
            showInProgress(getString(R.string.loading), false, true);
            JavaThreadPool.getInstance().excute(new GetAlarmCenterInfo());
        }else{
            if (zhujiInfo!=null && zhujiInfo.getAc() == 1){
                ly_noticecenter_unumber.setVisibility(View.GONE);
            }
            showInProgress(getString(R.string.loading), false, true);
            JavaThreadPool.getInstance().excute(new CheckStatus());
        }
        if (zhujiInfo!=null && zhujiInfo.getSetInfos() != null){
            if ("1".equals(zhujiInfo.getSetInfos().get(ZhujiInfo.GNSetNameMenu.alarmCenterReadOnly.value()))){
                updateBtn.setVisibility(View.GONE);
            }
        }
    }

    private void initView() {
        updateBtn = (Button) findViewById(R.id.userinfo_update_btn);
        et_port = (EditText) findViewById(R.id.et_port);
        et_ip_address = (EditText) findViewById(R.id.et_ip_address);
        et_user_id = (EditText) findViewById(R.id.et_user_id);
        status = (TextView) findViewById(R.id.tv_status);
        et_ac_number = (EditText) findViewById(R.id.et_ac_number);
        ly_noticecenter_unumber = (LinearLayout) findViewById(R.id.ly_noticecenter_unumber);
        ly_noticecenter_acid = (LinearLayout) findViewById(R.id.ly_noticecenter_acid);
        ly_noticecenter_status = (LinearLayout) findViewById(R.id.ly_noticecenter_status);
        ly_noticecenter_ip = (LinearLayout) findViewById(R.id.ly_noticecenter_ip);
        ly_noticecenter_port = (LinearLayout) findViewById(R.id.ly_noticecenter_port);
    }


    public void back(View view) {
        finish();
    }

    public void updateUserInfo(View view) {
        showInProgress(getString(R.string.loading), false, true);
        if (zhujiInfo != null && zhujiInfo.getAc() == 3){
            String acid = et_ac_number.getText().toString();
            if (StringUtils.isEmpty(acid)){
                Toast.makeText(mContext, getString(R.string.activity_ac_number_empty),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            JavaThreadPool.getInstance().excute(new UpdateAlarmCenterInfo(acid));
        }else{
            JavaThreadPool.getInstance().excute(new UpdateNoticeInfo());
        }
    }

    private class CheckStatus implements Runnable {
        @Override
        public void run() {
            String server = dcsp.getString(
                    DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("did", zhujiInfo.getId());
            String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/dset/zfinfo", object, NoticeCenterActivity.this);

            if (!TextUtils.isEmpty(result) && result.length() > 4) {
                JSONObject resultJson = null;

                try {
                    resultJson = JSONObject.parseObject(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (resultJson == null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(
                                    NoticeCenterActivity.this,
                                    getString(R.string.device_set_tip_responseerr),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }

                Message message = mHandler.obtainMessage(NOTICE_STATUS);
                message.obj = resultJson;
                mHandler.sendMessage(message);
            }
        }
    }

    private class UpdateNoticeInfo implements Runnable {
        @Override
        public void run() {
            String ip = et_ip_address.getText().toString();
            String port = et_port.getText().toString();
            String numbwer = et_user_id.getText().toString();
            if (TextUtils.isEmpty(ip)) {
                Toast.makeText(NoticeCenterActivity.this, getString(R.string.activity_not_ip), Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(port)) {
                Toast.makeText(NoticeCenterActivity.this, getString(R.string.activity_not_port), Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(numbwer)) {
                if (zhujiInfo!=null && zhujiInfo.getAc() != 1){
                    Toast.makeText(NoticeCenterActivity.this, getString(R.string.activity_not_number), Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            String server = dcsp.getString(
                    DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("did", zhujiInfo.getId());
            object.put("ip", ip);
            object.put("port", port);
            object.put("number", numbwer);
            String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/dset/zf", object, NoticeCenterActivity.this);

            if ("0".equals(result)) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(NoticeCenterActivity.this, getString(R.string.device_set_tip_success),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else if ("-3".equals(result)) {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(NoticeCenterActivity.this, getString(R.string.zjnothave),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else if ("-4".equals(result)) {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(NoticeCenterActivity.this, getString(R.string.activity_zhuji_not),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else if ("-5".equals(result)) {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(NoticeCenterActivity.this, getString(R.string.activity_editscene_set_falid),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }
    private class UpdateAlarmCenterInfo implements Runnable {
        String acid = "";

        public UpdateAlarmCenterInfo(String acid){
            this.acid = acid;
        }

        @Override
        public void run() {
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject pJsonObject = new JSONObject();
            pJsonObject.put("did", zhujiInfo != null ? zhujiInfo.getId() : 0);
            JSONArray array = new JSONArray();
            JSONObject object = new JSONObject();
            object.put("vkey", "alarm_centerid");
            object.put("value", acid);
            array.add(object);
            pJsonObject.put("vkeys", array);

            String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/d/p/set", pJsonObject, NoticeCenterActivity.this);
            if ("-3".equals(result)) {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(mContext, getString(R.string.net_error_nodata),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else if ("-5".equals(result)) {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(mContext, getString(R.string.device_not_getdata),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else if ("0".equals(result)) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(mContext, getString(R.string.success),
                                Toast.LENGTH_LONG).show();
                        finish();
                    }
                });

            } else {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(mContext, getString(R.string.net_error),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    private class GetAlarmCenterInfo implements Runnable {
        @Override
        public void run() {
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject pJsonObject = new JSONObject();
            pJsonObject.put("did", zhujiInfo.getId());
            JSONArray array = new JSONArray();
            JSONObject object = new JSONObject();
            object.put("vkey", "alarm_centerid");
            array.add(object);
            pJsonObject.put("vkeys", array);

            final String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/d/p/list", pJsonObject, NoticeCenterActivity.this);
            if ("-3".equals(result)) {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                    }
                });
            } else if ("-5".equals(result)) {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                    }
                });
            } else if (!StringUtils.isEmpty(result)) {
                final JSONArray array1 = JSONArray.parseArray(result);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        if (array1 != null) {
                            for (int i = 0; i < array1.size(); i++) {
                                JSONObject jsonObject = array1.getJSONObject(i);
                                if (jsonObject.getString("key").equals("alarm_centerid")) {
                                    if (jsonObject.containsKey("value")) {
                                        et_ac_number.setText(jsonObject.getString("value"));
                                    }
                                }
                            }
                        }
                    }
                });

            } else {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                    }
                });
            }
        }
    }
}
