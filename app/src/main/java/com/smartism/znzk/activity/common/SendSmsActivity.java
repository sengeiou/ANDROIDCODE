package com.smartism.znzk.activity.common;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.lsemtmf.genersdk.tools.commen.ToastTools;
import com.smartism.znzk.R;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.ToastUtil;
import com.smartism.znzk.view.alertview.AlertView;
import com.smartism.znzk.view.alertview.OnItemClickListener;
import com.smartism.znzk.zhicheng.activities.MZBaseActivity;

public class SendSmsActivity extends MZBaseActivity implements View.OnClickListener {

    private static final String LOG_DEBUG = "SendSmsActivity";
    public static final String SHARED_KEY_SUFFIX = "SEND_SMS_NUMBER";

    private final String SEND_OPNE_CONTENT = "AT+RMCTRL=00001004,1\\r\\n";
    private final String SEND_CLOSE_CONTENT = "AT+RMCTRL=00001004,0\\r\\n";
    private EditText mInputNumber ;
    private ImageView mOpenImg  , mCloseImg ;
    private TextView mOpenTv,mCloseTv;
    private Button mSendButton ;
    private boolean mCurrentStatus  = true;  //当前是否是点击开状态
    private long zhujiId ;
    private ContentObserver mContentSmsObserver ;
    private boolean mReceiverUserSend = false ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState==null){
            zhujiId = getIntent().getLongExtra("zhuji_id",-1);
        }else{
            savedInstanceState.getLong("zhuji_id",-1);
        }
        setTitle(getResources().getString(R.string.ssa_title));

        bindView();
        bindViewEvent();
        initViewStatus();

        registerSmsContentObserver();//注册监听用户是否发送短信

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong("zhuji_id",zhujiId);
        super.onSaveInstanceState(outState);
    }

    private void registerSmsContentObserver(){
        Uri uri = Uri.parse("content://sms/");
        mContentSmsObserver = new ContentObserver(new Handler()) {
            //可能会回调很多次，需要过滤
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange, uri);
                mReceiverUserSend = true ; //回调该方法，表示用户点击了短信应用的发送短信按钮
            }
        };
        getContentResolver().registerContentObserver(uri,true,mContentSmsObserver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mReceiverUserSend){
            ToastUtil.shortMessage(getString(R.string.ssa_send_sms_success));
            mReceiverUserSend = false ; //防止会多次回调
        }
    }

    private void sendSMS(String number, String message){
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"+number));
        intent.putExtra("sms_body", message);
        startActivity(intent);
    }



    private void bindView(){
        mInputNumber = findViewById(R.id.input_number);
        mOpenImg = findViewById(R.id.open_img);
        mCloseImg = findViewById(R.id.close_img);
        mOpenTv = findViewById(R.id.open_tv);
        mCloseTv = findViewById(R.id.close_tv);
        mSendButton = findViewById(R.id.send_btn);
    }

    private void bindViewEvent(){
        mCloseImg.setOnClickListener(this);
        mOpenImg.setOnClickListener(this);
        mSendButton.setOnClickListener(this);

        mInputNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().length()==11){
                    mSendButton.setEnabled(true);
                    mSendButton.setBackgroundResource(R.drawable.zhzj_default_button);
                }else{
                    mSendButton.setEnabled(false);
                    mSendButton.setBackgroundColor(getResources().getColor(R.color.gray_low));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void initViewStatus(){
        setOpenStatus(R.drawable.device_item_one_button_bg,getResources().getColor(R.color.zhzj_default));
        setCloseStatus(R.drawable.device_item_on_black_bg,getResources().getColor(R.color.black));

        mSendButton.setEnabled(false);
        mSendButton.setBackgroundColor(getResources().getColor(R.color.gray_low));

        String phoneNumber = getSaveSendPhoneNumber(getApplicationContext(),String.valueOf(zhujiId));
        mInputNumber.setText(phoneNumber);
        mInputNumber.setSelection(mInputNumber.getText().toString().length());
    }

    private void setOpenStatus(int resId, int textColor){
        mOpenImg.setImageResource(resId);
        mOpenTv.setTextColor(textColor);
    }

    private void setCloseStatus(int resId,int textColor){
        mCloseImg.setImageResource(resId);
        mCloseTv.setTextColor(textColor);
    }

    @Override
    public int setLayoutId() {
        return R.layout.activity_send_sms_layout;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.open_img:
                mCurrentStatus = true ;
                setOpenStatus(R.drawable.device_item_one_button_bg,getResources().getColor(R.color.zhzj_default));
                setCloseStatus(R.drawable.device_item_on_black_bg,getResources().getColor(R.color.black));
                break;
            case R.id.close_img:
                mCurrentStatus = false ;
                setOpenStatus(R.drawable.device_item_on_black_bg,getResources().getColor(R.color.black));
                setCloseStatus(R.drawable.device_item_one_button_bg,getResources().getColor(R.color.zhzj_default));
                break ;
            case R.id.send_btn:
                if(mCurrentStatus){
                    sendSMS(mInputNumber.getText().toString(),SEND_OPNE_CONTENT);
                }else{
                    sendSMS(mInputNumber.getText().toString(),SEND_CLOSE_CONTENT);
                }

                break ;
        }
    }

    private static String getSaveSendPhoneNumber(Context context, String zhujiId){
        String password = DataCenterSharedPreferences.getInstance(context, DataCenterSharedPreferences.Constant.CONFIG).getString(zhujiId+SHARED_KEY_SUFFIX, "");
        return password ;
    }

    private static boolean saveSendSMSNumber(Context context,String zhujiId,String number){
        if(TextUtils.isEmpty(number)){
            return false;
        }
        return  DataCenterSharedPreferences.getInstance(context,DataCenterSharedPreferences.Constant.CONFIG).putString(zhujiId+SHARED_KEY_SUFFIX,number).commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //销毁前保存一下用户输入过的号码
        if(mSendButton.isEnabled()){
            saveSendSMSNumber(getApplicationContext(),String.valueOf(zhujiId),mInputNumber.getText().toString());
        }

        if(mContentSmsObserver!=null){
            getContentResolver().unregisterContentObserver(mContentSmsObserver);
        }
    }
}
