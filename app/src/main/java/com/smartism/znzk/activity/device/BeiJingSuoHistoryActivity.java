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
import android.widget.LinearLayout;
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
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.DeviceInfo.ControlTypeMenu;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.Actions.VersionType;
import com.smartism.znzk.util.DataCenterSharedPreferences;
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

public class BeiJingSuoHistoryActivity extends ActivityParentActivity implements OnClickListener {
    private DeviceInfo deviceInfo;
    private ZhujiInfo zhuji;
    private ListView commandListView;
    private TextView title;
    private TextView tv_menu;
    private CommandAdapter commandAdapter;
    private List<CommandBean> commandList;
    private View footerView;
    //	private View headview;
    private RelativeLayout titleLay;

    // private LinearLayout footerView_layout;
    private Button footerView_button;
    private int totalSize = 0;
    private LinearLayout control_select;
    private ImageView select_all, select_cancle, select_del;
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
            if (msg.what == 4) {
                defHandler.removeMessages(4);
                mContext.cancelInProgress();
                Toast.makeText(mContext.getApplicationContext(), getString(R.string.timeout), Toast.LENGTH_SHORT).show();
            }
            if (msg.what == 10) { // 获取数据成功
                cancelInProgress();
                commandList.addAll((List<CommandBean>) msg.obj);
                commandAdapter.notifyDataSetChanged();
                if (totalSize == commandList.size() - 1) {
//					Toast.makeText(BeiJingSuoHistoryActivity.this,new SimpleDateFormat("MM-dd HH:mm:ss").format(new Date(Util.changeTimeZone(commandList.get(1).getDate("deviceCommandTime").getTime()))) ,
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
    private int type;

    class CommandBean {
        JSONObject jsonObject;
        boolean flag;

        public CommandBean(JSONObject jsonObject, boolean flag) {
            this.jsonObject = jsonObject;
            this.flag = flag;
        }

        public JSONObject getJsonObject() {
            return jsonObject;
        }

        public void setJsonObject(JSONObject jsonObject) {
            this.jsonObject = jsonObject;
        }

        public boolean isFlag() {
            return flag;
        }

        public void setFlag(boolean flag) {
            this.flag = flag;
        }

        @Override
        public String toString() {
            return "CommandBean{" +
                    "jsonObject=" + jsonObject +
                    ", flag=" + flag +
                    '}';
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化右边菜单
        menuWindow = new SelectAddPopupWindow(BeiJingSuoHistoryActivity.this, this, 1, false, deviceInfo);
        setContentView(R.layout.activity_bei_jing_suo_history_layout);
        control_select = (LinearLayout) findViewById(R.id.control_select);
        select_all = (ImageView) findViewById(R.id.select_all);
        select_cancle = (ImageView) findViewById(R.id.select_cancle);
        select_del = (ImageView) findViewById(R.id.select_del);
        deviceInfo = (DeviceInfo) getIntent().getSerializableExtra("device");
        titleLay = (RelativeLayout) findViewById(R.id.dinfo_layout);
        commandListView = (ListView) findViewById(R.id.command_list);
        title =findViewById(R.id.title);
        title.setText(deviceInfo.getName());
        commandAdapter = new CommandAdapter(BeiJingSuoHistoryActivity.this);
        footerView = LayoutInflater.from(BeiJingSuoHistoryActivity.this).inflate(R.layout.list_foot_loadmore, null);
//		headview = LayoutInflater.from(BeiJingSuoHistoryActivity.this).inflate(R.layout.listview_head, null);
//		headview.setVisibility(View.GONE);
//		keysgGridView = (MyGridView) headview.findViewById(R.id.command_key);
        footerView_button = (Button) footerView.findViewById(R.id.load_more);
        // footerView_layout.setVisibility(View.GONE);
        commandListView.addFooterView(footerView);
//		commandListView.addHeaderView(headview);
        commandList = new ArrayList<CommandBean>();
        tv_menu = (TextView) findViewById(R.id.menu_tv);
        tv_menu.setOnClickListener(this);
        JSONObject oo = new JSONObject();
        oo.put("deviceOperator", getString(R.string.history_head_deviceOperator));
        oo.put("deviceCommand", getString(R.string.history_head_content));
        oo.put("deviceCommandTime", getString(R.string.history_head_time));
        commandList.add(new CommandBean(oo, false));
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
        type = getIntent().getIntExtra("type", 0);
        initDeviceLaytouInfo();
        select_all.setOnClickListener(this);
        select_cancle.setOnClickListener(this);
        select_del.setOnClickListener(this);
        if (zhuji.isAdmin()||zhuji.getRolek().equals("lock_num_admin")||zhuji.getRolek().equals("lock_num_partner")){
            //管理、主賬戶、愛人、可以操作歷史
            commandListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    commandList.get(position).setFlag(!commandList.get(position).isFlag());
                    updateItem(position);
                }
            });
            commandListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    if (position>0){
                        control_select.setVisibility(View.VISIBLE);
                        selectFlag = true;
                        commandList.get(position).setFlag(true);
                        commandAdapter.notifyDataSetChanged();
                    }
                    return true;
                }
            });
        }
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
    /**
     * 改变列表选中状态
     */
    public void changeSelectStatus(boolean selectFlag){
        //第一项是标题，不改变
        for (int position=1;position<commandList.size();position++){
            commandList.get(position).setFlag(selectFlag);
        }
    }
    private void finishSelect(){
        changeSelectStatus(false);
        selectFlag = false;
        control_select.setVisibility(View.GONE);
        commandAdapter.notifyDataSetChanged();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.select_all:
                changeSelectStatus(true);
                commandAdapter.notifyDataSetChanged();
                break;
            case R.id.select_cancle:
                finishSelect();
                break;
            case R.id.select_del:
                defHandler.sendEmptyMessageDelayed(4, 15 * 1000);
                showInProgress(getString(R.string.operationing), false, false);
                JavaThreadPool.getInstance().excute(new DelCommandHistorys());
//                for (CommandBean bean: commandList){
//                    Log.e("TAG_!!!","CommandBean:" +bean.getJsonObject().toJSONString());
//                }
                break;
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
                        .requestoOkHttpPost(server + "/jdm/s3/d/dkeyupdate", pJsonObject, BeiJingSuoHistoryActivity.this);

                if ("0".equals(result)) {
                    defHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(BeiJingSuoHistoryActivity.this,
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
        keyItemAdapter = new KeyItemAdapter(BeiJingSuoHistoryActivity.this);
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
        zhuji = DatabaseOperator.getInstance().queryDeviceZhuJiInfo(ZhujiListFragment.getMasterId());
        key = dcsp.getInt(Constant.BTN_CONTROLSTYLE, 0);
    }

    public void back(View v) {
//        jumpToMain();
        finish();

    }


    public void lineChart(View v) {
        Intent intent = new Intent(BeiJingSuoHistoryActivity.this, null);
        intent.putExtra("device", deviceInfo);
        startActivity(intent);
    }

    private boolean selectFlag = false;

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
                viewCache.select = (ImageView) view.findViewById(R.id.select);
                view.setTag(viewCache);
            } else {
                viewCache = (DeviceInfoView) view.getTag();
            }
            if (i != 0) {
//                if (deviceInfo.getCa().equals("zns") && (MainApplication.app.getAppGlobalConfig().getVersion().equals(Actions.VersionType.CHANNEL_JUJIANG)) && i == 1) {
//                    viewCache.time.setTextColor(getResources().getColor(R.color.jjsuo_item_bg));
//                    viewCache.command.setTextColor(getResources().getColor(R.color.jjsuo_item_bg));
//                    viewCache.operator.setTextColor(getResources().getColor(R.color.jjsuo_item_bg));
//                }
                String opreator = commandList.get(i).getJsonObject().getString("send");

                viewCache.operator.setText(commandList.get(i).getJsonObject().getString("send"));
                try {
                    //需要做本地化时间转换。时区不用转服务器已经转了
//					viewCache.time.setText(SimpleDateFormat.getDateTimeInstance().format(commandList.get(i).getDate("deviceCommandTime")));
                    String time = new SimpleDateFormat("MM-dd HH:mm:ss").format(commandList.get(i).getJsonObject().getDate("deviceCommandTime"));
                    viewCache.time.setText(time);
                } catch (Exception e) {
                    viewCache.time.setText(commandList.get(i).getJsonObject().getString("deviceCommandTime"));
                }
                String command = commandList.get(i).getJsonObject().getString("deviceCommand");
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
                if (selectFlag) {
                    viewCache.select.setVisibility(View.VISIBLE);
                    if (commandList.get(i).isFlag()) {
                        viewCache.select.setImageResource(R.drawable.beijingsuo_quanxuann);
                    } else {
                        viewCache.select.setImageResource(R.drawable.beijingsuo_quanxuanmoren);
                    }
                } else {
                    viewCache.select.setVisibility(View.GONE);
                }
            } else {
                viewCache.select.setVisibility(View.GONE);
                viewCache.time.setText(commandList.get(i).getJsonObject().getString("deviceCommandTime"));
                viewCache.command.setText(commandList.get(i).getJsonObject().getString("deviceCommand"));
                viewCache.operator.setText(commandList.get(i).getJsonObject().getString("deviceOperator"));
            }
            if (!deviceInfo.getCak().equals("control")) {
                viewCache.operator.setVisibility(View.GONE);
            }


            return view;
        }

        class DeviceInfoView {
            ImageView select;
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
                    BeiJingSuoHistoryActivity.this);
