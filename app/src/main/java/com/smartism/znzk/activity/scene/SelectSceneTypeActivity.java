package com.smartism.znzk.activity.scene;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.smartism.znzk.R;
import com.smartism.znzk.domain.SceneInfo;

import java.io.Serializable;
import java.util.List;

/**
 * 添加智能场景
 */
public class SelectSceneTypeActivity extends AppCompatActivity implements View.OnClickListener {
    private RelativeLayout zidingyi, dingshi, liandong, select_scene_dsaf;
    private ImageView back_btn;
    private List<SceneInfo> securityItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_scene_type);
        initView();
        initEvevt();
    }

    private void initEvevt() {
        securityItems = (List<SceneInfo>) getIntent().getSerializableExtra("securityItems");
        zidingyi.setOnClickListener(this);
        dingshi.setOnClickListener(this);
        liandong.setOnClickListener(this);
        back_btn.setOnClickListener(this);
        select_scene_dsaf.setOnClickListener(this);
    }

    private void initView() {
        zidingyi = (RelativeLayout) findViewById(R.id.select_scene_zdy);
        dingshi = (RelativeLayout) findViewById(R.id.select_scene_ds);
        liandong = (RelativeLayout) findViewById(R.id.select_scene_ld);
        select_scene_dsaf = (RelativeLayout) findViewById(R.id.select_scene_dsaf);
        back_btn = (ImageView) findViewById(R.id.back_btn);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.select_scene_dsaf:
                intent.setClass(this, ScenceArmingDisarmingActivity.class);
                intent.putExtra("securityItems", (Serializable) securityItems);
                startActivity(intent);
                break;
            case R.id.select_scene_zdy:
                intent.setClass(this, CustomSceneActivity.class);
                startActivity(intent);
                break;
            case R.id.select_scene_ds:
                intent.setClass(this, TimingSceneActivity.class);
                startActivity(intent);
                break;
            case R.id.select_scene_ld:
                intent.setClass(this, LinkageSceneActivity.class);
                startActivity(intent);
                break;
            case R.id.back_btn:
                finish();
                break;
        }
    }
}
