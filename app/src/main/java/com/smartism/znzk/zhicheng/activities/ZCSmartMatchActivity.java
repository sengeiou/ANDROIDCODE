package com.smartism.znzk.zhicheng.activities;

import android.content.*;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.Group;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lsemtmf.genersdk.tools.commen.ToastTools;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.device.DeviceMainActivity;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.view.alertview.AlertView;
import com.smartism.znzk.view.alertview.OnItemClickListener;
import com.smartism.znzk.widget.customview.WithTextProgressDialog;
import com.smartism.znzk.zhicheng.models.ARCModel;
import com.smartism.znzk.zhicheng.tasks.GeneralHttpTask;
import com.smartism.znzk.zhicheng.tasks.HttpAsyncTask;

import java.util.*;

import static com.smartism.znzk.zhicheng.tasks.GeneralHttpTask.GET_SMART_MATCH_URL;

/*
* author mz
* */
public class ZCSmartMatchActivity extends MZBaseActivity implements View.OnClickListener,GeneralHttpTask.ILoadARKeysImpl, HttpAsyncTask.IHttpResultView {


    private static final String TAG = ZCSmartMatchActivity.class.getSimpleName() ;
    private Group mResultGroup ;
    int SEND_TIMEOUT = 99  ;
    long deviceId ,zhujiID;
    String deviceName;
    Button start_telligent_btn ;
    ListView list_view ;
    TextView tv_result ;
    private TextView mTipTv ;
    private WithTextProgressDialog mProgressView ;
    boolean isUserClick = false ; //记录用户是否点击了开始匹配
    Handler mHandler  = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 99:
                    hideProgress();
                    ToastTools.short_Toast(ZCSmartMatchActivity.this,getResources().getString(R.string.request_timeout));
                    break;
            }
        }
    };

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Actions.ACCETP_ONEDEVICE_MESSAGE)) {
                String zhuji_id = intent.getStringExtra("zhuji_id");
                //不是关于这个设备的不处理
                if (zhuji_id == null || Long.parseLong(zhuji_id) != zhujiID) {
                    return;
                }
                if(!isUserClick){
                    return ;
                }
                isUserClick = false ;
                String data = (String) intent.getSerializableExtra("zhuji_info");
                mHandler.removeMessages(SEND_TIMEOUT);
                JSONObject object = JSONObject.parseObject(data);
                if(object.getString("dt").equals("137")){
                    Map<String,String> msp = new HashMap<>();
                    msp.put("device_id",String.valueOf(1));
                    msp.put("mac",ZCIRRemoteList.CURRENT_IR_MAC_VALUE);
                    msp.put("vcode",object.getString("deviceCommand"));
                    mTask = new GeneralHttpTask(ZCSmartMatchActivity.this,GET_SMART_MATCH_URL);
                    mTask.setProgressText(getString(R.string.hwzf_brand_smart_irtip_second));
                    mTask.execute(msp);
                }
            } else if (intent.getAction().equals(Actions.SHOW_SERVER_MESSAGE)) {
                //返送指令失败返回
                mHandler.removeMessages(SEND_TIMEOUT);
                hideProgress();
                //返回指令操作失败
                JSONObject resultJson = null;
                try {
                    resultJson = JSON.parseObject(intent.getStringExtra("message"));
                } catch (Exception e) {
                    Log.w("DevicesList", "获取服务器返回消息，转换为json对象失败，用原始值处理");
                }
                if (resultJson != null) {
                    switch (resultJson.getIntValue("Code")) {
                        case 4:
                            Toast.makeText(ZCSmartMatchActivity.this, getString(R.string.tips_4), Toast.LENGTH_SHORT).show();
                            break;
                        case 5:
                            Toast.makeText(ZCSmartMatchActivity.this, getString(R.string.tips_5), Toast.LENGTH_SHORT).show();
                            break;
                        case 6:
                            Toast.makeText(ZCSmartMatchActivity.this, getString(R.string.tips_6), Toast.LENGTH_SHORT).show();
                            break;
                        case 7:
                            Toast.makeText(ZCSmartMatchActivity.this, getString(R.string.tips_7), Toast.LENGTH_SHORT).show();
                            break;
                        case 8:
                            Toast.makeText(ZCSmartMatchActivity.this, getString(R.string.tips_8), Toast.LENGTH_SHORT).show();
                            break;

                        default:
                            Toast.makeText(ZCSmartMatchActivity.this, "Unknown Info", Toast.LENGTH_SHORT).show();
                            break;
                    }
                } else {
                    Toast.makeText(ZCSmartMatchActivity.this, intent.getStringExtra("message"), Toast.LENGTH_SHORT)
                            .show();
                }
            }
        }
    };
    AlertView mTipView ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState==null){
            deviceId = getIntent().getLongExtra("did",0);
            deviceName = getIntent().getStringExtra("deviceName");
            zhujiID = getIntent().getLongExtra("zhujiID",0);

        }else{
            deviceId = savedInstanceState.getLong("did");
            deviceName = savedInstanceState.getString("deviceName");
            zhujiID = savedInstanceState.getLong("zhujiID");
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Actions.ACCETP_ONEDEVICE_MESSAGE);
        intentFilter.addAction(Actions.SHOW_SERVER_MESSAGE);
        registerReceiver(mReceiver,intentFilter);

        initSelfProgress();//初始化进度条
        initChild();
