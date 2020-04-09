package com.smartism.znzk.activity.smartlock;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lsemtmf.genersdk.tools.commen.ToastTools;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.camera.CameraListActivity;
import com.smartism.znzk.activity.device.DeviceCommandHistoryActivity;
import com.smartism.znzk.activity.user.GenstureInitActivity;
import com.smartism.znzk.activity.user.GenstureSettingActivity;
import com.smartism.znzk.activity.view.LockActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.*;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.view.alertview.AlertView;
import com.smartism.znzk.view.alertview.OnItemClickListener;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Created by win7 on 2017/6/23.
 */


public class LockInfoActivity extends ActivityParentActivity implements View.OnClickListener, OnItemClickListener {

    private TextView et_name, et_where;
    private TextView tv_model, tv_status, tv_lockid,tv_miaosu;
    private RelativeLayout rl_bind_camere, rl_history, rl_where, rl_name;
    private DeviceInfo deviceInfo;
    private LinearLayout ll_init;
    //    private DeviceInfo groupDevice;
    private AlertView mAlertViewExt;
    private InputMethodManager imm;
    private EditText etName;
    private int type = 0;
    private ZhujiInfo zhuji;
    private Context mContext;

    //设置锁密码
    RelativeLayout rl_pwd_set ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jjsuo_info);
        mContext = this;
        deviceInfo = (DeviceInfo) getIntent().getSerializableExtra("device");
        zhuji = (ZhujiInfo) getIntent().getSerializableExtra("zhuji");
