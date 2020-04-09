package com.smartism.znzk.activity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.LogUtil;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.view.alertview.AlertView;
import com.smartism.znzk.view.alertview.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

public class ZssOpenCloseHistoryActivity extends ActivityParentActivity implements OnClickListener {
    private DeviceInfo deviceInfo;
    private ListView commandListView;
    private Button title;//back
    private TextView tv_menu;//title
    private CommandAdapter commandAdapter;
    private List<JSONObject> commandList;
    private ZssMenuPopupWindow popupWindow;
    private Context mContext;
    private RelativeLayout relativeLayout;


    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 10) { // 获取数据成功
                cancelInProgress();
                commandList.clear();
                JSONObject oo = new JSONObject();
                oo.put("deviceCommandTime", "编号");
                oo.put("deviceCommand", "类型");
                oo.put("deviceOperator", "昵称");
                commandList.add(oo);
                commandList.addAll((List<JSONObject>) msg.obj);
                commandAdapter.notifyDataSetChanged();
            }
            return false;
        }
    };
    private Handler defHandler = new WeakRefHandler(mCallback);
    private int itemPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化右边菜单
//		menuWindow = new SelectAddPopupWindow(ZssOpenCloseHistoryActivity.this, this,1,false, deviceInfo);
        setContentView(R.layout.activity_zss_open_close_history);
        mContext = this;
        deviceInfo = (DeviceInfo) getIntent().getSerializableExtra("device");
        commandListView = (ListView) findViewById(R.id.command_list);
        title = (Button) findViewById(R.id.command_history_title);
        title.setText(deviceInfo.getName());
        relativeLayout = (RelativeLayout) findViewById(R.id.rl_open_close);
        commandAdapter = new CommandAdapter(ZssOpenCloseHistoryActivity.this);

        commandList = new ArrayList<JSONObject>();
        tv_menu = (TextView) findViewById(R.id.menu_tv);
        tv_menu.setOnClickListener(this);

        commandListView.setAdapter(commandAdapter);
        showInProgress(getString(R.string.loading), false, true);
        JavaThreadPool.getInstance().excute(new CommandLoad(0, Integer.MAX_VALUE));
        commandListView.setOnItemLongClickListener(this.itemLongClickListener);
        popupWindow = new ZssMenuPopupWindow(this, this);
    }

    private AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (position == 0) {
                return false;
            }
            itemPosition = position;
            popupWindow.updateDeviceMenu(mContext);
            popupWindow.showAtLocation(relativeLayout, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置
            return true;
        }
    };

    @Override
    protected void onResume() {

        super.onResume();

    }

    public void addLockUser(View v){
        LayoutInflater factory = LayoutInflater.from(ZssOpenCloseHistoryActivity.this);//提示框
        final View view = factory.inflate(R.layout.zss_edit_add_user, null);//这里必须是final的
        final EditText nicheng = (EditText) view.findViewById(R.id.et_nicheng);//获得输入框对象
        final EditText bianhao = (EditText) view.findViewById(R.id.et_bianhao);//获得输入框对象
        final RadioButton typePwd = (RadioButton) view.findViewById(R.id.et_type_pwd);//获得输入框对象
        final RadioButton typeKp = (RadioButton) view.findViewById(R.id.et_type_kp);//获得输入框对象
        final RadioButton typeZw = (RadioButton) view.findViewById(R.id.et_type_zw);//获得输入框对象
        new AlertDialog.Builder(mContext, R.style.AppTheme_Dialog_Alert)
                .setTitle("添加新用户")//提示框标题
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("确定",//提示框的两个按钮
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                if (TextUtils.isEmpty(bianhao.getText().toString().trim())) {
                                    Toast.makeText(ZssOpenCloseHistoryActivity.this, "编号不能为空", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if (!typePwd.isChecked() && !typeKp.isChecked() && !typeZw.isChecked()) {
                                    Toast.makeText(ZssOpenCloseHistoryActivity.this, "类型必须选择", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                showInProgress(getString(R.string.submiting), false, true);
                                JavaThreadPool.getInstance().excute(new Runnable() {
                                    @Override
                                    public void run() {
                                        String server = dcsp.getString(
                                                Constant.HTTP_DATA_SERVERS, "");
                                        JSONObject object = new JSONObject();
                                        object.put("did",deviceInfo.getId());
                                        if (typePwd.isChecked()){
                                            object.put("type", 2);
                                        }else if(typeKp.isChecked()){
                                            object.put("type", 1);
                                        }else if(typeZw.isChecked()){
                                            object.put("type", 0);
                                        }
                                        object.put("permission", 1);
                                        object.put("number", Integer.parseInt(bianhao.getText().toString()));
                                        object.put("lname", nicheng.getText().toString());
                                        server = server + "/jdm/s3/dln/add";
                                        String result = HttpRequestUtils.requestoOkHttpPost(server, object, ZssOpenCloseHistoryActivity.this);
                                        // -1参数为空，0删除成功
                                        if (result != null && result.equals("0")) {
                                            defHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    cancelInProgress();
                                                    Toast.makeText(ZssOpenCloseHistoryActivity.this, getString(R.string.add_success), Toast.LENGTH_SHORT).show();
                                                    JavaThreadPool.getInstance().excute(new CommandLoad(0, Integer.MAX_VALUE));
                                                }
                                            });
                                        }else{
                                            defHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    cancelInProgress();
                                                    Toast.makeText(ZssOpenCloseHistoryActivity.this, getString(R.string.net_error_operationfailed), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }
                                });
//                                        edit.getText().toString();
//                                        btn_weight.setText(edit.getText().toString());
                            }
                        })
                .setNegativeButton("取消", null).create().show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_setdevice:
                popupWindow.dismiss();
                LayoutInflater factory = LayoutInflater.from(ZssOpenCloseHistoryActivity.this);//提示框
                final View view = factory.inflate(R.layout.zss_edit_box, null);//这里必须是final的
                final EditText edit = (EditText) view.findViewById(R.id.et_nicheng);//获得输入框对象
                new AlertDialog.Builder(mContext, R.style.AppTheme_Dialog_Alert)
                        .setTitle("设置昵称")//提示框标题
                        .setView(view)
                        .setCancelable(false)
                        .setPositiveButton("确定",//提示框的两个按钮
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        if (TextUtils.isEmpty(edit.getText().toString().trim())) {
                                            Toast.makeText(ZssOpenCloseHistoryActivity.this, "昵称不能为空", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        showInProgress(getString(R.string.loading), false, true);
                                        JavaThreadPool.getInstance().excute(new Runnable() {
                                            @Override
                                            public void run() {
                                                String server = dcsp.getString(
                                                        Constant.HTTP_DATA_SERVERS, "");
                                                JSONObject object1 = new JSONObject();
                                                object1.put("did",deviceInfo.getId());
                                                object1.put("vid", commandList.get(itemPosition).getLongValue("id"));
                                                object1.put("nname", edit.getText().toString().trim());
                                                server = server + "/jdm/s3/dln/update";
                                                String result = HttpRequestUtils.requestoOkHttpPost(server, object1, ZssOpenCloseHistoryActivity.this);
                                                // -1参数为空，0删除成功
                                                if (result != null && result.equals("0")) {
                                                    defHandler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            cancelInProgress();
                                                            Toast.makeText(ZssOpenCloseHistoryActivity.this, getString(R.string.device_set_tip_success), Toast.LENGTH_SHORT).show();
                                                            JSONObject object = commandList.get(itemPosition);
                                                            object.put("lname", edit.getText().toString().trim());
                                                            commandAdapter.notifyDataSetChanged();
                                                        }
                                                    });

                                                }
                                            }
                                        });
//                                        edit.getText().toString();
//                                        btn_weight.setText(edit.getText().toString());
                                    }
                                })
                        .setNegativeButton("取消", null).create().show();
                break;
            case R.id.btn_deldevice:
                popupWindow.dismiss();
                new AlertView(getString(R.string.deviceslist_server_leftmenu_deltitle),
                        getString(R.string.deviceslist_server_leftmenu_delmessage),
                        getString(R.string.deviceslist_server_leftmenu_delcancel),
                        new String[]{getString(R.string.deviceslist_server_leftmenu_delbutton)}, null,
                        mContext, AlertView.Style.Alert,
                        new OnItemClickListener() {

                            @Override
                            public void onItemClick(Object o, final int position) {
                                if (position != -1) {
                                    showInProgress(getString(R.string.deviceslist_server_leftmenu_deltips), false, true);
                                    JavaThreadPool.getInstance().excute(new Runnable() {

                                        @Override
                                        public void run() {

                                            String server = dcsp.getString(
                                                    Constant.HTTP_DATA_SERVERS, "");
                                            JSONObject object = new JSONObject();
                                            JSONArray array = new JSONArray();
                                            JSONObject object1 = new JSONObject();
                                            object1.put("vid", commandList.get(itemPosition).getLongValue("id"));
                                            array.add(object1);
                                            object.put("vids", array);
                                            object.put("did",deviceInfo.getId());
                                            server = server + "/jdm/s3/dln/del";
                                            String result = HttpRequestUtils.requestoOkHttpPost(server, object, ZssOpenCloseHistoryActivity.this);
                                            // -1参数为空，0删除成功
                                            if (result != null && result.equals("0")) {
                                                defHandler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        cancelInProgress();
                                                        Toast.makeText(ZssOpenCloseHistoryActivity.this, getString(R.string.device_del_success), Toast.LENGTH_SHORT).show();
                                                        commandList.remove(itemPosition);
                                                        commandAdapter.notifyDataSetChanged();
                                                    }
                                                });

                                            }
                                        }
                                    });
                                }
                            }
                        }).show();
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
            default:
                break;
        }

    }


    public void back(View v) {
        finish();
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
                view = layoutInflater.inflate(R.layout.activity_zss_command_history_list_item, null);
                viewCache.number = (TextView) view.findViewById(R.id.last_time);
                viewCache.type = (TextView) view.findViewById(R.id.last_command);
                viewCache.nicknName = (TextView) view.findViewById(R.id.last_operator);
                view.setTag(viewCache);
            } else {
                viewCache = (DeviceInfoView) view.getTag();
            }
            if (i != 0) {
                String opreator = commandList.get(i).getString("lname");

                viewCache.nicknName.setText(opreator);
                String numberValue = commandList.get(i).getString("number");
                int length = 3 - numberValue.length();
                if (numberValue.length() < 3) {
                    for (int j = 0; j < length; j++) {
                        numberValue = "0" + numberValue;
                    }
                }
                viewCache.number.setText(numberValue);


                int command = commandList.get(i).getIntValue("type");
                if (command == 0) {
                    viewCache.type.setText("指纹");
                } else if (command == 1) {
                    viewCache.type.setText("卡片");
                } else if (command == 2) {
                    viewCache.type.setText("密码");
                }

            } else {
                viewCache.number.setText(commandList.get(i).getString("deviceCommandTime"));
                viewCache.type.setText(commandList.get(i).getString("deviceCommand"));
                viewCache.nicknName.setText(commandList.get(i).getString("deviceOperator"));
            }
            return view;
        }

        class DeviceInfoView {
            TextView number, type, nicknName;
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
            String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/dln/list", object,
                    ZssOpenCloseHistoryActivity.this);
            if ("-3".equals(result)) {
                defHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(ZssOpenCloseHistoryActivity.this, getString(R.string.history_response_nodevice),
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
                JSONArray array = resultJson.getJSONArray("result");
                if (array != null && !array.isEmpty()) {
                    for (int j = 0; j < array.size(); j++) {
                        commands.add(array.getJSONObject(j));
                    }
                }
                // 请求成功了，需要刷新数据到页面，也需要清除此设备的历史未读记录
                ContentValues values = new ContentValues();
                values.put("nr", 0); // 未读消息数
                DatabaseOperator.getInstance(ZssOpenCloseHistoryActivity.this).getWritableDatabase().update(
                        "DEVICE_STATUSINFO", values, "id = ?", new String[]{String.valueOf(deviceInfo.getId())});

                Message m = defHandler.obtainMessage(10);
                m.obj = commands;
                defHandler.sendMessage(m);
            } else if (result.equals("")) {
                defHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                    }
                });
            }
        }
    }


    public class ZssMenuPopupWindow extends PopupWindow {

        private View mMenuView;
        private Button btn_deldevice, btn_setdevice;

        public ZssMenuPopupWindow(Context context, View.OnClickListener itemsOnClick) {
            super(context);
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mMenuView = inflater.inflate(R.layout.zss_item_menu, null);
            btn_deldevice = (Button) mMenuView.findViewById(R.id.btn_deldevice);
            btn_setdevice = (Button) mMenuView.findViewById(R.id.btn_setdevice);

            btn_deldevice.setOnClickListener(itemsOnClick);
            btn_setdevice.setOnClickListener(itemsOnClick);
            //设置SelectPicPopupWindow的View
            this.setContentView(mMenuView);
            //设置SelectPicPopupWindow弹出窗体的宽
            this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            //设置SelectPicPopupWindow弹出窗体的高
            this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            //设置SelectPicPopupWindow弹出窗体可点击
            this.setFocusable(true);
            //设置SelectPicPopupWindow弹出窗体动画效果
            this.setAnimationStyle(R.style.Devices_list_menu_Animation);
            //实例化一个ColorDrawable颜色为半透明
            ColorDrawable dw = new ColorDrawable(0x00000000);
            //设置SelectPicPopupWindow弹出窗体的背景
            this.setBackgroundDrawable(dw);
            //mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
            mMenuView.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {

                    int height = mMenuView.findViewById(R.id.pop_layout).getTop();
                    int y = (int) event.getY();
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (y < height) {
                            dismiss();
                        }
                    }
                    return true;
                }
            });

        }


        public void updateDeviceMenu(Context context) {
            btn_setdevice.setText(context.getResources().getString(R.string.zss_item_edit));
            btn_deldevice.setText(context.getResources().getString(R.string.zss_item_del));
        }

    }
}
