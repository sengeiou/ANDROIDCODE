package com.smartism.znzk.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TimePicker;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.DeviceTimerInfo;
import com.smartism.znzk.domain.XiaXingInfo;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * Created by win7 on 2016/12/9.
 */
public class QWQNoticeActicity extends ActivityParentActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    public static final String TAG = QWQNoticeActicity.class.getSimpleName();
    private Context mContext;
    private boolean flag;//定时是否开启
    private TimePicker timePicker;
    private CheckBox box1, box2, box3, box4, box5, box6, box7, box_everyDay;
    private Button btn;

    private int hour, minute;
    private String time;
    private String[] i = new String[8];
    StringBuffer buffer = new StringBuffer("00000000");
    String checked;
    private DeviceInfo deviceInfo;
    private List<XiaXingInfo> xiaXing;

    private long sceneId = -1;

    private String result;
    private String timeBuffer;
    private long timeHourmin;
    private DeviceTimerInfo timerInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_qwq_notice);
        mContext = this;
        deviceInfo = (DeviceInfo) getIntent().getSerializableExtra("device");
        timerInfo = (DeviceTimerInfo) getIntent().getSerializableExtra("timerInfo");
        flag = getIntent().getBooleanExtra("status", false);
        initView();
        initData();
    }

    private void initData() {
        if (timerInfo != null && timerInfo.getId() != -1) {
            sceneId = timerInfo.getId();
        }
        timePicker.setIs24HourView(true);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDa, int minute) {
                time = hourOfDa + ":" + minute;
            }
        });
        box1.setOnCheckedChangeListener(this);
        box2.setOnCheckedChangeListener(this);
        box3.setOnCheckedChangeListener(this);
        box4.setOnCheckedChangeListener(this);
        box5.setOnCheckedChangeListener(this);
        box6.setOnCheckedChangeListener(this);
        box7.setOnCheckedChangeListener(this);
        box_everyDay.setOnCheckedChangeListener(this);
        btn.setOnClickListener(this);
