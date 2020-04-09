package com.smartism.znzk.activity.scene;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.device.ZhujiListFragment;
import com.smartism.znzk.domain.FoundInfo;
import com.smartism.znzk.domain.SceneInfo;
import com.smartism.znzk.util.CollectionsUtils;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.WeakRefHandler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.smartism.znzk.activity.scene.SceneActivity.SecuritySceneType_Arming;
import static com.smartism.znzk.activity.scene.SceneActivity.SecuritySceneType_DesArming;
import static com.smartism.znzk.activity.scene.SceneActivity.SecuritySceneType_Home;


public class ScenceArmingDisarmingEditActicity extends ActivityParentActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    public static final String TAG = ScenceArmingDisarmingEditActicity.class.getSimpleName();
    private Context mContext;
    private TimePicker timePicker;
    private AppCompatCheckBox box2, box3, box4, box5, box6, box7;
    private AppCompatCheckBox box_everyDay;
    private AppCompatCheckBox box1, checkbox_arming, checkbox_disarming, checkbox_athome;
    private AppCompatCheckBox[] views;
    private ImageView btn;
    private int hour, minute;
    private String time;
    StringBuffer buffer = new StringBuffer("00000000");
    String checked;
    private long sceneId = -1;
    private String result;
    private String timeBuffer;
    private long timeHourmin;
    private FoundInfo foundInfo;
    private List<SceneInfo> securityItems;

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            return false;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_arming_disarming_timing_edit);
        mContext = this;
        foundInfo = (FoundInfo) getIntent().getSerializableExtra("foundInfo");
        securityItems = (List<SceneInfo>) getIntent().getSerializableExtra("securityItems");
        initView();
        initData();
    }

    private void initData() {
        if (foundInfo != null && foundInfo.getId() != -1) {
            sceneId = foundInfo.getId();
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
        checkbox_athome.setOnCheckedChangeListener(this);
        checkbox_arming.setOnCheckedChangeListener(this);
        checkbox_disarming.setOnCheckedChangeListener(this);
        box_everyDay.setOnCheckedChangeListener(this);
        btn.setOnClickListener(this);
        if (sceneId != -1) {
            if (!CollectionsUtils.isEmpty(foundInfo.getTriggerInfos())) {
                FoundInfo.TriggerInfosEntity triggerInfosEntity = foundInfo.getTriggerInfos().get(0);
                timeHourmin = triggerInfosEntity.getTime();
                timeBuffer = triggerInfosEntity.getCycle();

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
                }else{
                    box_everyDay.setChecked(false);
                }


                if (timeBuffer.equals("00000001")){
                    box1.setChecked(true);
                    box2.setChecked(true);
                    box3.setChecked(true);
                    box4.setChecked(true);
                    box5.setChecked(true);
                    box6.setChecked(true);
                    box7.setChecked(true);
                }else {
                    box1.setChecked(timeBuffer.charAt(0) == '0' ? false : true);
                    box2.setChecked(timeBuffer.charAt(1) == '0' ? false : true);
                    box3.setChecked(timeBuffer.charAt(2) == '0' ? false : true);
                    box4.setChecked(timeBuffer.charAt(3) == '0' ? false : true);
                    box5.setChecked(timeBuffer.charAt(4) == '0' ? false : true);
                    box6.setChecked(timeBuffer.charAt(5) == '0' ? false : true);
                    box7.setChecked(timeBuffer.charAt(6) == '0' ? false : true);
 //               }
                box_everyDay.setChecked(timeBuffer.charAt(7) == '0' ? false : true);
               }
                buffer = new StringBuffer(timeBuffer);
                timePicker.setCurrentHour(hour);
                timePicker.setCurrentMinute(minute);

                time = hour + ":" + minute;
            }
            if (!CollectionsUtils.isEmpty(foundInfo.getControlInfos()) && !CollectionsUtils.isEmpty(securityItems)) {
                FoundInfo.ControlInfosEntity controlInfosEntity = foundInfo.getControlInfos().get(0);
                for (SceneInfo sceneInfo : securityItems) {
                    if (controlInfosEntity.getDeviceId().equals(String.valueOf(sceneInfo.getId()))) {
                        if (sceneInfo.getType() == SecuritySceneType_Home) {
                            checkbox_athome.setChecked(true);
                        } else if (sceneInfo.getType() == SecuritySceneType_Arming) {
                            checkbox_arming.setChecked(true);
                        } else if (sceneInfo.getType() == SecuritySceneType_DesArming) {
                            checkbox_disarming.setChecked(true);
                        }
                    }
                }
            }
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

//        switchButton = (SwitchButton) findViewById(switchButton);
        btn = (ImageView) findViewById(R.id.save);
        timePicker = (TimePicker) findViewById(R.id.time);
        box1 = (AppCompatCheckBox) findViewById(R.id.mon);
        checkbox_arming = (AppCompatCheckBox) findViewById(R.id.checkbox_arming);
        checkbox_disarming = (AppCompatCheckBox) findViewById(R.id.checkbox_disarming);
        checkbox_athome = (AppCompatCheckBox) findViewById(R.id.checkbox_athome);
        box2 = (AppCompatCheckBox) findViewById(R.id.tue);
        box3 = (AppCompatCheckBox) findViewById(R.id.wed);
        box4 = (AppCompatCheckBox) findViewById(R.id.thu);
        box5 = (AppCompatCheckBox) findViewById(R.id.fri);
        box6 = (AppCompatCheckBox) findViewById(R.id.sau);
        box7 = (AppCompatCheckBox) findViewById(R.id.sun);
        box_everyDay = (AppCompatCheckBox) findViewById(R.id.everyDay);
        views = new AppCompatCheckBox[]{box1, box2, box3, box4, box5, box6, box7};
    }

    public void back(View v) {
        finish();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.checkbox_arming:
                if (isChecked) {
                    checkbox_disarming.setChecked(false);
                    checkbox_athome.setChecked(false);
                }
                break;
            case R.id.checkbox_disarming:
                if (isChecked) {
                    checkbox_arming.setChecked(false);
                    checkbox_athome.setChecked(false);
                }
                break;
            case R.id.checkbox_athome:
                if (isChecked) {
                    checkbox_disarming.setChecked(false);
                    checkbox_arming.setChecked(false);
                }
                break;
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
                    for (int i = 0; i < views.length; i++) {
                        views[i].setChecked(true);
                    }
                } else {
                    for (int i = 0; i < views.length; i++) {
                        views[i].setChecked(false);
                    }
                }
                break;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save:

                if (!checkbox_athome.isChecked() && !checkbox_disarming.isChecked() && !checkbox_arming.isChecked()) {
                    Toast.makeText(mContext, getString(R.string.scence_arming_disarming_type), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!buffer.toString().contains("1")) {
                    Toast.makeText(ScenceArmingDisarmingEditActicity.this, getString(R.string.qwq_date), Toast.LENGTH_SHORT).show();
                    return;
                }
                showInProgress(getString(R.string.loading), false, true);
                JavaThreadPool.getInstance().excute(new Runnable() {
                    @Override
                    public void run() {
//                        final String m = dcsp.getString(DataCenterSharedPreferences.Constant.APP_MASTERID, "");
                        //替换
                        final String m = ZhujiListFragment.getMasterId() ;
                        long timer = 0;
                        String timeStr = String.valueOf(time);
                        String[] subString = null;
                        if (timeStr.length() >= 2) {
                            subString = timeStr.split(":");
                            timer = Long.parseLong(subString[0]) * 60 + Long.parseLong(subString[1]);
                        }
                        JSONObject o = new JSONObject();

                        o.put("m", m);
                        String n = "";
                        long scenceid = 0;
                        if (checkbox_arming.isChecked()) {
//                            scenceid = securityItems.get(0).getId();
                            for (SceneInfo info : securityItems) {
                                if (info.getType() == 4)
                                    scenceid = info.getId();
                            }
                        } else if (checkbox_disarming.isChecked()) {
                            for (SceneInfo info : securityItems) {
                                if (info.getType() == 5)
                                    scenceid = info.getId();
                            }
//                            scenceid = securityItems.get(1).getId();
                        } else if (checkbox_athome.isChecked()) {
                            for (SceneInfo info : securityItems) {
                                if (info.getType() == 3)
                                    scenceid = info.getId();
                            }
//                            scenceid = securityItems.get(2).getId();
                        }

                        o.put("n", "");

                        o.put("t", 6);
                        o.put("tt", timer);
                        o.put("tc", buffer.toString());
                        JSONObject object = new JSONObject();


                        object.put("cd", scenceid);//场景ID
                        //控制类型
                        object.put("ct", 2);
                        //控制的设备指令
//                        if (!TextUtils.isEmpty(deviceCommand)) {
//                            object.put("cc", deviceCommand);
//                        }
                        object.put("cc", "");

                        JSONArray array = new JSONArray();
                        array.add(object);
                        o.put("cl", array);

                        DataCenterSharedPreferences dcsp = DataCenterSharedPreferences.getInstance(
                                mContext, DataCenterSharedPreferences.Constant.CONFIG);
                        String server = dcsp
                                .getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                        if (sceneId != -1) {
                            o.put("id", sceneId);
                            result = HttpRequestUtils
                                    .requestoOkHttpPost(
                                             server + "/jdm/s3/scenes/update", o, ScenceArmingDisarmingEditActicity.this);
                        } else {
                            result = HttpRequestUtils
                                    .requestoOkHttpPost(
                                             server + "/jdm/s3/scenes/add", o, ScenceArmingDisarmingEditActicity.this);
                        }
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
                                    setResult(RESULT_OK);
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
                        } else if ("-15".equals(result)) {
                            mHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    cancelInProgress();
                                    Toast.makeText(mContext,
                                            getString(R.string.net_error_sendtimeout), Toast.LENGTH_LONG).show();
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
                        } else{
                            mHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    cancelInProgress();
                                    Toast.makeText(mContext,
                                            getString(R.string.net_error_operationfailed), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                });
                break;
        }

    }


}
