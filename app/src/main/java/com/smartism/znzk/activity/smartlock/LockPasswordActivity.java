package com.smartism.znzk.activity.smartlock;

import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.xiongmai.utils.XMProgressDialog;

import java.util.Calendar;

/**
 * Created by win7 on 2017/7/18.
 *  锁临时密码
 */

public class LockPasswordActivity extends ActivityParentActivity implements View.OnClickListener {

    private LinearLayout ll_main,ll_show_password;
    private RelativeLayout ll_youxiao;
    private EditText et_password;
    private LinearLayout ll_pass;
    private TextView txt_youxiao;


    private EditText edit_temppwd ; //临时授权密码
    private LinearLayout hongtaisuo ; //宏泰临时密码
    private DeviceInfo oprerationDeviceInfo ;
    private TextView save ;
    private XMProgressDialog mDialog ;
    private ListView authorization_method_list ;
    private String[] authorizations ;
    private int choicePosition=-1;
    private NumberPicker day_number_picker,hour_number_picker,minute_number_picker ;
    private  LinearLayout time_picker_linear ;



    private String inputPassword = ""; //用户输入的密码
    private int authoriseType = AuthoriseCode.AUTHORISE_TYPE_5MIN;//密码有效时长
    private LockPwdTimePopupWindow popupWindow;//有效时长菜单
    private String psd;//生成的密码
    private InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jjsuo_show_password);
        imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        initView();
        oprerationDeviceInfo = (DeviceInfo) getIntent().getSerializableExtra("device");
        if(oprerationDeviceInfo!=null&&oprerationDeviceInfo.getMc()!=null&&oprerationDeviceInfo.getMc().equals("hongtai")){
           initHongTaiView();
        }
    }

    int time = 0 ; //授权时间
    int times = 0 ; //授权次数
    private void handleHongTaiTempPwd(){
        //判断输入是否符合要求
        String temppwd = edit_temppwd.getText().toString();//临时密码
        String adminPwd = et_password.getText().toString(); //管理员密码
        if(TextUtils.isEmpty(temppwd)||temppwd.length()<6){
            Toast toast = Toast.makeText(this,"",Toast.LENGTH_LONG);
            toast.setText(getString(R.string.input_password_request));
            toast.show();
            return ;
        }
        if(TextUtils.isEmpty(adminPwd)||temppwd.length()<6){
            Toast toast = Toast.makeText(this,"",Toast.LENGTH_LONG);
            toast.setText(getString(R.string.input_password_request));
            toast.show();
            return ;
        }


        if(choicePosition==-1){
            Toast toast = Toast.makeText(this,"",Toast.LENGTH_LONG);
            toast.setText(getString(R.string.please_select_authorization_method));
            toast.show();
            return ;
        }else if(choicePosition==0){
            time = 24*60*60;
            times = 1;
        }else if(choicePosition==1){
            time = 60*60 ;
            times = 1;
        }else if(choicePosition==2){
            int day = day_number_picker.getValue();
            int hour = hour_number_picker.getValue();
            int minute = minute_number_picker.getValue() ;
            times=255 ;
            time = day*24*60*60 + hour*60 + minute;
            if(time<=0){
                Toast toast = Toast.makeText(this,"",Toast.LENGTH_LONG);
                toast.setText(getString(R.string.jjsuo_sp_please_choice_time));
                toast.show();
                return ;
            }
        }

        showInProgress(getString(R.string.ongoing));
        JavaThreadPool.getInstance().excute(new Runnable() {
            @Override
            public void run() {
                String server=dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                JSONObject object = new JSONObject();
                String temp_pwd = edit_temppwd.getText().toString();
                String admin_pwd= et_password.getText().toString();
                object.put("did",oprerationDeviceInfo.getId());
                object.put("pertotal", times);
                object.put("perstime",String.valueOf(time));
                object.put("conpassword",temp_pwd);
                object.put("adminpassword",admin_pwd);
                final String result = HttpRequestUtils.requestoOkHttpPost(server+"/jdm/s3/dln/addtmppwd",object,LockPasswordActivity.this);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        if(result.equals("0")){
                            //成功
                            Toast toast = Toast.makeText(LockPasswordActivity.this,"",Toast.LENGTH_LONG);
                            toast.setText(getString(R.string.fragment_time_success));
                            toast.show();
                            finish();
                        }else if(result.equals("-5")){
                            //密码错误
                            Toast toast = Toast.makeText(LockPasswordActivity.this,"",Toast.LENGTH_LONG);
                            toast.setText(getString(R.string.pw_incrrect));
                            toast.show();
                        }else if(result.equals("-3")){
                            //主机不在线
                            Toast toast = Toast.makeText(LockPasswordActivity.this,"",Toast.LENGTH_LONG);
                            toast.setText(getString(R.string.activity_zhuji_not));
                            toast.show();
                        }else{
                            //其它
                            Toast toast = Toast.makeText(LockPasswordActivity.this,"",Toast.LENGTH_LONG);
                            toast.setText(getString(R.string.operator_error));
                            toast.show();
                        }
                    }
                });
            }
        });

    }

    private void initHongTaiView(){
        findViewById(R.id.temp_password_notice).setVisibility(View.VISIBLE);
        time_picker_linear = findViewById(R.id.time_picker_linear);
        hongtaisuo = findViewById(R.id.hongtaisuo);
        edit_temppwd = findViewById(R.id.edit_temppwd);
        authorization_method_list = findViewById(R.id.authorization_method_list);
        save = findViewById(R.id.save);
        day_number_picker = findViewById(R.id.day_number_picker);//天
        hour_number_picker = findViewById(R.id.hour_number_picker);//时
        minute_number_picker = findViewById(R.id.minute_number_picker);//分
        day_number_picker.setMinValue(0);
        day_number_picker.setMaxValue(99);
        hour_number_picker.setMaxValue(23);
        hour_number_picker.setMinValue(0);
        minute_number_picker.setMaxValue(59);
        minute_number_picker.setMinValue(0);
        day_number_picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        hour_number_picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        minute_number_picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        hongtaisuo.setVisibility(View.VISIBLE);


        authorizations = new String[]{getString(R.string.authorization_method_one_day_at_a_time),getString(R.string.authorization_method_one_hour_at_a_time),
                getString(R.string.authorization_method_you_xian)};
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,8f,getResources().getDisplayMetrics()));
        drawable.setDither(true);
        drawable.setColor(Color.WHITE);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN){
            authorization_method_list.setBackground(drawable);
        }else{
            authorization_method_list.setBackgroundDrawable(drawable);
        }
        ArrayAdapter arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_single_choice,authorizations);
        authorization_method_list.setAdapter(arrayAdapter);
        authorization_method_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                choicePosition = position;
                if(position==2){
                    if(time_picker_linear.getVisibility()==View.GONE){
                        time_picker_linear.setVisibility(View.VISIBLE);
                    }
                }else{
                    if(time_picker_linear.getVisibility()==View.VISIBLE){
                        time_picker_linear.setVisibility(View.GONE);
                    }
                }
            }
        });
        authorization_method_list.setSelection(0);
        hongtaisuo.setVisibility(View.VISIBLE);
        ll_youxiao.setVisibility(View.GONE);
        edit_temppwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
        et_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
        save.setVisibility(View.VISIBLE);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleHongTaiTempPwd();
            }
        });

    }

    private void initView() {

        et_password = (EditText) findViewById(R.id.edit_mana);
        ll_pass = (LinearLayout) findViewById(R.id.ll_pass);
        ll_main = (LinearLayout) findViewById(R.id.ll_main);
        ll_show_password = (LinearLayout) findViewById(R.id.ll_show_password);
        ll_youxiao = (RelativeLayout) findViewById(R.id.ll_youxiao);
        txt_youxiao = (TextView) findViewById(R.id.txt_youxiao);
        popupWindow = new LockPwdTimePopupWindow(this, this);
        ll_youxiao.setOnClickListener(this);
        et_password.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                inputPassword = s.toString();
                showPasswordInPage();
            }
        });
    }

    public void copy(View v) {
        ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        manager.setText(psd);
        Toast.makeText(mContext, getString(R.string.deviceinfo_activity_copy_ok), Toast.LENGTH_SHORT).show();
    }

    public void back(View v) {
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_youxiao:
                //关闭软键盘
                imm.hideSoftInputFromWindow(et_password.getWindowToken(), 0);
                popupWindow.showAtLocation(ll_main, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置
                break;
            case R.id.btn_time5m:
                popupWindow.dismiss();
                txt_youxiao.setText(R.string.jjsuo_ps_time5m);
                authoriseType = AuthoriseCode.AUTHORISE_TYPE_5MIN;
                showPasswordInPage();
                break;
            case R.id.btn_time1h:
                popupWindow.dismiss();
                txt_youxiao.setText(R.string.jjsuo_ps_time1h);
                authoriseType = AuthoriseCode.AUTHORISE_TYPE_1HOUR;
                showPasswordInPage();
                break;
            case R.id.btn_time2h:
                popupWindow.dismiss();
                txt_youxiao.setText(R.string.jjsuo_ps_time2h);
                authoriseType = AuthoriseCode.AUTHORISE_TYPE_2HOUR;
                showPasswordInPage();
                break;
            case R.id.btn_time3h:
                popupWindow.dismiss();
                txt_youxiao.setText(R.string.jjsuo_ps_time3h);
                authoriseType = AuthoriseCode.AUTHORISE_TYPE_3HOUR;
                showPasswordInPage();
                break;
            case R.id.btn_time1d:
                popupWindow.dismiss();
                txt_youxiao.setText(R.string.jjsuo_ps_time1d);
                authoriseType = AuthoriseCode.AUTHORISE_TYPE_1DAY;
                showPasswordInPage();
                break;
            case R.id.btn_time2d:
                popupWindow.dismiss();
                txt_youxiao.setText(R.string.jjsuo_ps_time2d);
                authoriseType = AuthoriseCode.AUTHORISE_TYPE_2DAY;
                showPasswordInPage();
                break;
            case R.id.btn_time3d:
                popupWindow.dismiss();
                txt_youxiao.setText(R.string.jjsuo_ps_time3d);
                authoriseType = AuthoriseCode.AUTHORISE_TYPE_3DAY;
                showPasswordInPage();
                break;
        }
    }

    private void showPasswordInPage(){
        if(oprerationDeviceInfo!=null&&oprerationDeviceInfo.getMc()!=null&&oprerationDeviceInfo.getMc().equals("hongtai")){
            return ;
        }
        if (inputPassword.length() < 6 || inputPassword.length() > 10){
            ll_show_password.setVisibility(View.GONE);
        }else{
            ll_show_password.setVisibility(View.VISIBLE);
            psd = AuthoriseCode.getAuthorisePassword(inputPassword, authoriseType, Calendar.getInstance().get(Calendar.YEAR),
                    Calendar.getInstance().get(Calendar.MONTH) + 1, Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                    Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE));
            for (int i = 0;i<psd.length();i++){
                ((TextView) ll_pass.getChildAt(i)).setText(psd.charAt(i) + "");
            }

        }
    }

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch(msg.what){

            }
            return true;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);

    public class LockPwdTimePopupWindow extends PopupWindow {

        private View mMenuView;
        private Button btn_time5m, btn_time1h,btn_time2h,btn_time3h,btn_time1d,btn_time2d,btn_time3d;

        public LockPwdTimePopupWindow(Context context, View.OnClickListener itemsOnClick) {
            super(context);
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mMenuView = inflater.inflate(R.layout.suo_pwd_item_menu, null);
            btn_time5m = (Button) mMenuView.findViewById(R.id.btn_time5m);
            btn_time1h = (Button) mMenuView.findViewById(R.id.btn_time1h);
            btn_time2h = (Button) mMenuView.findViewById(R.id.btn_time2h);
            btn_time3h = (Button) mMenuView.findViewById(R.id.btn_time3h);
            btn_time1d = (Button) mMenuView.findViewById(R.id.btn_time1d);
            btn_time2d = (Button) mMenuView.findViewById(R.id.btn_time2d);
            btn_time3d = (Button) mMenuView.findViewById(R.id.btn_time3d);

            btn_time5m.setOnClickListener(itemsOnClick);
            btn_time1h.setOnClickListener(itemsOnClick);
            btn_time2h.setOnClickListener(itemsOnClick);
            btn_time3h.setOnClickListener(itemsOnClick);
            btn_time1d.setOnClickListener(itemsOnClick);
            btn_time2d.setOnClickListener(itemsOnClick);
            btn_time3d.setOnClickListener(itemsOnClick);
            //设置SelectPicPopupWindow的View
            this.setContentView(mMenuView);
            //设置SelectPicPopupWindow弹出窗体的宽
            this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            //设置SelectPicPopupWindow弹出窗体的高
            this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            //设置SelectPicPopupWindow弹出窗体可点击
            this.setFocusable(true);
            //设置SelectPicPopupWindow弹出窗体动画效果
            this.setAnimationStyle(R.style.Devices_list_menu_Animation);
            //实例化一个ColorDrawable颜色为半透明
            ColorDrawable dw = new ColorDrawable(0x00000000);
            //设置SelectPicPopupWindow弹出窗体的背景
            this.setBackgroundDrawable(dw);
            //mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
            mMenuView.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {

                    int height = mMenuView.findViewById(R.id.pop_layout).getTop();
                    int y = (int) event.getY();
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (y < height) {
                            dismiss();
                        }
                    }
                    return true;
                }
            });
        }
    }
}
