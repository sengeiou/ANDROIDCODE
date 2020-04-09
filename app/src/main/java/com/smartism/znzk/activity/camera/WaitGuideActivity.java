package com.smartism.znzk.activity.camera;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.smartism.znzk.R;

public class WaitGuideActivity extends CameraBaseActivity{
    private ImageView  back_btn;
    private Button next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait_guide);
        back_btn = (ImageView) findViewById(R.id.back_btn);
        next = (Button) findViewById(R.id.next);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                intent.setClass(WaitGuideActivity.this, AddWaitActicity.class);
                intent.putExtra("isMainList",getIntent().getBooleanExtra("isMainList",false));
                startActivity(intent);
            }
        });
    }

    @Override
    public int getActivityInfo() {
        return 0;
    }
}
