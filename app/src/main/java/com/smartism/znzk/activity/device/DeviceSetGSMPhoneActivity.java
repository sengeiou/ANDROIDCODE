package com.smartism.znzk.activity.device;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.CommonWebViewActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.SecurityUtil;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.view.SwitchButton.SwitchButton;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GSM号码和服务器报警号码列表
 *
 */
public class DeviceSetGSMPhoneActivity extends ActivityParentActivity {
    private final int mHandleWhat_1 = 1,mHandleWhat_9 = 9,mHandleWhat_10 = 10,mHandleWhat_11 = 11,mHandleWhat_12 = 12;//12展示余额不足提醒
    private long telDBId;
    private DeviceInfo operationDevice;
    private List<TelInfo> telList;
    private TelAdapter mAdapter;
    private ListView telListView;
    //    private CheckSwitchButton switchButton;
    private SwitchButton switchButton, btn_sms_status;
    private RelativeLayout rl_gsm_status, rl_msg;
    private LinearLayout commTopTipNetCallLayout,commTopTipWarningLayout;
    private boolean gsmFlag;
    private boolean isInit = false;
    private int type;//0为GSM电话报警  1为服务器电话报警 -1不支持
    private TextView txt_switch, txt_gsmnumber, title, tv_notice,tv_callsms_contacts,txt_balance;
    private ImageView addTel,history;
    private LinearLayout ll_addtel;
    private boolean isAdmin;
    private Map<String,String> zhujiSetInfos = new HashMap<>();//主机设置信息
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case mHandleWhat_1: // 初始化加载完成
                    if (telList == null) {
                        telList = new ArrayList<>();
                    }else{
                        telList.clear();
                    }
                    if (msg.obj != null) {
                        telList.addAll((List<TelInfo>) msg.obj);
                    }
                    if (mAdapter == null) {
                        mAdapter = new TelAdapter();
                        telListView.setAdapter(mAdapter);
                    }else {
                        mAdapter.notifyDataSetChanged();
                    }
                    if (telList.size() > 4) {
                        addTel.setVisibility(View.GONE);
                        ll_addtel.setEnabled(false);
                    }
//                    tv_notice.setVisibility(telList.size()==0 ? View.GONE : View.VISIBLE);
                    if (!gsmFlag) {
                        JavaThreadPool.getInstance().excute(new InitGSMStatus());
                    } else {
                        cancelInProgress();
                    }
                    break;

