package com.smartism.znzk.activity.SmartMedicine;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.MedicHabitInfo;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.view.weightPickerview.picker.TimePicker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;



/**
 * Created by win7 on 2017/5/15.
 */

public class MedicineSetTimeActivity extends ActivityParentActivity implements View.OnClickListener {

    private DeviceInfo deviceInfo;

    private ImageView yx_add;

    private TextView et_start_o, et_after_o, et_start_t, et_after_t, et_start_th, et_after_th;
    private TimePicker picker;

    private String hour1, min1, hour2, min2, hour3, min3, hour4, min4, hour5, min5, hour6, min6;

//    private int type1 = 0, type2 = 0, type3 = 0;

    private int time1, time2, time3, time4, time5, time6;

    private LinearLayout ll_define;

    private Button add;

    private List<MedicHabitInfo> infos;
    private List<MedicHabitInfo> habitInfos;


    private List<MedicHabitInfo> tempInfos;
    private final int time_out = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_time_set);
        deviceInfo = (DeviceInfo) getIntent().getSerializableExtra("device");
        habitInfos = (List<MedicHabitInfo>) getIntent().getSerializableExtra("infos");
        initView();
    }

    private void initView() {
        infos = new ArrayList<>();
        tempInfos = new ArrayList<>();
//        yx_back = (Button) findViewById(yx_back);
        yx_add = (ImageView) findViewById(R.id.yx_add);
        et_start_o = (TextView) findViewById(R.id.et_start_o);
        et_after_o = (TextView) findViewById(R.id.et_after_o);
        et_start_t = (TextView) findViewById(R.id.et_start_t);
        et_after_t = (TextView) findViewById(R.id.et_after_t);
        et_start_th = (TextView) findViewById(R.id.et_start_th);
        et_after_th = (TextView) findViewById(R.id.et_after_th);

        ll_define = (LinearLayout) findViewById(R.id.ll_define);
        ll_define.setOnClickListener(this);


        add = (Button) findViewById(R.id.add);
        add.setOnClickListener(this);

        yx_add.setOnClickListener(this);
//        yx_back.setOnClickListener(this);
        et_start_o.setOnClickListener(this);
        et_after_o.setOnClickListener(this);
        et_start_t.setOnClickListener(this);
        et_after_t.setOnClickListener(this);
        et_start_th.setOnClickListener(this);
        et_after_th.setOnClickListener(this);


        if (habitInfos != null && habitInfos.size() > 0) {
            Message m = mHandler.obtainMessage(2);
            m.obj = habitInfos;
            mHandler.sendMessage(m);
        } else {
            mHandler.sendEmptyMessageDelayed(10, 15 * 1000);
            showInProgress(getString(R.string.loading), false, true);
            JavaThreadPool.getInstance().excute(new Medcline());
        }


    }

    private long mornId, midId, nonnId;

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case time_out:
                    cancelInProgress();
                    Toast.makeText(mContext, getString(R.string.time_out), Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    mHandler.removeMessages(time_out);
                    if (msg.obj == null)
                        return true;

                    cancelInProgress();
                    List<MedicHabitInfo> medicHabitInfos = new ArrayList<>();
                    List<MedicHabitInfo> temps = new ArrayList<>();
                    medicHabitInfos.addAll((Collection<? extends MedicHabitInfo>) msg.obj);
                    for (int i = 0; i < medicHabitInfos.size(); i++) {
//                JSONObject o = new JSONObject();
//                o = array.getJSONObject(i);
                        MedicHabitInfo info = medicHabitInfos.get(i);
                        if (info.getType() == 1) {
                            temps.add(info);
                            mornId = info.getId();
                            int start_time = info.getStartTime();
                            int startTime_hour = start_time / 60;
                            int startTime_min = start_time - startTime_hour * 60;
                            hour1 = (startTime_hour < 10) ? "0" + startTime_hour : String.valueOf(startTime_hour);
                            min1 = (startTime_min < 10) ? "0" + startTime_min : String.valueOf(startTime_min);
                            et_start_o.setText(hour1 + ":" + min1);


                            int after_time = info.getEndTime();
                            int after_hour = after_time / 60;
                            int after_min = after_time - after_hour * 60;
                            hour2 = (after_hour < 10) ? "0" + after_hour : String.valueOf(after_hour);
                            min2 = (after_min < 10) ? "0" + after_min : String.valueOf(after_min);
                            et_after_o.setText(hour2 + ":" + min2);

                        } else if (info.getType() == 2) {
                            temps.add(info);
                            midId = info.getId();
                            int start_time = info.getStartTime();
                            int startTime_hour = start_time / 60;
                            int startTime_min = start_time - startTime_hour * 60;
                            hour3 = (startTime_hour < 10) ? "0" + startTime_hour : String.valueOf(startTime_hour);
                            min3 = (startTime_min < 10) ? "0" + startTime_min : String.valueOf(startTime_min);
                            et_start_t.setText(hour3 + ":" + min3);


                            int after_time = info.getEndTime();
                            int after_hour = after_time / 60;
                            int after_min = after_time - after_hour * 60;
                            hour4 = (after_hour < 10) ? "0" + after_hour : String.valueOf(after_hour);
                            min4 = (after_min < 10) ? "0" + after_min : String.valueOf(after_min);
                            et_after_t.setText(hour4 + ":" + min4);

                        } else if (info.getType() == 3) {
                            temps.add(info);
                            nonnId = info.getId();
                            int start_time = info.getStartTime();
                            int startTime_hour = start_time / 60;
                            int startTime_min = start_time - startTime_hour * 60;
                            hour5 = (startTime_hour < 10) ? "0" + startTime_hour : String.valueOf(startTime_hour);
                            min5 = (startTime_min < 10) ? "0" + startTime_min : String.valueOf(startTime_min);
                            et_start_th.setText(hour5 + ":" + min5);


                            int after_time = info.getEndTime();
                            int after_hour = after_time / 60;
                            int after_min = after_time - after_hour * 60;
                            hour6 = (after_hour < 10) ? "0" + after_hour : String.valueOf(after_hour);
                            min6 = (after_min < 10) ? "0" + after_min : String.valueOf(after_min);
                            et_after_th.setText(hour6 + ":" + min6);

                        }

                    }
                    infos.clear();
                    medicHabitInfos.removeAll(temps);
                    infos.addAll(medicHabitInfos);

                    for (int i = 0; i < infos.size(); i++) {

                        addViewItem(i);
                    }
                    break;

            }
            return false;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);

    class Medcline implements Runnable {
        @Override
        public void run() {
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();

            object.put("id", deviceInfo.getId());


            Log.e(TAG, object.toJSONString());

            String result;
            result = HttpRequestUtils
                    .requestoOkHttpPost(
                             server + "/jdm/s3/duth/list", object, MedicineSetTimeActivity.this);

            List<MedicHabitInfo> infos = new ArrayList<>();

            if (result.length() > 4) {

                JSONArray array = JSONObject.parseObject(result).getJSONArray("result");

                for (int i = 0; i < array.size(); i++) {
                    MedicHabitInfo info = new MedicHabitInfo();
                    JSONObject o = array.getJSONObject(i);
                    info.setId(o.getLongValue("id"));
                    info.setEndTime(o.getIntValue("endTime"));
                    info.setStartTime(o.getIntValue("startTime"));
                    info.setType(o.getIntValue("type"));
                    info.setValid(o.getBooleanValue("valid"));
                    info.setName(o.getString("name"));

                    infos.add(info);
                }


                Message m = mHandler.obtainMessage(2);
                m.obj = infos;
                mHandler.sendMessage(m);
            } else if (result.equals("{}")) {
                mHandler.removeMessages(time_out);
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                    }
                });
            } else {
                mHandler.removeMessages(time_out);
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(mContext, getString(R.string.register_tip_empty),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }


    class AddMedcline implements Runnable {
        @Override
        public void run() {
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();

            object.put("did", deviceInfo.getId());

            JSONArray array = new JSONArray();
            JSONObject o = new JSONObject();
            JSONObject o1 = new JSONObject();
            JSONObject o2 = new JSONObject();
                o.put("id", mornId);
                o.put("type", 1);
                o.put("valid", 1);
                o.put("startTime", Integer.valueOf(hour1) * 60 + Integer.valueOf(min1));
                o.put("endTime", Integer.valueOf(hour2) * 60 + Integer.valueOf(min2));
                o.put("name", getString(R.string.smart_medc_add_time_morn));
                array.add(o);
                o1.put("type", 2);
                o1.put("id", midId);
                o1.put("valid", 1);
                o1.put("startTime", Integer.valueOf(hour3) * 60 + Integer.valueOf(min3));
                o1.put("endTime", Integer.valueOf(hour4) * 60 + Integer.valueOf(min4));
                o1.put("name", getString(R.string.smart_medc_add_time_mid));
                array.add(o1);
                o2.put("id", nonnId);
                o2.put("type", 3);
                o2.put("valid", 1);
                o2.put("startTime", Integer.valueOf(hour5) * 60 + Integer.valueOf(min5));
                o2.put("endTime", Integer.valueOf(hour6) * 60 + Integer.valueOf(min6));
                o2.put("name", getString(R.string.smart_medc_add_time_noon));
                array.add(o2);

            for (int i = 0; i < infos.size(); i++) {
                JSONObject oo = new JSONObject();
                MedicHabitInfo info = infos.get(i);
                oo.put("id", info.getId());
                oo.put("name", info.getName());
                oo.put("type", info.getType());
                oo.put("startTime", info.getStartTime());
                oo.put("endTime", info.getEndTime());
                oo.put("endTime", info.getEndTime());
                oo.put("valid", info.isValid() ? 1 : 0);
                array.add(oo);
            }
            object.put("times", array);

            Log.e("duth", object.toJSONString());

            String result;
            result = HttpRequestUtils
                    .requestoOkHttpPost(
                             server + "/jdm/s3/duth/update", object, MedicineSetTimeActivity.this);


            if ("0".equals(result)) {
                mHandler.removeMessages(time_out);
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        //device_set_tip_success
                        cancelInProgress();
                        Toast.makeText(mContext, getString(R.string.gpio_success),
                                Toast.LENGTH_LONG).show();

                        Intent intent = getIntent();
                        setResult(RESULT_OK, intent);
                        finish();

                    }
                });
            } else if ("-3".equals(result)) {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        mHandler.removeMessages(time_out);
                        //device_set_tip_success
                        cancelInProgress();
                        infos.clear();
                        infos.addAll(tempInfos);
                        Toast.makeText(mContext, getString(R.string.device_not_getdata),
                                Toast.LENGTH_LONG).show();

                    }
                });
            } else if ("-4".equals(result)) {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        //device_set_tip_success
                        mHandler.removeMessages(time_out);
                        cancelInProgress();
                        infos.clear();
                        infos.addAll(tempInfos);
                        Toast.makeText(mContext, getString(R.string.smart_medc_set_time_repeat),
                                Toast.LENGTH_LONG).show();

                    }
                });
            } else {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        mHandler.removeMessages(time_out);
                        infos.clear();
                        infos.addAll(tempInfos);
                        cancelInProgress();
                        Toast.makeText(mContext, getString(R.string.register_tip_empty),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    private void addViewItem(final int i) {
        MedicHabitInfo info = null;
        View view1 = View.inflate(this, R.layout.activity_medicine_time_define_view, null);

        EditText et_name = (EditText) view1.findViewById(R.id.et_name);
        final TextView tv_morn = (TextView) view1.findViewById(R.id.tv_morn);
        final TextView tv_nonn = (TextView) view1.findViewById(R.id.tv_nonn);
        if (i != -1) {
            info = infos.get(i);
            infos.get(i).setType(i + 4);
            et_name.setText(info.getName());
            int start_time = info.getStartTime();
            int startTime_hour = start_time / 60;
            int startTime_min = start_time - startTime_hour * 60;
            String hour1 = (startTime_hour < 10) ? "0" + startTime_hour : String.valueOf(startTime_hour);
            String min1 = (startTime_min < 10) ? "0" + startTime_min : String.valueOf(startTime_min);
            tv_morn.setText(hour1 + ":" + min1);


            int after_time = info.getEndTime();
            int after_hour = after_time / 60;
            int after_min = after_time - after_hour * 60;
            String hour2 = (after_hour < 10) ? "0" + after_hour : String.valueOf(after_hour);
            String min2 = (after_min < 10) ? "0" + after_min : String.valueOf(after_min);
            tv_nonn.setText(hour2 + ":" + min2);

            view1.setTag(info.getId());

        } else {
            et_name.setFocusable(true);
            et_name.setFocusableInTouchMode(true);
            et_name.requestFocus();
        }


        ImageView iv_del = (ImageView) view1.findViewById(R.id.iv_del);

        tv_morn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePicker picker = new TimePicker(MedicineSetTimeActivity.this, TimePicker.HOUR_OF_DAY);
                picker.setLabel(getString(R.string.smart_medc_add_time_s), getString(R.string.smart_medc_add_time_f));
                picker.setTopLineVisible(false);
                picker.setOnTimePickListener(new TimePicker.OnTimePickListener() {
                    @Override
                    public void onTimePicked(String hour, String minute) {
                        tv_morn.setText(hour + ":" + minute);
                    }
                });
                picker.show();
            }
        });
        tv_nonn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePicker picker = new TimePicker(MedicineSetTimeActivity.this, TimePicker.HOUR_OF_DAY);
                picker.setLabel(getString(R.string.smart_medc_add_time_s), getString(R.string.smart_medc_add_time_f));
                picker.setTopLineVisible(false);
                picker.setOnTimePickListener(new TimePicker.OnTimePickListener() {
                    @Override
                    public void onTimePicked(String hour, String minute) {
                        tv_nonn.setText(hour + ":" + minute);
                    }
                });
                picker.show();
            }
        });
        ll_define.addView(view1);
        final View view2 = view1;

        iv_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (i != -1) {
                    MedicHabitInfo info = infos.get(i);
                    info.setValid(false);
//                    tempInfos.add(info);
//                    infos.remove(i);
                }
                ll_define.removeView(view2);
            }
        });

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add:
                addViewItem(-1);
                break;

            case R.id.yx_add:
                if (Util.isFastClick()) {
                    Toast.makeText(mContext, getString(R.string.activity_devices_commandhistory_tip), Toast.LENGTH_SHORT).show();
                }

                if (TextUtils.isEmpty(et_start_o.getText().toString()) || TextUtils.isEmpty(et_after_o.getText().toString())) {
                    Toast.makeText(mContext, getString(R.string.smart_medc_set_time_1),
                            Toast.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(et_start_t.getText().toString()) || TextUtils.isEmpty(et_after_t.getText().toString())) {
                    Toast.makeText(mContext, getString(R.string.smart_medc_set_time_2),
                            Toast.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(et_start_th.getText().toString()) || TextUtils.isEmpty(et_after_th.getText().toString())) {

                    Toast.makeText(mContext, getString(R.string.smart_medc_set_time_1),
                            Toast.LENGTH_LONG).show();
                    return;
                }
                if ((hour1 != null && hour2 == null) || (hour2 != null && hour1 == null)) {
                    Toast.makeText(mContext, getString(R.string.smart_medc_set_time_pl),
                            Toast.LENGTH_LONG).show();
                    return;
                } else if (hour1 != null && hour2 != null) {
                    time1 = Integer.valueOf(hour1) * 60 + Integer.valueOf(min1);
                    time2 = Integer.valueOf(hour2) * 60 + Integer.valueOf(min2);
                    if (time1 >= time2) {
                        Toast.makeText(mContext, getString(R.string.smart_medc_set_time), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if ((hour3 != null && hour4 == null) || (hour4 != null && hour3 == null)) {
                    Toast.makeText(mContext, getString(R.string.smart_medc_set_time_pl),
                            Toast.LENGTH_LONG).show();
                    return;
                } else if (hour3 != null && hour4 != null) {
                    time3 = Integer.valueOf(hour3) * 60 + Integer.valueOf(min3);
                    time4 = Integer.valueOf(hour4) * 60 + Integer.valueOf(min4);
                    if (time3 >= time4) {
                        Toast.makeText(mContext, getString(R.string.smart_medc_set_time), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if ((hour5 != null && hour6 == null) || (hour6 != null && hour5 == null)) {
                    Toast.makeText(mContext, getString(R.string.smart_medc_set_time_pl),
                            Toast.LENGTH_LONG).show();
                    return;
                } else if (hour5 != null && hour6 != null) {
                    time5 = Integer.valueOf(hour5) * 60 + Integer.valueOf(min5);
                    time6 = Integer.valueOf(hour6) * 60 + Integer.valueOf(min6);
                    if (time5 >= time6) {
                        Toast.makeText(mContext, getString(R.string.smart_medc_set_time), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                if (time3 != 0 && time3 <= time2) {
                    Toast.makeText(mContext, getString(R.string.smart_medc_set_time_21), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (time5 != 0 && time5 <= time4) {
                    Toast.makeText(mContext, getString(R.string.smart_medc_set_time_32), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (time5 != 0 && time5 <= time2) {
                    Toast.makeText(mContext, getString(R.string.smart_medc_set_time_31), Toast.LENGTH_SHORT).show();
                    return;
                }

//                if (tempInfos.size() > 0) {
//                    infos.removeAll(tempInfos);
//                }
                tempInfos = new ArrayList<>();
                tempInfos.addAll(infos);
                for (int i = 0; i < ll_define.getChildCount(); i++) {
                    View childAt = ll_define.getChildAt(i);
                    EditText editText = (EditText) childAt.findViewById(R.id.et_name);
                    TextView tv_morn = (TextView) childAt.findViewById(R.id.tv_morn);
                    TextView tv_nonn = (TextView) childAt.findViewById(R.id.tv_nonn);

                    if (TextUtils.isEmpty(editText.getText().toString().trim())) {
                        Toast.makeText(MedicineSetTimeActivity.this, "请输入名称", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (TextUtils.isEmpty(tv_morn.getText()) || TextUtils.isEmpty(tv_nonn.getText())) {
                        Toast.makeText(MedicineSetTimeActivity.this, getString(R.string.smart_medc_set_time_pl), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String[] arr1 = tv_morn.getText().toString().split(":");
                    String[] arr2 = tv_nonn.getText().toString().split(":");

                    int start = Integer.parseInt(arr1[0]) * 60 + Integer.parseInt(arr1[1]);
                    int end = Integer.parseInt(arr2[0]) * 60 + Integer.parseInt(arr2[1]);
                    if (start >= end) {
                        Toast.makeText(MedicineSetTimeActivity.this, getString(R.string.smart_medc_set_time), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (childAt.getTag() == null) {
                        MedicHabitInfo info = new MedicHabitInfo();
                        info.setId(0);
                        info.setType(i + 4);
                        info.setStartTime(start);
                        info.setEndTime(end);
                        info.setName(editText.getText().toString());
                        info.setValid(true);
                        infos.add(info);
                    } else {
                        for (int j = 0; j < infos.size(); j++) {
                            if (infos.get(j).getId() == (long) childAt.getTag()) {
                                infos.get(j).setType(i + 4);
                                infos.get(j).setStartTime(start);
                                infos.get(j).setEndTime(end);
                                infos.get(j).setName(editText.getText().toString());
                            }
                        }
                    }
                }
//                if (infos.size() >= 0) {
//                    infos.addAll(tempInfos);
//                }
//
                mHandler.sendEmptyMessageDelayed(time_out, 15 * 1000);
                showInProgress(getString(R.string.loading), false, true);
                JavaThreadPool.getInstance().excute(new AddMedcline());
                break;
            case R.id.et_start_o:
                picker = new TimePicker(this, TimePicker.HOUR_OF_DAY);
                picker.setLabel(getString(R.string.smart_medc_add_time_s), getString(R.string.smart_medc_add_time_f));
                picker.setTopLineVisible(false);
                picker.setOnTimePickListener(new TimePicker.OnTimePickListener() {
                    @Override
                    public void onTimePicked(String hour, String minute) {
                        hour1 = hour;
                        min1 = minute;
                        et_start_o.setText(hour + ":" + minute);
                    }
                });
                picker.show();
                break;
            case R.id.et_after_o:
                picker = new TimePicker(this, TimePicker.HOUR_OF_DAY);
                picker.setLabel(getString(R.string.smart_medc_add_time_s), getString(R.string.smart_medc_add_time_f));
                picker.setTopLineVisible(false);
                picker.setOnTimePickListener(new TimePicker.OnTimePickListener() {
                    @Override
                    public void onTimePicked(String hour, String minute) {
                        hour2 = hour;
                        min2 = minute;
                        et_after_o.setText(hour + ":" + minute);
                    }
                });
                picker.show();
                break;
            case R.id.et_start_t:
                picker = new TimePicker(this, TimePicker.HOUR_OF_DAY);
                picker.setLabel(getString(R.string.smart_medc_add_time_s), getString(R.string.smart_medc_add_time_f));
                picker.setTopLineVisible(false);
                picker.setOnTimePickListener(new TimePicker.OnTimePickListener() {
                    @Override
                    public void onTimePicked(String hour, String minute) {
                        hour3 = hour;
                        min3 = minute;
                        et_start_t.setText(hour + ":" + minute);
                    }
                });
                picker.show();
                break;
            case R.id.et_after_t:
                picker = new TimePicker(this, TimePicker.HOUR_OF_DAY);
                picker.setLabel(getString(R.string.smart_medc_add_time_s), getString(R.string.smart_medc_add_time_f));
                picker.setTopLineVisible(false);
                picker.setOnTimePickListener(new TimePicker.OnTimePickListener() {
                    @Override
                    public void onTimePicked(String hour, String minute) {
                        hour4 = hour;
                        min4 = minute;
                        et_after_t.setText(hour + ":" + minute);
                    }
                });
                picker.show();
                break;
            case R.id.et_start_th:
                picker = new TimePicker(this, TimePicker.HOUR_OF_DAY);
                picker.setLabel(getString(R.string.smart_medc_add_time_s), getString(R.string.smart_medc_add_time_f));
                picker.setTopLineVisible(false);
                picker.setOnTimePickListener(new TimePicker.OnTimePickListener() {
                    @Override
                    public void onTimePicked(String hour, String minute) {
                        hour5 = hour;
                        min5 = minute;
                        et_start_th.setText(hour + ":" + minute);
                    }
                });
                picker.show();
                break;
            case R.id.et_after_th:
                picker = new TimePicker(this, TimePicker.HOUR_OF_DAY);
                picker.setLabel(getString(R.string.smart_medc_add_time_s), getString(R.string.smart_medc_add_time_f));
                picker.setTopLineVisible(false);
                picker.setOnTimePickListener(new TimePicker.OnTimePickListener() {
                    @Override
                    public void onTimePicked(String hour, String minute) {
                        hour6 = hour;
                        min6 = minute;
                        et_after_th.setText(hour + ":" + minute);
                    }
                });
                picker.show();
                break;
        }
    }

    public void back(View v) {
        finish();
    }

}
