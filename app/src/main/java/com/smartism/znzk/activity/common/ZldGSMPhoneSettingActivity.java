package com.smartism.znzk.activity.common;


import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.alibaba.fastjson.JSONObject;
import com.lsemtmf.genersdk.tools.commen.ToastTools;
import com.smartism.znzk.R;
import com.smartism.znzk.zhicheng.activities.MZBaseActivity;
import com.smartism.znzk.zhicheng.tasks.HttpAsyncTask;

import static com.smartism.znzk.zhicheng.tasks.HttpAsyncTask.Zhuji_GSM_INIT_STATUS_FALG;
import static com.smartism.znzk.zhicheng.tasks.HttpAsyncTask.Zhuji_GSM_PHONE_ADD_FLAG;
import static com.smartism.znzk.zhicheng.tasks.HttpAsyncTask.Zhuji_GSM_PHONE_DELETE_FLAG;


/*
* 致利德添加报警号码
* */
public class ZldGSMPhoneSettingActivity extends MZBaseActivity implements HttpAsyncTask.IHttpResultView {

    private TextView mGroupPhoneTipTv;
    private Button mSave ;
    private EditText mInputPhoneEdit;
    private String group = "";
    private String phone ="";
    private final int TYPE = 0 ; //gsm
    private long mZhujiId ;
    private long id = -1 ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState==null){
            group = getIntent().getStringExtra("group");
            phone = getIntent().getStringExtra("phone");
            mZhujiId = getIntent().getLongExtra("zhuji_id",-1);
            id = getIntent().getLongExtra("id",-1);
        }else{
            mZhujiId = savedInstanceState.getLong("zhuji_id");
            group = savedInstanceState.getString("group");
            phone = savedInstanceState.getString("phone");
            id = savedInstanceState.getLong("id");
        }
        setTitle(getString(R.string.devices_list_menu_dialog_gsmsetting));
        initViews();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong("id",id);
        outState.putString("group",group);
        outState.putLong("zhuji_id",mZhujiId);
        outState.putString("phone",phone);
        super.onSaveInstanceState(outState);
    }

    private void initViews(){
        mGroupPhoneTipTv = findViewById(R.id.group_phone_tip);
        mSave = findViewById(R.id.save);
        mInputPhoneEdit = findViewById(R.id.input_time);

        mGroupPhoneTipTv.setText(getString(R.string.zld_gsmphone_tip,Integer.parseInt(group)+1));
        mInputPhoneEdit.setText(phone);
        mInputPhoneEdit.setSelection(phone.length());

        //设置button显示文字，如果有报警号码，显示更新，没有显示添加
        if(TextUtils.isEmpty(phone)){
            mSave.setText(getString(R.string.activity_scene_item_add));
        }else{
            mSave.setText(getString(R.string.update_prompt_title));
        }
    }

    //创建右上角删除菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.zc_air_delete_menu,menu);

        //如果没有号码过来，就不显示菜单，否则显示菜单
        if(TextUtils.isEmpty(phone)){
            return false ;
        }else{
            return super.onCreateOptionsMenu(menu);
        }

    }


    //删除菜单相应操作
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.air_delete:
                JSONObject object = new JSONObject();
                object.put("did",mZhujiId);
                object.put("id",id);
                new HttpAsyncTask(this,Zhuji_GSM_PHONE_DELETE_FLAG).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,object);
                return true ;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //当前活动显示的布局
    @Override
    public int setLayoutId() {
        return R.layout.activity_zld_gsmphone_setting_layout;
    }

    //修改号码请求
    private void requestAddPhone(String phoneNumber){
        JSONObject object = new JSONObject();
        object.put("did",mZhujiId);
        object.put("type", TYPE);
        object.put("tel",phoneNumber);
        object.put("telId",Integer.parseInt(group));
        new HttpAsyncTask(this,Zhuji_GSM_PHONE_ADD_FLAG).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,object);
    }

    //修改按钮的点击事件
    public void saveEvent(View view) {
        String phoneNumber = mInputPhoneEdit.getText().toString();
        if(TextUtils.isEmpty(phoneNumber)){
            ToastTools.short_Toast(getApplicationContext(),getString(R.string.register_tip_phone_empty));
            return;
        }

        //上传数据保存
        requestAddPhone(phoneNumber);
    }

    //请求返回的回调接口
    @Override
    public void setResult(int flag, String result) {
        if(flag==HttpAsyncTask.Zhuji_GSM_PHONE_ADD_FLAG){
            if(result.equals("0")){
                Toast.makeText(getApplicationContext(), getString(R.string.success),
                        Toast.LENGTH_LONG).show();
                finish();
            }else if(result.equals("-3")){
                ToastTools.short_Toast(getApplicationContext(),getString(R.string.net_error_nopermission));
            }else if("-4".equals(result)){
                ToastTools.short_Toast(this,getResources().getString(R.string.activity_zhuji_not));
            }else{
                ToastTools.short_Toast(getApplicationContext(),getString(R.string.operator_error));
            }
        }else if(flag==HttpAsyncTask.Zhuji_GSM_PHONE_DELETE_FLAG){
            if(result.equals("0")){
                Toast.makeText(getApplicationContext(), getString(R.string.success),
                        Toast.LENGTH_LONG).show();
                finish();
            }else if(result.equals("-3")){
                ToastTools.short_Toast(getApplicationContext(),getString(R.string.net_error_nopermission));
            }else if("-4".equals(result)){
                ToastTools.short_Toast(this,getResources().getString(R.string.activity_zhuji_not));
            }else{
                ToastTools.short_Toast(getApplicationContext(),getString(R.string.operator_error));
            }
        }
    }
}
