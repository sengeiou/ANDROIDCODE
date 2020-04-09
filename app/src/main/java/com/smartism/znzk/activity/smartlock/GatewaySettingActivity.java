package com.smartism.znzk.activity.smartlock;


import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.alibaba.fastjson.JSONObject;
import com.lsemtmf.genersdk.tools.commen.ToastTools;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.FragmentParentActivity;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.util.*;
import com.smartism.znzk.view.pickerview.TimePickerView;
import com.smartism.znzk.widget.customview.CustomProgressView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GatewaySettingActivity extends FragmentParentActivity implements View.OnClickListener {

    private TimePickerView mTimePickerView;
    Toolbar mToolbar;
    LinearLayout ll_start_time, ll_end_time;
    TextView tv_start_time, tv_end_time;
    EditText name_edit, password_edit;
    private View mProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gateway_settting_layout);
        Util.setStatusBarColor(this, getResources().getColor(R.color.mediumpurple));
        mProgressView = new CustomProgressView(this);
        ViewGroup viewGroup = (ViewGroup) getWindow().getDecorView();
        viewGroup.addView(mProgressView);
        showOrHideProgress(false);//隐藏进度条

        password_edit = findViewById(R.id.password_edit);
        name_edit = findViewById(R.id.name_edit);
        mToolbar = findViewById(R.id.toolbar_gateway);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        ll_start_time = findViewById(R.id.ll_start_time);
        ll_end_time = findViewById(R.id.ll_end_time);
        tv_end_time = findViewById(R.id.tv_end_time);
        tv_start_time = findViewById(R.id.tv_start_time);

        ll_end_time.setOnClickListener(this);
        ll_start_time.setOnClickListener(this);

        mTimePickerView = new TimePickerView(this, TimePickerView.Type.ALL);
        mTimePickerView.setRange(2018, 2050);
        mTimePickerView.setCyclic(true);
        mTimePickerView.setCancelable(true);
        mTimePickerView.setTime(new Date());
        mTimePickerView.setTitle(getResources().getString(R.string.hongcai_time_title));
        // 时间选择后回调
        mTimePickerView.setOnTimeSelectListener(new TimePickerView.OnTimeSelectListener() {
            @Override
            public void onTimeSelect(Date date) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                switch (count) {
                    case 0:
                        tv_start_time.setText(format.format(date));
                        judgeTimeRight(tv_start_time);
                        mTimePickerView.setTime(date);
                        break;
                    case 1:
                        tv_end_time.setText(format.format(date));
                        judgeEndTimeRight();
                        break;
                }

            }
        });

        if (savedInstanceState != null) {
            tv_start_time.setText(savedInstanceState.getString("start_time"));
            tv_end_time.setText(savedInstanceState.getString("end_time"));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        String start_time = tv_start_time.getText().toString();
        String end_time = tv_end_time.getText().toString();

        if (!TextUtils.isEmpty(start_time)) {
            outState.putString("start_time", start_time);
        }
        if (!TextUtils.isEmpty(end_time)) {
            outState.putString("end_time", end_time);
        }
        super.onSaveInstanceState(outState);
    }

    void showOrHideProgress(boolean isShow) {
        if (isShow) {
            mProgressView.setVisibility(View.VISIBLE);
        } else {
            mProgressView.setVisibility(View.GONE);
        }
    }

    public void save(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive() && name_edit.getWindowToken() != null) {
            imm.hideSoftInputFromWindow(name_edit.getWindowToken(), 0);
        }
        if (checkInputRight()) {
            showOrHideProgress(true);
            JavaThreadPool.getInstance().excute(new Runnable() {
                @Override
                public void run() {
                    String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                    String password = password_edit.getText().toString();
                    String userName = name_edit.getText().toString().trim();
                    //格式不是传时间戳
                    start_time = DecimalUtils.handleMatcheString(start_time, "[\\s:-]");
                    end_time = DecimalUtils.handleMatcheString(end_time, "[\\s:-]");
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("did", ((DeviceInfo) getIntent().getSerializableExtra("device")).getId());
                    jsonObject.put("type", 2);
                    jsonObject.put("lname", userName);
                    jsonObject.put("permission", 4);
                    jsonObject.put("conpassword", password);
                    jsonObject.put("perstart", start_time.substring(2));
                    jsonObject.put("perend", end_time.substring(2));
                    final String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/dln/add", jsonObject, GatewaySettingActivity.this);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showOrHideProgress(false);
                            if (result.equals("0")) {
                                //成功
                                Toast toast = Toast.makeText(GatewaySettingActivity.this, "", Toast.LENGTH_LONG);
                                toast.setText(getString(R.string.fragment_time_success));
                                toast.show();
                                setResult(RESULT_OK);
                                finish();
                            } else if (result.equals("-3")) {
                                //主机不在线
                                Toast toast = Toast.makeText(GatewaySettingActivity.this, "", Toast.LENGTH_LONG);
                                toast.setText(getString(R.string.activity_zhuji_not));
                                toast.show();
                            } else if (result.equals("-9")) {
                                //密码已存在
                                Toast toast = Toast.makeText(GatewaySettingActivity.this, "", Toast.LENGTH_LONG);
                                toast.setText(getString(R.string.password_repeat));
                                toast.show();
                            } else if (result.equals("-6")) {
                                //无权限
                                Toast toast = Toast.makeText(GatewaySettingActivity.this, "", Toast.LENGTH_LONG);
                                toast.setText(getString(R.string.insufficient_permissions));
                                toast.show();
                            } else if (result.equals("-7")) {
                                Toast toast = Toast.makeText(GatewaySettingActivity.this, "", Toast.LENGTH_LONG);
                                toast.setText(getString(R.string.admin_password_error));
                                toast.show();
                            } else if (result.equals("-8")) {
                                Toast toast = Toast.makeText(GatewaySettingActivity.this, "", Toast.LENGTH_LONG);
                                toast.setText(getString(R.string.jieaolihua_time_error));
                                toast.show();
                            } else if (result.equals("-11")) {
                                Toast toast = Toast.makeText(GatewaySettingActivity.this, "", Toast.LENGTH_LONG);
                                toast.setText(getString(R.string.jieaolihua_remote_full));
                                toast.show();
                            } else {
                                Toast toast = Toast.makeText(GatewaySettingActivity.this, "", Toast.LENGTH_LONG);
                                toast.setText(getString(R.string.net_error_operationfailed));
                                toast.show();
                            }
                        }
                    });
                }
            });
        }

    }


    //判断选择的时间与当前时间的合法性
    private void judgeTimeRight(TextView tv) {
        try {
            String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());//保存当前时间
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date current_time = format.parse(currentTime);
            Date start_time = format.parse(tv.getText().toString());

            if (start_time.getTime() < current_time.getTime()) {
                ToastTools.long_Toast(this, getResources().getString(R.string.jieaolihua_start_time_prompt));
                tv.setTextColor(Color.GRAY);
            } else {
                tv.setTextColor(getResources().getColor(R.color.mediumpurple));
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    //判断结束时间是否合法
    private void judgeEndTimeRight() {
        String startTime = tv_start_time.getText().toString();
        try {
            String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());//保存当前时间
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date current_time = format.parse(currentTime);
            Date end_time = format.parse(tv_end_time.getText().toString());

            if (!TextUtils.isEmpty(startTime)) {
                Date start_time = format.parse(startTime);
                if (end_time.getTime() <= start_time.getTime()) {
                    ToastTools.long_Toast(this, getResources().getString(R.string.jieaolihua_end_time_prompt));
                    tv_end_time.setTextColor(Color.GRAY);
                } else {
                    tv_end_time.setTextColor(getResources().getColor(R.color.mediumpurple));
                }
            } else {
                if (end_time.getTime() <= current_time.getTime()) {
                    ToastTools.long_Toast(this, getResources().getString(R.string.jieaolihua_end_time_current));
                    tv_end_time.setTextColor(Color.GRAY);
                    return;
                }
            }


        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    //检查输入是否合法
    private boolean checkInputRight() {

        if (TextUtils.isEmpty(name_edit.getText().toString())) {
            ToastTools.short_Toast(this, getResources().getString(R.string.userinfo_activity_nicheng));
            return false;
        }
        if (TextUtils.isEmpty(password_edit.getText().toString())) {
            ToastTools.short_Toast(this, getResources().getString(R.string.remind_input_wifi_pwd1));
            return false;
        }
        if (TextUtils.isEmpty(tv_end_time.getText().toString()) || TextUtils.isEmpty(tv_start_time.getText().toString())) {
            ToastTools.short_Toast(this, getResources().getString(R.string.jieaolihua_time_not_empty));
            return false;
        }
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());//保存当前时间
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date start_time = null, end_time = null;
        try {
            Date current_time = format.parse(currentTime);
            start_time = format.parse(tv_start_time.getText().toString());
            end_time = format.parse(tv_end_time.getText().toString());
            if (start_time.getTime() < current_time.getTime()) {
                ToastTools.short_Toast(this, getResources().getString(R.string.jieaolihua_start_time_prompt));
                return false;
            }

            if (end_time.getTime() <= start_time.getTime()) {
                ToastTools.short_Toast(this, getResources().getString(R.string.jieaolihua_end_time_prompt));
                return false;
            }
        } catch (ParseException e) {
            return false;
        }
        this.start_time = tv_start_time.getText().toString();
        this.end_time = tv_end_time.getText().toString();
        return true;
    }

    int count = -1; //0表示点击开始时间、1表示点击结束时间
    String start_time, end_time; //开始时间到结束时间

    @Override
    public void onClick(View v) {
        //隐藏输入法
        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
        switch (v.getId()) {
            case R.id.ll_start_time:
                count = 0;
                break;
            case R.id.ll_end_time:
                count = 1;
                break;
        }
        mTimePickerView.show();
    }
}
