package com.smartism.znzk.activity.device.add;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.smartism.znzk.R;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.zhicheng.activities.MZBaseActivity;

public class AddZhujiByApFailureActivity extends MZBaseActivity {

    private ListView mErrorListView ;
    private Button mKnowBtn ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.add_zhuji_by_ap_failure_title));

        mErrorListView = findViewById(R.id.cause_listview);
        mKnowBtn = findViewById(R.id.know_btn);
        mErrorListView.setAdapter(new ArrayAdapter(this,R.layout.text_view_with_small_left_dot,getResources().getStringArray(R.array.add_zhuji_by_ap_failure_cause)));
        mKnowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                if (Actions.VersionType.CHANNEL_QYJUNHONG.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                        ||Actions.VersionType.CHANNEL_HCTZ.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                    intent.setClass(getApplicationContext(), AddZhujiActivity.class);
                } else {
                    intent.setClass(getApplicationContext(), AddZhujiWayChooseActivity.class);
                }
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public int setLayoutId() {
        return R.layout.activity_add_zhuji_by_ap_failure_layout;
    }
}
