package com.smartism.znzk.activity.device.add;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartism.znzk.R;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.domain.HeaterShadowInfo;
import com.smartism.znzk.util.Actions;

public class AddZhujiGuideActivity extends AppCompatActivity {
    private ImageView icon_zhuji,iv_back;
    private CheckBox select_sure;
    private Button btn_next;
    boolean isMainList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_zhuji_guide);
        isMainList = getIntent().getBooleanExtra("isMainList", false);
        initView();
        initEvent();
        initDate();
        }

    private void initDate() {
        btn_next.setEnabled(false);
        select_sure.setChecked(false);
    }

    private void initEvent() {
        select_sure.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                changeBtnBg(isChecked);
            }
        });
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ConnectActivity.class);
                intent.putExtra(HeaterShadowInfo.type, getIntent().getStringExtra(HeaterShadowInfo.type));
                intent.putExtra("isMainList", isMainList);
                intent.putExtra("ssid", getIntent().getStringExtra("ssid"));
                intent.putExtra("password", getIntent().getStringExtra("password"));
                intent.putExtra("apSsid", getIntent().getStringExtra("apSsid"));
                startActivityForResult(intent,5);
            }
        });
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initView() {
        icon_zhuji = (ImageView) findViewById(R.id.icon_zhuji);
        select_sure = (CheckBox) findViewById(R.id.select_sure);
        btn_next = (Button) findViewById(R.id.btn_next);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        startAnim();//启动动画

        if (isMainList){
            ((TextView) findViewById(R.id.activity_add_zhuji_title)).setText(R.string.add_devices_title);
        }
    }

    private void startAnim(){
        final AnimationDrawable anim;
        anim = (AnimationDrawable) icon_zhuji.getDrawable();
        ViewTreeObserver.OnPreDrawListener opdl = new ViewTreeObserver.OnPreDrawListener(){
            @Override
            public boolean onPreDraw() {
                anim.start();
                return true;
            }
        };
        icon_zhuji.getViewTreeObserver().addOnPreDrawListener(opdl);
    }

    private void changeBtnBg(boolean next){
        if (next){
            btn_next.setBackgroundResource(R.drawable.zhzj_button_n);
        }else {
            btn_next.setBackgroundResource(R.drawable.zhzj_button_p);
        }
        btn_next.setEnabled(next);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==5&&resultCode==11){
            setResult(resultCode);
            finish();
        }else if (requestCode == 5 && resultCode == 8) {
            setResult(resultCode);
            finish();
        }
    }
}
