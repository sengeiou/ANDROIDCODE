package com.smartism.znzk.activity.scene;

;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.smartism.znzk.R;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.ToastUtil;
import com.smartism.znzk.zhicheng.activities.MZBaseActivity;

/*
* author :mz
* */
public class SecenControlBySmsActivity extends MZBaseActivity implements View.OnClickListener {

    public static final String SHARED_KEY_SUFFIX = "SEND_SMS_NUMBER";
    private EditText mInputNumber ;
    private Button mSendButton ;
    private int  mCurrentStatus  = 0;  //当前是否是点击布防状态 0布防 1撤防...
    private long zhujiId ;
    private ImageView mChefangImg,mBufangImg ;
    private ContentObserver mContentObserver ;
    private boolean isReceiverSend = false ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState==null){
            zhujiId = getIntent().getLongExtra("zhuji_id",-1);
        }else{
            zhujiId = savedInstanceState.getLong("zhuji_id",-1);
        }
        setTitle(getString(R.string.scba_title));

        bindView();
        bindViewEvent();
        initViewStatus();

        registerSmsSendContentObserver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isReceiverSend){
            ToastUtil.shortMessage(getString(R.string.scba_after_send_tip));
            isReceiverSend = false ; //防止会多次回调
        }
    }

    private void registerSmsSendContentObserver(){
        Uri uri = Uri.parse("content://sms/");
        mContentObserver = new ContentObserver(new Handler()) {
            //这个方法会回调多次，需要过滤
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange, uri);
                isReceiverSend = true ;

            }
        };

        getContentResolver().registerContentObserver(uri,true,mContentObserver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong("zhuji_id",zhujiId);
        super.onSaveInstanceState(outState);
    }

    private void sendSMS(String number,String message){
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"+number));
        intent.putExtra("sms_body", message);
        startActivity(intent);
    }



    private void bindView(){
        mChefangImg = findViewById(R.id.close_img);
        mBufangImg = findViewById(R.id.open_img);
        mInputNumber = findViewById(R.id.input_number);
        mSendButton = findViewById(R.id.send_btn);
    }

    private void bindViewEvent(){
        mChefangImg.setOnClickListener(this);
        mBufangImg.setOnClickListener(this);
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

    private void setImgStatus(int resId,ImageView imageView){
        imageView.setImageResource(resId);
    }

    private void initViewStatus(){

        mSendButton.setEnabled(false);
        mSendButton.setBackgroundColor(getResources().getColor(R.color.gray_low));

        String phoneNumber = getSaveSendPhoneNumber(getApplicationContext(),String.valueOf(zhujiId));
        mInputNumber.setText(phoneNumber);
        mInputNumber.setSelection(mInputNumber.getText().toString().length());


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.open_img:
                mCurrentStatus = 0 ;
                setImgStatus(R.drawable.zhzj_sy_shefang_hover,mBufangImg);
                setImgStatus(R.drawable.zhzj_sy_chefang,mChefangImg);
                break ;
            case R.id.close_img:
                mCurrentStatus = 1 ;
                setImgStatus(R.drawable.zhzj_sy_chefang_hover,mChefangImg);
                setImgStatus(R.drawable.zhzj_sy_shefang,mBufangImg);
                break ;
            case R.id.send_btn:
                StringBuilder sb  = new StringBuilder();
                sb.append("Scene:")
                        .append("\"")
                        .append(String.valueOf(zhujiId))
                        .append("\"")
                        .append(",")
                        .append("\"");
                switch (mCurrentStatus){
                    case 0:
                        //布防短信内容
                        sb.append("0").append("\"");
                        break ;
                    case 1:
                        //撤防短信内容
                        sb.append("1").append("\"");
                        break ;
                }
                sendSMS(mInputNumber.getText().toString(),sb.toString());
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

        if(mContentObserver!=null){
            getContentResolver().unregisterContentObserver(mContentObserver);
        }
    }

    @Override
    public int setLayoutId() {
        return R.layout.activity_secen_control_by_sms;
    }
}
