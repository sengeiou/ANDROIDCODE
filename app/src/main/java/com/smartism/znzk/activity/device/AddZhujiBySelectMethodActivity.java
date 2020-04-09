package com.smartism.znzk.activity.device;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.smartism.znzk.R;
import com.smartism.znzk.activity.device.add.AddZhujiActivity;
import com.smartism.znzk.activity.device.add.AddZhujiByAPActivity;
import com.smartism.znzk.activity.device.add.GSMDistributionNetworkActivity;
import com.smartism.znzk.activity.device.add.GeneralCollectionWifiActivity;
import com.smartism.znzk.domain.HeaterShadowInfo;
import com.smartism.znzk.zhicheng.activities.MZBaseActivity;

import java.util.ArrayList;
import java.util.List;

public class AddZhujiBySelectMethodActivity extends MZBaseActivity {

    private ListView mPeiwangMethodList ;
    private BaseAdapter mAdapter ;
    private List<String> mData ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.add_zhuji_by_ap_other_tip));
        bindView();
        bindEvent();
        bindData();
    }

    private void bindView(){
        mPeiwangMethodList = findViewById(R.id.pei_wangmethod_list);
    }

    private void bindEvent(){
        mPeiwangMethodList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                if(mData.get(position).equals(getResources().getString(R.string.add_zhuji_by_ap_name))){
                    intent.setClass(getApplicationContext(), AddZhujiByAPActivity.class);
                    intent.putExtra(HeaterShadowInfo.type, getIntent().getStringExtra(HeaterShadowInfo.type));
                    intent.putExtra("flags",0);
                }else if(mData.get(position).equals(getResources().getString(R.string.zhuji_peiwang_smsmethod))){
                    //短信配网
                    intent.setClass(getApplicationContext(), GeneralCollectionWifiActivity.class);
                    intent.putExtra("title",getString(R.string.zhuji_perwang_sms_title));
                    intent.putExtra("need_exit",true);
                    intent.putExtra("exit_activity", AddZhujiActivity.class);
                    intent.putExtra("next_activity", GSMDistributionNetworkActivity.class);
                }
                startActivity(intent);
            }
        });
    }

    private void bindData(){
        mData = new ArrayList<>();
        String[] temp = getResources().getStringArray(R.array.peiwang_method_array);
        for(int i=0;i<temp.length;i++){
            mData.add(temp[i]);
        }
        mAdapter = new ArrayAdapter(getApplication(),R.layout.peiwang_method_item_layout,android.R.id.text1,mData);
        mPeiwangMethodList.setAdapter(mAdapter);
    }

    @Override
    public int setLayoutId() {
        return R.layout.activity_add_zhuji_select_method_layout;
    }
}
