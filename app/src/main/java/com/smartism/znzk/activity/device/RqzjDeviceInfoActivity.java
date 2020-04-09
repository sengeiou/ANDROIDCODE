package com.smartism.znzk.activity.device;


import android.annotation.SuppressLint;
import android.content.*;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.constraint.Group;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lsemtmf.genersdk.tools.commen.ToastTools;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentMonitorActivity;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.HistoryCommandInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.fragment.SettingListFragment;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.LogUtil;
import com.smartism.znzk.view.SwitchButton.SwitchButton;
import com.smartism.znzk.widget.customview.CustomProgressView;
import com.smartism.znzk.zhicheng.activities.MZBaseActivity;
import com.smartism.znzk.zhicheng.tasks.HttpAsyncTask;
import com.smartism.znzk.zhicheng.tasks.LoadCommandsInfo;
import com.smartism.znzk.zhicheng.tasks.LoadHistoryAsyncTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RqzjDeviceInfoActivity extends MZBaseActivity implements LoadHistoryAsyncTask.ILoadHistoryIterface
        ,LoadCommandsInfo.ILoadCommands,SettingListFragment.OnItemLickListener{
    ListView commandListView ;
    ImageView logo ;
    TextView name,where,type ,ranqi_tv;
    int get_history_size  = 20; //每一次获取的个数-History
    FrameLayout mFootView ;
    DeviceInfo operaterDeviceInfo ;
    List<HistoryCommandInfo>  histories = new ArrayList<>();
    int currentPosition = 0; //记录滚动的位置
    boolean isLastPosition = false ; //记录是否滑动到底
    CommandAdapter mAdapter ;
    SwitchButton switch_toggle ;
    Group rq_group ;
    private LinearLayout mWifirqLayout ;
    private TextView mWifirqGasTv,mWifirqTempTv ;
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Actions.ACCETP_ONEDEVICE_MESSAGE)){
                String zhuji_id = intent.getStringExtra("zhuji_id");
                //不是关于这个设备的不处理
                if(zhuji_id==null||Long.parseLong(zhuji_id)!=operaterDeviceInfo.getId()){
                    return ;
                }
                String data = (String) intent.getSerializableExtra("zhuji_info");
                    if (data != null) {
                        //燃气主机
                       if(operaterDeviceInfo.getCa().equals(DeviceInfo.CaMenu.rqzj.value())){
                           try {
                               JSONObject object = JSONObject.parseObject(data);
                               if(object==null){
                                   return ;
                               }
                               if(object.getString("dt").equals("14")){
                                   ranqi_tv.setText(getResources().getString(R.string.rqzj_nongdu,object.getString("deviceCommand")));
                               }else if(object.getString("dt").equals("95")){
                                   if(object.getString("deviceCommand").equals("0")){
                                       switch_toggle.setCheckedImmediatelyNoEvent(false);
                                   }else{
                                       switch_toggle.setCheckedImmediatelyNoEvent(true);
                                   }
                               }
                           } catch (Exception ex) {
                               //防止json无数据崩溃
                           }
                       }else if(operaterDeviceInfo.getCa().equals(DeviceInfo.CaMenu.wifirqbjq.value())){
                           try {
                               JSONObject object = JSONObject.parseObject(data);
                               if(object==null){
                                   return ;
                               }
                               if(object.getString("dt").equals("14")){
                                   mWifirqGasTv.setText(getResources().getString(R.string.rqzj_nongdu,object.getString("deviceCommand")));
                               }else if(object.getString("dt").equals("3")){
                                   mWifirqTempTv.setText(getResources().getString(R.string.ranqi_wendu,object.getString("deviceCommand")));
                               }
                           } catch (Exception ex) {
                               //防止json无数据崩溃
                           }
                       }
                    }

            }else if (intent.getAction().equals(Actions.SHOW_SERVER_MESSAGE)) {
                //操作失败了，复原
                switch_toggle.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        switch_toggle.setCheckedImmediatelyNoEvent(!switch_toggle.isChecked());
                    }
                },1000);

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
                            Toast.makeText(RqzjDeviceInfoActivity.this, getString(R.string.tips_4), Toast.LENGTH_SHORT).show();
                            break;
                        case 5:
                            Toast.makeText(RqzjDeviceInfoActivity.this, getString(R.string.tips_5), Toast.LENGTH_SHORT).show();
                            break;
                        case 6:
                            Toast.makeText(RqzjDeviceInfoActivity.this, getString(R.string.tips_6), Toast.LENGTH_SHORT).show();
                            break;
                        case 7:
                            Toast.makeText(RqzjDeviceInfoActivity.this, getString(R.string.tips_7), Toast.LENGTH_SHORT).show();
                            break;
                        case 8:
                            Toast.makeText(RqzjDeviceInfoActivity.this, getString(R.string.tips_8), Toast.LENGTH_SHORT).show();
                            break;

                        default:
                            Toast.makeText(RqzjDeviceInfoActivity.this, "Unknown Info", Toast.LENGTH_SHORT).show();
                            break;
                    }

                } else {
                    Toast.makeText(RqzjDeviceInfoActivity.this, intent.getStringExtra("message"), Toast.LENGTH_SHORT)
                            .show();
                }
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState==null){
            operaterDeviceInfo = (DeviceInfo) getIntent().getSerializableExtra("device");
        }else{
            operaterDeviceInfo  = (DeviceInfo) savedInstanceState.getSerializable("device");
        }
        initial();
        requestHistoryData(); //刚开始的时候请求一下历史记录
        initShowHide(); //初始化显示
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(DeviceInfo.CaMenu.wifirqbjq.value().equals(operaterDeviceInfo.getCa())){
            getMenuInflater().inflate(R.menu.setting_menu,menu);
        }
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent();
        intent.putExtra("device_id",operaterDeviceInfo.getId());
        switch (item.getItemId()){
            case R.id.setting_menu_item:
        //        mWifirqbjqDialog.show(getSupportFragmentManager(),"wifirqbjq");
                intent.setClass(this,WiFiRqSettingActivity.class);
                startActivity(intent);
                return true ;
            case R.id.info_menu_item:
                intent.setClass(this,DeviceDetailActivity.class);
                intent.putExtra("device",operaterDeviceInfo);
                startActivity(intent);
                return true;
                default:
                    return super.onOptionsItemSelected(item);
        }
    }

    DialogFragment mWifirqbjqDialog  ;
    private void initShowHide(){
        if(operaterDeviceInfo.getCa().equals(DeviceInfo.CaMenu.rqzj.value())){
          //    rq_group.setVisibility(View.VISIBLE);
              ranqi_tv.setVisibility(View.VISIBLE);
        }else if(operaterDeviceInfo.getCa().equals(DeviceInfo.CaMenu.wifirqbjq.value())){
              mWifirqLayout.setVisibility(View.VISIBLE);
              //默认值
              mWifirqGasTv.setText(getResources().getString(R.string.rqzj_nongdu,"0"));
              mWifirqTempTv.setText(getResources().getString(R.string.ranqi_wendu,"26.0"));
          //    mWifirqbjqDialog = SettingListFragment.newInstance(getResources().getStringArray(R.array.jingyuanxin_setting_items));
        }
    }


    public void initial() {
        mWifirqGasTv = findViewById(R.id.wifirqbjq_gas_tv);
        mWifirqTempTv = findViewById(R.id.wifirqbjq_temp_tv);
        mWifirqLayout = findViewById(R.id.wifirqbjq_parent);
        rq_group = findViewById(R.id.rq_group);
        ranqi_tv = findViewById(R.id.ranqi_tv);
        switch_toggle = findViewById(R.id.switch_toggle);
        commandListView = findViewById(R.id.command_list);
        logo = (ImageView) findViewById(R.id.device_logo);
        name = (TextView) findViewById(R.id.d_name);
        where = (TextView) findViewById(R.id.d_where);
        type = (TextView) findViewById(R.id.d_type);
        mAdapter = new CommandAdapter(this,operaterDeviceInfo,histories);
        mFootView = (FrameLayout) initFooterView();
        commandListView.setAdapter(mAdapter);
        //添加底部布局
        commandListView.addFooterView(mFootView);
        commandListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                    switch (scrollState){
                        case SCROLL_STATE_IDLE:
                        //用户滚动停止的时候,记录一下当前第一个列表项
                            currentPosition =  view.getFirstVisiblePosition();
                            if(isLastPosition&&commandListView.getFooterViewsCount()!=0&&mFootView!=null){
                                mFootView.setVisibility(View.VISIBLE);
                                requestHistoryData();
                            }
                            break ;
                    }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastItem = visibleItemCount+firstVisibleItem ;
                if(lastItem==totalItemCount){
                    isLastPosition = true ;
                }else{
                    isLastPosition = false ;
                }
            }
        });

        setTitle(operaterDeviceInfo.getName());
        name.setText(operaterDeviceInfo.getName());
        where.setText(operaterDeviceInfo.getWhere());
        type.setText(operaterDeviceInfo.getType());
        ImageLoader.getInstance().displayImage(dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "")
                + "/devicelogo/" + operaterDeviceInfo.getLogo(), logo, (ImageLoadingListener) null);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Actions.ACCETP_ONEDEVICE_MESSAGE);
        filter.addAction(Actions.SHOW_SERVER_MESSAGE);
        registerReceiver(mReceiver,filter);

        switch_toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggleHandle(isChecked);
            }
        });
        new LoadCommandsInfo(this).execute(operaterDeviceInfo.getId());

        //初始化显示的燃气浓度值
        ranqi_tv.setText(getResources().getString(R.string.rqzj_nongdu,"0"));
    }

    void toggleHandle(boolean isChecked){
        if(operaterDeviceInfo.getCa().equals(DeviceInfo.CaMenu.rqzj.value())){
            SyncMessage message1 = new SyncMessage();
            message1.setCommand(SyncMessage.CommandMenu.rq_control.value());
            message1.setDeviceid(operaterDeviceInfo.getId());
            if(isChecked){
                message1.setSyncBytes(new byte[]{(byte)1});
            }else{
                message1.setSyncBytes(new byte[]{(byte)0});
            }
            SyncMessageContainer.getInstance().produceSendMessage(message1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mReceiver!=null){
            unregisterReceiver(mReceiver);
        }
    }

    @Override
    public int setLayoutId() {
        return R.layout.activity_rqzj_device_info_layout;
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("device",operaterDeviceInfo);
        super.onSaveInstanceState(outState);
    }

    void requestHistoryData(){
        JSONObject jsonObject = new JSONObject() ;
        jsonObject.put("id", operaterDeviceInfo.getId());
        jsonObject.put("start", histories.size());
        jsonObject.put("size", get_history_size);
        new LoadHistoryAsyncTask(this,operaterDeviceInfo.getId()).execute(jsonObject);
    }

    View initFooterView(){
        FrameLayout parent = new FrameLayout(this);
        ListView.LayoutParams lp = new ListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,50,getResources().getDisplayMetrics()));
        parent.setLayoutParams(lp);
        CustomProgressView progressView = new CustomProgressView(this);
        progressView.setBackgroundColor(Color.parseColor("#FFFFFF"));
        parent.addView(progressView);
        return parent;
    }


    int nullCount = 0;
    @Override
    public void showHistory(List<HistoryCommandInfo> data,int totalCount){
        if(data!=null){
            //说明有数据，可能所有的加载完了
            if((totalCount-nullCount)==histories.size()){
                //说明后台没有历史记录了
                commandListView.removeFooterView(mFootView);
                mFootView=null;
            }else{
                List<HistoryCommandInfo> temp = new ArrayList<>();
                for(int i=0;i<data.size();i++){
                    if(TextUtils.isEmpty(data.get(i).getCommand())){
                        nullCount++;
                        continue;
                    }
                    temp.add(data.get(i));
                }
                histories.addAll(temp);
                mAdapter.notifyDataSetChanged();
                commandListView.setSelection(currentPosition);
                if((totalCount)<=get_history_size){
                    //说明数据不足get_history_size的大小
                    commandListView.removeFooterView(mFootView);
                    mFootView=null;
                }
            }
        }else{
            //数据加载失败,隐藏底部View
            mFootView.setVisibility(View.GONE);
        }

        if(commandListView.getFooterViewsCount()==0){
            //说明数据加载完毕
            TextView  textView = (TextView) getLayoutInflater().inflate(android.R.layout.simple_list_item_1,commandListView,false);
            textView.setGravity(Gravity.CENTER);
            textView.setText(getResources().getString(R.string.no_data_tip));
            textView.setTextColor(Color.GRAY);
            commandListView.addFooterView(textView);
        }

    }

    @Override
    public void loadCommands(List<CommandInfo> lists) {
        if(lists==null||lists.size()==0){
            return ;
        }
        for(CommandInfo info:lists){
            //燃气主机初始化
            if(operaterDeviceInfo.getCa().equals(DeviceInfo.CaMenu.rqzj.value())){
                if(info.getCtype().equals("14")){
                    ranqi_tv.setText(getResources().getString(R.string.rqzj_nongdu,info.getCommand()));
                }else if(info.getCtype().equals("95")){
                    if(info.getCommand().equals("1")){
                        switch_toggle.setCheckedImmediatelyNoEvent(true);
                    }else{
                        switch_toggle.setCheckedImmediatelyNoEvent(false);
                    }
                }
            }else if(operaterDeviceInfo.getCa().equals(DeviceInfo.CaMenu.wifirqbjq.value())){
                if(info.getCtype().equals("14")){
                    mWifirqGasTv.setText(getResources().getString(R.string.rqzj_nongdu,info.getCommand()));
                }else if(info.getCtype().equals("3")){
                    mWifirqTempTv.setText(getResources().getString(R.string.ranqi_wendu,info.getCommand()));
                }
            }

        }
    }

    @Override
    public void error(String message) {

    }

    @Override
    public void success(String message) {

    }

    @Override
    public void onItemClick(String data, int position) {

    }

    public static  class CommandAdapter extends BaseAdapter {
        LayoutInflater layoutInflater;
        Resources mResources ;
        DeviceInfo deviceInfo ;
        DataCenterSharedPreferences dcsp;
        List<HistoryCommandInfo> histories ;
        public CommandAdapter(Context context,DeviceInfo deviceInfo,List<HistoryCommandInfo> histories) {
            layoutInflater = LayoutInflater.from(context);
            mResources = context.getResources();
            this.deviceInfo = deviceInfo ;
            this.histories = histories;
            dcsp = DataCenterSharedPreferences.getInstance(context,DataCenterSharedPreferences.Constant.CONFIG);
        }

        @Override
        public int getCount() {
            return histories.size();
        }

        @Override
        public Object getItem(int arg0) {
            return histories.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        /**
         * 返回一个view视图，填充gridview的item
         */
        @SuppressLint("NewApi")
        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            CommandAdapter.DeviceInfoView viewCache = new CommandAdapter.DeviceInfoView();
            if (view == null) {
                view = layoutInflater.inflate(R.layout.activity_zhzj_device_command_item, null);
                viewCache.tv_month = (TextView) view.findViewById(R.id.tv_month);
                viewCache.tv_day = (TextView) view.findViewById(R.id.tv_day);
                viewCache.tv_xingqi = (TextView) view.findViewById(R.id.tv_xingqi);
                viewCache.tv_time = (TextView) view.findViewById(R.id.tv_time);
                viewCache.tv_oper = (TextView) view.findViewById(R.id.tv_oper);
                viewCache.iv_circle_hover = (ImageView) view.findViewById(R.id.iv_circle_hover);
                viewCache.iv_circle = (ImageView) view.findViewById(R.id.iv_circle);
                viewCache.gray_line = (View) view.findViewById(R.id.gray_line);
                viewCache.tv_command = (TextView) view.findViewById(R.id.tv_command);
                view.setTag(viewCache);
            } else {
                viewCache = (CommandAdapter.DeviceInfoView) view.getTag();
            }
            HistoryCommandInfo commandInfo = histories.get(i);
            String[] array = commandInfo.getDate().split(":");
            if (i != 0) {
                String[] array1 = histories.get(i - 1).getDate().split(":");
                if (!array1[2].equals(array[2])) {
                    viewCache.tv_day.setVisibility(View.VISIBLE);
                    viewCache.tv_month.setVisibility(View.VISIBLE);
                    viewCache.tv_xingqi.setVisibility(View.VISIBLE);
                    viewCache.iv_circle_hover.setVisibility(View.VISIBLE);
                    viewCache.iv_circle.setVisibility(View.GONE);
                    viewCache.tv_time.setTextColor(mResources.getColor(R.color.zhzj_default));
                    viewCache.tv_oper.setTextColor(mResources.getColor(R.color.zhzj_default));
                    viewCache.tv_command.setTextColor(mResources.getColor(R.color.zhzj_default));
                } else {
                    viewCache.iv_circle_hover.setVisibility(View.GONE);
                    viewCache.iv_circle.setVisibility(View.VISIBLE);
                    viewCache.tv_day.setVisibility(View.GONE);
                    viewCache.tv_month.setVisibility(View.GONE);
                    viewCache.tv_xingqi.setVisibility(View.GONE);
                    viewCache.tv_time.setTextColor(mResources.getColor(R.color.black));
                    viewCache.tv_oper.setTextColor(mResources.getColor(R.color.black));
                    viewCache.tv_command.setTextColor(mResources.getColor(R.color.black));
                }
            } else {
                viewCache.tv_day.setVisibility(View.VISIBLE);
                viewCache.tv_month.setVisibility(View.VISIBLE);
                viewCache.tv_xingqi.setVisibility(View.VISIBLE);
                viewCache.iv_circle_hover.setVisibility(View.VISIBLE);
                viewCache.iv_circle.setVisibility(View.GONE);
                viewCache.tv_time.setTextColor(mResources.getColor(R.color.zhzj_default));
                viewCache.tv_oper.setTextColor(mResources.getColor(R.color.zhzj_default));
                viewCache.tv_command.setTextColor(mResources.getColor(R.color.zhzj_default));
            }
            String[] array1;
            try {
                array1 = histories.get(i + 1).getDate().split(":");
                if (!array1[2].equals(array[2])) {
                    viewCache.gray_line.setVisibility(View.GONE);
                } else {
                    viewCache.gray_line.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                viewCache.gray_line.setVisibility(View.GONE);
            }
            viewCache.tv_xingqi.setText(commandInfo.getDayOfWeek());
            viewCache.tv_month.setText(array[1]);
            viewCache.tv_day.setText(array[2]);
            viewCache.tv_time.setText(array[3] + ":" + array[4]+":"+array[5]);
            viewCache.tv_command.setText(commandInfo.getCommand() != null ? commandInfo.getCommand() : "");
            viewCache.tv_oper.setText(commandInfo.getOpreator() != null ? commandInfo.getOpreator() : "");

            String command = commandInfo.getCommand();
            if (DeviceInfo.CaMenu.tizhongceng.value().equals(deviceInfo.getCa())) {
                try {
                    String unitName = "";
                    long commandUnit = Long.parseLong(command.substring(0, 4));
                    double commandValue = Integer.parseInt(command.substring(4), 16) / 10.0;
                    if (commandUnit == 2) {
                        unitName = "KG";
                    }
                    viewCache.tv_command.setText(commandValue + unitName);
                } catch (Exception e) {
                    viewCache.tv_command.setText("error");
                }
            } else if (DeviceInfo.CaMenu.wenduji.value().equals(deviceInfo.getCa()) ||DeviceInfo.CaMenu.wenshiduji.value().equals(deviceInfo.getCa())){
                if (dcsp.getString(DataCenterSharedPreferences.Constant.SHOW_TEMPERATURE_UNIT, "ssd").equals("ssd")) {
                    viewCache.tv_command.setText(command);
                } else if (dcsp.getString(DataCenterSharedPreferences.Constant.SHOW_TEMPERATURE_UNIT, "ssd").equals("hsd")) {
                    if (command.contains("℃")) {
                        viewCache.tv_command.setText(((float) Math
                                .round((Float.parseFloat(command.substring(0, command.indexOf("℃"))) * 1.8 + 32) * 10)
                                / 10) + "℉" + command.substring(command.indexOf("℃") + 1));
                    } else {
                        viewCache.tv_command.setText(command);
                    }
                }
            }
            if (!deviceInfo.getCak().equals("control")) {
                viewCache.tv_oper.setVisibility(View.GONE);
            }
            return view;
        }

        class DeviceInfoView {
            TextView time, command, operator;
            public TextView tv_day, tv_month, tv_xingqi, tv_time, tv_oper, tv_command;
            public ImageView iv_circle_hover, iv_circle;
            private View gray_line;
        }
    }
}
