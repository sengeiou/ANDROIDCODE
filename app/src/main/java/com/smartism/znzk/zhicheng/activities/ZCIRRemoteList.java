package com.smartism.znzk.zhicheng.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.yaokan.YKDownLoadCodeActivity;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.view.alertview.AlertView;
import com.smartism.znzk.view.alertview.OnItemClickListener;
import com.smartism.znzk.zhicheng.models.ARCModel;
import android.os.Handler;
import com.smartism.znzk.zhicheng.tasks.GetBrandAsyncTask;
import com.smartism.znzk.zhicheng.tasks.HttpAsyncTask;
import com.smartism.znzk.zhicheng.tasks.ZCIRRemoteTask;

import java.util.ArrayList;
import java.util.List;

//展示当前设备添加的空调遥控器
/*
* author mz
* */
public class ZCIRRemoteList extends MZBaseActivity implements HttpAsyncTask.IHttpResultView, ZCIRRemoteTask.IzcIrRemoteImpl {

    public   static String CURRENT_IR_MAC_VALUE="";//当前进入的红外设备的MAC值
    GridView ir_gridview ;
    long deviceId ,zhujiID;
    List<ARCModel> mModels = new ArrayList<>();
    BaseAdapter mAdapter  ;
    String deviceName  ="",bipc;
    int goToDest = 0 ;
    ARCModel currentModel ;
    int deletePosition =-1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState==null){
            deviceId = getIntent().getLongExtra("did",0);
            deviceName = getIntent().getStringExtra("deviceName");
            zhujiID = getIntent().getLongExtra("zhujiID",0);
            bipc = getIntent().getStringExtra("bipc");
        }else{
            deviceId = savedInstanceState.getLong("did");
            deviceName  = savedInstanceState.getString("deviceName");
            zhujiID = savedInstanceState.getLong("zhujiID");
            bipc = savedInstanceState.getString("bipc");
        }
        initChild();
    }


    @Override
    protected void onResume() {
        super.onResume();
        //请求当前设备绑定的遥控器数据
        requestData();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong("did",deviceId);
        outState.putLong("zhujiID",zhujiID);
        outState.putString("deviceName",deviceName);
        outState.putString("bipc",bipc);
        super.onSaveInstanceState(outState);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
     //   getMenuInflater().inflate(R.menu.zc_air_add_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
           case  R.id.add_btn:
               goToDest = 2;
               new ZCIRRemoteTask(ZCIRRemoteList.this,zhujiID).execute();
               return true ;
        }
        return super.onOptionsItemSelected(item);
    }

    void initChild(){
        ir_gridview = findViewById(R.id.ir_gridview);
        mAdapter = new MyAdapter();
        ir_gridview.setAdapter(mAdapter);
        setTitle(deviceName);

        ir_gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //点击之前我们来完成mac值的获取
                if(position==mModels.size()-1){
                    goToDest = 2;
                }else{
                    currentModel = mModels.get(position);
                    goToDest = 1;
                }
                new ZCIRRemoteTask(ZCIRRemoteList.this,zhujiID).execute();
            }
        });

        ir_gridview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if(position==mModels.size()-1){
                    return true;
                }
                new AlertView(getString(R.string.deviceslist_server_leftmenu_deltitle),
                        getString(R.string.deviceslist_server_leftmenu_delmessage),
                        getString(R.string.deviceslist_server_leftmenu_delcancel),
                        new String[]{getString(R.string.deviceslist_server_leftmenu_delbutton)}, null,
                        mContext, AlertView.Style.Alert,
                        new OnItemClickListener() {
                            @Override
                            public void onItemClick(Object o, final int position) {
                                if (position != -1) {
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("did",deviceId);
                                    jsonObject.put("id",mModels.get(position).getLocalServereid());
                                    deletePosition = position ;
                                   new HttpAsyncTask(ZCIRRemoteList.this,HttpAsyncTask.IR_REMOTE_DELETE_URL_FLAG).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,jsonObject);
                                }
                            }
                        }).show();
                return true ;
            }
        });
    }

    HttpAsyncTask mTask ;
    void requestData(){
        mTask = new HttpAsyncTask(this,HttpAsyncTask.IR_REMOTE_GET_URL_FLAG);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("did",deviceId);
        jsonObject.put("c",false);
        mTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,jsonObject);
    }

    @Override
    public int setLayoutId() {
        return R.layout.activity_zcirremote_list_layout;
    }

    @Override
    public void setResult(int flag, String result) {
        //-3遥控器为空
        if(HttpAsyncTask.IR_REMOTE_GET_URL_FLAG==flag){
            if(result==null||result.equals("")){
                error(getResources().getString(R.string.hwzf_server_data_error));
            }else if(result.equals("-3")){
                mModels.clear();
            }else{
                mModels.clear();
                JSONArray array = JSONObject.parseArray(result);
                for(int i=0;i<array.size();i++){
                    JSONObject object = array.getJSONObject(i);
                    ARCModel temp = new ARCModel();
                    temp.setParentName(object.getString("bname"));
                    temp.setKfId(object.getString("codeId"));
                    temp.setRcName(object.getString("type"));
                    temp.setLocalServereid(object.getString("id"));
                    mModels.add(temp);
                }

            }
            //用了GridView，无法添加footview，造一个
            ARCModel arc = new ARCModel();
            arc.setParentName("footview");//没必要
            mModels.add(arc);
            mAdapter.notifyDataSetChanged();
        }else if(flag==HttpAsyncTask.IR_REMOTE_DELETE_URL_FLAG&&"0".equals(result)){
            if(deletePosition!=-1){
                mModels.remove(deletePosition);
                mAdapter.notifyDataSetChanged();
                deletePosition = -1;
            }

        }
    }

    @Override
    public void handleResult(String result) {
        if(result!=null&&!result.equals("")){
            Intent intent = new Intent();
            //跳转到空调控制界面
            if(goToDest==1) {
                intent.setClass(ZCIRRemoteList.this,AirConditioningActivity.class);
                intent.putExtra("content_info",currentModel);
                intent.putExtra("bipc",bipc);
            }else if(goToDest==2){
                intent.setClass(ZCIRRemoteList.this,ZCInfraredModeSelectionActivity.class);
                intent.putExtra("deviceName",deviceName);
            }
            intent.putExtra("did",deviceId);
            intent.putExtra("zhujiID",zhujiID);
            CURRENT_IR_MAC_VALUE = result ;
            startActivity(intent);
        }else{
            error(getResources().getString(R.string.hwzf_server_data_error));
        }
    }

    class MyAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return mModels.size();
        }

        @Override
        public Object getItem(int position) {
            return mModels.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder ;
            if(convertView==null){
                viewHolder = new ViewHolder();
                //创建新的
                View view = getLayoutInflater().inflate(R.layout.zc_ir_child_layout,parent,false);
                viewHolder.brandName = view.findViewById(R.id.display_brand_name);
                viewHolder.remoteName = view.findViewById(R.id.display_remote_tv);
                viewHolder.img = view.findViewById(R.id.ir_pic);
                view.setTag(viewHolder);
                convertView = view;

            }else{
                //使用以前的
                viewHolder = (ViewHolder) convertView.getTag();
            }

            if(position==mModels.size()-1){
                //footview角色
                viewHolder.brandName.setText(getResources().getString(R.string.hwzf_download_title_small));
                viewHolder.remoteName.setText(getResources().getString(R.string.hwzf_download_title));
                viewHolder.img.setImageResource(R.drawable.ic_tb_add_btn);
            }else{
                viewHolder.brandName.setText(mModels.get(position).getParentName());
                viewHolder.remoteName.setText(mModels.get(position).getRcName());
                viewHolder.img.setImageResource(R.drawable.ic_air_pic);
            }
            return convertView;
        }

        class ViewHolder{
            TextView brandName ,remoteName ;
            ImageView img ;

        }
    }
}