                case mHandleWhat_9:
                    cancelInProgress();
                    Toast.makeText(DeviceSetGSMPhoneActivity.this,
                            getString(R.string.device_del_success),
                            Toast.LENGTH_LONG).show();
                    JavaThreadPool.getInstance().excute(new InitDeviceInfoThread());
                    break;
                case mHandleWhat_10: // 修改完成
                    cancelInProgress();
                    Toast.makeText(DeviceSetGSMPhoneActivity.this,
                            getString(R.string.device_set_tip_success),
                            Toast.LENGTH_LONG).show();
                    finish();
                    break;
                case mHandleWhat_11://数据库加载完成
                    //这里1是不支持
                    if (type == 0 && "1".equalsIgnoreCase(zhujiSetInfos.get(ZhujiInfo.GNSetNameMenu.supportSwitchGSM.value()))){
                        rl_gsm_status.setVisibility(View.GONE);
                    }else{
                        if (MainApplication.app.getAppGlobalConfig().isShowCallAlarm()
                                || type == 1) {
                            rl_gsm_status.setVisibility(View.VISIBLE); //GSM报警启用 和 电话报警启用是同一个布局
                        }
                    }
                    if ("1".equalsIgnoreCase(zhujiSetInfos.get(ZhujiInfo.GNSetNameMenu.supportAlarmPhone.value()))){
                        commTopTipNetCallLayout.setVisibility(View.GONE);
                    }
                    break;
                case mHandleWhat_12://显示余额提醒
                    commTopTipWarningLayout.setVisibility(View.VISIBLE);
                    txt_balance.setText(R.string.activity_device_setgsm_scall_balance);
                    commTopTipWarningLayout.setOnClickListener((v) -> {
                        String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                        server = server.replace("https://","http://");//用https国旗会不显示，后续国旗改到https之后再修改过来
                        long uid = dcsp.getLong(DataCenterSharedPreferences.Constant.LOGIN_APPID, 0);
                        String code = dcsp.getString(DataCenterSharedPreferences.Constant.LOGIN_CODE, "");
                        String n = Util.randomString(12);
                        String s = SecurityUtil.createSign("", MainApplication.app.getAppGlobalConfig().getAppid(), MainApplication.app.getAppGlobalConfig().getAppSecret(), code, n);

                        Intent intent = new Intent();
                        intent.setClass(getApplicationContext(),CommonWebViewActivity.class);
                        intent.putExtra("url",server + "/shop/store/phoneindex?masterid=" + operationDevice.getMasterId() + "&uid=" + uid + "&s=" + s + "&n=" + n + "&appid=" + MainApplication.app.getAppGlobalConfig().getAppid());
                        startActivity(intent);
                    });
                    break;
            }
            return false;
        }
    };
    private Handler defaultHandler = new WeakRefHandler(mCallback);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_set_gsm);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        telListView = (ListView) findViewById(R.id.lv_tel);
        telListView.setVisibility(View.VISIBLE);

        rl_gsm_status = (RelativeLayout) findViewById(R.id.rl_gsm_status);
        rl_msg = (RelativeLayout) findViewById(R.id.rl_msg);
        switchButton = (SwitchButton) findViewById(R.id.btn_gsm_status);
        btn_sms_status = (SwitchButton) findViewById(R.id.btn_sms_status);
        txt_switch = (TextView) findViewById(R.id.txt_switch);
        txt_gsmnumber = (TextView) findViewById(R.id.txt_gsmnumber);
        tv_notice = (TextView) findViewById(R.id.tv_notice);
        txt_balance = (TextView) findViewById(R.id.textview_auto_roll);
        title = (TextView) findViewById(R.id.title);
        ll_addtel = (LinearLayout) findViewById(R.id.ll_addtel);
        addTel = (ImageView) findViewById(R.id.addTel);
        history = (ImageView) findViewById(R.id.history);
        commTopTipNetCallLayout = (LinearLayout) findViewById(R.id.comm_top_tip_layout);
        commTopTipWarningLayout = (LinearLayout) findViewById(R.id.common_top_tip_warning);
        tv_callsms_contacts = (TextView) findViewById(R.id.txt_callsms_contacts);
        operationDevice = (DeviceInfo) getIntent().getSerializableExtra(
                "device");
        type = getIntent().getIntExtra("type", -1);
        if (type == 1) {//电话
         //   alarm_tip_tv.setVisibility(View.VISIBLE);  报警电话每隔十分钟提示一次
            txt_switch.setText(getString(R.string.activity_device_setgsm_scall));
            title.setText(getString(R.string.activity_device_setgsm_phone_tip));
            txt_gsmnumber.setText(getString(R.string.activity_device_setgsm_phonenumber));
            findViewById(R.id.txt_switch_tip).setVisibility(View.VISIBLE);

            final SpannableStringBuilder style = new SpannableStringBuilder();

            //设置文字
            style.append(getString(R.string.activity_device_setgsm_scall_number));

            //设置部分文字点击事件
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                   Util.addContactsByContract(mContext);
                }
            };
            int start = getString(R.string.activity_device_setgsm_scall_number).indexOf(getString(R.string.activity_device_setgsm_scall_click));
            int end = getString(R.string.activity_device_setgsm_scall_click).length();
            style.setSpan(clickableSpan, start, start + end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tv_callsms_contacts.setText(style);

            //设置部分文字颜色
            ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.parseColor("#0000FF"));
            style.setSpan(foregroundColorSpan, start, start + end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            //配置给TextView
            tv_callsms_contacts.setMovementMethod(LinkMovementMethod.getInstance());
            tv_callsms_contacts.setText(style);

            //查询短信电话余额
            JavaThreadPool.getInstance().excute(new LoadSmsAndCallBalanceThread());
        } else {
            rl_msg.setVisibility(View.GONE);
            commTopTipNetCallLayout.setVisibility(View.VISIBLE);
            commTopTipNetCallLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClass(getApplicationContext(),DeviceSetGSMPhoneActivity.class);
                    intent.putExtra("device", operationDevice);
                    intent.putExtra("type", 1);
                    startActivity(intent);
                    finish();
                }
            });
        }
        if (operationDevice == null) {
            Toast.makeText(DeviceSetGSMPhoneActivity.this,
                    getString(R.string.device_set_tip_nopro), Toast.LENGTH_LONG)
                    .show();

            finish();
        }
        if (type == 1) { //服务器发送短信或者电话
            history.setVisibility(View.VISIBLE);
        }
        history.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), CommonWebViewActivity.class);
                String server = mContext.getDcsp().getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                long uid = mContext.getDcsp().getLong(DataCenterSharedPreferences.Constant.LOGIN_APPID, 0);
                String code = mContext.getDcsp().getString(DataCenterSharedPreferences.Constant.LOGIN_CODE, "");

                String v1 = "";
                String n = Util.randomString(12);
                String s = SecurityUtil.createSign(v1, MainApplication.app.getAppGlobalConfig().getAppid(), MainApplication.app.getAppGlobalConfig().getAppSecret(), code, n);

                intent.putExtra("url",server + "/jdm/page/smshistory/index?v="+v1+"&uid="+uid+"&n="+n+"&s="+s+"&appid="+MainApplication.app.getAppGlobalConfig().getAppid());
                startActivity(intent);
            }
        });
        ll_addtel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeviceSetGSMPhoneActivity.this, DeviceUpdatetGSMPhoneActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("device", operationDevice);
                intent.putExtra("type", type);
                startActivity(intent);
            }
        });
        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                if (isInit) {
                    showInProgress(getString(R.string.loading), false, true);
                    JavaThreadPool.getInstance().excute(new Runnable() {
                        @Override
                        public void run() {
                            int statusCode;
                            String server = dcsp.getString(
                                    DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                            String url = server + "/jdm/s3/gsmtel/ustatus";
                            JSONObject object = new JSONObject();
                            object.put("did", operationDevice.getId());
                            if (type == 1) {
                                if (isChecked) {
                                    statusCode = 1;
                                    object.put("scall", statusCode);
                                } else {
                                    statusCode = 0;
                                    object.put("scall", statusCode);
                                }
                            } else {
                                if (isChecked) {
                                    statusCode = 1;
                                    object.put("s", statusCode);
                                } else {
                                    statusCode = 0;
                                    object.put("s", statusCode);
                                }
                            }
                            object.put("type", type);
                            String result = HttpRequestUtils.requestoOkHttpPost(url, object, DeviceSetGSMPhoneActivity.this);
                            if ("0".equals(result)) {
                                //电话打开时，短信也要打开
                                if (type == 1 && isChecked){
                                    runOnUiThread(() ->{
                                        btn_sms_status.setChecked(true);
                                    });
                                }
//                                SyncMessageContainer.getInstance().produceSendMessage(new SyncMessage(SyncMessage.CommandMenu.rq_refresh));//完成修改，发送服务器刷新指令
                                final int finalStatusCode = statusCode;
                                defaultHandler.post(new Runnable() {

                                    @Override
                                    public void run() {
                                        cancelInProgress();
                                        if (finalStatusCode == 1) {
                                            switchButton.setChecked(true);
                                            if (type == 0) {
                                                Toast.makeText(DeviceSetGSMPhoneActivity.this,
                                                        getString(R.string.success),
                                                        Toast.LENGTH_LONG).show();

                                            } else {
                                                Toast.makeText(DeviceSetGSMPhoneActivity.this, getString(R.string.success), Toast.LENGTH_LONG).show();
                                                if (telList == null || telList.size() == 0) {
                                                    Intent intent = new Intent(DeviceSetGSMPhoneActivity.this, DeviceUpdatetGSMPhoneActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                                    intent.putExtra("device", operationDevice);
                                                    intent.putExtra("type", type);
                                                    startActivity(intent);
                                                }
                                            }
                                        } else {
                                            switchButton.setChecked(false);
                                            Toast.makeText(DeviceSetGSMPhoneActivity.this,
                                                    getString(R.string.success),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });

                            } else if ("-3".equals(result)) {
                                defaultHandler.post(new Runnable() {

                                    @Override
                                    public void run() {
                                        cancelInProgress();
                                        Toast.makeText(DeviceSetGSMPhoneActivity.this,
                                                getString(R.string.net_error_nopermission),
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                            } else if ("-4".equals(result)) {
                                defaultHandler.post(new Runnable() {

                                    @Override
                                    public void run() {
                                        cancelInProgress();
                                        Toast.makeText(DeviceSetGSMPhoneActivity.this,
                                                getString(R.string.net_error_failed),
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                            } else {
                                defaultHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(DeviceSetGSMPhoneActivity.this,
                                                getString(R.string.initfailed),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        }
                    });

                } else {
                    isInit = true;
                }
            }
        });


        btn_sms_status.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {

                if (isInit) {
                    showInProgress(getString(R.string.loading), false, true);
                    JavaThreadPool.getInstance().excute(new Runnable() {
                        @Override
                        public void run() {
                            int statusCode = 0;
                            String server = dcsp.getString(
                                    DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                            String url = server + "/jdm/s3/gsmtel/ustatus";
                            JSONObject object = new JSONObject();
                            object.put("did", operationDevice.getId());
                            if (type == 1) {
                                if (isChecked) {
                                    statusCode = 1;
                                    object.put("ssms", statusCode);
                                } else {
                                    statusCode = 0;
                                    object.put("ssms", statusCode);
                                }
                            }
                            object.put("type", type);
                            String result = HttpRequestUtils.requestoOkHttpPost(url, object, DeviceSetGSMPhoneActivity.this);
                            if ("0".equals(result)) {
                                //短信关闭时，电话也要关闭
                                if (type == 1 && !isChecked){
                                    runOnUiThread(() ->{
                                        switchButton.setChecked(false);
                                    });
                                }
//                                SyncMessageContainer.getInstance().produceSendMessage(new SyncMessage(SyncMessage.CommandMenu.rq_refresh));//完成修改，发送服务器刷新指令
                                final int finalStatusCode = statusCode;
                                defaultHandler.post(new Runnable() {

                                    @Override
                                    public void run() {
                                        cancelInProgress();
                                        if (finalStatusCode == 1) {
                                            btn_sms_status.setChecked(true);
                                            if (type == 0) {
                                                Toast.makeText(DeviceSetGSMPhoneActivity.this,
                                                        getString(R.string.success),
                                                        Toast.LENGTH_LONG).show();

                                            } else {
                                                Toast.makeText(DeviceSetGSMPhoneActivity.this, getString(R.string.success), Toast.LENGTH_LONG).show();
                                                if (telList == null || telList.size() == 0) {
                                                    Intent intent = new Intent(DeviceSetGSMPhoneActivity.this, DeviceUpdatetGSMPhoneActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                                    intent.putExtra("device", operationDevice);
                                                    intent.putExtra("type", type);
                                                    startActivity(intent);
                                                }
                                            }

                                        } else {
                                            btn_sms_status.setChecked(false);

                                            Toast.makeText(DeviceSetGSMPhoneActivity.this,
                                                    getString(R.string.success),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });

                            } else if ("-3".equals(result)) {
                                defaultHandler.post(new Runnable() {

                                    @Override
                                    public void run() {
                                        cancelInProgress();
                                        Toast.makeText(DeviceSetGSMPhoneActivity.this,
                                                getString(R.string.net_error_nopermission),
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                            } else if ("-4".equals(result)) {
                                defaultHandler.post(new Runnable() {

                                    @Override
                                    public void run() {
                                        cancelInProgress();
                                        Toast.makeText(DeviceSetGSMPhoneActivity.this,
                                                getString(R.string.net_error_failed),
                                                Toast.LENGTH_LONG).show();
                                    }
                                });
                            } else {
                                defaultHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(DeviceSetGSMPhoneActivity.this,
                                                getString(R.string.initfailed),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    });

                } else {
                    showInProgress(getString(R.string.net_error_exception), false, true);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        showInProgress(getString(R.string.loading), false, true);
        JavaThreadPool.getInstance().excute(new InitDeviceInfoThread());
        JavaThreadPool.getInstance().excute(new InitDeviceSetThread());
    }

    class TelAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return telList.size();
        }

        @Override
        public Object getItem(int arg0) {
            return telList.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public View getView(final int arg0, View view, ViewGroup parent) {
            MyHolder myHolder = null;
            if (view == null) {
                myHolder = new MyHolder();
                view = LayoutInflater.from(DeviceSetGSMPhoneActivity.this).inflate(R.layout.activity_gsm_tel_item, null);
                myHolder.tv = (TextView) view.findViewById(R.id.tv_tel);
                myHolder.editBtn = (Button) view.findViewById(R.id.btn_update);
                myHolder.deleteBtn = (Button) view
                        .findViewById(R.id.btn_delete);
                view.setTag(myHolder);
            } else {
                myHolder = (MyHolder) view.getTag();
            }

            myHolder.tv.setText(getResources().getString(R.string.gsm_phonenumber) + (arg0 + 1) + ": " + telList.get(arg0).getTel());

            myHolder.editBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View view) {
                    updateTel(arg0);
                }
            });
            myHolder.deleteBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View view) {
                    telDBId = telList.get(arg0).getId();
                    showInProgress(getString(R.string.loading), false, true);
                    JavaThreadPool.getInstance().excute(new DeleteGSMTelThread());
                }
            });

            return view;
        }

        class MyHolder {
            public TextView tv;
            public Button editBtn, deleteBtn;
        }

    }

    private void updateTel(int arg0) {
        Intent intent = new Intent(this, DeviceUpdatetGSMPhoneActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("device", operationDevice);
        intent.putExtra("id", telList.get(arg0).getId());
        intent.putExtra("tel", telList.get(arg0).getTel());
        intent.putExtra("type", type);
        intent.putExtra("country", telList.get(arg0).getCountry());
        intent.putExtra("typeWay", telList.get(arg0).getTypeWay());
        startActivity(intent);
    }

//    public void addTel(View v) {
//        Intent intent = new Intent(this, DeviceUpdatetGSMPhoneActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        intent.putExtra("device", operationDevice);
//        intent.putExtra("type", type);
//        startActivity(intent);
//    }

    public void back(View v) {
        finish();
    }

    protected void onDestroy() {
        defaultHandler.removeCallbacksAndMessages(null);
        defaultHandler = null;
        super.onDestroy();
    }

    class InitDeviceSetThread implements Runnable {
        @Override
        public void run() {
            zhujiSetInfos = DatabaseOperator.getInstance().queryZhujiSets(operationDevice.getId());
            defaultHandler.sendEmptyMessage(mHandleWhat_11);
        }
    }
    class InitDeviceInfoThread implements Runnable {
        @Override
        public void run() {
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("did", operationDevice.getId());
            object.put("type", type);
            String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/gsmtel/list", object, DeviceSetGSMPhoneActivity.this);
            if ("0".equals(result)) { // 无数据
                defaultHandler.sendEmptyMessage(mHandleWhat_1);
            } else if (!StringUtils.isEmpty(result) && result.length() >= 2) {//无数据时会返回[] ？
                JSONArray resultBack = null;
                try {
                    resultBack = JSON.parseArray(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (resultBack == null) {
                    defaultHandler.sendEmptyMessage(mHandleWhat_1);
                    return;
                }
                List<TelInfo> telInfoList = new ArrayList<>();
                for (int i = 0; i < resultBack.size(); i++) {
                    JSONObject obj = resultBack.getJSONObject(i);
                    TelInfo telInfo = new TelInfo();
                    telInfo.setId(obj.getLongValue("id"));
                    telInfo.setTel(obj.getString("tel"));
                    telInfo.setCountry(obj.getString("contry"));
                    telInfo.setTypeWay(obj.getIntValue("typeWay"));
                    telInfoList.add(telInfo);
                }
                Message m = defaultHandler.obtainMessage(1);
                m.obj = telInfoList;
                defaultHandler.sendMessage(m);
            }
        }
    }

    class DeleteGSMTelThread implements Runnable {

        @Override
        public void run() {
            String server = dcsp.getString(
                    DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("did", operationDevice.getId());
            object.put("uid", dcsp.getLong(Constant.LOGIN_APPID, 0));
            object.put("code", dcsp.getString(Constant.LOGIN_CODE, ""));
            object.put("id", telDBId);
            String result = null;
            result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/gsmtel/del", object, DeviceSetGSMPhoneActivity.this);
            if ("0".equals(result)) {
                defaultHandler.sendEmptyMessage(mHandleWhat_9);
            } else if ("-3".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(DeviceSetGSMPhoneActivity.this,
                                getString(R.string.net_error_nopermission),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else if ("-4".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(DeviceSetGSMPhoneActivity.this,
                                getString(R.string.net_error_failed),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }
    class LoadSmsAndCallBalanceThread implements Runnable {

        @Override
        public void run() {
            String server = dcsp.getString(
                    DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("id", operationDevice.getId());
            String result = null;
            result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/smsorder/balance", object, DeviceSetGSMPhoneActivity.this);
            if (result.length() > 5) {
                JSONObject jsResult = JSONObject.parseObject(result);
                if (jsResult.getIntValue("smsTotal") < 5 || jsResult.getIntValue("callTotal") < 5){
                    defaultHandler.sendEmptyMessage(mHandleWhat_12);
                }
            }
        }
    }

    public class InitGSMStatus implements Runnable {
        @Override
        public void run() {
            String server = dcsp.getString(
                    DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            String url = server + "/jdm/s3/gsmtel/status";
            JSONObject object = new JSONObject();
            object.put("did", operationDevice.getId());
            object.put("type", type);
            String result = HttpRequestUtils.requestoOkHttpPost(url, object, DeviceSetGSMPhoneActivity.this);
            if (!StringUtils.isEmpty(result) && result.length() > 5 && type == 1) {
                try {
                    final JSONObject back = JSONObject.parseObject(result);
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            if ("1".equals(back.getString("scall"))) {
                                switchButton.setCheckedImmediatelyNoEvent(true);
                            } else {
                                switchButton.setCheckedImmediatelyNoEvent(false);
                            }
                            if ("1".equals(back.getString("ssms"))) {
                                btn_sms_status.setCheckedImmediatelyNoEvent(true);
                            } else {
                                btn_sms_status.setCheckedImmediatelyNoEvent(false);
                            }
                            isInit = true;
                            gsmFlag = true;
                        }
                    });
                } catch (Exception ex) {
                }
            } else if ("0".equals(result) && type == 0) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        switchButton.setCheckedImmediatelyNoEvent(false);
                        isInit = true;
                        gsmFlag = true;
                    }
                });

            } else if ("1".equals(result) && type == 0) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        switchButton.setCheckedImmediatelyNoEvent(true);
                        isInit = true;
                        gsmFlag = true;
                    }
                });
            } else if ("-3".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(DeviceSetGSMPhoneActivity.this,
                                getString(R.string.activity_device_setgsm_notgsm),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(DeviceSetGSMPhoneActivity.this,
                                getString(R.string.net_error_weizhi),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }
    }

    public static class TelInfo {
        private long id;
        private String country;
        private String tel="";
        private int telId ;
        private int typeWay;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getTel() {
            return tel;
        }

        public int getTelId() {
            return telId;
        }

        public void setTelId(int telId) {
            this.telId = telId;
        }

        public void setTel(String tel) {
            this.tel = tel;
        }

        public int getTypeWay() {
            return typeWay;
        }

        public void setTypeWay(int typeWay) {
            this.typeWay = typeWay;
        }

        @Override
        public String toString() {
            return "telId:"+telId+"-tel:"+tel;
        }
    }
}
