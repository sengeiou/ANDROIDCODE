package com.smartism.znzk.activity.device.add;

import android.annotation.TargetApi;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.just.agentweb.AgentWeb;
import com.just.agentweb.WebViewClient;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.DeviceMainFragment;
import com.smartism.znzk.activity.device.DeviceMainActivity;
import com.smartism.znzk.activity.device.ZhujiListFragment;
import com.smartism.znzk.activity.user.factory.FactoryAddDevicesActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessage.CodeMenu;
import com.smartism.znzk.communication.protocol.SyncMessage.CommandMenu;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.domain.CategoryInfo;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.view.alertview.AlertView;
import com.smartism.znzk.view.alertview.AlertView.Style;
import com.smartism.znzk.view.alertview.OnDismissListener;
import com.smartism.znzk.view.alertview.OnItemClickListener;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddDeviceActivity extends ActivityParentActivity implements OnClickListener {
    private AlertView autoPeiTips;
    //    private Button peidui;
//    private AutoCompleteTextView type;
    private List<String> typeList;
    private List<String> typeListTemp;
    private ArrayAdapter<String> typeAdapter;
    private LinearLayout typeLayout;
    // 仅下行添加按钮
    private JSONArray keys;
    private GridView keysGridView;
    private KeyItemAdapter keyItemAdapter;
    private ImageView ingIco;
    private TextView ingText,tv_buy;

    private Button match_btn;
    // 定义结束
    private JSONObject properties = new JSONObject();

    //    private Map<String, Map<String, Object>> typs = new HashMap<String, Map<String, Object>>();
    private boolean isOnlyControl = false;
    // 播放声音
    private SoundPool soundPool = null;
    // 声音的资源id
    private int sourceid;
    //    private ListView lv;
//    private EditText et_search;

    private HashMap<String, Object> map;

    private DeviceInfo deviceInfo;
    private CategoryInfo categoryInfo ;

    private AlertView alertView;


    private BroadcastReceiver defaultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (Actions.PEIDUI_ACTIONS.equals(intent.getAction())) { // 配对返回
                if (CodeMenu.zero.value() == intent.getIntExtra("code", -10000)) {
                    // 进入配对 修改按钮的显示字样
                    cancelInProgress();
                    defaultHandler.removeMessages(1);
                    if (isOnlyControl) { // 下行设备配对
                        autoPeiTips = new AlertView(getString(R.string.add_shoudong_only_tips_title),
                                getString(R.string.add_shoudong_only_tips)
                                        + (map != null
                                        ? map.get("pdMessage") : ""),
                                getString(R.string.add_shoudong_only_failed),
                                new String[]{getString(R.string.add_shoudong_only_success)}, null,
                                AddDeviceActivity.this, Style.Alert,
                                new com.smartism.znzk.view.alertview.OnItemClickListener() {

                                    @Override
                                    public void onItemClick(Object o, int position) {
                                        if (position != -1) {
                                            // 确认配对成功
                                            showInProgress(getString(R.string.add_submit_finish), false, true);
                                            SyncMessage message = new SyncMessage(CommandMenu.rq_pdByHand_onlyControl);
                                            try {
                                                message.setSyncBytes(properties.toJSONString().getBytes("UTF-8"));
                                            } catch (UnsupportedEncodingException e) {
                                                e.printStackTrace();
                                            }
                                            SyncMessageContainer.getInstance().produceSendMessage(message);
                                            defaultHandler.sendEmptyMessageDelayed(4,
                                                    5 * 1000);//5秒超时
                                        }
                                    }
                                });
                        ViewGroup extView = (ViewGroup) LayoutInflater.from(AddDeviceActivity.this)
                                .inflate(R.layout.activity_add_device_keys, null);
                        keysGridView = (GridView) extView.findViewById(R.id.keys);
//                        keysGridView.setVisibility(View.GONE); //不显示手动指令
                        ingIco = (ImageView) extView.findViewById(R.id.sending_icon);
                        Animation imgloading_animation = AnimationUtils.loadAnimation(
                                AddDeviceActivity.this, R.anim.loading_revolve);
                        imgloading_animation.setInterpolator(new LinearInterpolator());
                        ingIco.startAnimation(imgloading_animation);
                        ingText = (TextView) extView.findViewById(R.id.sending_tips);
                        if (map.get("keys") instanceof ArrayList) {
                            keys = (JSONArray) JSONArray.toJSON(map.get("keys"));
                        } else {
                            keys = (JSONArray) map.get("keys");//通过intent序列化传会将jsonarray变为arraylist
                        }
                        //声光报警器配对按键布局更改
                        if(categoryInfo.getCkey().equals("lb")){
                            for(int i=0;i<keys.size();i++){
                                Map<String,Object> subKey = (Map<String, Object>) keys.get(i);
                                Object value = subKey.get("n");
                               /* if(value instanceof String && !value.equals("布防")){
                                    keys.remove(i);
                                    i--;
                                }*/
                               int temp = (int) subKey.get("i");
                               if(temp!=1){
                                   keys.remove(i);
                                   i-- ;
                               }
                            }

                            //需要进行布局处理来显示光报警器配对
                            ViewGroup ailien_parent = extView.findViewById(R.id.ailien_key_parent);
                            View view = getLayoutInflater().inflate(R.layout.activity_history_key_item,extView,false);
                            TextView keyTv= view.findViewById(R.id.dinfo_keyname);
                            keyTv.setBackgroundResource(R.drawable.device_item_one_button_bg);
                            keyTv.setText(getString(R.string.ailien_add_send_instruction));
                            keyTv.setGravity(Gravity.CENTER);
                            ViewGroup.LayoutParams lp = keyTv. getLayoutParams();
                            lp.width= (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,90,getResources().getDisplayMetrics());
                            lp.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,90,getResources().getDisplayMetrics());
                            keyTv.setLayoutParams(lp);
                            keyTv.setOnClickListener(new OnClickListener(){
                                @Override
                                public void onClick(View v) {
                                    sendControlCommand(0);
                                    removeAutoSendControlCommand();
                                }
                            });
                            ailien_parent.addView(view);
                            ailien_parent.setVisibility(View.VISIBLE);
                            keysGridView.setVisibility(View.GONE);
                        }else{
                            keyItemAdapter = new KeyItemAdapter(AddDeviceActivity.this);
                            keysGridView.setAdapter(keyItemAdapter);
                        }
                        autoPeiTips.addExtView(extView);
                        autoPeiTips.show();

                        //设置按钮的点击事件，发送指令
                        keysGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                sendControlCommand(position);
                                removeAutoSendControlCommand();
                            }
                        });
                        defaultHandler.sendEmptyMessageDelayed(2, 15 * 1000);//15秒超时
                        if(Actions.VersionType.CHANNEL_SZJIAJIAAN.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                                    &&categoryInfo.getCkey().equals("cz")){
                                TextView sending_tips = extView.findViewById(R.id.sending_tips);
                                ImageView sending_icon = extView.findViewById(R.id.sending_icon);
                                sending_icon.setVisibility(View.GONE);
                                sending_tips.setVisibility(View.GONE);
                        }else{
                            sendControlCommand(0);
                            defaultHandler.sendEmptyMessageDelayed(5, 2 * 1000);//2秒后重发
                            autoPeiTips.setOnDismissListener(new OnDismissListener() {
                                @Override
                                public void onDismiss(Object o) {
                                    removeAutoSendControlCommand();
                                }
                            });
                        }
                    } else {
                        // 分三种配对方式，1、手动配对，2、内嵌app配对，3、自动配对
                        if (map != null
                                && "neiqian".equals(map.get("controlType"))) {
                            // 内嵌设备的配对 好像不需要做什么操作
                        } else if (map != null
                                && (Boolean) map.get("autoPeidui")
                                && !"neiqian".equals(map.get("controlType"))) {
                            // 自动配对
                            autoPeiTips = new AlertView(getString(R.string.add_auto_tips_title),
                                    getString(R.string.add_auto_tips_msg), null,
                                    new String[]{getString(R.string.add_auto_tips_over)}, null,
                                    AddDeviceActivity.this, Style.Alert,
                                    new com.smartism.znzk.view.alertview.OnItemClickListener() {

                                        @Override
                                        public void onItemClick(Object o, int position) {
                                            defaultHandler.removeMessages(3); // 关掉自动结束配对定时handler
                                            // 结束配对
                                            SyncMessage message = new SyncMessage(CommandMenu.rq_pdByAuto);
                                            try {
                                                message.setSyncBytes(properties.toJSONString().getBytes("UTF-8"));
                                            } catch (UnsupportedEncodingException e) {
                                                e.printStackTrace();
                                            }
                                            SyncMessageContainer.getInstance().produceSendMessage(message);
                                            Intent in = new Intent();
                                            in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                            in.setClass(AddDeviceActivity.this, DeviceMainActivity.class);
                                            startActivity(in);
                                            Toast.makeText(getApplicationContext(), getString(R.string.add_auto_tips_over), Toast.LENGTH_LONG)
                                                    .show();
                                        }
                                    });
                            autoPeiTips.show();
                            defaultHandler.sendEmptyMessageDelayed(3, 3 * 60000);// 自动配对3分钟退出
                        } else if (map != null) {
                            // 手动配对
                            if (mProgressDialog == null) {
                                mProgressDialog = new ProgressDialog(AddDeviceActivity.this);
                                mProgressDialog.setOnCancelListener(new OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        SyncMessage message = new SyncMessage();
                                        message.setCommand(CommandMenu.rq_pdByHandE.value());
                                        message.setCode(CodeMenu.zero.value());
                                        try {
                                            message.setSyncBytes(properties.toJSONString().getBytes("UTF-8"));
                                        } catch (UnsupportedEncodingException e) {
                                            e.printStackTrace();
                                        }
                                        SyncMessageContainer.getInstance().produceSendMessage(message);
                                        defaultHandler.removeMessages(2);
                                    }
                                });
                                mProgressDialog.setMessage(getString(R.string.add_shoudong_tips)
                                        + map.get("pdMessage"));
                                mProgressDialog.setIndeterminate(false);
                                mProgressDialog.setCancelable(true);
                            }
                            mProgressDialog.show();
                            defaultHandler.sendEmptyMessageDelayed(2, 15000);
                        }
                    }
                } else if (CodeMenu.rp_pdByHand_success.value() == intent.getIntExtra("code", -10000)) {
                    soundPool.play(sourceid, 1, 1, 0, 0, 1);
                    // 配对成功啦
                    cancelInProgress();
                    SyncMessageContainer.getInstance()
                            .produceSendMessage(new SyncMessage(SyncMessage.CommandMenu.rq_refresh));
                    if (map != null
                            && "neiqian".equals(map.get("controlType"))) {
                        // 内嵌设备的配对 检测是否安装，已经安装则打开，未安装则安装并提示
                        String cpackage = String.valueOf(map.get("apkPackage"));
                        if (Util.appIsInstalled(AddDeviceActivity.this, cpackage)) {
                            // 已经安装，打开
                            intent = new Intent(Intent.ACTION_VIEW);
                            intent.setComponent(new ComponentName(cpackage.substring(0, cpackage.lastIndexOf("/")),
                                    cpackage.replace("/", ".")));
                            startActivity(intent);
                        } else {
                            // 提示安装
                            Toast.makeText(AddDeviceActivity.this, getString(R.string.add_shoudong_neiqian_tips),
                                    Toast.LENGTH_LONG).show();
                            String downloadString = String
                                    .valueOf(map.get("apkDownload"));
                            if (downloadString.startsWith("jdmapk://")) {
                                // 内嵌apk
                                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                                        + "/jdm_app_tmp.apk");
                                int resourct = 0;
                                if (downloadString.substring(9).equals("p2pipcam_hvcipc_6_5")) {
//									resourct = R.raw.p2pipcam_hvcipc_6_5;
                                }
                                try {
                                    FileUtils.copyInputStreamToFile(getResources().openRawResource(resourct), file);
                                } catch (NotFoundException e) {
                                } catch (IOException e) {
                                }
                                Util.install(AddDeviceActivity.this, Uri.fromFile(file));
                            } else if (downloadString.startsWith("http://")) {
                                // http连接，需要下载。
                            }
                        }
                    } else {
                        defaultHandler.removeMessages(2);// 配对成功了则移除配对失败提示
                        Toast.makeText(AddDeviceActivity.this, getString(R.string.add_shoudong_success),
                                Toast.LENGTH_LONG).show();
                    }
                    Intent in = new Intent();
                    in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    in.setClass(AddDeviceActivity.this, DeviceMainActivity.class);
                    startActivity(in);
                    if(Actions.VersionType.CHANNEL_HCTZ.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
                        DeviceMainFragment.hongcai_tiaozhuan = true ;
                    }

                } else if (CodeMenu.rp_pdByHand_zhujioffline.value() == intent.getIntExtra("code", -10000)) { // 主机不在线
                    cancelInProgress();
                    Toast.makeText(AddDeviceActivity.this, getString(R.string.add_shoudong_failed_nohost),
                            Toast.LENGTH_LONG).show();
                } else if (CodeMenu.rp_pdByHand_nozhuji.value() == intent.getIntExtra("code", -10000)) { // 主机不存在
                    cancelInProgress();
                    Toast.makeText(AddDeviceActivity.this, getString(R.string.tips_1), Toast.LENGTH_LONG).show();
                } else if (CodeMenu.rp_pdByHand_notype.value() == intent.getIntExtra("code", -10000)) { // 无此设备类型
                    cancelInProgress();
                    Toast.makeText(AddDeviceActivity.this, getString(R.string.tips_2), Toast.LENGTH_LONG).show();
                } else if (CodeMenu.rp_pdByHand_needjihuo.value() == intent.getIntExtra("code", -10000)) { // 需要激活，自动配对的配对成功
                    if (map != null
                            && (Boolean) map.get("autoPeidui")) {
                        try {
                            JSONObject object = JSON.parseObject(intent.getStringExtra("msg"));
                            Toast.makeText(AddDeviceActivity.this, object.getString("n") + " " + getString(R.string.add_success),
                                    Toast.LENGTH_LONG).show();
                        } catch (Exception ex) {
                            Log.e(TAG, "onReceive: 自动配对数据上传错误，自动配对数据显示失败", ex);
                        }
                    } else {
                        cancelInProgress();
                        defaultHandler.removeMessages(1);
                        new Builder(AddDeviceActivity.this).setCancelable(true)
                                .setMessage(getString(R.string.add_submit_needjihuo_message))// 设置对话框内容
                                .setTitle(getString(R.string.add_submit_needjihuo_title))// 设置对话框标题
                                .setNegativeButton(getString(R.string.cancel), null)
                                .setPositiveButton(getString(R.string.add_submit_button_jihuo),
                                        new DialogInterface.OnClickListener() {// 设置对话框[肯定]按钮
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();// 关闭对话框
                                                Intent intent = new Intent();
                                                intent.setClass(getApplicationContext(), FactoryAddDevicesActivity.class);
                                                intent.putExtra(FactoryAddDevicesActivity.MODEL_FACTORY_TYPE,
                                                        FactoryAddDevicesActivity.MODEL_FACTORY_TYPE_ACTIVATION);
                                                intent.putExtra("type", properties.getString("t")); // 取到当前点击的type
                                                startActivity(intent);
                                                finish();
                                            }
                                        }).create().show();
                    }
                } else if (CodeMenu.rp_pdByHand_servererror.value() == intent.getIntExtra("code", -10000)) { // 服务器错误
                    cancelInProgress();
                    Toast.makeText(AddDeviceActivity.this, getString(R.string.net_error_servererror), Toast.LENGTH_LONG)
                            .show();
                } else if (CodeMenu.rp_pdByHand_drepeat.value() == intent.getIntExtra("code", -10000)) { // 设备以及存在
                    cancelInProgress();
                    new AlertView(getString(R.string.tips), getString(R.string.activity_add_device_drepeat), null,
                            new String[]{getString(R.string.sure)}, null, AddDeviceActivity.this, Style.Alert, null)
                            .show();
                } else if (CodeMenu.rp_pdByHand_dinotherhub.value() == intent.getIntExtra("code", -10000)) { // 设备已经添加到其它主机下
                    cancelInProgress();
                    String dataInfo = intent.getStringExtra("data_info");
                    if (!StringUtils.isEmpty(dataInfo)) {
                        try {
                            JSONObject object = JSON.parseObject(dataInfo);
                            if (alertView == null) {
                                alertView = new AlertView(getString(R.string.tips), String.format(getString(R.string.activity_add_device_dinotherhub), object.getString("m")), null,
                                        new String[]{getString(R.string.sure)}, null, AddDeviceActivity.this, Style.Alert, null);
                                if (!alertView.isShowing())
                                    alertView.show();
                            }
//                            new AlertView(getString(R.string.tips), String.format(getString(R.string.activity_add_device_dinotherhub), object.getString("m")), null,
//                                    new String[]{getString(R.string.sure)}, null, AddDeviceActivity.this, Style.Alert, null)
//                                    .show();
                        } catch (Exception ex) {
                            Log.e(TAG, "json转换出错", ex);
                        }
                    } else {
                        if (alertView == null) {
                            alertView = new AlertView(getString(R.string.tips), String.format(getString(R.string.activity_add_device_dinotherhub), "unknown"), null,
                                    new String[]{getString(R.string.sure)}, null, AddDeviceActivity.this, Style.Alert, null);
                            if (!alertView.isShowing())
                                alertView.show();
                        }
                    }
                }
            }
            if (Actions.PEIDUI_FAILED.equals(intent.getAction())) { // 配对失败
                cancelInProgress();
                Toast.makeText(AddDeviceActivity.this, getString(R.string.add_shoudong_failed), Toast.LENGTH_LONG)
                        .show();
            } else if (Actions.PEIDUI_FAILED_TIMEOUT.equals(intent.getAction())) { // 仅控制类型配对超时
                cancelInProgress();
                Toast.makeText(AddDeviceActivity.this, getString(R.string.tips_3), Toast.LENGTH_LONG).show();
            } else if (Actions.PEIDUI_MODEN_EXIT.equals(intent.getAction())) { // 退出配对模式失败
                cancelInProgress();
                Toast.makeText(AddDeviceActivity.this, getString(R.string.tips_4), Toast.LENGTH_LONG).show();
            }
        }
    };

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            cancelInProgress();
            switch (msg.what) {
                case 10: // 初始化加载数据完成
                    typeList.clear();
                    typeList.addAll((Collection<? extends String>) msg.obj);
                    typeListTemp.clear();
                    typeListTemp.addAll((Collection<? extends String>) msg.obj);
                    typeAdapter.notifyDataSetChanged();
//                    int t = getIntent().getIntExtra("type", 0);
//                    if (t == 2) { // 条形码
//                        String typeInto = getIntent().getStringExtra("value");
//                        boolean ishave = false;
//                        if (typeInto != null && !"".equals(typeInto)) {
//                            if (typs != null) {
//                                for (String key : typs.keySet()) {
//                                    if (typeInto.equals(String.valueOf(typs.get(key).get("code")))) {
//                                        et_search.setText(key);
//                                        et_search.setEnabled(false);
////                                        peidui.performClick();
//                                        ishave = true;
//                                        break;
//                                    }
//                                }
//                            }
//                        }
//                        if (!ishave) {
//                            Toast.makeText(getApplicationContext(), getString(R.string.activity_add_device_notidentify),
//                                    Toast.LENGTH_LONG).show();
//                        }
//                    } else if (t == 1) { // 二维码 传入的是类型
//                        String typeInto = getIntent().getStringExtra("value");
//                        if (typeInto != null && !"".equals(typeInto)) {
//                            et_search.setText(typeInto);
//                            et_search.setEnabled(false);
////                            peidui.performClick();
//                        }
//                    }
                    break;
                case 1:
                    Toast.makeText(getApplicationContext(), getString(R.string.add_shoudong_failed_again),
                            Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    defaultHandler.removeMessages(5); //移除重发
                    if (autoPeiTips != null && autoPeiTips.isShowing()) {
                        autoPeiTips.dismiss();
                    }
                    Toast.makeText(getApplicationContext(), getString(R.string.add_shoudong_failed_chaoshi),
                            Toast.LENGTH_LONG).show();
                    break;
                case 3:
                    autoPeiTips.dismiss();
                    Intent in = new Intent();
                    in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    in.setClass(AddDeviceActivity.this, DeviceMainActivity.class);
                    startActivity(in);
                    Toast.makeText(getApplicationContext(), getString(R.string.add_auto_tips_auto_over), Toast.LENGTH_LONG)
                            .show();
                    break;
                case 4:
                    Toast.makeText(getApplicationContext(), getString(R.string.timeout),
                            Toast.LENGTH_SHORT).show();
                    break;
                case 5: //自动发送第一个指令
                    sendControlCommand(0);
                    break;
            }
            return false;
        }
    };
    private Handler defaultHandler = new WeakRefHandler(mCallback);

    private boolean isRegFilter = false;
    private long id = -1;
    private List<DeviceInfo> dInfos;
    private RelativeLayout rl_error;
    private View mErrorView;
    private AgentWeb mAgentWeb;
    private LinearLayout mAgentWebLayout;
    private ZhujiInfo zhuji ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        deviceInfo = (DeviceInfo) getIntent().getSerializableExtra("device");
        zhuji = (ZhujiInfo) getIntent().getSerializableExtra("zhuji");
        categoryInfo = (CategoryInfo) getIntent().getSerializableExtra("category");
        initView();
        initData();
    }

    private void initView() {
        tv_buy = (TextView) findViewById(R.id.tv_buy);
//        if (Actions.VersionType.CHANNEL_ZHILIDE.equals(MainApplication.app.getAppGlobalConfig().getVersion()))
//            tv_buy.setVisibility(View.INVISIBLE);
//        if(Actions.VersionType.CHANNEL_UHOME.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
//            //uhome
//            tv_buy.setVisibility(View.GONE);
//        }
        rl_error = (RelativeLayout) findViewById(R.id.rl_error);
        mAgentWebLayout = (LinearLayout) findViewById(R.id.web_view_layout);
        match_btn = (Button) findViewById(R.id.match_btn);
        match_btn.setOnClickListener(this);
        dInfos = new ArrayList<>();
        // 注册广播
        IntentFilter receiverFilter = new IntentFilter();
        receiverFilter.addAction(Actions.PEIDUI_ACTIONS);
        receiverFilter.addAction(Actions.PEIDUI_FAILED);
        receiverFilter.addAction(Actions.PEIDUI_FAILED_TIMEOUT);
        receiverFilter.addAction(Actions.PEIDUI_MODEN_EXIT);
        receiverFilter.addAction(Actions.CONNECTION_FAILED);
        receiverFilter.addAction(Actions.SEND_SEARCHZHUJICOMMAND);
        receiverFilter.addAction(Actions.CONNECTION_SUCCESS);
        registerReceiver(defaultReceiver, receiverFilter);
        isRegFilter = true;

        if(Actions.VersionType.CHANNEL_ZHILIDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            tv_buy.setVisibility(View.GONE);
        }else if(Actions.VersionType.CHANNEL_UHOME.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            tv_buy.setVisibility(View.GONE);
        }else if(Actions.VersionType.CHANNEL_HCTZ.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            tv_buy.setVisibility(View.GONE);
        }
    }


    private void initData() {

        id = getIntent().getLongExtra("id", -1);

        Bundle bundle = getIntent().getExtras();
        deviceInfo = (DeviceInfo) bundle.getSerializable("device");
        map = (HashMap<String, Object>) bundle.getSerializable("map");

        String result = (String) map.get("details_url");
        if (map != null && !TextUtils.isEmpty(result)){
            mAgentWeb = AgentWeb.with(this)
                    .setAgentWebParent(mAgentWebLayout, new LinearLayout.LayoutParams(-1, -1))
                    .useDefaultIndicator()
                    .interceptUnkownUrl()
                    .setWebViewClient(mWebViewClient)
                    .createAgentWeb()
                    .ready()
                    .go(result);
        }
//        web_view.setWebChromeClient(new WebChromeClient() {
//            @Override
//            public void onProgressChanged(WebView view, int newProgress) {
//
//                if (newProgress >= 80) {
//                    // 网页加载完成
//                    cancelInProgress();
//                    if (isNetworkConnected(mContext)) {
//                        web_view.setVisibility(View.VISIBLE);
//                        rl_error.setVisibility(View.GONE);
//                    }
//                } else {
//                    // 加载中
//                    showInProgress(getString(R.string.loading), true, true);
//                }
//
//
//            }
//        });


//        if ("".equals(dcsp.getString(Constant.APP_MASTERID, ""))) {
//            Toast.makeText(getApplicationContext(), getString(R.string.add_shoudong_failed_nohost2), Toast.LENGTH_LONG)
//                    .show();
//            finish();
//        }

//        properties.put("m", dcsp.getString(Constant.APP_MASTERID, ""));
        //替换
        properties.put("m", ZhujiListFragment.getMasterId());
//        showInProgress(getString(R.string.loading), false, true);
        createSoundPool();
        // 载入音频流，返回在池中的id
        sourceid = soundPool.load(this, R.raw.pdsuccess, 0);
//        JavaThreadPool.getInstance().excute(new TypeLoad());
    }


    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return super.shouldOverrideUrlLoading(view, request);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            showErrorPage();
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            new AlertView(getString(R.string.tips), getString(R.string.notification_error_ssl_cert_invalid), getString(R.string.cancel),
                    new String[]{getString(R.string.sure)}, null, AddDeviceActivity.this, AlertView.Style.Alert, new OnItemClickListener() {

                @Override
                public void onItemClick(Object o, int position) {
                    if (position != -1) {
                        handler.proceed();
                    }else {
                        handler.cancel();
                    }
                }
            }).show();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 5 && resultCode == 11) {
            setResult(resultCode);
            finish();
        } else if (requestCode == 5 && resultCode == 8) {
            setResult(resultCode);
            finish();
        }
    }

    public boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager
                    .getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    private void showErrorPage() {
        rl_error.setVisibility(View.VISIBLE);
        mAgentWebLayout.setVisibility(View.GONE);
        rl_error.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mAgentWeb.getUrlLoader().reload();
            }
        });

    }


    protected void createSoundPool() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            createNewSoundPool();
        } else {
            createOldSoundPool();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void createNewSoundPool() {
        // 指定声音池的最大音频流数目为10，声音品质为5
        AudioAttributes.Builder b = new AudioAttributes.Builder();
        b.setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE); // 播放声音通道
        b.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION);
        SoundPool.Builder sBuilder = new SoundPool.Builder();
        sBuilder.setMaxStreams(10);
        sBuilder.setAudioAttributes(b.build());
        soundPool = sBuilder.build();
    }

    @SuppressWarnings("deprecation")
    protected void createOldSoundPool() {
        // 指定声音池的最大音频流数目为10，声音品质为5,第二个参数为通过哪种方式播放，修改手机上的声音大小会影响到
        soundPool = new SoundPool(5, AudioManager.STREAM_VOICE_CALL, 0);
    }

    private void removeAutoSendControlCommand() {
        defaultHandler.removeMessages(5); //移除重发
        if (ingText != null) {
            ingText.setText(getString(R.string.add_auto_tips_auto_sendstop));
            ingIco.setVisibility(View.GONE);
        }
    }

    private void sendControlCommand(int position) {
        SyncMessage message = new SyncMessage(CommandMenu.rq_pdByHand_onlyControl);
        message.setCode(1); //code 1表示发送指令
        try {
            properties.put("i", keys.getJSONObject(position).getIntValue("i"));
            message.setSyncBytes(properties.toJSONString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        SyncMessageContainer.getInstance().produceSendMessage(message);
    }

    public void back(View v) {
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.match_btn:
                if (map != null) {
                    if (map.get("netType") != null && Integer.parseInt(map.get("netType").toString()) == 0) {//0为wifi添加类型
                        Intent intent = new Intent();
                        intent.setClass(getApplicationContext(), Add8266WifiActivity.class);
                        intent.putExtra("isMainList", true);
                        startActivityForResult(intent, 5);
                        return;
                    }
                    properties.put("t", map.get("type"));
                    properties.put("tid", map.get("id"));
                    isOnlyControl = (Boolean) map.get("onlyControl");
                }
                showInProgress(getString(R.string.add_shoudong_submit_into), false, false);
                defaultHandler.sendEmptyMessageDelayed(1, 5000);
                // 发送配对指令
                SyncMessage message = new SyncMessage();
                if (map != null
                        && (Boolean) map.get("autoPeidui")
                        && !"neiqian".equals(map.get("controlType"))) {
                    message.setCommand(CommandMenu.rq_pdByAuto.value());
                    message.setCode(CodeMenu.rq_pdByAuto_into.value());
                } else {
                    message.setCommand(CommandMenu.rq_pdByHand.value());
                }

                try {
                    Log.d("czm",properties.toJSONString()+"-masterid:"+ ZhujiListFragment.getMasterId());
                    message.setSyncBytes(properties.toJSONString().getBytes("UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                SyncMessageContainer.getInstance().produceSendMessage(message);

                break;

            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        defaultHandler.removeCallbacksAndMessages(null);
        defaultHandler = null;
        if (isRegFilter) {
            isRegFilter = false;
            this.unregisterReceiver(defaultReceiver);
        }
        super.onDestroy();
        if (mAgentWeb!=null) {
            mAgentWeb.getWebLifeCycle().onDestroy();
        }
    }

    /**
     * 播放配对成功提示音
     */
    private void startMusic() {
        try {
        } catch (Exception e) {
            Log.e("AlertMessage", "播放音乐失败", e);
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
            if (keys != null) {
                return keys.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (keys != null) {
                return keys.get(position);
            }
            return null;
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
            viewCache.keyname.setText(keys.getJSONObject(position).getString("n"));
            viewCache.keybg.setBackgroundResource(R.drawable.device_item_one_button_bg);
            return view;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAgentWeb!=null) {
            mAgentWeb.getWebLifeCycle().onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAgentWeb!=null) {
            mAgentWeb.getWebLifeCycle().onPause();
        }
    }
}
