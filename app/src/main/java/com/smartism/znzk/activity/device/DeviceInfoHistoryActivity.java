package com.smartism.znzk.activity.device;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.LogUtil;
import com.smartism.znzk.util.WeakRefHandler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 设备信息，摄像头绑定，和摄像头播放，按钮编辑，按钮点击控制，按钮长按1S控制一次，历史记录(已隐藏)
 * @author 2016年08月24日 by 王建
 *
 */
public class DeviceInfoHistoryActivity extends ActivityParentActivity implements OnClickListener {
    private DeviceInfo deviceInfo;
    private ZhujiInfo zhuji;
    private ListView commandListView;
    private Button title;
    private CommandAdapter commandAdapter;
    private List<JSONObject> commandList;
    private Contact mContact;
    private int totalSize = 0;
    private View footerView;
    private Button footerView_button;


    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 10) { // 获取数据成功
                cancelInProgress();
                commandList.addAll((List<JSONObject>) msg.obj);
                commandAdapter.notifyDataSetChanged();
                if (totalSize == commandList.size() - 1) {
                    commandListView.removeFooterView(footerView);
                }
            }
            return false;
        }
    };
    private Handler defHandler = new WeakRefHandler(mCallback);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info_history);
        mContact = (Contact) getIntent().getSerializableExtra("contact");
        deviceInfo = (DeviceInfo) getIntent().getSerializableExtra("device");
        //判断是否是绑定摄像头的，然后显示相应的UI
        commandListView = (ListView) findViewById(R.id.command_list);
        commandAdapter = new CommandAdapter(DeviceInfoHistoryActivity.this);
        footerView = LayoutInflater.from(this).inflate(R.layout.list_foot_loadmore, null);
        footerView_button = (Button) footerView.findViewById(R.id.load_more);
        footerView_button.setOnClickListener(this);
        commandList = new ArrayList<JSONObject>();
        JSONObject oo = new JSONObject();
        oo.put("deviceCommandTime", getString(R.string.history_head_time));
        oo.put("deviceCommand", getString(R.string.history_head_content));
        oo.put("deviceOperator", getString(R.string.history_head_deviceOperator));
        commandList.add(oo);
        commandListView.setAdapter(commandAdapter);
        showInProgress(getString(R.string.loading), false, true);
        JavaThreadPool.getInstance().excute(new CommandLoad(0, 20));
        if ("wsd".equals(deviceInfo.getCa()))
            findViewById(R.id.command_history_linechart).setVisibility(View.VISIBLE);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.load_more:
                JavaThreadPool.getInstance().excute(new CommandLoad(0, 20));
                break;
            default:
                break;
        }

    }



    public void lineChart(View v) {
//        Intent intent = new Intent(DeviceInfoHistoryActivity.this, THLineChartActivity.class);
        Intent intent = new Intent(DeviceInfoHistoryActivity.this, null);
        intent.putExtra("device", deviceInfo);
        startActivity(intent);

    }

    public void back(View v) {
        finish();
    }
    private long exitTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN ) {
//           if ((System.currentTimeMillis() - exitTime) > 2000) {
//                T.showShort(this, R.string.press_again_monitor);
//                exitTime = System.currentTimeMillis();
//            } else {
               finish();
//            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    class CommandAdapter extends BaseAdapter {
        LayoutInflater layoutInflater;

        public CommandAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return commandList.size();
        }

        @Override
        public Object getItem(int arg0) {
            return commandList.get(arg0);
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

            DeviceInfoView viewCache = new DeviceInfoView();
            if (view == null) {
                view = layoutInflater.inflate(R.layout.activity_device_command_history_list_item, null);
                viewCache.time = (TextView) view.findViewById(R.id.last_time);
                viewCache.command = (TextView) view.findViewById(R.id.last_command);
                viewCache.operator=(TextView) view.findViewById(R.id.last_operator);
                view.setTag(viewCache);
            } else {
                viewCache = (DeviceInfoView) view.getTag();
            }
            if (i != 0) {
                String opreator=commandList.get(i).getString("send");

                viewCache.operator.setText(commandList.get(i).getString("send"));
                try {
                    viewCache.time.setText(new SimpleDateFormat("MM-dd HH:mm:ss").format(new Date(commandList.get(i).getDate("deviceCommandTime").getTime())));
                } catch (Exception e) {
                    viewCache.time.setText(commandList.get(i).getString("deviceCommandTime"));
                }
                String command = commandList.get(i).getString("deviceCommand");
                if ("tzc".equals(deviceInfo.getCa())) {
                    try {
                        String unitName = "";
                        long commandUnit = Long.parseLong(command.substring(0,4));
                        double commandValue = Integer.parseInt(command.substring(4),16)/10.0;
                        if (commandUnit == 2) {
                            unitName = "KG";
                        }
                        viewCache.command.setText(commandValue+unitName);
                    } catch (Exception e) {
                        viewCache.command.setText("error");
                    }
                }else{
                    if (dcsp.getString(Constant.SHOW_TEMPERATURE_UNIT, "ssd").equals("ssd")) {
                        viewCache.command.setText(command);
                    } else if (dcsp.getString(Constant.SHOW_TEMPERATURE_UNIT, "ssd").equals("hsd")) {
                        if (command.contains("℃")) {
                            viewCache.command.setText(((float) Math
                                    .round((Float.parseFloat(command.substring(0, command.indexOf("℃"))) * 1.8 + 32) * 10)
                                    / 10) + "℉" + command.substring(command.indexOf("℃") + 1));
                        } else {
                            viewCache.command.setText(command);
                        }
                    }
                }
            } else {
                viewCache.time.setText(commandList.get(i).getString("deviceCommandTime"));
                viewCache.command.setText(commandList.get(i).getString("deviceCommand"));
                viewCache.operator.setText(commandList.get(i).getString("deviceOperator"));
            }
            if (!deviceInfo.getCak().equals("control")) {
                viewCache.operator.setVisibility(View.GONE);
            }
            return view;
        }

        class DeviceInfoView {
            TextView time, command, operator;
        }
    }

    class CommandLoad implements Runnable {
        private int start, size;

        public CommandLoad(int start, int size) {
            this.size = size;
            this.start = start;
        }

        @Override
        public void run() {
            String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("id", deviceInfo.getId());
            object.put("start", this.start);
            object.put("size", this.size);
//            object.put("uid", dcsp.getLong(Constant.LOGIN_APPID, 0));
//            object.put("code", dcsp.getString(Constant.LOGIN_CODE, ""));
//            object.put("lang", Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry());
//            Log.e("wxb",deviceInfo.getId()+"-"+this.start+"-"+this.size+"-"+dcsp.getLong(Constant.LOGIN_APPID, 0)+"-"+dcsp.getString(Constant.LOGIN_CODE, "")+"-"+">>>>");
            String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/d/hm",object,DeviceInfoHistoryActivity.this);
//            String result = HttpRequestUtils.requestHttpServer(
//                     server + "/jdm/service/hm?v="
//                            + URLEncoder.encode(SecurityUtil.crypt(object.toJSONString(), Constant.KEY_HTTP)),
//                    DeviceInfoHistoryActivity.this, defHandler);

            if ("-3".equals(result)) {
                defHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(DeviceInfoHistoryActivity.this, getString(R.string.history_response_nodevice),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else if (result.length() > 4 ) {

                List<JSONObject> commands = new ArrayList<JSONObject>();
                JSONObject resultJson = null;
                try {
                    resultJson = JSON.parseObject(result);
                } catch (Exception e) {
                    LogUtil.e(getApplicationContext(),TAG,"解密错误：：",e);
                    return;
                }
                JSONArray array = resultJson.getJSONArray("result");
                if (array != null && !array.isEmpty()) {
                    for (int j = 0; j < array.size(); j++) {
                        commands.add(array.getJSONObject(j));
                    }
                }
                // 请求成功了，需要刷新数据到页面，也需要清除此设备的历史未读记录
                ContentValues values = new ContentValues();
                values.put("nr", 0); // 未读消息数
                DatabaseOperator.getInstance(DeviceInfoHistoryActivity.this).getWritableDatabase().update(
                        "DEVICE_STATUSINFO", values, "id = ?", new String[] { String.valueOf(deviceInfo.getId()) });

                totalSize = resultJson.getIntValue("allCount");
                Message m = defHandler.obtainMessage(10);
                m.obj = commands;
                Log.e("wxb",commands.size()+"_commands.size()");
                defHandler.sendMessage(m);
            }
        }
    }



}
