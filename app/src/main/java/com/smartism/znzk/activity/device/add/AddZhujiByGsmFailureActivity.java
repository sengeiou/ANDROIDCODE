package com.smartism.znzk.activity.device.add;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.smartism.znzk.R;
import com.smartism.znzk.zhicheng.activities.MZBaseActivity;

public class AddZhujiByGsmFailureActivity extends MZBaseActivity {

    private ListView mErrorListView ;
    private Button mKnowBtn,mTryAgainBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.azgfa_peiwang_failed));

        mErrorListView = findViewById(R.id.cause_listview);
        mTryAgainBtn = findViewById(R.id.try_again_btn);
        mKnowBtn = findViewById(R.id.know_btn);
        mErrorListView.setAdapter(new ArrayAdapter(this,R.layout.text_view_with_small_left_dot,getResources().getStringArray(R.array.gsm_failure_cause)));
        mKnowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setClass(getApplicationContext(), AddZhujiActivity.class);
                startActivity(intent);
                finish();
            }
        });

        mTryAgainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setClass(getApplicationContext(), GeneralCollectionWifiActivity.class);
                intent.putExtra("title",getString(R.string.zhuji_perwang_sms_title));
                intent.putExtra("need_exit",true);
                intent.putExtra("exit_activity", AddZhujiActivity.class);
                intent.putExtra("next_activity", GSMDistributionNetworkActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    //显示退出菜单项
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //需要退出按钮才加载退出菜单
        getMenuInflater().inflate(R.menu.exit_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.exit_menu_item:
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setClass(getApplicationContext(), AddZhujiActivity.class);
                if(intent.resolveActivity(getPackageManager())!=null){
                    //存在Activity在进行跳转
                    startActivity(intent);
                }
                finish();
                return true ;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public int setLayoutId() {
        return R.layout.activity_add_zhuji_by_gsm_failure;
    }
}