//        showInProgress(getString(R.string.loading), false, false);
//        JavaThreadPool.getInstance().excute(new Runnable() {
//            @Override
//            public void run() {
//                DataCenterSharedPreferences dcsp = DataCenterSharedPreferences.getInstance(
//                        mContext, DataCenterSharedPreferences.Constant.CONFIG);
//                String server = dcsp
//                        .getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
//                JSONObject pJsonObject = new JSONObject();
//                pJsonObject.put("did", deviceInfo.getId());
//                final String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/d/dkeycomms", pJsonObject, QWQNoticeActicity.this);
//                mHandler.post(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        cancelInProgress();
//                        String o = null;
//                        try {
//                            o = result;
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//
//                        xiaXing = JSON.parseArray(o, XiaXingInfo.class);
//                        if (xiaXing != null) {
//                            deviceCommand = xiaXing.get(0).getS();
//                        }
//                    }
//                });
//            }
//        });
        if (sceneId != -1) {
            timeBuffer = String.valueOf(timerInfo.getCycle());
            int length = 8 - timeBuffer.length();
            for (int i = 0; i < length; i++) {
                timeBuffer = "0" + timeBuffer;
            }
            timeHourmin = timerInfo.getTime();
            hour = (int) (timeHourmin / 60);
            minute = (int) (timeHourmin - hour * 60);
            timePicker.setCurrentHour(hour);
            timePicker.setCurrentMinute(minute);
            if (timeBuffer.charAt(timeBuffer.length() - 1) == '1') {
                box_everyDay.setChecked(true);
                box1.setChecked(false);
                box2.setChecked(false);
                box3.setChecked(false);
                box4.setChecked(false);
                box5.setChecked(false);
                box6.setChecked(false);
                box7.setChecked(false);
            } else {
                box_everyDay.setChecked(false);
                box1.setChecked(timeBuffer.charAt(0) == '0' ? false : true);
                box2.setChecked(timeBuffer.charAt(1) == '0' ? false : true);
                box3.setChecked(timeBuffer.charAt(2) == '0' ? false : true);
                box4.setChecked(timeBuffer.charAt(3) == '0' ? false : true);
                box5.setChecked(timeBuffer.charAt(4) == '0' ? false : true);
                box6.setChecked(timeBuffer.charAt(5) == '0' ? false : true);
                box7.setChecked(timeBuffer.charAt(6) == '0' ? false : true);
            }
            buffer = new StringBuffer(timeBuffer);
            timePicker.setCurrentHour(hour);
            timePicker.setCurrentMinute(minute);

            time = hour + ":" + minute;
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            String currentTime = sdf.format(new Date());

            String[] subString = null;
            subString = currentTime.split(":");
            timePicker.setCurrentHour(Integer.valueOf(subString[0]));
            timePicker.setCurrentMinute(Integer.valueOf(subString[1]));
            timePicker.setIs24HourView(true);
            time = Integer.valueOf(subString[0]) + ":" + Integer.valueOf(subString[1]);
        }

    }

    private void initView() {
        btn = (Button) findViewById(R.id.save);
        timePicker = (TimePicker) findViewById(R.id.time);
        box1 = (CheckBox) findViewById(R.id.mon);
        box2 = (CheckBox) findViewById(R.id.tue);
        box3 = (CheckBox) findViewById(R.id.wed);
        box4 = (CheckBox) findViewById(R.id.thu);
        box5 = (CheckBox) findViewById(R.id.fri);
        box6 = (CheckBox) findViewById(R.id.sau);
        box7 = (CheckBox) findViewById(R.id.sun);
        box_everyDay = (CheckBox) findViewById(R.id.everyDay);
    }

    public void back(View v) {
        finish();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


        switch (buttonView.getId()) {

            case R.id.mon:
                if (isChecked) {
                    checked = "1";
                    buffer.replace(0, 1, "1");
                } else {
                    checked = "0";
                    buffer.replace(0, 1, "0");
                }
                break;
            case R.id.tue:
                if (isChecked) {
                    checked = "1";
                    buffer.replace(1, 2, "1");
                } else {
                    buffer.replace(1, 2, "0");
                    checked = "0";
                }
                break;
            case R.id.wed:
                if (isChecked) {
                    buffer.replace(2, 3, "1");
                    checked = "1";
                } else {
                    buffer.replace(2, 3, "0");
                    checked = "0";
                }
                break;
            case R.id.thu:
                if (isChecked) {
                    checked = "1";
                    buffer.replace(3, 4, "1");
                } else {
                    buffer.replace(3, 4, "0");
                    checked = "0";
                }
                break;
            case R.id.fri:
                if (isChecked) {
                    buffer.replace(4, 5, "1");
                    checked = "1";
                } else {
                    buffer.replace(4, 5, "0");
                    checked = "0";
                }
                break;
            case R.id.sau:
                if (isChecked) {
                    buffer.replace(5, 6, "1");
                    checked = "1";
                } else {
                    buffer.replace(5, 6, "0");
                    checked = "0";
                }
                break;
            case R.id.sun:
                if (isChecked) {
                    buffer.replace(6, 7, "1");
                    checked = "1";
                } else {
                    buffer.replace(6, 7, "0");
                    checked = "0";
                }
                break;
            case R.id.everyDay:
                if (isChecked) {
                    buffer.replace(0, 8, "00000001");
                    box1.setChecked(false);
                    box2.setChecked(false);
                    box3.setChecked(false);
                    box4.setChecked(false);
                    box5.setChecked(false);
                    box6.setChecked(false);
                    box7.setChecked(false);
                } else {
//                    buffer.replace(7, 8, "0");
                }
                break;
        }
        if (!buffer.toString().substring(0, 7).equals("0000000")) {
            buffer.replace(7, 8, "0");
            box_everyDay.setChecked(false);
        }
//        Toast.makeText(mContext, buffer.toString(), Toast.LENGTH_SHORT).show();
    }




    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save:
                showInProgress(getString(R.string.loading), false, true);
                JavaThreadPool.getInstance().excute(new Runnable() {
                    @Override
                    public void run() {

                        long timer = 0;
                        String timeStr = String.valueOf(time);
                        String[] subString = null;
                        if (timeStr.length() >= 2) {
                            subString = timeStr.split(":");
                            timer = Long.parseLong(subString[0]) * 60 + Long.parseLong(subString[1]);
                        }
                        JSONObject o = new JSONObject();


                        o.put("t", "1");
                        o.put("tt", timer);
                        if (buffer.toString().equals("00000000")) {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    cancelInProgress();
                                    Toast.makeText(QWQNoticeActicity.this, getString(R.string.qwq_date), Toast.LENGTH_SHORT).show();
//                                    finish();
                                }
                            });

                            return;
                        }
                        o.put("tc", buffer.toString());
                        JSONObject object = new JSONObject();
                        //设备ID
                        object.put("cd", deviceInfo.getId());
                        //控制类型
                        object.put("ct", 1);
                        //控制的设备指令
