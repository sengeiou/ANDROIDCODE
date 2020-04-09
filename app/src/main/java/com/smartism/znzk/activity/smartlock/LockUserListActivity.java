package com.smartism.znzk.activity.smartlock;

import android.annotation.SuppressLint;
import android.content.*;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.*;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.view.alertview.AlertView;
import com.smartism.znzk.view.alertview.OnItemClickListener;
import com.smartism.znzk.xiongmai.utils.XMProgressDialog;

import java.util.ArrayList;
import java.util.List;

public class LockUserListActivity extends ActivityParentActivity implements OnClickListener,OnItemClickListener{
    private DeviceInfo deviceInfo;
    private ListView commandListView;
    private TextView tv_menu;//title
    private CommandAdapter commandAdapter;
    private List<JSONObject> commandList;
    private ZssMenuPopupWindow popupWindow;
    private Context mContext;
    private RelativeLayout relativeLayout;


    //宏泰
    private ImageView add_user_img;
    private AlertDialog mDialog;


    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 10) { // 获取数据成功
                cancelInProgress();
                commandList.clear();
                JSONObject oo = new JSONObject();
                oo.put("deviceCommandTime",getString(R.string.jjsuo_user_number));
                oo.put("deviceCommand", getString(R.string.jjsuo_user_type));
                oo.put("deviceOperator", getString(R.string.jjsuo_user_lname));
                oo.put("deviceOperatorName",getString(R.string.jjsuo_user_character));
                commandList.add(oo);
                commandList.addAll((List<JSONObject>) msg.obj);
                commandAdapter.notifyDataSetChanged();
            }
            return false;
        }
    };
    private Handler defHandler = new WeakRefHandler(mCallback);
    private int itemPosition = -1;

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action   = intent.getAction();
            switch (action){
                case Actions.REFRESH_DEVICES_LIST:
                    JavaThreadPool.getInstance().excute(new CommandLoad(0, Integer.MAX_VALUE));
                    break ;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化右边菜单
     //	menuWindow = new SelectAddPopupWindow(ZssOpenCloseHistoryActivity.this, this,1,false, deviceInfo);
        setContentView(R.layout.activity_lock_user_list);
        mContext = this;
        deviceInfo = (DeviceInfo) getIntent().getSerializableExtra("device");
        commandListView = (ListView) findViewById(R.id.command_list);
        relativeLayout = (RelativeLayout) findViewById(R.id.rl_open_close);
        commandAdapter = new CommandAdapter(LockUserListActivity.this);

        commandList = new ArrayList<JSONObject>();
        tv_menu = (TextView) findViewById(R.id.menu_tv);
        tv_menu.setText(deviceInfo.getName());

        commandListView.setAdapter(commandAdapter);
        showInProgress(getString(R.string.loading), false, true);
        JavaThreadPool.getInstance().excute(new CommandLoad(0, Integer.MAX_VALUE));
        commandListView.setOnItemLongClickListener(this.itemLongClickListener);
        popupWindow = new ZssMenuPopupWindow(this, this);

        add_user_img = findViewById(R.id.add_user_img);
        addUserHongTai();

        if(Actions.VersionType.CHANNEL_JAOLH.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            add_user_img.setVisibility(View.VISIBLE);
            commandListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        ZhujiInfo zhujiInfo = DatabaseOperator.getInstance().queryDeviceZhuJiInfo(deviceInfo.getZj_id());
                        if(zhujiInfo!=null&&zhujiInfo.isAdmin()){
                            String perStartTime = commandList.get(position).getString("perStartTime");
                            String perEndTime = commandList.get(position).getString("perEndTime");
                            if(!TextUtils.isEmpty(perStartTime)&&!TextUtils.isEmpty(perEndTime)){
                                Intent intent = new Intent(mContext,JieaoLockUserInfoActivity.class);
                                intent.putExtra("perStartTime",perStartTime);
                                intent.putExtra("perEndTime",perEndTime);
                                intent.putExtra("number",commandList.get(position).getString("number"));
                                intent.putExtra("lname",commandList.get(position).getString("lname"));
                                startActivity(intent);
                            }
                        }
                }
            });
            add_user_img.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClass(mContext,GatewaySettingActivity.class);
                    intent.putExtra("device",deviceInfo);
                    startActivityForResult(intent,99);
                }
            });
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(Actions.REFRESH_DEVICES_LIST);
        registerReceiver(receiver,filter);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==99&&resultCode==RESULT_OK){
            //刷新数据
            JavaThreadPool.getInstance().excute(new CommandLoad(0, Integer.MAX_VALUE));
        }
    }

    EditText admin_pwd_edit,new_pwd_edit,user_name_edit;
    TextView hongtai_cancel_user ,hongtai_confirm_user;
    XMProgressDialog xmProgressDialog ;
    private void addUserHongTai(){
        if(deviceInfo!=null&&deviceInfo.getMc()!=null&&deviceInfo.getMc().equals("hongtai")){
            final LinearLayout add_user_view = (LinearLayout) getLayoutInflater().inflate(R.layout.hongtai_lock_add_user,commandListView,false);
            GradientDrawable drawable = new GradientDrawable();
            drawable.setColor(Color.WHITE);
            drawable.setCornerRadius(getResources().getDimension(R.dimen.dp_12));
            add_user_view.setBackgroundDrawable(drawable);
            admin_pwd_edit  = add_user_view.findViewById(R.id.admin_pwd_edit);
            new_pwd_edit = add_user_view.findViewById(R.id.new_pwd_edit);
            user_name_edit = add_user_view.findViewById(R.id.user_name_edit);
            hongtai_cancel_user = add_user_view.findViewById(R.id.hongtai_cancel_user);
            hongtai_confirm_user = add_user_view.findViewById(R.id.hongtai_confirm_user);

            mDialog =  new AlertDialog.Builder(mContext).setView(add_user_view).setCancelable(true)
                    .create();
            mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            add_user_img.setVisibility(View.VISIBLE);
            add_user_img.setVisibility(View.VISIBLE);
            add_user_img.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    if(!mDialog.isShowing()){
                        mDialog.show();
                    }
                }
            });

            hongtai_cancel_user.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    mDialog.dismiss();
                    admin_pwd_edit.setText("");
                    new_pwd_edit.setText("");
                    user_name_edit.setText("");
                }
            });

            hongtai_confirm_user.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    mDialog.dismiss();
                    String adminpwd = admin_pwd_edit.getText().toString();
                    String userpwd= new_pwd_edit.getText().toString();
                    String name = user_name_edit.getText().toString();
                    if(TextUtils.isEmpty(adminpwd)||adminpwd.length()<6){
                        Toast toast = Toast.makeText(mContext,"",Toast.LENGTH_LONG);
                        toast.setText(getString(R.string.input_password_request));
                        toast.show();
                        return ;
                    }

                    if(TextUtils.isEmpty(userpwd)||userpwd.length()<6){
                        Toast toast = Toast.makeText(mContext,"",Toast.LENGTH_LONG);
                        toast.setText(getString(R.string.input_password_request));
                        toast.show();
                        return ;
                    }

                    if(!TextUtils.isEmpty(name)){
                        name = name.trim();
                        List<String> presenceUserName = new ArrayList<>();
                        for(int i=1;i<commandList.size();i++){
                            String tempName  = commandList.get(i).getString("lname");
                            if(TextUtils.isEmpty(tempName)){
                                //为null或者"",避免出现null
                                tempName = "";
                            }
                            presenceUserName.add(tempName);
                        }
                        for(String temp : presenceUserName){
                            if(temp.equals(name)){
                                //用户名已存在
                                Toast toast = Toast.makeText(mContext,"",Toast.LENGTH_LONG);
                                toast.setText(getString(R.string.jjsuo_sp_name));
                                toast.show();
                                return ;
                            }
                        }
                    }else{
                        Toast toast = Toast.makeText(mContext,"",Toast.LENGTH_LONG);
                        toast.setText(getString(R.string.username_error));
                        toast.show();
                        return ;
                    }

                    showInProgress(getString(R.string.ongoing));
                    JavaThreadPool.getInstance().excute(new Runnable() {
                        @Override
                        public void run() {
                            String server = dcsp.getString(Constant.HTTP_DATA_SERVERS,"");
                            String admin = admin_pwd_edit.getText().toString();
                            String user = new_pwd_edit.getText().toString();
                            String userName = user_name_edit.getText().toString().trim();
                            JSONObject object = new JSONObject();
                            object.put("did",deviceInfo.getId());
                            object.put("type",2);
                            object.put("lname",userName);
                            object.put("permission",1);
                            object.put("conpassword",user);
                            object.put("adminpassword",admin);
                            final String result = HttpRequestUtils.requestoOkHttpPost(server+"/jdm/s3/dln/add",object,LockUserListActivity.this);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    admin_pwd_edit.setText("");
                                    new_pwd_edit.setText("");
                                    user_name_edit.setText("");
                                    if(result.equals("0")){
                                        //成功
                                        Toast toast = Toast.makeText(LockUserListActivity.this,"",Toast.LENGTH_LONG);
                                        toast.setText(getString(R.string.fragment_time_success));
                                        toast.show();
                                        //请求获取最新列表
                                        JavaThreadPool.getInstance().excute(new CommandLoad(0, Integer.MAX_VALUE));
                                        return ;
                                    }else if(result.equals("-3")){
                                        //主机不在线
                                        Toast toast = Toast.makeText(LockUserListActivity.this,"",Toast.LENGTH_LONG);
                                        toast.setText(getString(R.string.activity_zhuji_not));
                                        toast.show();
                                    }else if(result.equals("-5")){
                                        //密码已存在
                                        Toast toast = Toast.makeText(mContext,"",Toast.LENGTH_LONG);
                                        toast.setText(getString(R.string.password_repeat));
                                        toast.show();
                                    }else if(result.equals("-6")){
                                        //无权限
                                        Toast toast = Toast.makeText(mContext,"",Toast.LENGTH_LONG);
                                        toast.setText(getString(R.string.insufficient_permissions));
                                        toast.show();
                                    }else if(result.equals("-7")){
                                        Toast toast = Toast.makeText(mContext,"",Toast.LENGTH_LONG);
                                        toast.setText(getString(R.string.admin_password_error));
                                        toast.show();
                                    }else{
                                        Toast toast = Toast.makeText(LockUserListActivity.this,"",Toast.LENGTH_LONG);
                                        toast.setText(getString(R.string.net_error_operationfailed));
                                        toast.show();
                                    }
                                    cancelInProgress();
                                }
                            });
                        }
                    });

                }
            });
        }
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
        LayoutInflater factory = LayoutInflater.from(LockUserListActivity.this);//提示框
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
                                    Toast.makeText(LockUserListActivity.this, "编号不能为空", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if (!typePwd.isChecked() && !typeKp.isChecked() && !typeZw.isChecked()) {
                                    Toast.makeText(LockUserListActivity.this, "类型必须选择", Toast.LENGTH_SHORT).show();
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
                                        String result = HttpRequestUtils.requestoOkHttpPost( server, object, LockUserListActivity.this);
                                        // -1参数为空，0删除成功
                                        if (result != null && result.equals("0")) {
                                            defHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    cancelInProgress();
                                                    Toast.makeText(LockUserListActivity.this, getString(R.string.add_success), Toast.LENGTH_SHORT).show();
                                                    JavaThreadPool.getInstance().excute(new CommandLoad(0, Integer.MAX_VALUE));
                                                }
                                            });
                                        }else{
                                            defHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    cancelInProgress();
                                                    Toast.makeText(LockUserListActivity.this, getString(R.string.net_error_operationfailed), Toast.LENGTH_SHORT).show();
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
        if(receiver!=null){
            unregisterReceiver(receiver);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_setdevice:
                popupWindow.dismiss();
                LayoutInflater factory = LayoutInflater.from(LockUserListActivity.this);//提示框
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
                                            Toast.makeText(LockUserListActivity.this, "昵称不能为空", Toast.LENGTH_SHORT).show();
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
                                                String result = HttpRequestUtils.requestoOkHttpPost( server, object1, LockUserListActivity.this);
                                                // -1参数为空，0删除成功
                                                if (result != null && result.equals("0")) {
                                                    defHandler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            cancelInProgress();
                                                            Toast.makeText(LockUserListActivity.this, getString(R.string.device_set_tip_success), Toast.LENGTH_SHORT).show();
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
               if(MainApplication.app.getAppGlobalConfig().getVersion().equals(Actions.VersionType.CHANNEL_ZHICHENG)
                       ||deviceInfo.getCa().equals(DeviceInfo.CaMenu.wifizns.value())
                       ||Actions.VersionType.CHANNEL_JAOLH.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
                    new AlertView(getString(R.string.deviceslist_server_leftmenu_deltitle),
                            getString(R.string.weight_del_user),
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
                                                String temp = object.toJSONString();
                                                String result = HttpRequestUtils.requestoOkHttpPost( server, object, LockUserListActivity.this);
                                                // -1参数为空，0删除成功
                                                if (result != null && result.equals("0")) {
                                                    defHandler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            cancelInProgress();
                                                            Toast.makeText(LockUserListActivity.this, getString(R.string.device_del_success), Toast.LENGTH_SHORT).show();
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
                }else if(deviceInfo.getCa().equals(DeviceInfo.CaMenu.zhinengsuo.value())){
                   type = 3;
                   inputPwd(type);
               }
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


    private InputMethodManager imm;
    private EditText etName;
    private AlertView mAlertViewExt;
    private int type = -1;
    private void inputPwd(int type) {
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        String title = "";
        String content = "";

        if(type==3){
            title = getString(R.string.admin_password_input);
            content = getString(R.string.inputpassword);
        }
        //拓展窗口
        mAlertViewExt = new AlertView(null, title, getString(R.string.cancel), null, new String[]{getString(R.string.compele)},
                this, AlertView.Style.Alert, this);
        ViewGroup extView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.activity_addzhuji_alertext_form, null);
        etName = (EditText) extView.findViewById(R.id.etName);

        etName.setText(title);
        etName.setHint(content);

        if(type==3){
            etName.setText("");
            etName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});//最大输入6位
            etName.setInputType(InputType.TYPE_CLASS_NUMBER);//只能输入数字
            etName.setTransformationMethod(PasswordTransformationMethod.getInstance());//设置为密文
        }

        etName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                //输入框出来则往上移动
                boolean isOpen = imm.isActive();
                mAlertViewExt.setMarginBottom(isOpen && focus ? 120 : 0);
            }
        });
        mAlertViewExt.addExtView(extView);
        mAlertViewExt.show();
    }

    @Override
    public void onItemClick(Object o, int position) {
        closeKeyboard();
        //判断是否是拓展窗口View，而且点击的是非取消按钮
        if (o == mAlertViewExt && position != AlertView.CANCELPOSITION) {
            if (type == 3) {
                String pwd = etName.getText().toString();
                if(TextUtils.isEmpty(pwd)||pwd.length()<0){
                    Toast toast = Toast.makeText(mContext,"",Toast.LENGTH_LONG);
                    toast.setText(getString(R.string.input_password_request));
                    toast.show();
                    return ;
                }
                showInProgress(getString(R.string.ongoing));
                JavaThreadPool.getInstance().excute(new Runnable() {
                    @Override
                    public void run() {
                        String pwd = etName.getText().toString().trim();
                        String server = dcsp.getString(
                                Constant.HTTP_DATA_SERVERS, "");
                        JSONObject object = new JSONObject();
                        JSONArray array = new JSONArray();
                        JSONObject object1 = new JSONObject();
                        object1.put("vid", commandList.get(itemPosition).getLongValue("id"));
                        array.add(object1);
                        object.put("vids", array);
                        object.put("adminpassword",pwd);
                        object.put("did",deviceInfo.getId());
                        server = server + "/jdm/s3/dln/del";
                        String temp = object.toJSONString();
                        String result = HttpRequestUtils.requestoOkHttpPost( server, object, LockUserListActivity.this);
                        // -1参数为空，0删除成功
                        if (result != null && result.equals("0")) {
                            defHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    cancelInProgress();
                                    Toast.makeText(LockUserListActivity.this, getString(R.string.device_del_success), Toast.LENGTH_SHORT).show();
                                    commandList.remove(itemPosition);
                                    commandAdapter.notifyDataSetChanged();
                                }
                            });
                        }else if(result!=null&&result.equals("-6")){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    cancelInProgress();
                                    Toast.makeText(LockUserListActivity.this, getString(R.string.pw_incrrect), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }else{
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    cancelInProgress();
                                    Toast.makeText(LockUserListActivity.this, getString(R.string.operator_error), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
            }

        }
    }

    private void closeKeyboard() {
        //关闭软键盘
        imm.hideSoftInputFromWindow(etName.getWindowToken(), 0);
        //恢复位置
        mAlertViewExt.setMarginBottom(0);
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
                viewCache.role_name = view.findViewById(R.id.role_name);
                view.setTag(viewCache);
            } else {
                viewCache = (DeviceInfoView) view.getTag();
            }

            //宏泰锁判断
            if(!(deviceInfo!=null&&deviceInfo.getMc()!=null&&deviceInfo.getMc().equals("hongtai"))){
                viewCache.role_name.setVisibility(View.GONE);
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
                    viewCache.type.setText(getString(R.string.jjsuo_user_type_0));
                } else if (command == 1) {
                    viewCache.type.setText(getString(R.string.jjsuo_user_type_1));
                } else if (command == 2) {
                    viewCache.type.setText(getString(R.string.jjsuo_user_type_2));
                }else if(command==3){
                    viewCache.type.setText(getString(R.string.jjsuo_user_type_3));
                }else if(command ==4){
                    viewCache.type.setText(getString(R.string.jjsuo_user_type_4));
                }else if(command==5){
                    viewCache.type.setText(getString(R.string.jjsuo_user_type_5));
                }else if(command==6){
                    viewCache.type.setText(getString(R.string.jjsuo_user_type_6));
                }else if(command==7){
                    viewCache.type.setText(getString(R.string.jjsuo_user_type_7));
                }else if(command==8){
                    viewCache.type.setText(getString(R.string.jjsuo_user_type_8));
                }else if(command ==9){
                    viewCache.type.setText(getString(R.string.jjsuo_user_type_9));
                }else{
                    //这行有必要，因为没有找到合适的type时，会在重用时，会显示之前的类型
                    viewCache.type.setText(getString(R.string.jjsuo_user_type_un));
                }
                String roleName = commandList.get(i).getString("roleName");
                viewCache.role_name.setText(roleName);
            } else {
                viewCache.role_name.setText(commandList.get(i).getString("deviceOperatorName"));
                viewCache.number.setText(commandList.get(i).getString("deviceCommandTime"));
                viewCache.type.setText(commandList.get(i).getString("deviceCommand"));
                viewCache.nicknName.setText(commandList.get(i).getString("deviceOperator"));
            }
            return view;
        }

        class DeviceInfoView {
            TextView number, type, nicknName,role_name;
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
            String result = HttpRequestUtils.requestoOkHttpPost(
                     server + "/jdm/s3/dln/list", object,
                    LockUserListActivity.this);
            if ("-3".equals(result)) {
                defHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(LockUserListActivity.this, getString(R.string.history_response_nodevice),
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
                DatabaseOperator.getInstance(LockUserListActivity.this).getWritableDatabase().update(
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

        public ZssMenuPopupWindow(Context context, OnClickListener itemsOnClick) {
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
            btn_deldevice.setVisibility(View.VISIBLE);
            if((deviceInfo!=null&&deviceInfo.getMc()!=null&&deviceInfo.getMc().equals("hongtai"))
                    ||Actions.VersionType.CHANNEL_JAOLH.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
                String temp = commandList.get(itemPosition).getString("roleName");
                if(temp.equals("管理员")){
                    //管理员可以删除
                    btn_deldevice.setVisibility(View.GONE);
                }
            }
            btn_setdevice.setText(context.getResources().getString(R.string.zss_item_edit));
            btn_deldevice.setText(context.getResources().getString(R.string.zss_item_del));
        }

    }
}