//        groupDevice = (DeviceInfo) getIntent().getSerializableExtra("group");
        initView();

       if(zhuji.isAdmin()) {
           if (DeviceInfo.TypeCompanyMenu.zhichengwifi.getValue().equals(zhuji.getBrandName())
                   || DeviceInfo.TypeCompanyMenu.zhicheng.getValue().equals(zhuji.getBrandName())
                   || DeviceInfo.TypeCompanyMenu.zhicheng.getValue().equals(deviceInfo.getMc())
                   || DeviceInfo.TypeCompanyMenu.jieaolihua.getValue().equals(deviceInfo.getMc())){
                //wifi锁设置密码
                rl_pwd_set.setVisibility(View.VISIBLE);
                rl_pwd_set.setOnClickListener(this);
            }
        }
    }

    boolean hasBatteryPercent = false;
    private void initView() {
        //设置锁的密码
        rl_pwd_set = findViewById(R.id.rl_pwd_set);

        et_name = (TextView) findViewById(R.id.et_name);
        et_where = (TextView) findViewById(R.id.et_where);
        tv_model = (TextView) findViewById(R.id.tv_model);
        tv_status = (TextView) findViewById(R.id.tv_status);
        tv_lockid = (TextView) findViewById(R.id.tv_lockid);

        rl_bind_camere = (RelativeLayout) findViewById(R.id.rl_bind_camere);
        rl_history = (RelativeLayout) findViewById(R.id.rl_history);
        rl_where = (RelativeLayout) findViewById(R.id.rl_where);
        rl_name = (RelativeLayout) findViewById(R.id.rl_name);
        rl_name.setOnClickListener(this);
        rl_bind_camere.setOnClickListener(this);
        rl_history.setOnClickListener(this);
        rl_where.setOnClickListener(this);
        tv_miaosu = findViewById(R.id.tv_miaosu);

        ll_init = (LinearLayout) findViewById(R.id.ll_init);
        ll_init.setOnClickListener(this);
//        if (zhuji!=null && zhuji.isAdmin())
//            ll_init.setVisibility(View.VISIBLE);

        et_name.setText(deviceInfo.getName() != null ? deviceInfo.getName() : "");
        et_where.setText(deviceInfo.getWhere() != null ? deviceInfo.getWhere() : "");
        tv_model.setText(deviceInfo.getType());
        tv_lockid.setText(deviceInfo.getSlaveId());

        List<CommandInfo> infos = null;
        if(zhuji.getId()==deviceInfo.getId()){
            //锁作为主机
            infos = DatabaseOperator.getInstance().queryAllCommands(deviceInfo.getZj_id());
        }else{
            //锁作为主机下的设备
            infos = DatabaseOperator.getInstance().queryAllCommands(deviceInfo.getId());
        }
        if(infos!=null&&infos.size()>0){
            for(CommandInfo info :infos){
                if(info.getCtype().equals("39")){
                    tv_miaosu.setText(getString(R.string.deviceslist_zhuji_battery_power));
                    tv_status.setText(info.getCommand()+"%");
                    hasBatteryPercent = true;
                    break;
                }
            }
        }
        if(!hasBatteryPercent){
            tv_status.setText(!deviceInfo.isLowb() ? getString(R.string.jjsuo_info_v_normal) : getString(R.string.jjsuo_info_v_unnormal));
        }

        //设备在群组里面时，不显示摄像头绑定
        //目前只有巨将有绑定功能(群组下不可选择绑定摄像头)
        if (/*groupDevice==null
                && */!DeviceInfo.CaMenu.ipcamera.value().equals(deviceInfo.getCa())
                && !DeviceInfo.ControlTypeMenu.zhuji.value().equals(deviceInfo.getControlType())
                && !DeviceInfo.CaMenu.hongwaizhuanfaqi.value().equals(deviceInfo.getCa())) {
            if (MainApplication.app.getAppGlobalConfig().isBipcn()) {
                rl_bind_camere.setVisibility(View.VISIBLE);
            }
        }
    }


    private void closeKeyboard() {
        //关闭软键盘
        imm.hideSoftInputFromWindow(etName.getWindowToken(), 0);
        //恢复位置
        mAlertViewExt.setMarginBottom(0);
    }

    public void back(View v) {
//        jumpToMain();
        finish();
    }

    private void jumpToMain() {
        deviceInfo = DatabaseOperator.getInstance(mContext).queryDeviceInfo(deviceInfo.getId());
        Intent intent = new Intent(this, LockInfoActivity.class);
        intent.putExtra("device", deviceInfo);
        startActivity(intent);
    }

    private AlertView alertView;


    private void initLockStatus() {
//        mHandler.sendEmptyMessageDelayed(10, 15 * 1000);
        JavaThreadPool.getInstance().excute(new Runnable() {
            @Override
            public void run() {
                String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                JSONObject object = new JSONObject();
                object.put("did", deviceInfo.getId());
                String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/dln/init", object, LockInfoActivity.this);

                if ("0".equals(result)) {
                    if (mHandler.hasMessages(10)) {
                        mHandler.removeMessages(10);
                    }
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
//                            cancelInProgress();
                            Toast.makeText(LockInfoActivity.this, getString(R.string.jujiangsuo_init_success),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-3".equals(result)) {
                    if (mHandler.hasMessages(10)) {
                        mHandler.removeMessages(10);
                    }
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(LockInfoActivity.this, getString(R.string.history_response_nodevice),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-4".equals(result)) {
                    if (mHandler.hasMessages(10)) {
                        mHandler.removeMessages(10);
                    }
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(LockInfoActivity.this, getString(R.string.operator_error),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-5".equals(result)) {
                    if (mHandler.hasMessages(10)) {
                        mHandler.removeMessages(10);
                    }
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(LockInfoActivity.this, getString(R.string.net_error_operationfailed),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    if (mHandler.hasMessages(10)) {
                        mHandler.removeMessages(10);
                    }
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(LockInfoActivity.this, getString(R.string.jujiangsuo_init_error),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == 3 && resultCode == RESULT_OK) {
            Toast.makeText(this, getString(R.string.jujiangsuo_init_optioning), Toast.LENGTH_LONG).show();
            initLockStatus();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_init:
                alertView =
                        new AlertView(getString(R.string.activity_weight_notice),
                                getString(R.string.jjsuo_init_notice),
                                getString(R.string.deviceslist_server_leftmenu_delcancel),
                                new String[]{getString(R.string.confirm)}, null,
                                mContext, AlertView.Style.Alert,
                                new OnItemClickListener() {

                                    @Override
                                    public void onItemClick(Object o, final int position) {
                                        if (position != -1) {
                                        }
                                    }
                                });
                alertView.show();
                break;
            case R.id.rl_name:
                type = 1;
                updateInfo(type);
                break;
            case R.id.rl_where:
                type = 2;
                updateInfo(type);
                break;
            case R.id.rl_bind_camere:
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), CameraListActivity.class);
                intent.putExtra("device", deviceInfo);
                startActivity(intent);
                break;
            case R.id.rl_history:
                Intent deviceIntent = new Intent();
                deviceIntent.setClass(this, DeviceCommandHistoryActivity.class);
                deviceIntent.putExtra("device", deviceInfo);
                startActivity(deviceIntent);
                break;
            case R.id.rl_pwd_set:
                //弹出密码设置框
                type=3;
                updateInfo(type);
                break;
        }
    }

    private void updateInfo(int type) {
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        String title = "";
        String content = "";
        if (type == 1) {
            title = getString(R.string.input_device_name);
            content = deviceInfo.getName();
        } else if (type == 2) {
            title = getString(R.string.jjsuo_info_where);
            content = deviceInfo.getWhere();
        }else if(type==3){
            title = getString(R.string.register_pass_button);
            content = getString(R.string.activity_changepassword_new_hit);
        }
        //拓展窗口
        mAlertViewExt = new AlertView(null, title, getString(R.string.cancel), null, new String[]{getString(R.string.compele)}, this, AlertView.Style.Alert, this);
        ViewGroup extView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.activity_addzhuji_alertext_form, null);
        etName = (EditText) extView.findViewById(R.id.etName);

        etName.setText(content);
        etName.setHint(title);

        if(type==3){
            //每次都重新创建，不用恢复了
            etName.setInputType(InputType.TYPE_CLASS_NUMBER);//数字
            etName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});//限制字数
            etName.setText("");
            etName.setHint(content);
        }

        etName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                //输入框出来则往上移动
                boolean isOpen = imm.isActive();
                mAlertViewExt.setMarginBottom(isOpen && focus ? 120 : 0);
            }
        });
        mAlertViewExt.addExtView(extView);
        mAlertViewExt.show();
    }

    @Override
    public void onItemClick(Object o, int position) {
        closeKeyboard();
        //判断是否是拓展窗口View，而且点击的是非取消按钮
        if (o == mAlertViewExt && position != AlertView.CANCELPOSITION) {
            if(type==3){
                String pwd = etName.getText().toString();
                if(!TextUtils.isEmpty(pwd)){
                    if(pwd.length()==6){
                        //进行密码设置
                        showInProgress(getString(R.string.device_set_tip_inupdate),true,false);
                            //捷奥利华,志诚,网关锁
                            if(deviceInfo!=null&&deviceInfo.getCa()!=null&&deviceInfo.getCa().equals(DeviceInfo.CaMenu.zhinengsuo.value())){
                                JavaThreadPool.getInstance().excute(new PasswordSet(deviceInfo.getId(),pwd));
                            }else if(deviceInfo!=null&&deviceInfo.getCa()!=null&&deviceInfo.getCa().equals(DeviceInfo.CaMenu.wifizns.value())){
                                JavaThreadPool.getInstance().excute(new PasswordSet(zhuji.getId(),pwd));
                            }
                    }else{
                        ToastTools.short_Toast(this,getString(R.string.zhicheng_set_pwd_failed));
                    }
                }else{
                    ToastTools.short_Toast(this,getString(R.string.login_tip_password_empty));
                }

            }else{
                String name = etName.getText().toString();
                showInProgress(getString(R.string.device_set_tip_inupdate), false, true);
                JavaThreadPool.getInstance().excute(new UpdateInfoThread(name));
            }
            return;
        }
    }
    //wifi锁密码设置
    private class PasswordSet implements  Runnable{

        long id ;
        String password ;
        PasswordSet(long zjId,String password){
            id = zjId ;
            this.password  = password ;
        }

        @Override
        public void run() {
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("did", id);
            JSONArray array = new JSONArray();
            JSONObject o = new JSONObject();
            o.put("vkey","pwd_control");
            o.put("value",password);
            array.add(o);
            object.put("vkeys", array);
            String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/d/p/set", object, LockInfoActivity.this);
            if (result != null && result.equals("0")) {
                DatabaseOperator.getInstance().insertOrUpdateDeviceCommand(id,"pwd_control",password);
               //设置成功
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        ToastTools.short_Toast(mContext,getString(R.string.success));
                    }
                });
            } else {
               runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                       cancelInProgress();
                       ToastTools.short_Toast(mContext,getString(R.string.operator_error));
                   }
               });
            }
        }
    }

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch(msg.what){
                case 10:
                    cancelInProgress();
                    if (msg.obj != null) {
                        JSONObject resultBack = (JSONObject) msg.obj;
                        ContentValues values = new ContentValues();
                        if (resultBack.get("deviceName") != null) {
                            et_name.setText(String.valueOf(resultBack.get("deviceName")));
//                    deviceInfo.setName(String.valueOf(resultBack.get("deviceName")));
                            values.put("device_name", resultBack.getString("deviceName"));
                        }
                        if (resultBack.get("deviceWhere") != null) {
                            et_where.setText(String.valueOf(resultBack.get("deviceWhere")));
//                    deviceInfo.setWhere(String.valueOf(resultBack.get("deviceWhere")));
                            values.put("device_where", resultBack.getString("deviceWhere"));
                        }
                        if (values.containsKey("device_name") || values.containsKey("device_where")) {
                            try {
                                DatabaseOperator.getInstance().getWritableDatabase().update("DEVICE_STATUSINFO", values, "id = ?", new String[]{String.valueOf(resultBack.getLongValue("deviceId"))});
                            } catch (Exception e) {
                                Log.e(TAG, "获取数据库失败");
                            }
                        }
                    }

//                Intent intent = new Intent();
//                intent.putExtra("device", deviceInfo);
//                setResult(2, intent);
//                sendBroadcast(new Intent(Actions.REFRESH_DEVICES_LIST));
                    Toast.makeText(LockInfoActivity.this, getString(R.string.device_set_tip_success), Toast.LENGTH_LONG).show();
                break;
            }
            return true;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);





    class UpdateInfoThread implements Runnable {
        private String name;

        public UpdateInfoThread(String name) {
            this.name = name;

        }

        @Override
        public void run() {
            String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("id", deviceInfo.getId());
//					object.put("appid", dcsp.getLong(Constant.LOGIN_APPID, 0));
//					object.put("uid",dcsp.getLong(Constant.LOGIN_APPID, 0));
//					object.put("code",dcsp.getString(Constant.LOGIN_CODE,""));
            if (type == 1) {
                if (!name.equals("")) {
                    object.put("name", name);
                }
            } else {
                if (!name.equals("")) {
                    object.put("where", name);
                }
            }


//            String secuirt = set_num_edit.getText().toString();
//            object.put("number", secuirt);

            String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/d/uinfo", object, LockInfoActivity.this);

            if ("-3".equals(result)) {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(LockInfoActivity.this, getString(R.string.device_set_tip_nodevice), Toast.LENGTH_LONG).show();
                    }
                });
            } else if (!StringUtils.isEmpty(result)) {
                JSONObject resultBack = null;
                try {
                    resultBack = JSON.parseObject(result);
                } catch (Exception e) {
                    LogUtil.e(getApplicationContext(), TAG, "解密错误：：", e);
                }
                if (resultBack == null) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(LockInfoActivity.this, getString(R.string.device_set_tip_responseerr), Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }
//                updateDBDate(resultBack);
                Message msg = Message.obtain();
                msg.what = 10;
                msg.obj = resultBack;
                mHandler.sendMessage(msg);

            }
        }

        private void updateDBDate(JSONObject resultBack) {
            if (DeviceInfo.ControlTypeMenu.zhuji.value().equals(deviceInfo.getControlType())) {
                ContentValues values = new ContentValues();
                if (resultBack.get("deviceName") != null) {
                    values.put("name", resultBack.getString("deviceName"));
                } else {
                    values.put("name", "");
                }
                if (resultBack.get("deviceWhere") != null) {
                    values.put("dwhere", resultBack.getString("deviceWhere"));
                } else {
                    values.put("dwhere", "");
                }
                try {
                    DatabaseOperator.getInstance().getWritableDatabase().update("ZHUJI_STATUSINFO", values, "id = ?", new String[]{String.valueOf(resultBack.getLongValue("deviceId"))});
                } catch (Exception e) {
                    Log.e(TAG, "获取数据库失败");
                }
            } else {
                ContentValues values = new ContentValues();
                if (resultBack.get("deviceName") != null) {
                    values.put("device_name", resultBack.getString("deviceName"));
                } else {
                    values.put("device_name", "");
                }
                if (resultBack.get("deviceWhere") != null) {
                    values.put("device_where", resultBack.getString("deviceWhere"));
                } else {
                    values.put("device_where", "");
                }
                try {
                    DatabaseOperator.getInstance().getWritableDatabase().update("DEVICE_STATUSINFO", values, "id = ?", new String[]{String.valueOf(resultBack.getLongValue("deviceId"))});
                } catch (Exception e) {
                    Log.e(TAG, "获取数据库失败");
                }
            }
        }
    }


}
