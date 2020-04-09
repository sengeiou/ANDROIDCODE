package com.smartism.znzk.activity.device;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.DeviceInfo.ControlTypeMenu;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.Actions.VersionType;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.LogUtil;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.view.MyGridView;
import com.smartism.znzk.view.SelectAddPopupWindow;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class DeviceCommandHistoryActivity extends ActivityParentActivity implements OnClickListener {
    private DeviceInfo deviceInfo;
    private ZhujiInfo zhuji;
    private ListView commandListView;
    private TextView title;
//    private TextView tv_menu;
    private CommandAdapter commandAdapter;
    private List<JSONObject> commandList;
    private View footerView;
    //	private View headview;
    private RelativeLayout titleLay;

    // private LinearLayout footerView_layout;
    private Button footerView_button;
    private int totalSize = 0;
    private KeyItemAdapter keyItemAdapter;
    private EditKeyItemAdapter editItemAdapter;
    private MyGridView keysgGridView; //正常的指令面板
    private RelativeLayout AFpanel; //安防面板
    private List<CommandKey> keys;
    public static int key;//判断按键是震动 1、有声 2、无声0
    // 自定义的弹出框类 右边菜单
    SelectAddPopupWindow menuWindow; // 弹出框
    // 锁存发送 1S发送一次，按住2秒后启动
    private boolean suocun = false;
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 10) { // 获取数据成功
                cancelInProgress();
                commandList.addAll((List<JSONObject>) msg.obj);
                commandAdapter.notifyDataSetChanged();
                if (totalSize == commandList.size() - 1) {
//					Toast.makeText(DeviceCommandHistoryActivity.this,new SimpleDateFormat("MM-dd HH:mm:ss").format(new Date(Util.changeTimeZone(commandList.get(1).getDate("deviceCommandTime").getTime()))) ,
//							Toast.LENGTH_LONG).show();
//					Log.e("sss",commandList.toString());
                    commandListView.removeFooterView(footerView);
                }
            } else if (msg.what == 11) { // 键值获取成功
//				initKeys();
//				if (keys!=null && !keys.isEmpty()) {
//					tv_menu.setVisibility(View.VISIBLE);
//				}
//				keyItemAdapter.notifyDataSetChanged();
            } else if (msg.what == 12) { // 锁存&发射
                if (suocun || msg.arg2 == 1) {
                    if (msg.arg2 == 1) {
                        defHandler.removeMessages(12);
                        return true;
                    }
                    SyncMessage message1 = new SyncMessage();
                    message1.setCommand(SyncMessage.CommandMenu.rq_control.value());
                    message1.setDeviceid(deviceInfo.getId());
                    // 操作
                    message1.setSyncBytes(new byte[]{(byte) keys.get(msg.arg1).getSort()});
                    SyncMessageContainer.getInstance().produceSendMessage(message1);
                    if (suocun) {
                        defHandler.sendMessageDelayed(defHandler.obtainMessage(12, msg.arg1, 0), 1000);
                    }
                }
            }
            return false;
        }
    };
    private Handler defHandler = new WeakRefHandler(mCallback);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化右边菜单
        menuWindow = new SelectAddPopupWindow(DeviceCommandHistoryActivity.this, this, 1, false, deviceInfo);
        if (!com.smartism.znzk.util.StringUtils.isEmpty(MainApplication.app.getAppGlobalConfig().getAPPID())) {
            setContentView(R.layout.activity_device_command_history);
        }else{
            setContentView(R.layout.activity_device_command_history_nop2pview);
        }
        deviceInfo = (DeviceInfo) getIntent().getSerializableExtra("device");
        titleLay = (RelativeLayout) findViewById(R.id.dinfo_layout);
        commandListView = (ListView) findViewById(R.id.command_list);
        title = (TextView) findViewById(R.id.tv_title);
        title.setText(deviceInfo.getName());
        commandAdapter = new CommandAdapter(DeviceCommandHistoryActivity.this);
        footerView = LayoutInflater.from(DeviceCommandHistoryActivity.this).inflate(R.layout.list_foot_loadmore, null);
