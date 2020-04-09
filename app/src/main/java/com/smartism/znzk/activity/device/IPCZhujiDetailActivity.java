package com.smartism.znzk.activity.device;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.device.share.ShareDevicesActivity;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;


public class IPCZhujiDetailActivity extends ActivityParentActivity implements OnClickListener {
    private Context mContext;
    private TextView tv_zhujiipc_name,tv_zhujiipc_where;
    private LinearLayout ll_users;
    private ZhujiInfo zhuji;
    private DeviceInfo deviceInfo;
    private ImageView iv_share;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ipczhuji_detail);
        Intent intent = getIntent();
        deviceInfo = (DeviceInfo) intent.getSerializableExtra("device");
        zhuji = DatabaseOperator.getInstance(mContext).queryDeviceZhuJiInfo(deviceInfo.getZj_id());
        mContext = this;
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void initViews() {
        tv_zhujiipc_name = (TextView) findViewById(R.id.tv_zhujiipc_name);
        tv_zhujiipc_where = (TextView) findViewById(R.id.tv_zhujiipc_where);
        if (deviceInfo != null) {
            tv_zhujiipc_name.setText(deviceInfo.getName());
            tv_zhujiipc_where.setText(deviceInfo.getWhere());
        }
        ll_users = (LinearLayout) findViewById(R.id.ll_users);
        ll_users.setOnClickListener(this);
        iv_share = (ImageView) findViewById(R.id.iv_share);
        iv_share.setOnClickListener(this);
        if (zhuji.isAdmin()) {
            //主账户才显示分享
            iv_share.setVisibility(View.VISIBLE);
        }
    }

    String result = null;

    public void back(View v) {
        finish();
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.ll_users:
                intent.putExtra("device", deviceInfo);
                intent.setClass(this, PerminssonTransActivity.class);
                startActivity(intent);
                break;
            case R.id.iv_share:
                intent.putExtra("pattern", "status_forver");
                intent.putExtra("shareid", deviceInfo.getId());
                intent.setClass(getApplicationContext(), ShareDevicesActivity.class);
                startActivity(intent);
                break;
        }
    }
}
