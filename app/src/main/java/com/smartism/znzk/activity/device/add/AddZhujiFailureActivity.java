package com.smartism.znzk.activity.device.add;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.smartism.znzk.R;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.domain.HeaterShadowInfo;
import com.smartism.znzk.util.Actions;

public class AddZhujiFailureActivity extends AppCompatActivity {
    private ImageView iv_back;
    private Button btn_rebuild;
    private ListView mErrorListView ;
    private Button mApBtn ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_zhuji_test);
        initView();
        initEvent();
    }

    private void initEvent() {
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(8,new Intent());
                finish();
            }
        });

        btn_rebuild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(10);
                finish();
            }
        });

        mApBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(),AddZhujiByAPActivity.class);
                intent.putExtra(HeaterShadowInfo.type, getIntent().getStringExtra(HeaterShadowInfo.type));
                intent.putExtra("flags",0);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initView() {
        mErrorListView = findViewById(R.id.cause_listview);
        btn_rebuild = (Button) findViewById(R.id.btn_rebuild);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        mApBtn = findViewById(R.id.ap_btn);

        mErrorListView.setAdapter(new ArrayAdapter(this,R.layout.text_view_with_small_left_dot,getResources().getStringArray(R.array.connect_failure_reasons)));
        //目前智慧主机开发环境显示AP配网
        if(Actions.VersionType.CHANNEL_ZHZJ.equals(MainApplication.app.getAppGlobalConfig().getVersion())&&MainApplication.app.getAppGlobalConfig().isDebug()){
            mApBtn.setVisibility(View.VISIBLE);
        }else{
           mApBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            setResult(8);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
