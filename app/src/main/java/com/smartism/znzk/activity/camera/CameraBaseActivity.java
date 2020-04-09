package com.smartism.znzk.activity.camera;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.smartism.znzk.util.ActivityTaskManager;

/**
 * Created by Administrator on 2017/6/21.
 */

public class CameraBaseActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityTaskManager.getActivityTaskManager().addActivity(this);
    }

    @Override
    public int getActivityInfo() {
        return 0;
    }

    @Override
    protected int onPreFinshByLoginAnother() {
        return 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityTaskManager.getActivityTaskManager().finishActivity(this);
    }
}
