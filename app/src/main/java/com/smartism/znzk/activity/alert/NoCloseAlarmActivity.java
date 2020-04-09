package com.smartism.znzk.activity.alert;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;

import java.util.ArrayList;
import java.util.List;

public class NoCloseAlarmActivity extends ActivityParentActivity {

    public static final String  TAG ="NoCloseAlarmActivity";
    public final static int REQUEST_CODE= 0X88;
    ListView no_close_listview ;
    ImageView no_close_top_back ;
    int[] timesId = new int[]{R.string.no_colse_alarm_close,R.string.no_close_alarm_five_minute,
            R.string.no_close_alarm_ten_minute,R.string.no_close_alarm_fifteen_minute,R.string.no_close_alarm_thirty};
    final String[] times = new String[timesId.length];
    MyAdapter mAdapter ;
    int backToTime = 0;//默认值为关闭 ,单位秒
    DeviceInfo operationDevice ;
    String defaultTime = times[0];
    ProgressDialog mDialog ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.no_close_activity);
        //初始化选项
        for(int i=0;i<timesId.length;i++){
            times[i] = getResources().getString(timesId[i]);
        }
        defaultTime = getIntent().getStringExtra("defaultTime");
        operationDevice = (DeviceInfo) getIntent().getSerializableExtra("device");
        no_close_listview = findViewById(R.id.no_close_listview);
        no_close_top_back = findViewById(R.id.no_close_top_back);
        mAdapter = new MyAdapter(this,defaultTime);

        no_close_listview.setAdapter(mAdapter);
       no_close_listview.setOnItemClickListener(mAdapter);
        no_close_top_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //在setResult方法之后需要销毁当前活动，否则无法传递值回去*/
                finish();
            }
        });
    }

    private void setAlarmDnc(final int backToTime){
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject bossJSON = new JSONObject();
                JSONArray erziArray = new JSONArray();
                JSONObject sunziJSON = new JSONObject();
                bossJSON.put("did",operationDevice.getId());
                sunziJSON.put("vkey","alarm_dnc");
                sunziJSON.put("value",backToTime+"");
                erziArray.add(sunziJSON);
                bossJSON.put("vkeys",erziArray);
                String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS,"");
                //设置未关门时长
                String result = HttpRequestUtils.requestoOkHttpPost(server+"/jdm/s3/d/p/set",bossJSON,NoCloseAlarmActivity.this);
                Log.v(TAG,"设置未关门时长结果:" + result);
                if(result.equals("0")){
                    //设置成功
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            cancelProgress();
                            //设置选中状态
                           mAdapter.setCheckedStatus(mAdapter.currentItem);
                            Toast.makeText(NoCloseAlarmActivity.this,getResources().getString(R.string.deviceinfo_activity_success),Toast.LENGTH_LONG).show();
                            Intent intent = getIntent();
                            List<CheckedTextView> list = mAdapter.mList;
                            for(int i=0;i<list.size();i++){
                                if(list.get(i).isChecked()){
                                    intent.putExtra("resultTime",list.get(i).getText());
                                }
                            }
                            setResult(REQUEST_CODE,intent);
                        }
                    });
                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //设置失败
                            cancelProgress();
                        }
                    });
                }
            }
        }).start();
    }

    public void showProgress(String text, boolean bIndeterminate, boolean bCancelable) {
        if(mDialog==null){
            mDialog = new ProgressDialog(this);
        }
        mDialog.setMessage(text);
        mDialog.setIndeterminate(bIndeterminate);
        mDialog.setCancelable(bCancelable);

        if (!mDialog.isShowing()) {
            mDialog.show();
        }

    }

    public void cancelProgress(){
        if(mDialog==null){
            return ;
        }
        if(mDialog.isShowing()){
            mDialog.dismiss();
        }
    }


    class MyAdapter extends BaseAdapter implements  AdapterView.OnItemClickListener{
        Context mContext ;
        String defaultTime ;
        int currentItem ;
        List<CheckedTextView> mList = new ArrayList<>();
        public MyAdapter(Context context,String defaultTime){
            mContext = context ;
            if(TextUtils.isEmpty(defaultTime)){
                this.defaultTime = times[0];
            }else{
                this.defaultTime = defaultTime ;
            }
           //为适配不同语言的手机
        }
        @Override
        public int getCount() {
            return times.length;
        }

        @Override
        public Object getItem(int position) {
            return times[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CheckedTextView v = null ;
            if(convertView==null){
                v = (CheckedTextView) LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_single_choice,parent,false);
                v.setFocusable(false);//设置其无法获得焦点
                mList.add(v);//保存View
            }else{
                v = (CheckedTextView) convertView;
            }

            if(defaultTime.equals(times[position])){
                v.setChecked(true);
            }
            v.setText(times[position]);
            return v;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            switch (position){
                /*
                * 采用position来解决不同语言适配问题
                * */
                case 0:
                    backToTime = 0 ;
                    break;
                case 1:
                    backToTime = 300;
                    break;
                case 2:
                    backToTime = 600;
                    break;
                case 3:
                    backToTime = 900;
                    break;
                case 4:
                    backToTime = 1800;
                    break;
            }
            currentItem = position  ;
            showProgress(getString(R.string.please_wait),true,false);
            setAlarmDnc(backToTime);
        }

        void setCheckedStatus(int position){
            //设置选中状态
            CheckedTextView v = (CheckedTextView) mList.get(position);
            v.setChecked(true);
            for(CheckedTextView checkedTextView:mList){
                if(!checkedTextView.equals(v)){
                    checkedTextView.setChecked(false);
                }
            }
        }
    }
}
