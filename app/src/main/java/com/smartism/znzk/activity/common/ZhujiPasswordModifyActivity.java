package com.smartism.znzk.activity.common;



import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.util.ToastUtil;
import com.smartism.znzk.zhicheng.activities.MZBaseActivity;
import com.smartism.znzk.zhicheng.tasks.HttpAsyncTask;
import com.smartism.znzk.zhicheng.tasks.LoadZhujiAndDeviceTask;

import java.util.List;

public class ZhujiPasswordModifyActivity extends MZBaseActivity implements View.OnClickListener, HttpAsyncTask.IHttpResultView {

    private Button mChangePasswordBtn ;
    private LinearLayout mOriginalLayout ;
    private EditText mOriginalEdit,mNewPwdEdit,mConfirmPwdEdit;
    private String mZhujiPwd ; //保存主机密码
    private long mZhujiId ;

    Toast toast;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState==null){
            mZhujiId = getIntent().getLongExtra("device_id",-1);
        }else{
            mZhujiId = savedInstanceState.getLong("device_id",-1);
        }
        setTitle(getString(R.string.zhuji_pwd_setting_title));
        bindView();
        bindEvent();
        initData();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong("device_id",mZhujiId);
        super.onSaveInstanceState(outState);
    }

    private void initData(){
        new LoadZhujiAndDeviceTask().queryAllCommandInfo(mZhujiId, new LoadZhujiAndDeviceTask.ILoadResult<List<CommandInfo>>() {
            @Override
            public void loadResult(List<CommandInfo> result) {
                for(CommandInfo commandInfo:result){
                    if(commandInfo.getCtype().equals("145")){
                        mZhujiPwd = commandInfo.getCommand() ;
                        if(!TextUtils.isEmpty(mZhujiPwd)){
                            mOriginalLayout.setVisibility(View.VISIBLE);
                        }else{
                            mOriginalLayout.setVisibility(View.GONE);
                        }
                        break ;
                    }
                }
            }
        });
    }

    private boolean  checkInput(){
        //首先检查输入长度是否符合要求，其次验证原始密码，最后发送请求
        if(mOriginalLayout.getVisibility()==View.VISIBLE){
            //可见验证
            if(!checkOriginalPwd(4,8,mOriginalEdit)){
                return false ;
            }
        }


        if(!checkNewPwd(4,8,mNewPwdEdit)){
            return false ;
        }

        if(!checkComfirmPwd(4,8,mConfirmPwdEdit,mNewPwdEdit.getText().toString())){
            return false ;
        }

        return true ; //所有输入合法，进行下一步
    }

    private boolean checkComfirmPwd(int min,int max,EditText editText,String confirmString){
        String value = editText.getText().toString() ;
        if(TextUtils.isEmpty(value)){
            ToastUtil.longMessage(getString(R.string.zhuji_pwd_confirm_tip));
            return false ;
        }

        if(value.length()<min||value.length()>max){
            ToastUtil.longMessage(getString(R.string.zhuji_pwd_length_error));
            return false ;
        }

        if(!value.equals(confirmString)){
            ToastUtil.longMessage(getString(R.string.zhuji_pwd_confirm_error));
            return false ;
        }

        return true ;
    }

    private boolean checkNewPwd(int min,int max,EditText editText){
        String value = editText.getText().toString() ;
        if(TextUtils.isEmpty(value)){
            ToastUtil.longMessage(getString(R.string.zhuji_pwd_new_tip));
            return false ;
        }
        if(value.length()<min||value.length()>max){
            ToastUtil.longMessage(getString(R.string.zhuji_pwd_length_error));
            return false ;
        }
        return true ;
    }
    private boolean checkOriginalPwd(int min,int max,EditText editText){
        String value = editText.getText().toString() ;
        if(TextUtils.isEmpty(value)){
            ToastUtil.longMessage(getString(R.string.zhuji_pwd_current_input));
            return false ;
        }
        if(value.length()<min||value.length()>max){
            ToastUtil.longMessage(getString(R.string.zhuji_pwd_length_error));
            return false ;
        }
        if(!value.equals(mZhujiPwd)){
            ToastUtil.longMessage(getString(R.string.zhuji_pwd_original_error));
            return false ;
        }
        return true ;
    }

    private void bindView(){
        mChangePasswordBtn = findViewById(R.id.change_password_btn);
        mOriginalLayout = findViewById(R.id.original_layout);
        mOriginalEdit  = findViewById(R.id.original_password_edit);
        mNewPwdEdit = findViewById(R.id.new_password_edit);
        mConfirmPwdEdit = findViewById(R.id.confirm_password_edit);
    }

    private void bindEvent(){
        mChangePasswordBtn.setOnClickListener(this);
    }

    @Override
    public int setLayoutId() {
        return R.layout.activity_zhuji_password_modify;
    }

    private void requestData(){
        JSONObject pJsonObject = new JSONObject();
        pJsonObject.put("did", mZhujiId);
        JSONArray array = new JSONArray();
        JSONObject object = new JSONObject();
        object.put("vkey", "145");
        object.put("value",mNewPwdEdit.getText().toString());
        array.add(object);
        pJsonObject.put("vkeys", array);
        //请求数据
        new HttpAsyncTask(this,HttpAsyncTask.Zhuji_SET_URL_FLAG).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,pJsonObject);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.change_password_btn:
                if(checkInput()){
                    //所有输入合法，开始提交请求
                    requestData();
                }
                break ;
        }
    }

    @Override
    public void setResult(int flag, String result) {
        if(flag==HttpAsyncTask.Zhuji_SET_URL_FLAG){
            if ("-3".equals(result)) {
                ToastUtil.shortMessage(getString(R.string.net_error_nodata));
            } else if ("-5".equals(result)) {
                ToastUtil.shortMessage(getString(R.string.device_not_getdata));
            } else if ("0".equals(result)) {
                ToastUtil.shortMessage(getString(R.string.success));
                finish();
            }else if("-4".equals(result)){
                ToastUtil.shortMessage(getString(R.string.activity_zhuji_not));
            }
        }
    }

    @Override
    public void success(String message) {

    }

    @Override
    public void error(String message) {

    }
}
