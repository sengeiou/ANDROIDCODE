package com.smartism.znzk.activity.device;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.common.ImageViewActivity;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.LogUtil;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.view.alertview.AlertView;
import com.smartism.znzk.view.alertview.OnItemClickListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class BeiJingMaoYanActivity extends ActivityParentActivity implements View.OnClickListener{
    private final int dHandler_timeout = 1,dHandler_bindlock = 2;
    private ListView listView;
    private BeiJingMaoYanActivity.MaoYanAdapter myadapter;
    private RadioButton button_picture,button_wifiset,button_bindblock;
    private TextView title;
    private View headView;
    private DeviceInfo deviceInfo;
    private List<CommandInfo> commandInfos;
    private ZhujiInfo zhujiInfo;
    private int totalSize = 0;
    private List<MaoYanBean> commandList;
    private View footerView;
    private Button footerView_button;
    private AlertView mAlertViewExt;
    private EditText mAlertEdit_ssid,mAlertEdit_pwd;
    private InputMethodManager imm;
    private List<DeviceInfo> deviceList;
    private List<String> clockNames;
    private List<String> clockSlaveids;
    private String clockSelectSlaveid;
    private AlertDialog listDialog;

    // 显示图片的配置
    DisplayImageOptions options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.loading)
            .showImageOnFail(R.drawable.sorrow).cacheInMemory(true).cacheOnDisc(true)
            .bitmapConfig(Bitmap.Config.RGB_565).imageScaleType(ImageScaleType.EXACTLY_STRETCHED)// 设置图片以如何的编码方式显示
            .resetViewBeforeLoading(true)// 设置图片在下载前是否重置，复位
            // .displayer(new RoundedBitmapDisplayer(20))//是否设置为圆角，弧度为多少
            .displayer(new FadeInBitmapDisplayer(100))// 是否图片加载好后渐入的动画时间
            .build();

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case dHandler_timeout: //超时
                    defaultHandler.removeMessages(dHandler_timeout);
                    mContext.cancelInProgress();
                    Toast.makeText(mContext.getApplicationContext(), getString(R.string.timeout), Toast.LENGTH_SHORT).show();
                    break;
                case 10: // 获取数据成功
                    cancelInProgress();
                    commandList.addAll((List<MaoYanBean>) msg.obj);
                    myadapter.notifyDataSetChanged();
                    if (totalSize == commandList.size()) {
                        listView.removeFooterView(footerView);
                    }
                    break;
                case dHandler_bindlock:
                    cancelInProgress();
                    defaultHandler.removeMessages(dHandler_timeout);
                    listDialog.dismiss();
                    Toast.makeText(mContext,getString(R.string.activity_beijingmy_bindsuccess),Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            return false;
        }
    };
    private Handler defaultHandler = new WeakRefHandler(mCallback);

    private BroadcastReceiver defaultReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (Actions.REFRESH_DEVICES_LIST.equals(intent.getAction())) { // 数据刷新完成广播
            } else if (Actions.ACCETP_ONEDEVICE_MESSAGE.equals(intent.getAction())) { // 某一个设备的推送广播
                if (intent.getStringExtra("device_id")!=null && deviceInfo.getId() == Long.parseLong(intent.getStringExtra("device_id"))) {
                    String data = (String) intent.getSerializableExtra("device_info");
                    if (data != null) {
                        JSONObject object = JSONObject.parseObject(data);
                        if ("2".equals(object.getString("sort")) && progressIsShowing()) {
                            Toast.makeText(mContext, getString(R.string.rq_control_sendsuccess),
                                    Toast.LENGTH_SHORT).show();
                            mContext.cancelInProgress();
                            defaultHandler.removeMessages(dHandler_timeout);
                        }
                    }
                }
            } else if (Actions.CONNECTION_FAILED_SENDFAILED.equals(intent.getAction())) { // 发送失败
                mContext.cancelInProgress();
                Toast.makeText(mContext, getString(R.string.rq_control_sendfailed),
                        Toast.LENGTH_SHORT).show();
                defaultHandler.removeMessages(dHandler_timeout);
            } else if (Actions.SHOW_SERVER_MESSAGE.equals(intent.getAction())) { // 显示服务器信息
                defaultHandler.removeMessages(dHandler_timeout);
                mContext.cancelInProgress();
                JSONObject resultJson = null;
                try {
                    resultJson = JSON.parseObject(intent.getStringExtra("message"));
                } catch (Exception e) {
                    Log.w("DevicesList", "获取服务器返回消息，转换为json对象失败，用原始值处理");
                }
                if (resultJson != null) {
                    switch (resultJson.getIntValue("Code")) {
                        case 4:
                            Toast.makeText(mContext, getString(R.string.tips_4), Toast.LENGTH_SHORT).show();
                            break;
                        case 5:
                            Toast.makeText(mContext, getString(R.string.tips_5), Toast.LENGTH_SHORT).show();
                            break;
                        case 6:
                            Toast.makeText(mContext, getString(R.string.tips_6), Toast.LENGTH_SHORT).show();
                            break;
                        case 7:
                            Toast.makeText(mContext, getString(R.string.tips_7), Toast.LENGTH_SHORT).show();
                            break;
                        case 8:
                            Toast.makeText(mContext, getString(R.string.tips_8), Toast.LENGTH_SHORT).show();
                            break;

                        default:
                            Toast.makeText(mContext, "Unknown Info", Toast.LENGTH_SHORT).show();
                            break;
                    }

                } else {
                    Toast.makeText(mContext, intent.getStringExtra("message"), Toast.LENGTH_SHORT)
                            .show();

                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bei_jing_maoyan);
        initView();
        initData();
        initRegisterReceiver();
    }

    private void initData() {
        deviceInfo = (DeviceInfo) getIntent().getSerializableExtra("device");
        commandList = new ArrayList<>();
        clockNames = new ArrayList<>();
        clockSlaveids = new ArrayList<>();
        myadapter = new BeiJingMaoYanActivity.MaoYanAdapter(commandList, this);
        listView.setAdapter(myadapter);
        JavaThreadPool.getInstance().excute(new loadAllDevicesInfo());
        initCommandList();
        footerView_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // 加载更多按钮点击
                JavaThreadPool.getInstance().excute(new CommandHistoryLoad(commandList.size() - 1 < 0 ? 0 : commandList.size() - 1, 5));
            }
        });
        commandInfos = DatabaseOperator.getInstance(this).queryAllCommands(deviceInfo.getId());
        title.setText(deviceInfo.getName());
    }

    private void initCommandList(){
        JavaThreadPool.getInstance().excute(new CommandHistoryLoad(0, 5));
    }

    private void initView() {
        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        headView = LayoutInflater.from(this).inflate(R.layout.beijingmaoyan_headview, null, false);
        listView = (ListView) findViewById(R.id.beijingsuo_list);
        title = (TextView) findViewById(R.id.title);
        footerView = LayoutInflater.from(BeiJingMaoYanActivity.this).inflate(R.layout.list_foot_loadmore, null);
        footerView_button = (Button) footerView.findViewById(R.id.load_more);
        listView.addHeaderView(headView);
        listView.addFooterView(footerView);
        button_bindblock = (RadioButton) findViewById(R.id.button_bindblock);
        button_wifiset = (RadioButton) findViewById(R.id.button_wifiset);
        button_picture = (RadioButton) findViewById(R.id.button_picture);
        button_wifiset.setOnClickListener(this);
        button_picture.setOnClickListener(this);
        button_bindblock.setOnClickListener(this);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertView(null, null, getString(R.string.cancel), null, new String[]{getString(R.string.delete)},
                        mContext, AlertView.Style.ActionSheet, new OnItemClickListener() {
                    @Override
                    public void onItemClick(Object o, int p) {
                        switch (p){
                            case 0:
                                JavaThreadPool.getInstance().excute(new DelCommandHistory(commandList.get(position).getId()));
                                break;
                            default:
                                break;
                        }
                    }
                }).show();
                return true;
            }
        });
    }

    public void back(View v) {
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_picture:
                defaultHandler.sendEmptyMessageDelayed(dHandler_timeout, 15 * 1000);
                showInProgress(getString(R.string.operationing),false,false);
                SyncMessage message = new SyncMessage();
                message.setCommand(SyncMessage.CommandMenu.rq_control.value());
                message.setDeviceid(deviceInfo.getId());
                // 操作 这里的操作写死，需要在服务器的配置页面中配置此按钮对应的指令是什么 2指令是发起拍照
                message.setSyncBytes(new byte[]{0x02});
                SyncMessageContainer.getInstance().produceSendMessage(message);
                break;
            case R.id.button_wifiset:
                //拓展窗口
                mAlertViewExt = new AlertView(getString(R.string.activity_beijingmy_wifititle), null, getString(R.string.cancel), null, new String[]{getString(R.string.submit)}, this, AlertView.Style.Alert, new OnItemClickListener(){

                    @Override
                    public void onItemClick(Object o, int position) {
                        if (position != -1){
                            if ("".equals(mAlertEdit_ssid.getText().toString()) || "".equals(mAlertEdit_pwd.getText().toString())){
                                Toast.makeText(mContext,getString(R.string.activity_beijingmy_wifiinfoempty),Toast.LENGTH_SHORT).show();
                                return;
                            }
                            showInProgress(getString(R.string.operationing),false,true);
                            String[] keys = new String[2];
                            String[] values = new String[2];
                            keys[0] = String.valueOf(CommandInfo.CommandTypeEnum.setWifiSSID.value());
                            values[0] = mAlertEdit_ssid.getText().toString();
                            keys[1] = String.valueOf(CommandInfo.CommandTypeEnum.setWifiPassword.value());
                            values[1] = mAlertEdit_pwd.getText().toString();
                            JavaThreadPool.getInstance().excute(new PropertiesSet(0,keys,values));
                        }
                    }
                });
                ViewGroup extView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.layout_alertview_alertext_form,null);
                mAlertEdit_ssid = (EditText) extView.findViewById(R.id.wifi_ssid);
                mAlertEdit_ssid.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean focus) {
                        //输入框出来则往上移动
                        boolean isOpen=imm.isActive();
                        mAlertViewExt.setMarginBottom(isOpen&&focus ? 120 :0);
                    }
                });
                mAlertEdit_pwd = (EditText) extView.findViewById(R.id.wifi_pwd);
                mAlertEdit_pwd.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean focus) {
                        //输入框出来则往上移动
                        boolean isOpen=imm.isActive();
                        mAlertViewExt.setMarginBottom(isOpen&&focus ? 120 :0);
                    }
                });
                for (CommandInfo comm : commandInfos) {
                    if (comm != null && comm.getCtype() .equals(CommandInfo.CommandTypeEnum.setWifiSSID.value()) ){
                        mAlertEdit_ssid.setText(comm.getCommand());
                    }else if (comm != null && comm.getCtype() .equals(CommandInfo.CommandTypeEnum.setWifiPassword.value()) ){
                        mAlertEdit_pwd.setText(comm.getCommand());
                    }
                }
                mAlertViewExt.addExtView(extView);
                mAlertViewExt.show();
                break;
            case R.id.button_bindblock:
                clockNames.clear();
                int selectitem = -1;
                if (deviceList!=null && !deviceList.isEmpty()){
                    for (int i = 0;i<deviceList.size();i++){
                        DeviceInfo d = deviceList.get(i);
                        if (DeviceInfo.CaMenu.zhinengsuo.value().equals(d.getCa())) {
                            clockNames.add(d.getName()+"("+(d.getWhere()==null?"":d.getwIndex())+d.getType()+")");
                            clockSlaveids.add(d.getSlaveId());
                            for (CommandInfo com : commandInfos) {
                                if (d.getSlaveId()!=null && com.getCtype() .equals(CommandInfo.CommandTypeEnum.setPictureCommSlaveId.value()) && d.getSlaveId().equals(com.getCommand())){
                                    selectitem = i;
                                }
                            }
                        }
                    }
                }
                if (!clockNames.isEmpty()){
                    AlertDialog.Builder builder = new AlertDialog.Builder(BeiJingMaoYanActivity.this);
                    builder.setTitle(getString(R.string.activity_devices_list_choose_operation));
                    builder.setSingleChoiceItems(clockNames.toArray(new CharSequence[clockNames.size()]), selectitem, new DialogInterface.OnClickListener() {//单选

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            clockSelectSlaveid = clockSlaveids.get(which);
                        }
                    });
                    builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.setPositiveButton(getString(R.string.sure), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (clockSelectSlaveid!=null){
                                showInProgress(getString(R.string.operationing),false,true);
                                defaultHandler.sendEmptyMessageDelayed(dHandler_timeout, 15 * 1000);
                                String[] keys = new String[1];
                                String[] values = new String[1];
                                keys[0] = String.valueOf(CommandInfo.CommandTypeEnum.setPictureCommSlaveId.value());
                                values[0] = clockSelectSlaveid;
                                JavaThreadPool.getInstance().excute(new PropertiesSet(dHandler_bindlock,keys,values));
                            }else{
                                Toast.makeText(mContext,getString(R.string.activity_beijingmy_noselectbindlock),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    listDialog = builder.create();
                    listDialog.show();
                    clockSelectSlaveid = null;
                }else{
                    Toast.makeText(this,getString(R.string.activity_beijingmy_nosmartlock),Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * 注册广播
     */
    private void initRegisterReceiver() {
        IntentFilter receiverFilter = new IntentFilter();
        receiverFilter.addAction(Actions.REFRESH_DEVICES_LIST);
        receiverFilter.addAction(Actions.ACCETP_ONEDEVICE_MESSAGE);
        receiverFilter.addAction(Actions.CONNECTION_FAILED_SENDFAILED);
        receiverFilter.addAction(Actions.SHOW_SERVER_MESSAGE);
        this.registerReceiver(defaultReceiver, receiverFilter);
    }


    /**
     * 猫眼图片列表
     */
    class MaoYanAdapter extends BaseAdapter {
        private List<BeiJingMaoYanActivity.MaoYanBean> imageList;
        private LayoutInflater layoutInflater;

        public MaoYanAdapter(List<BeiJingMaoYanActivity.MaoYanBean> imageList, Context context) {
            this.imageList = imageList;
            this.layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return imageList.size();
        }

        @Override
        public Object getItem(int position) {
            return imageList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            BeiJingMaoYanActivity.MaoYanAdapter.ViewHande hande = null;
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.item_beijingsuo_imagelist, null, false);
                hande = new BeiJingMaoYanActivity.MaoYanAdapter.ViewHande(convertView);
                convertView.setTag(hande);
            } else {
                hande = (BeiJingMaoYanActivity.MaoYanAdapter.ViewHande) convertView.getTag();
            }
            hande.setValue(imageList.get(position));
            return convertView;
        }

        class ViewHande {
            private TextView time;
            private RecyclerView mRecyclerView;
            private BeiJingMaoYanActivity.BeijingImgAdapter mAdapter;
            public ViewHande(View view) {
                time = (TextView) view.findViewById(R.id.beijingsuo_time);
                //得到控件
                mRecyclerView = (RecyclerView) view.findViewById(R.id.beijngsuo_recycle);
                //设置布局管理器
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(BeiJingMaoYanActivity.this);
                linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                mRecyclerView.setLayoutManager(linearLayoutManager);

            }

            public void setValue(BeiJingMaoYanActivity.MaoYanBean bean) {
                time.setText(bean.getTime());
                //设置适配器
                mAdapter = new BeiJingMaoYanActivity.BeijingImgAdapter(BeiJingMaoYanActivity.this, bean.getImgList());
                mRecyclerView.setAdapter(mAdapter);
            }

        }
    }


    /**
     * 横向图片列表
     */
    public class BeijingImgAdapter extends
            RecyclerView.Adapter<BeiJingMaoYanActivity.BeijingImgAdapter.ViewHolder> {
        private LayoutInflater mInflater;
        private List<String> mDatas;

        public BeijingImgAdapter(Context context, List<String> datats) {
            mInflater = LayoutInflater.from(context);
            mDatas = datats;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public ViewHolder(View arg0) {
                super(arg0);
            }

            ImageView mImg;
            TextView mTxt;
        }

        @Override
        public int getItemCount() {
            return mDatas.size();
        }

        /**
         * 创建ViewHolder
         */
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = mInflater.inflate(R.layout.activity_bjrecycler_item,
                    viewGroup, false);
            BeiJingMaoYanActivity.BeijingImgAdapter.ViewHolder viewHolder = new BeiJingMaoYanActivity.BeijingImgAdapter.ViewHolder(view);

            viewHolder.mImg = (ImageView) view
                    .findViewById(R.id.beijing_recycle_item_img);
            return viewHolder;
        }

        /**
         * 设置值
         */
        @Override
        public void onBindViewHolder(final BeiJingMaoYanActivity.BeijingImgAdapter.ViewHolder viewHolder, final int i) {
            ImageLoader.getInstance()
                    .displayImage(
                            mDatas.get(i),
                            viewHolder.mImg,options, new BeiJingMaoYanActivity.MImageLoadingBar());
            viewHolder.mImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(BeiJingMaoYanActivity.this, ImageViewActivity.class);
                    intent.putExtra("img_url", mDatas.get(i));
                    startActivity(intent);
                }
            });
        }

    }

    class MaoYanBean {
        private String id;
        private String time;
        private List<String> imgList;

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public List<String> getImgList() {
            return imgList;
        }

        public void setImgList(List<String> imgList) {
            this.imgList = imgList;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (defaultReceiver != null) {
            mContext.unregisterReceiver(defaultReceiver);
        }
    }

    class CommandHistoryLoad implements Runnable {
        private int start, size;

        public CommandHistoryLoad(int start, int size) {
            this.size = size;
            this.start = start;
        }

        @Override
        public void run() {
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("id", deviceInfo.getId());
            object.put("start", this.start);
            object.put("size", this.size);
            String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/d/hm",object,BeiJingMaoYanActivity.this);
            if ("-3".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(BeiJingMaoYanActivity.this, getString(R.string.history_response_nodevice),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else if (result.length() > 4 ) {

                List<MaoYanBean> list = new ArrayList<>();
                JSONObject resultJson = null;
                try {
                    resultJson = JSON.parseObject(result);
                } catch (Exception e) {
                    LogUtil.e(getApplicationContext(),TAG,"解密错误：：",e);
                    return;
                }

                try {
                    JSONArray array = resultJson.getJSONArray("result");
                    if (array != null && !array.isEmpty()) {
                        for (int j = 0; j < array.size(); j++) {
                            JSONObject p = array.getJSONObject(j);
                            MaoYanBean bean = new MaoYanBean();
                            bean.setTime(SimpleDateFormat.getDateTimeInstance().format(p.getDate("deviceCommandTime")));//getDateTimeInstance会使用本地格式化
                            List<String> imgs = new ArrayList<>();
                            JSONObject jsonurl = JSON.parseObject(p.getString("deviceCommand"));
                            JSONArray arrayImg = jsonurl.getJSONArray("urls");
                            for (int h = 0; h < arrayImg.size(); h++) {
                                imgs.add(arrayImg.getString(h));
                            }
                            bean.setImgList(imgs);
                            bean.setId(p.getString("id"));
                            list.add(bean);
                        }
                    }
                }catch (Exception ex){
                    //防止json出错崩溃
                    LogUtil.e(getApplicationContext(),TAG,"获取服务器猫眼图片列表错误：：",ex);
                }
                // 请求成功了，需要刷新数据到页面，也需要清除此设备的历史未读记录
                ContentValues values = new ContentValues();
                values.put("nr", 0); // 未读消息数
                DatabaseOperator.getInstance(BeiJingMaoYanActivity.this).getWritableDatabase().update(
                        "DEVICE_STATUSINFO", values, "id = ?", new String[] { String.valueOf(deviceInfo.getId()) });

                totalSize = resultJson.getIntValue("allCount");
                Message m = defaultHandler.obtainMessage(10);
                m.obj = list;
                defaultHandler.sendMessage(m);
            }
        }
    }
    class DelCommandHistory implements Runnable {
        private String vid;

        public DelCommandHistory(String vid) {
            this.vid = vid;
        }

        @Override
        public void run() {
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("id", deviceInfo.getId());
            object.put("vid", vid);
            String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/d/hmd",object,BeiJingMaoYanActivity.this);
            if ("-3".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(BeiJingMaoYanActivity.this, getString(R.string.history_response_nodevice),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else if ("0".equals(result)){
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        initCommandList();
                        Toast.makeText(BeiJingMaoYanActivity.this, getString(R.string.success),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }else{
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(BeiJingMaoYanActivity.this, getString(R.string.net_error_operationfailed),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
    public class MImageLoadingBar implements ImageLoadingListener {

        @Override
        public void onLoadingCancelled(String arg0, View arg1) {
            arg1.clearAnimation();
        }

        @Override
        public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
            arg1.clearAnimation();
        }

        @Override
        public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
            arg1.clearAnimation();
        }

        @Override
        public void onLoadingStarted(String arg0, View arg1) {
            arg1.startAnimation(mContext.imgloading_animation);
        }
    }

    /**
     * what 为0 时直接在线程内部处理，非0才会回调出来
     */
    private class PropertiesSet implements Runnable {
        int what = 0;
        String[] keys,values;
        public PropertiesSet(int what,String[] keys,String[] values){
            this.what = what;
            this.keys = keys;
            this.values = values;
        }
        @Override
        public void run() {
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("did", deviceInfo.getId());
            JSONArray array = new JSONArray();
            if (keys!=null && keys.length > 0 && values!=null && keys.length == values.length){
                for (int i=0; i<keys.length;i++){
                    JSONObject o = new JSONObject();
                    o.put("vkey", keys[i]);
                    o.put("value",values[i]);
                    array.add(o);
                }
            }
            object.put("vkeys", array);
            String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/d/p/set", object, BeiJingMaoYanActivity.this);

            if (result != null && result.equals("0")) {
                if (what != 0){
                    defaultHandler.sendMessage(defaultHandler.obtainMessage(what));
                }else{
                    defaultHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(BeiJingMaoYanActivity.this, getString(R.string.success),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                defaultHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(BeiJingMaoYanActivity.this, getString(R.string.operator_error),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
    class loadAllDevicesInfo implements Runnable {

        @Override
        public void run() {
            Cursor cursor = DatabaseOperator.getInstance(BeiJingMaoYanActivity.this).getWritableDatabase()
                    .rawQuery("select * from DEVICE_STATUSINFO", new String[]{});
            deviceList = new ArrayList<>();
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    deviceList.add(DatabaseOperator.getInstance(getApplicationContext()).buildDeviceInfo(cursor));
                }
                cursor.close();
            }
        }
    }
}
