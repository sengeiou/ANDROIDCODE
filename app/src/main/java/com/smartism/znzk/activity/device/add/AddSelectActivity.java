package com.smartism.znzk.activity.device.add;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.smartism.znzk.R;
import com.smartism.znzk.activity.camera.AddContactTypeActivity;

public class AddSelectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_select);

        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.select_deviec_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(AddSelectActivity.this, AddContactTypeActivity.class);
                intent.putExtra("isMainList",true);
                startActivity(intent);
            }
        });

        findViewById(R.id.select_deviec_zhuji).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(AddSelectActivity.this, AddZhujiActivity.class);
                startActivity(intent);
            }
        });
    }
}
