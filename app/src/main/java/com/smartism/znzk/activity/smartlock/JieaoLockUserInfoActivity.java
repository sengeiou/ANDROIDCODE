package com.smartism.znzk.activity.smartlock;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.FragmentParentActivity;
import com.smartism.znzk.util.Util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

//已经进行了单元测试
public class JieaoLockUserInfoActivity extends FragmentParentActivity {

    TextView number_tv,name_tv,start_time_tv,end_time_tv;
    Toolbar mToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jieao_lock_userinfo_layout);
        Util.setStatusBarColor(this,getResources().getColor(R.color.mediumpurple));
        mToolbar = findViewById(R.id.toolbar);
        number_tv = findViewById(R.id.number_tv);
        name_tv = findViewById(R.id.name_tv);
        start_time_tv = findViewById(R.id.start_time_tv);
        end_time_tv = findViewById(R.id.end_time_tv);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        if(savedInstanceState==null){
            Intent intent = getIntent();
            number_tv.setText(intent.getStringExtra("number"));
            name_tv.setText(intent.getStringExtra("lname"));
            String start_time = intent.getStringExtra("perStartTime");
            String end_time = intent.getStringExtra("perEndTime");
            start_time_tv.setText(handleTime(start_time));
            end_time_tv.setText(handleTime(end_time));
        }else{
            number_tv.setText(savedInstanceState.getString("number"));
            name_tv.setText(savedInstanceState.getString("lname"));
            start_time_tv.setText(savedInstanceState.getString("perStartTime"));
            end_time_tv.setText(savedInstanceState.getString("perEndTime"));
        }
    }

    //1812201940 ==》2018-12-20 19:40
    private String handleTime(String time){
        String result = "20";
        for(int i=0;i<time.length();i++){
            result = result  + time.charAt(i);
            if(i==1||i==3){
                result = result+"-";
            }else if(i==5){
                result =result+" ";
            }else if(i==7){
                result = result+":";
            }
        }
        return result ;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("perStartTime",start_time_tv.getText().toString());
        outState.putString("perEndTime",end_time_tv.getText().toString());
        outState.putString("number",number_tv.getText().toString());
        outState.putString("lname",name_tv.getText().toString());
        super.onSaveInstanceState(outState);

    }
}