//			String result = HttpRequestUtils.requestHttpServer(
//					 server + "/jdm/service/hm?v="
//							+ URLEncoder.encode(SecurityUtil.crypt(object.toJSONString())),
//					BeiJingSuoHistoryActivity.this, defHandler);
            if ("-3".equals(result)) {
                defHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(BeiJingSuoHistoryActivity.this, getString(R.string.history_response_nodevice),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else if (result.length() > 4) {

                List<CommandBean> commands = new ArrayList<CommandBean>();
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
                        commands.add(new CommandBean(array.getJSONObject(j), false));
                    }
                }
                // 请求成功了，需要刷新数据到页面，也需要清除此设备的历史未读记录
                ContentValues values = new ContentValues();
                values.put("nr", 0); // 未读消息数
                DatabaseOperator.getInstance(BeiJingSuoHistoryActivity.this).getWritableDatabase().update(
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
//							+ URLEncoder.encode(SecurityUtil.crypt(object.toJSONString())),
//					BeiJingSuoHistoryActivity.this, defHandler);
            String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/d/dkeycomms", object, BeiJingSuoHistoryActivity.this);
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
                convertView = View.inflate(BeiJingSuoHistoryActivity.this, R.layout.spinner_image_item, null);
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

    /**********
     * 安防面板控制区域
     *******/
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


    /**
     * 第三种方法 调用一次getView()方法；Google推荐的做法
     *
     * @param position 要更新的位置
     */
    private void updateItem(int position) {
        /**第一个可见的位置**/
        int firstVisiblePosition = commandListView.getFirstVisiblePosition();
        /**最后一个可见的位置**/
        int lastVisiblePosition = commandListView.getLastVisiblePosition();

        /**在看见范围内才更新，不可见的滑动后自动会调用getView方法更新**/
        if (position >= firstVisiblePosition && position <= lastVisiblePosition) {
            /**获取指定位置view对象**/
            View view = commandListView.getChildAt(position - firstVisiblePosition);
            commandAdapter.getView(position, view, commandListView);
        }
    }

    //多选删除
    class DelCommandHistorys implements Runnable {
        private List<CommandBean> removeList;

        public DelCommandHistorys() {
            removeList = new ArrayList<>();
        }

        @Override
        public void run() {
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("id", deviceInfo.getId());
            JSONArray array = new JSONArray();
            for (CommandBean bean : commandList) {
                if (bean.isFlag()) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("vid", bean.getJsonObject().getString("id"));
                    array.add(jsonObject);
                    removeList.add(bean);
                    Log.e("TAG_！！！！","CommandBean:"+bean.toString());
//                    array.add(bean.getId());
                }
            }
            object.put("vids", array);
            String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/d/hmds", object, BeiJingSuoHistoryActivity.this);
            if ("-3".equals(result)) {
                defHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        defHandler.removeMessages(4);
                        cancelInProgress();
                        Toast.makeText(BeiJingSuoHistoryActivity.this, getString(R.string.history_response_nodevice),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else if ("0".equals(result)) {
                defHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        defHandler.removeMessages(4);
                        cancelInProgress();
                        commandList.removeAll(removeList);
                        finishSelect();
                        Toast.makeText(BeiJingSuoHistoryActivity.this, getString(R.string.success),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                defHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        defHandler.removeMessages(4);
                        cancelInProgress();
                        Toast.makeText(BeiJingSuoHistoryActivity.this, getString(R.string.net_error_operationfailed),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }


}
