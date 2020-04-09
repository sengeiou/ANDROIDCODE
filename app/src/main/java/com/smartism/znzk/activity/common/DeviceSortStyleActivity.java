package com.smartism.znzk.activity.common;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;

/**
 * Created by win7 on 2017/6/20.
 */

public class DeviceSortStyleActivity extends ActivityParentActivity implements View.OnClickListener {

    private RelativeLayout rl_smart, rl_sort;
    private ImageView iv_smart, iv_sort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_sort_style);
        initView();
    }

    private void initView() {
        rl_smart = (RelativeLayout) findViewById(R.id.rl_smart);
        rl_sort = (RelativeLayout) findViewById(R.id.rl_sort);
        iv_smart = (ImageView) findViewById(R.id.iv_smart);
        iv_sort = (ImageView) findViewById(R.id.iv_sort);
        rl_sort.setOnClickListener(this);
        rl_smart.setOnClickListener(this);
        if (dcsp.getString(DataCenterSharedPreferences.Constant.SHOW_DLISTSORT, "zhineng").equals("zhineng")) {
            iv_smart.setImageResource(R.drawable.zhzj_xuanzhong);
            iv_smart.setTag(R.drawable.zhzj_xuanzhong);
        } else {
            iv_sort.setTag(R.drawable.zhzj_xuanzhong);
            iv_sort.setImageResource(R.drawable.zhzj_xuanzhong);
        }
    }

    public void sure(View v) {
        if (iv_smart.getTag() != null) {
            dcsp.putString(DataCenterSharedPreferences.Constant.SHOW_DLISTSORT, "zhineng").commit();
        } else {
            dcsp.putString(DataCenterSharedPreferences.Constant.SHOW_DLISTSORT, "sort").commit();
        }
        Intent intent = new Intent();
        intent.setAction(Actions.REFRESH_DEVICES_LIST); // 发送一个广播刷新页面
        sendBroadcast(intent);
        finish();
    }

    public void back(View v) {
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_smart:
                iv_smart.setImageResource(R.drawable.zhzj_xuanzhong);
                iv_sort.setImageResource(R.drawable.zhzj_moren);
                iv_smart.setTag(R.drawable.zhzj_xuanzhong);
                iv_sort.setTag(null);
                break;
            case R.id.rl_sort:
                iv_sort.setImageResource(R.drawable.zhzj_xuanzhong);
                iv_smart.setImageResource(R.drawable.zhzj_moren);
                iv_sort.setTag(R.drawable.zhzj_xuanzhong);
                iv_smart.setTag(null);
                break;
        }
    }
}