//                        if (!TextUtils.isEmpty(deviceCommand)) {
//                            object.put("cc", deviceCommand);
//                        }
                        object.put("cc", 2);

                        JSONArray array = new JSONArray();
                        array.add(object);
                        o.put("cl", array);
                        if (sceneId != -1) {
                            o.put("id", sceneId);

                        } else {
                            o.put("did", deviceInfo.getId());
                            o.put("s", (flag == true) ? 1 : 0);
                        }
                        DataCenterSharedPreferences dcsp = DataCenterSharedPreferences.getInstance(
                                mContext, DataCenterSharedPreferences.Constant.CONFIG);
                        String server = dcsp
                                .getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                        if (sceneId != -1) {
                            result = HttpRequestUtils
                                    .requestoOkHttpPost(server + "/jdm/s3/dtc/update", o, QWQNoticeActicity.this);
                        } else {
                            result = HttpRequestUtils
                                    .requestoOkHttpPost(server + "/jdm/s3/dtc/add", o, QWQNoticeActicity.this);
                        }

//				String result = HttpRequestUtils
//						.requestHttpServer(
//								 server + "/jdm/service/scenes/add?v="
//										+ URLEncoder.encode(
//												SecurityUtil.crypt(pJsonObject.toJSONString(), Constant.KEY_HTTP)),
//								mContext, mHandler);

                        // -1参数为空 -2校验失败 -3type为1时时间或周期为空 -4type为2时触发设备id或指令为空 -5未获取到数据
                        // -6控制的设备id或指令为空 -7解析失败 -8名称为空 -9lang为空 -10masterid为空
                        // -11类型只能为0,1,2中的一个 -12类型为空 -14被控制的设备必填
                        if ("0".equals(result)) {
                            mHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    cancelInProgress();
                                    if (sceneId != -1) {
                                        Toast.makeText(mContext, getString(R.string.device_set_tip_success),
                                                Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(mContext, getString(R.string.add_success),
                                                Toast.LENGTH_LONG).show();
                                    }

                                    finish();
                                }
                            });
                        } else if ("-1".equals(result)) {
                            mHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    cancelInProgress();
                                    Toast.makeText(mContext, getString(R.string.register_tip_empty),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        } else if ("-2".equals(result)) {
                            mHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    cancelInProgress();
                                    Toast.makeText(mContext, getString(R.string.device_check_failure),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        } else if ("-3".equals(result)) {
                            mHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    cancelInProgress();
                                    Toast.makeText(mContext, getString(R.string.activity_editscene_type_1_empty),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        } else if ("-4".equals(result)) {
                            mHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    cancelInProgress();
                                    Toast.makeText(mContext, getString(R.string.activity_editscene_type_2_empty),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        } else if ("-5".equals(result)) {
                            mHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    cancelInProgress();
                                    Toast.makeText(mContext,
                                            getString(R.string.activity_editscene_type_control_erro), Toast.LENGTH_LONG).show();
                                }
                            });
                        } else if ("-6".equals(result)) {
                            mHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    cancelInProgress();
                                    Toast.makeText(mContext,
                                            getString(R.string.activity_editscene_type_control_empty), Toast.LENGTH_LONG)
                                            .show();
                                }
                            });
                        } else if ("-7".equals(result)) {
                            mHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    cancelInProgress();
                                    Toast.makeText(mContext, getString(R.string.activity_editscene_paser_erro),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        } else if ("-8".equals(result)) {
                            mHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    cancelInProgress();
                                    Toast.makeText(mContext, getString(R.string.activity_editscene_name_empty),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        } else if ("-9".equals(result)) {
                            mHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    cancelInProgress();
                                    Toast.makeText(mContext, getString(R.string.activity_editscene_lang_empty),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        } else if ("-10".equals(result)) {
                            mHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    cancelInProgress();
                                    Toast.makeText(mContext, getString(R.string.activity_editscene_masterid_empty),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        } else if ("-11".equals(result)) {
                            mHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    cancelInProgress();
                                    Toast.makeText(mContext, getString(R.string.activity_editscene_type_only),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        } else if ("-12".equals(result)) {
                            mHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    cancelInProgress();
                                    Toast.makeText(mContext, getString(R.string.activity_editscene_type_empty),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        } else if ("-13".equals(result)) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    cancelInProgress();
                                    Toast.makeText(mContext, getString(R.string.activity_editscene_isexist),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        } else if ("-14".equals(result)) {
                            mHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    cancelInProgress();
                                    Toast.makeText(mContext,
                                            getString(R.string.activity_editscene_type_control_sure), Toast.LENGTH_LONG).show();
                                }
                            });
                        } else if ("-20".equals(result)) {
                            mHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    cancelInProgress();
                                    Toast.makeText(mContext,
                                            getString(R.string.activity_editscene_not), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                });
                break;
        }

    }


    public class LoadOneScence implements Runnable {
        private long id;

        public LoadOneScence(long sceneId) {
            this.id = sceneId;
        }

        @Override
        public void run() {
            showInProgress(getString(R.string.loading), false, true);
            JavaThreadPool.getInstance().excute(new Runnable() {

                @Override
                public void run() {
                    String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                    JSONObject pJsonObject = new JSONObject();
                    pJsonObject.put("id", id);
                    result = HttpRequestUtils
                            .requestoOkHttpPost(server + "/jdm/s3/scenes/get", pJsonObject, QWQNoticeActicity.this);
                    if ("-3".equals(result)) {
                        mHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                cancelInProgress();
                                Toast.makeText(QWQNoticeActicity.this, getString(R.string.device_not_getdata),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    } else if (result != null && result.length() > 3) {
                        mHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                cancelInProgress();


                                if (result == null || "".equals(result)) {
                                    cancelInProgress();
                                    Toast.makeText(QWQNoticeActicity.this, getString(R.string.device_set_tip_responseerr), Toast.LENGTH_SHORT).show();
                                    return;
                                }
//                                "triggerInfos": [
//                                {
//                                    "createTime": 1482050813000,
//                                        "cycle": "00101000",
//                                        "sceneId": 32917428895744000,
//                                        "id": 32917428916715520,
//                                        "time": 1006,
//                                        "triggerSceneId": 0,
//                                        "type": 1,
//                                        "updateTime": 1482050813000,
//                                        "valid": true
//                                }
//                                ]
                                JSONObject jsonObject = JSONObject.parseObject(result);

                                JSONArray array = jsonObject.getJSONArray("triggerInfos");

                                JSONObject jsonObject1 = array.getJSONObject(0);

                                timeBuffer = jsonObject1.getString("cycle");
                                timeHourmin = jsonObject1.getLongValue("time");

                                Message message = mHandler.obtainMessage(2);
                                message.arg1 = (int) timeHourmin;
                                message.obj = timeBuffer;
                                mHandler.sendMessage(message);

                            }
                        });
                    }
                }
            });
        }
    }
}