/*        //提示框，教用户如何使用智能匹配,安装应用后，第一次打开这个界面自动弹出
        mTipView = new AlertView(getResources().getString(R.string.hwzf_zhinengpiper_title),
                Html.fromHtml(getString(R.string.hwzf_zhinengpipei_content)),
                null,
                new String[]{getString(R.string.confirm)}, null,
                mContext, AlertView.Style.Alert,
                new OnItemClickListener() {
                    @Override
                    public void onItemClick(Object o, final int position) {
                        if (position != -1) {
                            mTipView.dismiss();
                        }
                    }
                });
        mTipView.setCancelable(true);
      //  if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean(TAG,false)){
        if(!DataCenterSharedPreferences.getInstance(getApplicationContext(),DataCenterSharedPreferences.Constant.CONFIG).getBoolean(TAG,false)){
            //表示用户第一次进入这个页面，进行操作提示
            list_view.postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            if(mTipView!=null&&!mTipView.isShowing()){
                                mTipView.show();
                                DataCenterSharedPreferences.getInstance(ZCSmartMatchActivity.this.getApplicationContext(),DataCenterSharedPreferences.Constant.CONFIG)
                                        .putBoolean(TAG,true).commit();
                            }
                        }
                    }
            ,1000);

        }*/
    }

    private void initSelfProgress(){
       mProgressView = new WithTextProgressDialog(this);
    }

    GeneralHttpTask mTask ;
    BaseAdapter mAdapter ;
    void initChild(){
        mResultGroup = findViewById(R.id.match_result_group);
        start_telligent_btn = findViewById(R.id.start_telligent_btn);
        list_view = findViewById(R.id.list_view);
        tv_result = findViewById(R.id.tv_result);
        mTipTv= findViewById(R.id.tip_tv);
        mTipTv.setText(Html.fromHtml(getResources().getString(R.string.hwzf_zhinengpipei_content)));

        mAdapter = new MyAdapter();
        list_view.setAdapter(mAdapter);

        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                new AlertView(getResources().getString(R.string.hwzf_brand_add_remote_title),
                        mModels.get(position).getRcName(),
                        getString(R.string.deviceslist_server_leftmenu_delcancel),
                        new String[]{getString(R.string.confirm)}, null,
                        mContext, AlertView.Style.Alert,
                        new OnItemClickListener() {
                            @Override
                            public void onItemClick(Object o, final int position) {
                                if (position != -1) {
                                    //绑定设备
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("did",String.valueOf(deviceId));
                                    jsonObject.put("codeId",mModels.get(position).getKfId());
                                    jsonObject.put("source","hlzk");
                                    jsonObject.put("tname","kt");
                                    jsonObject.put("bname",mModels.get(position).getRcName().split("-",2)[0].trim());
                                    jsonObject.put("type",mModels.get(position).getRcName().split("-",2)[1].trim());
                                    jsonObject.put("zip",0);
                                    jsonObject.put("v",1);
                                    new HttpAsyncTask(ZCSmartMatchActivity.this,HttpAsyncTask.IR_REMOTE_ADD_URL_FLAG).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,jsonObject);
                                }
                            }
                        }).show();
            }
        });
        start_telligent_btn.setOnClickListener(this);
        //设置标题
        setTitle(getResources().getString(R.string.hwzf_zhinengpipei));
        mDialog.getDialog().setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if(mTask!=null){
                    mTask.cancel(true);//取消任务
                    if(mHandler!=null&&mHandler.hasMessages(99)){
                        mHandler.removeMessages(99);
                    }
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong("did",deviceId);
        outState.putString("deviceName",deviceName);
        outState.putLong("zhujiID",zhujiID);
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mHandler!=null){
            mHandler.removeCallbacksAndMessages(null);
        }

        if(mReceiver!=null){
            unregisterReceiver(mReceiver);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
     //   getMenuInflater().inflate(R.menu.zc_help_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.zc_help_item:
                if(mTipView!=null&&!mTipView.isShowing()){
                    mTipView.show();
                }
                return true ;
        }
        return super.onOptionsItemSelected(item);
    }

    //当前活动显示的布局
    @Override
    public int setLayoutId() {
        return R.layout.activity_zcsmart_match_layout;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start_telligent_btn:
                //开始匹配
                isUserClick = true;
                mResultGroup.setVisibility(View.GONE);
                showProgress(getString(R.string.hwzf_brand_smart_irtip_first));
                mHandler.sendEmptyMessageDelayed(SEND_TIMEOUT,20*1000);
                break ;
        }
    }

    @Override
    public void showProgress(String text) {
            mProgressView.setText(text);
            if(!mProgressView.isShowing()){
                mProgressView.show();
            }
    }

    @Override
    public void hideProgress() {
        if(mProgressView.isShowing()){
            mProgressView.dismiss();
        }
    }

    List<ARCModel> mModels = new ArrayList<>();
    @Override
    public void getRequestResult(String results) {
        mModels.clear();
        if(results==null||results.equals("")){
            error(getResources().getString(R.string.hwzf_pipei_fail));
        }else{
            mModels.clear();
            JSONObject jsonObject = JSONObject.parseObject(results);
            start_telligent_btn.setText(getString(R.string.hwzf_restart_pipei));
            mResultGroup.setVisibility(View.VISIBLE);
            mTipTv.setVisibility(View.GONE);
            String result = jsonObject.getString("result");
            String[] temp = result.split("\\|\\|");
            for(int i=0;i<temp.length;i++){
                ARCModel arcModel = new ARCModel();
                String middle = temp[i];
                arcModel.setKfId(middle.split("=")[0]);
                arcModel.setRcName(middle.split("=")[1]);
                mModels.add(arcModel);
            }
        }
        mAdapter.notifyDataSetChanged();

    }

    @Override
    public void setResult(int flag, String result) {
        if(flag==HttpAsyncTask.IR_REMOTE_ADD_URL_FLAG){
        if(result.equals("0")){
            //请求成功
            Intent intent = new Intent();
            intent.setClass(this, DeviceMainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }
}

    //显示匹配成功遥控器
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
            TextView textView ;
            if(convertView==null){
                textView  = (TextView) getLayoutInflater().inflate(android.R.layout.simple_list_item_1,parent,false);
            }else{
                textView = (TextView) convertView;
            }
            textView.setText(mModels.get(position).getRcName());
            return textView;
        }
    }
}
