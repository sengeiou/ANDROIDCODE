package com.smartism.znzk.xiongmai.activities;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.device.DeviceMainActivity;
import com.smartism.znzk.domain.camera.CameraInfo;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.ToastUtil;
import com.smartism.znzk.zhicheng.activities.MZBaseActivity;
import com.smartism.znzk.zhicheng.tasks.HttpAsyncTask;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.smartism.znzk.util.DataCenterSharedPreferences.Constant.SECURITY_SETTING_PWD;
import static com.smartism.znzk.zhicheng.tasks.HttpAsyncTask.Zhuji_IPC_ADD_FLAG;

public class XiongmaiPasswordModifyActivity extends MZBaseActivity implements HttpAsyncTask.IHttpResultView {



    private EditText mOriginalEdit;
    private EditText mNewEdit, mConfirmEdit;
    private Button mChangeBtn ;

    private CameraInfo mCameraInfo ;
    private long deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState==null){
            mCameraInfo = (CameraInfo) getIntent().getSerializableExtra("camera_info");
            deviceId = getIntent().getLongExtra("device_id",0);
        }else{
            mCameraInfo = (CameraInfo) savedInstanceState.getSerializable("camera_info");
            deviceId = savedInstanceState.getLong("device_id");
        }

        setTitle(getString(R.string.register_pass_button));
        initView();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong("device_id",deviceId);
        outState.putSerializable("camera_info",mCameraInfo);
        super.onSaveInstanceState(outState);
    }

    private void initView() {
        mOriginalEdit =findViewById(R.id.original_password_edit);
        mNewEdit = findViewById(R.id.new_password_edit);
        mConfirmEdit = findViewById(R.id.confirm_password_edit);
        mChangeBtn = findViewById(R.id.change_password_btn);


        mChangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkInput()){
                    requestData(mConfirmEdit.getText().toString());
                }
            }
        });
    }

    private void requestData(String password){
        JSONObject pJsonObject = new JSONObject();
        pJsonObject.put("c",mCameraInfo.getC());//摄像头品牌
        pJsonObject.put("id", mCameraInfo.getId());//摄像头序列号
        pJsonObject.put("n", mCameraInfo.getN());//摄像头名称
        pJsonObject.put("p", password);//摄像头密码
        pJsonObject.put("did",deviceId);//摄像头主机id


        //请求数据
        new HttpAsyncTask(this, Zhuji_IPC_ADD_FLAG).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,pJsonObject);

    }


    private boolean checkInput(){
        if(!checkOriginalPwd(mOriginalEdit.getText().toString())){
            //原始密码不合法
            return false ;
        }

        if((!checkPassword(mNewEdit.getText().toString())||!checkPassword(mConfirmEdit.getText().toString()))){
            //新密码不合法
            ToastUtil.shortMessage(getString(R.string.weak_password));
            return false ;
        }

        //检测密码的一致性
        if(!mNewEdit.getText().toString().equals(mConfirmEdit.getText().toString())){
            ToastUtil.longMessage(getString(R.string.zhuji_pwd_confirm_error));
            return false ;
        }

        return true ;

    }

    //6-30位，必须包含字母和数字
    private  boolean checkPassword(String password) {
        Pattern Password_Pattern = Pattern.compile("^[a-zA-Z0-9]{6,30}$");
        Matcher matcher = Password_Pattern.matcher(password);
        if (matcher.matches()) {
            return true;
        }
        return false;
    }

    //检查当前密码
    private boolean checkOriginalPwd(String password){
        if(TextUtils.isEmpty(password)){
            ToastUtil.longMessage(getString(R.string.zhuji_pwd_current_input));
            return false ;
        }

        if(!password.equals(mCameraInfo.getOriginalP())){
            ToastUtil.longMessage(getString(R.string.zhuji_pwd_original_error));
            return false ;
        }
        return true ;
    }

    @Override
    public int setLayoutId() {
        return R.layout.activity_xiongmai_password_modify;
    }

    @Override
    public void setResult(int flag, String result) {
        switch (flag){
            case Zhuji_IPC_ADD_FLAG:
                if ("-3".equals(result)) {
                    ToastUtil.shortMessage(getString(R.string.net_error_nodata));
                } else if ("-5".equals(result)) {
                    ToastUtil.shortMessage(getString(R.string.device_not_getdata));
                } else if ("0".equals(result)) {
                    ToastUtil.shortMessage(getString(R.string.success));
                    //密码保存到本地
                    String confirmPwd = mConfirmEdit.getText().toString() ;
                    DataCenterSharedPreferences.getInstance(getApplicationContext()
                            , DataCenterSharedPreferences.Constant.XM_CONFIG).putString(mCameraInfo.getId() + SECURITY_SETTING_PWD,confirmPwd).commit();
                    Intent intent = new Intent();
                    intent.setClass(this,DeviceMainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                break ;
        }
    }

    @Override
    public void success(String message) {

    }

    @Override
    public void error(String message) {

    }
}