//		headview = LayoutInflater.from(DeviceCommandHistoryActivity.this).inflate(R.layout.listview_head, null);
//		headview.setVisibility(View.GONE);
//		keysgGridView = (MyGridView) headview.findViewById(R.id.command_key);
        footerView_button = (Button) footerView.findViewById(R.id.load_more);
        // footerView_layout.setVisibility(View.GONE);
        commandListView.addFooterView(footerView);
//		commandListView.addHeaderView(headview);
        commandList = new ArrayList<JSONObject>();
//        tv_menu = (TextView) findViewById(R.id.menu_tv);
//        tv_menu.setOnClickListener(this);
        JSONObject oo = new JSONObject();
        oo.put("deviceOperator", getString(R.string.history_head_deviceOperator));
        oo.put("deviceCommand", getString(R.string.history_head_content));
        oo.put("deviceCommandTime", getString(R.string.history_head_time));
        commandList.add(oo);
        commandListView.setAdapter(commandAdapter);
        showInProgress(getString(R.string.loading), false, true);
        JavaThreadPool.getInstance().excute(new CommandLoad(0, 20));
        footerView_button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // footerView_layout.setVisibility(View.GONE);
                // 加载更多按钮点击
                JavaThreadPool.getInstance().excute(new CommandLoad(commandList.size() - 1, 20));
            }
        });
        initDeviceLaytouInfo();
    }

    @Override
    protected void onResume() {

        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (menuWindow != null) {
            menuWindow.dismiss();
            menuWindow = null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.menu_tv:
//				if(tv_menu.getText().toString().equals(getString(R.string.action_settings))){
//					menuWindow.showAtLocation(v, Gravity.TOP | Gravity.RIGHT, 0,
//							Util.dip2px(getApplicationContext(), 55) + Util.getStatusBarHeight(this));
//				}else{
//					saveModify();
//					tv_menu.setText(getString(R.string.action_settings));
//				}
                break;
            case R.id.pop_edit:
//				editItemAdapter = new EditKeyItemAdapter(this);
//				keysgGridView.setAdapter(editItemAdapter);
//				keysgGridView.setVisibility(View.VISIBLE);
//				tv_menu.setText(getString(R.string.save));
            /*if (tv_menu.getText().toString().equals(getString(R.string.edit))) {

			} else {
				saveModify();
				tv_menu.setText(getString(R.string.action_settings));
			}*/
//				menuWindow.dismiss();
                break;
            case R.id.pop_shock:
                key = 1;
                dcsp.putInt(Constant.BTN_CONTROLSTYLE, key).commit();
                menuWindow.dismiss();
                break;
            case R.id.pop_voiced:
                key = 2;
                dcsp.putInt(Constant.BTN_CONTROLSTYLE, key).commit();
                menuWindow.dismiss();
                break;
            case R.id.pop_silent:
                key = 0;
                dcsp.putInt(Constant.BTN_CONTROLSTYLE, key).commit();
                menuWindow.dismiss();
                break;
            case R.id.pop_voice_and_shock:
                key = 3;
                dcsp.putInt(Constant.BTN_CONTROLSTYLE, key).commit();
                menuWindow.dismiss();
                break;
            default:
                break;
        }

    }

    private void saveModify() {
        final long did = deviceInfo.getId();
        JavaThreadPool.getInstance().excute(new Runnable() {

            @Override
            public void run() {
                String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
                JSONObject pJsonObject = new JSONObject();
                pJsonObject.put("did", did);
                JSONArray p = new JSONArray();
                for (CommandKey key : keys) {
                    JSONObject o = new JSONObject();
                    o.put("id", key.getId());
                    o.put("n", key.getName());
                    o.put("i", key.getIoc());
                    o.put("w", key.getWhere());
                    p.add(o);

                }
                pJsonObject.put("keys", p);
                final String result = HttpRequestUtils
                        .requestoOkHttpPost(server + "/jdm/s3/d/dkeyupdate", pJsonObject, DeviceCommandHistoryActivity.this);

                if ("0".equals(result)) {
                    defHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(DeviceCommandHistoryActivity.this,
                                    getString(R.string.activity_editscene_modify_success), Toast.LENGTH_LONG).show();
                            defHandler.sendEmptyMessage(11);
                        }
                    });
                }
            }
        });
    }

    private void initKeys() {
        // keysgGridView = (GridView)findViewById(R.id.command_key);
        keyItemAdapter = new KeyItemAdapter(DeviceCommandHistoryActivity.this);
        keysgGridView.setAdapter(keyItemAdapter);
//		headview.setVisibility(View.VISIBLE);
        // keysgGridView.setVisibility(View.VISIBLE);
    }

    private void initDeviceLaytouInfo() {
        ImageView logo = (ImageView) findViewById(R.id.device_logo);
        TextView name = (TextView) findViewById(R.id.d_name);
        TextView where = (TextView) findViewById(R.id.d_where);
        TextView type = (TextView) findViewById(R.id.d_type);
        where.setText(deviceInfo.getWhere());
        type.setText(deviceInfo.getType());
        if (ControlTypeMenu.wenduji.value().equals(deviceInfo.getControlType())) {
            // 设置图片
            if (VersionType.CHANNEL_UCTECH.equals(((MainApplication) getApplication()).getAppGlobalConfig().getVersion())) {
                try {
                    logo.setImageBitmap(BitmapFactory
                            .decodeStream(getAssets().open("uctech/uctech_t_" + deviceInfo.getChValue() + ".png")));
                } catch (IOException e) {
//					Log.e("uctech", "读取图片文件错误");
                }
            } else {
                ImageLoader.getInstance().displayImage(dcsp.getString(Constant.HTTP_DATA_SERVERS, "")
                        + "/devicelogo/" + deviceInfo.getLogo(), logo, new ImageLoadingBar());
            }
            name.setText(deviceInfo.getName() + "CH" + deviceInfo.getChValue());
        } else if (ControlTypeMenu.wenshiduji.value().equals(deviceInfo.getControlType())) {
            if (VersionType.CHANNEL_UCTECH.equals(((MainApplication) getApplication()).getAppGlobalConfig().getVersion())) {
                try {
                    logo.setImageBitmap(BitmapFactory
                            .decodeStream(getAssets().open("uctech/uctech_th_" + deviceInfo.getChValue() + ".png")));
                } catch (IOException e) {
//					Log.e("uctech", "读取图片文件错误");
                }
            } else {
                ImageLoader.getInstance().displayImage(dcsp.getString(Constant.HTTP_DATA_SERVERS, "")
                        + "/devicelogo/" + deviceInfo.getLogo(), logo, new ImageLoadingBar());
            }
            name.setText(deviceInfo.getName() + "CH" + deviceInfo.getChValue());
        } else {
            // 设置图片
            ImageLoader.getInstance().displayImage(dcsp.getString(Constant.HTTP_DATA_SERVERS, "") + "/devicelogo/" + deviceInfo.getLogo(),
                    logo, new ImageLoadingBar());
            name.setText(deviceInfo.getName());
        }
        if (deviceInfo.getControlType().contains("xiaxing") && !"zjykq".equals(deviceInfo.getCa()) && !"lb".equals(deviceInfo.getCa())) { //喇叭和主机遥控器需要屏蔽原有按键
            JavaThreadPool.getInstance().excute(new CommandKeyLoad());
        }
        if (MainApplication.app.getAppGlobalConfig().isShowAFpanel()) {
            if (deviceInfo.getType().equals("502R")) {
                AFpanel = (RelativeLayout) findViewById(R.id.anfang_panel);
                AFpanel.setVisibility(View.VISIBLE);
            }
        }
//        zhuji = DatabaseOperator.getInstance(DeviceCommandHistoryActivity.this)
//                .queryDeviceZhuJiInfo(dcsp.getString(Constant.APP_MASTERID, ""));
        //替换
        zhuji = DatabaseOperator.getInstance(DeviceCommandHistoryActivity.this).queryDeviceZhuJiInfo(ZhujiListFragment.getMasterId());
        key = dcsp.getInt(Constant.BTN_CONTROLSTYLE, 0);
    }

    public void back(View v) {
        finishActivity();
    }

    public void finishActivity(){
        Contact mContact = (Contact) getIntent().getSerializableExtra("contact");
        String action = getIntent().getStringExtra("action");
        if (mContact!=null){
            if (action!=null&&!"".equals(action)){
                Intent intent = new Intent();
                intent.putExtra("device",deviceInfo);
                intent.putExtra("contact",mContact);
                intent.setAction(action);
                startActivity(intent);
            }
        }
        finish();
    }


    public void lineChart(View v) {
//        Intent intent = new Intent(DeviceCommandHistoryActivity.this, THLineChartActivity.class);
        Intent intent = new Intent(DeviceCommandHistoryActivity.this, null);
        intent.putExtra("device", deviceInfo);
        startActivity(intent);
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
                viewCache.operator = (TextView) view.findViewById(R.id.last_operator);
                view.setTag(viewCache);
            } else {
                viewCache = (DeviceInfoView) view.getTag();
            }
            if (i != 0) {
                String opreator = commandList.get(i).getString("send");

                viewCache.operator.setText(commandList.get(i).getString("send"));
                try {
                    //需要做本地化时间转换。时区不用转服务器已经转了
//					viewCache.time.setText(SimpleDateFormat.getDateTimeInstance().format(commandList.get(i).getDate("deviceCommandTime")));
                    String time = new SimpleDateFormat("MM-dd HH:mm:ss").format(commandList.get(i).getDate("deviceCommandTime"));
                    viewCache.time.setText(time);
                } catch (Exception e) {
                    viewCache.time.setText(commandList.get(i).getString("deviceCommandTime"));
                }
                String command = commandList.get(i).getString("deviceCommand");
                if ("tzc".equals(deviceInfo.getCa())) {
                    try {
                        String unitName = "";
                        long commandUnit = Long.parseLong(command.substring(0, 4));
                        double commandValue = Integer.parseInt(command.substring(4), 16) / 10.0;
                        if (commandUnit == 2) {
                            unitName = "KG";
                        }
                        viewCache.command.setText(commandValue + unitName);
                    } catch (Exception e) {
                        viewCache.command.setText("error");
                    }
                } else {
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
//			object.put("uid", dcsp.getLong(Constant.LOGIN_APPID, 0));
//			object.put("code", dcsp.getString(Constant.LOGIN_CODE, ""));
//			object.put("lang", Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry());
            String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/d/hm", object,
                    DeviceCommandHistoryActivity.this);
//			String result = HttpRequestUtils.requestHttpServer(
//					 server + "/jdm/service/hm?v="
//							+ URLEncoder.encode(SecurityUtil.crypt(object.toJSONString(), Constant.KEY_HTTP)),
//					DeviceCommandHistoryActivity.this, defHandler);
            if ("-3".equals(result)) {
                defHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(DeviceCommandHistoryActivity.this, getString(R.string.history_response_nodevice),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else if (result.length() > 4) {

                List<JSONObject> commands = new ArrayList<JSONObject>();
                JSONObject resultJson = null;
                try {
                    resultJson = JSON.parseObject(result);
                } catch (Exception e) {
                    LogUtil.e(getApplicationContext(), TAG, "解密错误：：", e);
                    return;
                }

                //Log.e("aaa", resultJson.toJSONString()+"resultJson解析出来的");
                JSONArray array = resultJson.getJSONArray("result");
                if (array != null && !array.isEmpty()) {
                    for (int j = 0; j < array.size(); j++) {
                        commands.add(array.getJSONObject(j));
                    }
                }
                // 请求成功了，需要刷新数据到页面，也需要清除此设备的历史未读记录
                ContentValues values = new ContentValues();
                values.put("nr", 0); // 未读消息数
                DatabaseOperator.getInstance(DeviceCommandHistoryActivity.this).getWritableDatabase().update(
                        "DEVICE_STATUSINFO", values, "id = ?", new String[]{String.valueOf(deviceInfo.getId())});

                totalSize = resultJson.getIntValue("allCount");
                Message m = defHandler.obtainMessage(10);
                m.obj = commands;
                defHandler.sendMessage(m);
            }
        }
    }

    class CommandKeyLoad implements Runnable {
        @Override
        public void run() {
            /*
			 * if (ControlTypeMenu.xiaxing_1.value().equals(deviceInfo.
			 * getControlType ())) { keys = new ArrayList<CommandKey>();
			 * CommandKey key = new CommandKey(); key.setSort(2);
			 * key.setName("OF/OFF"); key.setWhere(1); keys.add(key); Message m
			 * = defHandler.obtainMessage(11); defHandler.sendMessage(m); }else
			 * if( ControlTypeMenu.xiaxing_2.value().equals(deviceInfo.
			 * getControlType ())){ keys = new ArrayList<CommandKey>();
			 * CommandKey key = new CommandKey(); key.setSort(1);
			 * key.setName("ON"); key.setWhere(1); keys.add(key); key = new
			 * CommandKey(); key.setSort(0); key.setName("OFF");
			 * key.setWhere(2); keys.add(key); Message m =
			 * defHandler.obtainMessage(11); defHandler.sendMessage(m); }else{
			 */
            String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("did", deviceInfo.getId());
//			object.put("uid", dcsp.getLong(Constant.LOGIN_APPID, 0));
//			object.put("code", dcsp.getString(Constant.LOGIN_CODE, ""));
//			String result = HttpRequestUtils.requestHttpServer(
//					 server + "/jdm/service/dkeycommands?v="
//							+ URLEncoder.encode(SecurityUtil.crypt(object.toJSONString(), Constant.KEY_HTTP)),
//					DeviceCommandHistoryActivity.this, defHandler);
            String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/d/dkeycomms", object, DeviceCommandHistoryActivity.this);
            if (result.length() > 2) {
                keys = new ArrayList<CommandKey>();
                JSONArray array = JSON.parseArray(result);
                if (array != null && !array.isEmpty()) {
                    for (int j = 0; j < array.size(); j++) {
                        CommandKey key = new CommandKey();
                        key.setSort(array.getJSONObject(j).getIntValue("s"));
                        key.setName(array.getJSONObject(j).getString("n"));
                        key.setIoc(array.getJSONObject(j).getString("i"));
                        key.setWhere(array.getJSONObject(j).getIntValue("w"));
                        key.setId(array.getJSONObject(j).getIntValue("id"));
                        keys.add(key);
                    }
                }
                Message m = defHandler.obtainMessage(11);
                defHandler.sendMessage(m);
            }

        }
    }

    class CommandKey implements Serializable {
        private long id;
        private int sort;
        private String name;
        private String ioc;
        private int where;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public int getSort() {
            return sort;
        }

        public void setSort(int sort) {
            this.sort = sort;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIoc() {
            return ioc;
        }

        public void setIoc(String ioc) {
            this.ioc = ioc;
        }

        public int getWhere() {
            return where;
        }

        public void setWhere(int where) {
            this.where = where;
        }

        @Override
        public String toString() {
            return "CommandKey [id=" + id + ", sort=" + sort + ", name=" + name + ", ioc=" + ioc + ", where=" + where
                    + "]";
        }

    }


    class KeyItemAdapter extends BaseAdapter {
        /**
         * 视图内部类
         *
         * @author Administrator
         */
        class DeviceInfoView {
            ImageView keybg;
            TextView keyname;
        }

        LayoutInflater layoutInflater;

        public KeyItemAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return keys.size();
        }

        @Override
        public Object getItem(int position) {
            return keys.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            DeviceInfoView viewCache = new DeviceInfoView();
            if (view == null) {
                view = layoutInflater.inflate(R.layout.activity_history_key_item, null);
                viewCache.keybg = (ImageView) view.findViewById(R.id.dinfo_keybg);
                viewCache.keyname = (TextView) view.findViewById(R.id.dinfo_keyname);
                view.setTag(viewCache);
            } else {
                viewCache = (DeviceInfoView) view.getTag();
            }
            viewCache.keyname.setText(keys.get(position).getName());
            if (!StringUtils.isEmpty(keys.get(position).getIoc())) {
                viewCache.keybg.setImageResource(
                        getResources().getIdentifier(keys.get(position).getIoc(), "drawable", getBaseContext().getPackageName()));
//				ImageLoader.getInstance().displayImage(
//						 dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "")
//								+ "/devicelogo/keysico/" + keys.get(position).getIoc(),
//						viewCache.keybg, new ImageLoadingBar());
            } else {
                viewCache.keybg.setBackgroundResource(R.drawable.device_item_one_button_bg);
            }

            viewCache.keybg.playSoundEffect(SoundEffectConstants.CLICK);
            viewCache.keybg.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            if (Util.isFastClick()) {
                                Toast.makeText(mContext, getString(R.string.activity_devices_commandhistory_tip), Toast.LENGTH_SHORT).show();
                            } else {
                                v.setPressed(true);
                                suocun = true;
                                defHandler.sendMessage(defHandler.obtainMessage(12, position, 0));
                                Vibrator vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
                                AudioManager mAudioMgr = (AudioManager) getApplicationContext().getSystemService(getApplicationContext().AUDIO_SERVICE);
                                if (key == 1) {
                                    vibrator.vibrate(new long[]{0, 200}, -1);
                                } else if (key == 2) {
                                    mAudioMgr.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_LEFT);
                                } else if (key == 3) {
                                    vibrator.vibrate(new long[]{0, 200}, -1);
                                    mAudioMgr.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_LEFT);
                                } else {
                                    if (vibrator != null) {
                                        vibrator.cancel();
                                    }

                                }
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            v.setPressed(false);
                            suocun = false;
                            defHandler.sendMessage(defHandler.obtainMessage(12, position, 1));
                            break;

                        default:
                            break;
                    }
                    return true;
                }
            });
            return view;
        }

    }

    class EditKeyItemAdapter extends BaseAdapter {
        /**
         * 视图内部类
         *
         * @author Administrator
         */
        class DeviceInfoView {
            Spinner edit_keybg;
            EditText edit_keyname;

        }

        LayoutInflater layoutInflater;

        public EditKeyItemAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return keys.size();
        }

        @Override
        public Object getItem(int position) {
            return keys.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            DeviceInfoView viewCache = new DeviceInfoView();
            if (view == null) {
                view = layoutInflater.inflate(R.layout.activity_history_edit_key_item, null);
                viewCache.edit_keybg = (Spinner) view.findViewById(R.id.edit_keybg);
                viewCache.edit_keyname = (EditText) view.findViewById(R.id.edit_keyname);
                view.setTag(viewCache);
            } else {
                viewCache = (DeviceInfoView) view.getTag();
            }

            viewCache.edit_keyname.setText(keys.get(position).getName());
            final List<String> resId = new ArrayList<String>();
            resId.add("checkbutton_circular");
            resId.add("checkbutton_rectangle");
            resId.add("checkbutton_square");
            resId.add("checkbutton_triangle");
            for (int i = 0; i < resId.size(); i++) {
                if (keys.get(position).getIoc() != null && keys.get(position).getIoc().equals(resId.get(i))) {
                    String temp = resId.get(0);
                    resId.set(0, resId.get(i));
                    resId.set(i, temp);

                }
            }
            MyAdapter mAdapter = new MyAdapter(resId);

            viewCache.edit_keybg.setAdapter(mAdapter);
            final EditText et = viewCache.edit_keyname;
            viewCache.edit_keyname.addTextChangedListener(new TextWatcher() {
                private CharSequence temp;
                private int editStart;
                private int editEnd;
                private int maxLen = 8;

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    temp = s;
                }

                @Override
                public void afterTextChanged(Editable s) {
                    editStart = et.getSelectionStart();
                    editEnd = et.getSelectionEnd();
                    Log.i("gongbiao1", "" + editStart);
                    if (calculateLength(s.toString()) > maxLen) {
                        s.delete(editStart - 1, editEnd);
                        int tempSelection = editStart;
                        et.setText(s);
                        et.setSelection(tempSelection);
                    }

                    keys.get(position).setName(s.toString());
                }

                private int calculateLength(String etstring) {
                    char[] ch = etstring.toCharArray();

                    int varlength = 0;
                    for (int i = 0; i < ch.length; i++) {
                        if ((ch[i] >= 0x2E80 && ch[i] <= 0xFE4F) || (ch[i] >= 0xA13F && ch[i] <= 0xAA40)
                                || ch[i] >= 0x80) { // 中文字符范围0x4e00 0x9fbb
                            varlength = varlength + 2;
                        } else {
                            varlength++;
                        }
                    }
                    // 这里也可以使用getBytes,更准确嘛
                    // varlength =
                    // etstring.getBytes(CharSet.forName(GBK)).lenght;//
                    // 编码根据自己的需求，注意u8中文占3个字节...
                    return varlength;
                }
            });

            viewCache.edit_keybg.setOnItemSelectedListener(new OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    keys.get(position).setIoc(resId.get(pos));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            return view;
        }

    }

    class MyAdapter extends BaseAdapter {
        private List<String> resId;

        public MyAdapter(List<String> resId) {
            this.resId = resId;
        }

        @Override
        public int getCount() {
            return resId.size();
        }

        @Override
        public Object getItem(int position) {
            return resId.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SpinnerImage spImg = new SpinnerImage();

            if (convertView == null) {
                convertView = View.inflate(DeviceCommandHistoryActivity.this, R.layout.spinner_image_item, null);
                spImg.iv_spinner = (ImageView) convertView.findViewById(R.id.iv_spinner);
                convertView.setTag(spImg);
            } else {
                spImg = (SpinnerImage) convertView.getTag();
            }
            spImg.iv_spinner.setImageResource(
                    getResources().getIdentifier(resId.get(position), "drawable", getBaseContext().getPackageName()));

            return convertView;

        }

        class SpinnerImage {
            ImageView iv_spinner;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            finishActivity();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**********安防面板控制区域*******/
    public void setDisarmingModel(View v) {
        SyncMessage message1 = new SyncMessage();
        message1.setCommand(SyncMessage.CommandMenu.rq_control.value());
        message1.setDeviceid(deviceInfo.getId());
        // 操作
        message1.setSyncBytes(new byte[]{(byte) 2});
        SyncMessageContainer.getInstance().produceSendMessage(message1);
    }

    public void setArmingModel(View v) {
        SyncMessage message1 = new SyncMessage();
        message1.setCommand(SyncMessage.CommandMenu.rq_control.value());
        message1.setDeviceid(deviceInfo.getId());
        // 操作
        message1.setSyncBytes(new byte[]{(byte) 1});
        SyncMessageContainer.getInstance().produceSendMessage(message1);
    }

    public void setHomeModel(View v) {
        SyncMessage message1 = new SyncMessage();
        message1.setCommand(SyncMessage.CommandMenu.rq_control.value());
        message1.setDeviceid(deviceInfo.getId());
        // 操作
        message1.setSyncBytes(new byte[]{(byte) 3});
        SyncMessageContainer.getInstance().produceSendMessage(message1);
    }

    public void setPanicModel(View v) {
        SyncMessage message1 = new SyncMessage();
        message1.setCommand(SyncMessage.CommandMenu.rq_control.value());
        message1.setDeviceid(deviceInfo.getId());
        // 操作
        message1.setSyncBytes(new byte[]{(byte) 4});
        SyncMessageContainer.getInstance().produceSendMessage(message1);
    }
    /******安防面板控制结束*********/
}
