package com.smartism.znzk.activity.camera;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.smartism.znzk.R;

public class AddContactTypeActivity extends AppCompatActivity {
    private int i;//3、技威，5、v380
    private int isCameraList;//判断是否是返回摄像头列表还是返回设备列表 0、返回设备列表，1、返回摄像头列表
    private Button back_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact_type);
        i = getIntent().getIntExtra("int", 3);
        isCameraList = getIntent().getIntExtra("isCameraList", 0);
        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.select_camera_net).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(AddContactTypeActivity.this, AddContactActivity.class);
                intent.putExtra("int", i);
                intent.putExtra("isCameraList", isCameraList);
                intent.putExtra("isMainList",getIntent().getBooleanExtra("isMainList",false));
                startActivity(intent);
            }
        });
        findViewById(R.id.select_camera_radar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(AddContactTypeActivity.this, RadarAddActivity.class);
                intent.putExtra("int", i);
                intent.putExtra("isCameraList", isCameraList);
                intent.putExtra("isMainList",getIntent().getBooleanExtra("isMainList",false));
                startActivity(intent);
            }
        });
    }
}
