package com.smartism.znzk.activity.camera;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.domain.ZhujiInfo;

/**
 * 摄像头联动报警
 */
public class CameraBindAlarmGuideActivity extends ActivityParentActivity implements View.OnClickListener {
    ImageView back;

    private ZhujiInfo zhuji;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_bind_alarm_guide_list);
        zhuji = (ZhujiInfo) getIntent().getSerializableExtra("zhuji");
        initView();
    }


    private void initView() {
        back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(this);
    }


    public void sure(View view) {
        Intent intent = new Intent();
        intent.putExtra("zhuji", zhuji);
        intent.setClass(this, CameraBindAlarmActivity.class);
        startActivity(intent);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
        }
    }
}
