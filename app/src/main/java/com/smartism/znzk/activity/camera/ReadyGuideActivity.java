package com.smartism.znzk.activity.camera;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartism.znzk.R;

public class ReadyGuideActivity extends CameraBaseActivity {
    private ImageView back_btn;
    private Button next;
    TextView show_message;
    ImageView ready_img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ready_guide);
        back_btn = (ImageView) findViewById(R.id.back_btn);
        show_message = (TextView) findViewById(R.id.show_message);
        ready_img = (ImageView) findViewById(R.id.ready_img);
        AnimationDrawable drawable = (AnimationDrawable) ready_img.getBackground();
        drawable.start();
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
                intent.setClass(ReadyGuideActivity.this, WaitGuideActivity.class);
                intent.putExtra("isMainList",getIntent().getBooleanExtra("isMainList",false));
                startActivity(intent);
            }
        });
        show_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(ReadyGuideActivity.this, AddMessageActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public int getActivityInfo() {
        return 1;
    }
}

