package com.smartism.znzk.activity.device;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.GroupInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.view.SelectAddPopupWindow;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SecurityDisarmingActivity extends ActivityParentActivity implements View.OnClickListener {
    private ZhujiInfo zhuji;
    private long zhuji_id;
    private static final int handler_data = 1;
    private static final int dHandlerWhat_initsuccess = 2;
    private static final int dHandlerWhat_savesuccess = 3;
    private static final int dHandlerWhat_timeout = 4;
    private ListView listView;
    private SecurityAdapter securityAdapter;
    private List<DeviceInfo> dInfos;    //显示数据
    private List<DeviceInfo> deviceInfos;    //本地数据
    private List<Long> deviceList;    //服务器数据
    public SelectAddPopupWindow menuWindow; // 弹出框
    private Button securtity_save;

    private TextView name;
    private boolean isShowList = false;


    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            List<DeviceInfo> list = null;
            List<Long> slist = null;
            switch (msg.what) {
                case handler_data:
                    deviceList.clear();
                    List<Long> lists = (List<Long>) msg.obj;
                    if (lists != null) {
                        for (long str : lists) {
                            deviceList.add(str);
                        }
                    }

                    if (isShowList) {
                        dInfos.clear();
                        for (DeviceInfo deviceInfo : deviceInfos) {
                            for (long str : deviceList) {
                                if (str == deviceInfo.getId()) {
                                    dInfos.add(deviceInfo);
                                }
                            }
                        }
                        securityAdapter.notifyDataSetChanged();
                    }
                    isShowList = true;

                    break;
                case dHandlerWhat_initsuccess:
                    list = (List<DeviceInfo>) msg.obj;
                    dInfos.clear();
                    deviceInfos.clear();
                    if (list != null) {
                        for (DeviceInfo deviceInfo : list) {
                            if (DeviceInfo.CakMenu.security.value()
                                    .equals(deviceInfo.getCak())) {
                                deviceInfos.add(deviceInfo);
                            }
                        }
                    }
                    if (isShowList) {
                        for (DeviceInfo deviceInfo : deviceInfos) {
                            for (long str : deviceList) {
                                if (str == deviceInfo.getId()) {
                                    dInfos.add(deviceInfo);
                                }
                            }
                        }
                        securityAdapter.notifyDataSetChanged();
                    }
                    isShowList = true;
                    break;
                case dHandlerWhat_savesuccess:
                    cancelInProgress();
                    defHandler.removeMessages(dHandlerWhat_timeout);
                    securtity_save.setVisibility(View.GONE);
                    name.setText(getResources().getString(R.string.activity_security_rightmenu_title_disarming));
                    dInfos.clear();
                    if (deviceList.size() > 0) {
                        for (DeviceInfo deviceInfo : deviceInfos) {
                            for (long str : deviceList) {
                                if (str == deviceInfo.getId()) {
                                    dInfos.add(deviceInfo);
                                }
                            }
                        }
                    }
                    securityAdapter.notifyDataSetChanged();
                    break;
                case dHandlerWhat_timeout:
                    cancelInProgress();
                    defHandler.removeMessages(dHandlerWhat_timeout);
                    Toast.makeText(SecurityDisarmingActivity.this, getResources().getString(R.string.time_out), Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    };
    private Handler defHandler = new WeakRefHandler(mCallback);

    // 显示图片的配置
    DisplayImageOptions options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.loading)
            .showImageOnFail(R.drawable.sorrow).cacheInMemory(true).cacheOnDisc(true)
            .bitmapConfig(Bitmap.Config.RGB_565).imageScaleType(ImageScaleType.EXACTLY_STRETCHED)// 设置图片以如何的编码方式显示
            .resetViewBeforeLoading(true)// 设置图片在下载前是否重置，复位
            // .displayer(new RoundedBitmapDisplayer(20))//是否设置为圆角，弧度为多少
            .displayer(new FadeInBitmapDisplayer(100))// 是否图片加载好后渐入的动画时间
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security);
        zhuji_id = getIntent().getLongExtra("zhuji_id", 0);
        if (zhuji_id != 0){
            zhuji = DatabaseOperator.getInstance(SecurityDisarmingActivity.this)
                    .queryDeviceZhuJiInfo(zhuji_id);
        }else{
//            zhuji = DatabaseOperator.getInstance(SecurityDisarmingActivity.this)
//                    .queryDeviceZhuJiInfo(dcsp.getString(DataCenterSharedPreferences.Constant.APP_MASTERID, ""));
            //替换
            zhuji = DatabaseOperator.getInstance(SecurityDisarmingActivity.this).queryDeviceZhuJiInfo(ZhujiListFragment.getMasterId());
        }
        JavaThreadPool.getInstance().excute(new LoadAllDevicesInfo(dHandlerWhat_initsuccess));
        JavaThreadPool.getInstance().excute(new LoadSecurity(handler_data));
        initView();
        initData();
        initEvent();

    }

    private void initEvent() {
        securtity_save.setOnClickListener(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (dInfos != null) {
                    if (dInfos.get(position).isFlag()) {
                        dInfos.get(position).setFlag(false);
                    } else {
                        dInfos.get(position).setFlag(true);
                    }
                }
                securityAdapter.notifyDataSetChanged();
            }
        });
    }

    private void initData() {
        dInfos = new ArrayList<>();
        deviceList = new ArrayList<>();
        deviceInfos = new ArrayList<>();
        securityAdapter = new SecurityAdapter();
        listView.setAdapter(securityAdapter);
        name.setText(getResources().getString(R.string.activity_security_rightmenu_title_disarming));
    }

    @Override
    public void onClick(View v) {
        securtity_save.setVisibility(View.VISIBLE);
        switch (v.getId()) {
            case R.id.group_add:
                menuWindow.dismiss();
                name.setText(getResources().getString(R.string.activity_security_rightmenu_add_disarming));
                dInfos.clear();
                if (deviceInfos != null) {
                    for (DeviceInfo d : deviceInfos) {
                        d.setFlag(false);
                        dInfos.add(d);
                    }
                }
                for (long str : deviceList) {
                    for (DeviceInfo deviceInfo : deviceInfos) {
                        if (str == deviceInfo.getId()) {
                            dInfos.remove(deviceInfo);
                            break;
                        }
                    }
                }
                securityAdapter.notifyDataSetChanged();
                break;
            case R.id.group_dele:
                menuWindow.dismiss();
                name.setText(getResources().getString(R.string.activity_security_rightmenu_del_disarming));
                dInfos.clear();
                for (DeviceInfo deviceInfo : deviceInfos) {
                    for (long str : deviceList) {
                        if (str == deviceInfo.getId()) {
                            deviceInfo.setFlag(true);
                            dInfos.add(deviceInfo);
                        }
                    }
                }
                securityAdapter.notifyDataSetChanged();
                break;
            case R.id.securtity_save:
                showInProgress(getString(R.string.submiting), false, true);
                defHandler.sendEmptyMessageDelayed(dHandlerWhat_timeout, 8 * 1000);
                JavaThreadPool.getInstance().excute(new SaveSecurity(dHandlerWhat_savesuccess));
                break;
        }

    }

    class SecurityAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return dInfos != null ? dInfos.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return dInfos != null ? dInfos.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SecurityInfoView spImg = new SecurityInfoView();

            if (convertView == null) {
                convertView = View.inflate(SecurityDisarmingActivity.this, R.layout.activity_add_group_devices_list_item, null);
                spImg.ioc = (ImageView) convertView.findViewById(R.id.device_logo);
                spImg.checked = (ImageView) convertView.findViewById(R.id.checked);
                spImg.name = (TextView) convertView.findViewById(R.id.device_name);
                spImg.type = (TextView) convertView.findViewById(R.id.device_type);
                convertView.setTag(spImg);
            } else {
                spImg = (SecurityInfoView) convertView.getTag();
            }
            if (getResources().getString(R.string.activity_security_rightmenu_title_disarming)
                    .equals(name.getText())) {
                spImg.checked.setVisibility(View.GONE);
            } else {
                spImg.checked.setVisibility(View.VISIBLE);
            }
            setShowInfo(spImg, position);
            return convertView;
        }
    }

    class SecurityInfoView {
        ImageView ioc, checked;
        TextView name, where, type;
    }

    /**
     * 设置设备logo图片和名称
     *
     * @param i
     */
    private void setShowInfo(SecurityInfoView viewCache, int i) {
        if (DeviceInfo.ControlTypeMenu.wenduji.value().equals(dInfos.get(i).getControlType())) {
            // 设置图片
            if (Actions.VersionType.CHANNEL_UCTECH.equals(((MainApplication) getApplication()).getAppGlobalConfig().getVersion())) {
                try {
                    viewCache.ioc.setImageBitmap(BitmapFactory.decodeStream(
                            getAssets().open("uctech/uctech_t_" + dInfos.get(i).getChValue() + ".png")));
                } catch (IOException e) {
                    Log.e("uctech", "读取图片文件错误");
                }
            } else {
                ImageLoader.getInstance()
                        .displayImage(
                                 dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "") + "/devicelogo/"
                                        + dInfos.get(i).getLogo(),
                                viewCache.ioc, options, new ImageLoadingBar());
            }
            viewCache.name.setText(dInfos.get(i).getName() + "CH" + dInfos.get(i).getChValue());
        } else if (DeviceInfo.ControlTypeMenu.wenshiduji.value().equals(dInfos.get(i).getControlType())) {
            if (Actions.VersionType.CHANNEL_UCTECH.equals(((MainApplication) getApplication()).getAppGlobalConfig().getVersion())) {
                try {
                    viewCache.ioc.setImageBitmap(BitmapFactory.decodeStream(
                            getAssets().open("uctech/uctech_th_" + dInfos.get(i).getChValue() + ".png")));
                } catch (IOException e) {
                    Log.e("uctech", "读取图片文件错误");
                }
            } else {
                ImageLoader.getInstance()
                        .displayImage(
                                 dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "") + "/devicelogo/"
                                        + dInfos.get(i).getLogo(),
                                viewCache.ioc, options, new ImageLoadingBar());
            }
            viewCache.name.setText(dInfos.get(i).getName() + "CH" + dInfos.get(i).getChValue());
        } else {
            // 设置图片
            ImageLoader.getInstance().displayImage( dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "")
                    + "/devicelogo/" + dInfos.get(i).getLogo(), viewCache.ioc, options, new ImageLoadingBar());
            viewCache.name.setText(dInfos.get(i).getName());
        }
        viewCache.type
                .setText(((dInfos.get(i).getWhere() == null || "null".equals(dInfos.get(i).getWhere())) ? "" : (dInfos.get(i).getWhere() + " "))
                        + dInfos.get(i).getType());
        if (dInfos.get(i).isFlag()) {
            viewCache.checked.setBackgroundResource(R.drawable.zhzj_password_remember_hover);
        } else {
            viewCache.checked.setBackgroundResource(R.drawable.zhzj_password_remember);
        }
    }

    private void initView() {
        listView = (ListView) findViewById(R.id.securtity_list);
        menuWindow = new SelectAddPopupWindow(SecurityDisarmingActivity.this, this, 1);
        name = (TextView) findViewById(R.id.title_edit_securtity);
        securtity_save = (Button) findViewById(R.id.securtity_save);
        securtity_save.setVisibility(View.GONE);
    }

    public void back(View v) {
        finish();
    }

    /**
     * 保存按钮点击
     *
     * @param view
     */
    public void save(View view) {
        setBackGround(0.7f);
        menuWindow.showAtLocation(view, Gravity.TOP | Gravity.RIGHT, 0,
                Util.dip2px(getApplicationContext(), 55) + Util.getStatusBarHeight(this));
    }

    private void setBackGround(float v) {
        WindowManager.LayoutParams manager = getWindow().getAttributes();
        manager.alpha = v;
        getWindow().setAttributes(manager);
    }

    class SaveSecurity implements Runnable {
        private int what;
        private List<Long> idlist;

        public SaveSecurity(int what) {
            this.what = what;
            idlist = new ArrayList<>();
        }

        @Override
        public void run() {

            if (zhuji_id == 0) {
                return;
            }
            String http = "";
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject pJsonObject = new JSONObject();
            pJsonObject.put("id", zhuji_id);
            JSONArray array = new JSONArray();
            if (getResources().getString(R.string.activity_security_rightmenu_del_disarming)
                    .equals(name.getText())) {
//                http = "/jdm/s3/d/delearming";
                http = "/jdm/s3/d/deldisarming";
            } else {
//                http = "/jdm/s3/d/addearming";
                http = "/jdm/s3/d/adddisarming";
            }
            for (DeviceInfo deviceInfo : dInfos) {
                if (deviceInfo.isFlag()) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id", deviceInfo.getId());
                    idlist.add(deviceInfo.getId());
                    array.add(jsonObject);
                }
            }
            pJsonObject.put("dids", array);
            String result = HttpRequestUtils.requestoOkHttpPost( server + http, pJsonObject, SecurityDisarmingActivity.this);
            if ("0".equals(result)) {
                if (getResources().getString(R.string.activity_security_rightmenu_del_disarming)
                        .equals(name.getText())) {
                    deviceList.removeAll(idlist);
                } else {
                    deviceList.addAll(idlist);
                }

                Message m = defHandler.obtainMessage(this.what);
                defHandler.sendMessage(m);
            } else if ("-3".equals(result)) {
                defHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        defHandler.removeMessages(dHandlerWhat_timeout);
                        cancelInProgress();
                        Toast.makeText(SecurityDisarmingActivity.this, getString(R.string.net_error_programs),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } else if ("-5".equals(result)) {
                defHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        defHandler.removeMessages(dHandlerWhat_timeout);
                        cancelInProgress();
                        if (getResources().getString(R.string.activity_security_rightmenu_del_disarming)
                                .equals(name.getText())) {
                            Toast.makeText(SecurityDisarmingActivity.this, getString(R.string.net_error_hava_record_del),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SecurityDisarmingActivity.this, getString(R.string.net_error_hava_record_add),
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            } else if ("-6".equals(result)) {
                defHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        defHandler.removeMessages(dHandlerWhat_timeout);
                        cancelInProgress();
                        Toast.makeText(SecurityDisarmingActivity.this, getString(R.string.activity_zhuji_not),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                cancelInProgress();
                defHandler.removeMessages(dHandlerWhat_timeout);
            }
        }

    }

    class LoadSecurity implements Runnable {
        private int what;

        public LoadSecurity(int what) {
            this.what = what;
        }

        @Override
        public void run() {
            if (zhuji_id == 0) {
                return;
            }
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject pJsonObject = new JSONObject();
            pJsonObject.put("id", zhuji_id);
            String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/d/disarming", pJsonObject, SecurityDisarmingActivity.this);
            if (result != null && result.length() > 4) {
                JSONArray resultJson = null;
                try {
                    resultJson = JSON.parseArray(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                List<Long> list = new ArrayList<>();
                for (int i = 0; i < resultJson.size(); i++) {
                    JSONObject object = resultJson.getJSONObject(i);
                    list.add(object.getLong("id"));
                }
                Message m = defHandler.obtainMessage(this.what);
                m.obj = list;
                defHandler.sendMessage(m);
            } else if ("-3".equals(result)) {
                defHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(SecurityDisarmingActivity.this, getString(R.string.net_error_programs),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private int sortType;
    private int deviceCount;
    private DeviceInfo operationDevice;

    class LoadAllDevicesInfo implements Runnable {
        private int what;

        public LoadAllDevicesInfo() {
        }

        public LoadAllDevicesInfo(int what) {
            this.what = what;
        }

        @Override
        public void run() {
            if (zhuji == null) {
                List<ZhujiInfo> tmpzhuji = DatabaseOperator.getInstance(mContext).queryAllZhuJiInfos();
                if (!tmpzhuji.isEmpty()) {
                    zhuji = tmpzhuji.get(0);
                }
            }
            DeviceInfo shexiangtou = null;
            List<DeviceInfo> deviceLists = new ArrayList<DeviceInfo>();
            List<DeviceInfo> deviceList_close = new ArrayList<DeviceInfo>();
            if (zhuji != null) {
                if (dcsp.getBoolean(DataCenterSharedPreferences.Constant.SHOW_ZHUJI, true)) {
                    // 设置属性
                    DeviceInfo deviceInfo = new DeviceInfo();
                    deviceInfo.setId(zhuji.getId());
                    deviceInfo.setName(zhuji.getName());
                    deviceInfo.setWhere(zhuji.getWhere());
                    deviceInfo.setStatus(zhuji.getUpdateStatus());
                    deviceInfo.setControlType(DeviceInfo.ControlTypeMenu.zhuji.value());
                    deviceInfo.setLogo(zhuji.getLogo());
                    deviceInfo.setGsm(zhuji.getGsm());
                    deviceInfo.setFlag(zhuji.isAdmin()); // 利用deviceInfo的flag存主机的是否admin信息
                    deviceInfo.setPowerStatus(zhuji.getPowerStatus());
                    deviceInfo.setLowb(zhuji.getBatteryStatus() == 1 ? true : false);//是否底电
                    deviceLists.add(deviceInfo); // 主机实例化一个对象来代替
                }
                List<GroupInfo> gInfos = DatabaseOperator.getInstance(mContext)
                        .queryAllGroups(zhuji.getId());
                if (gInfos != null && !gInfos.isEmpty()) {
                    for (GroupInfo g : gInfos) {
                        DeviceInfo dInfo = new DeviceInfo();
                        dInfo.setId(g.getId());
                        dInfo.setName(g.getName());
                        dInfo.setBipc(g.getBipc());
                        dInfo.setLogo(g.getLogo());
                        dInfo.setControlType(DeviceInfo.ControlTypeMenu.group.value());
                        dInfo.setAcceptMessage(1);
                        deviceLists.add(dInfo);
                    }
                }
                sortType = dcsp.getString(DataCenterSharedPreferences.Constant.SHOW_DLISTSORT, "zhineng").equals("zhineng") ? 0 : 1;
                String ordersql = "";
                if (sortType == 0) {
                    ordersql = "order by device_lasttime desc";
                } else {
                    ordersql = "order by sort desc";
                }
                Cursor cursor = DatabaseOperator.getInstance(mContext).getReadableDatabase().rawQuery(
                        "select * from DEVICE_STATUSINFO where zj_id = ? " + ordersql,
                        new String[]{String.valueOf(zhuji.getId())});
                if (cursor != null && cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        DeviceInfo deviceInfo = DatabaseOperator.getInstance(mContext)
                                .buildDeviceInfo(cursor);
                        if (!DeviceInfo.ControlTypeMenu.group.value().equals(deviceInfo.getControlType())
                                && !DeviceInfo.ControlTypeMenu.zhuji.value().equals(deviceInfo.getControlType())) {
                            deviceCount++;
                        }
                        if ("zhuji_fmq".equals(deviceInfo.getCa())) {
                            continue;
                        }
                        if (DatabaseOperator.getInstance(mContext).isInGroup(deviceInfo)) {
                            if(!Actions.VersionType.CHANNEL_HCTZ.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
                              continue;
                            }
                        }
                        if ("shexiangtou".equals(deviceInfo.getControlType())) {
                            shexiangtou = deviceInfo;
                            if (shexiangtou.getIpc() != null) {
                                JSONArray array = JSONArray.parseArray(shexiangtou.getIpc());

                                if (array != null) {
                                    shexiangtou.setStatus(0); // 显示指令
                                    shexiangtou.setLastCommand(
                                            array.size() + getString(R.string.deviceslist_camera_count));// 显示摄像头个数
                                }
                            }
                            continue;
                        } else if (deviceInfo.getControlType().equals(DeviceInfo.ControlTypeMenu.wenshiduji.value())
                                | deviceInfo.getControlType().equals(DeviceInfo.ControlTypeMenu.wenduji.value())) {
                            List<CommandInfo> commandInfos = DatabaseOperator.getInstance(mContext).queryAllCommands(deviceInfo.getId());
                            if (commandInfos != null && commandInfos.size() > 0) {
                                String c = "";
                                for (int j = 0; j < commandInfos.size(); j++) {
                                    if (commandInfos.get(j).getCtype().equals(CommandInfo.CommandTypeEnum.temperature.value())) {
                                        c = new DecimalFormat("0.0").format(Double.parseDouble(commandInfos.get(j).getCommand())) + "℃";
                                        deviceInfo.setLastUpdateTime(commandInfos.get(j).getCtime());
                                        break;
                                    }
                                }
                                for (int j = 0; j < commandInfos.size(); j++) {
                                    if (commandInfos.get(j).getCtype().equals(CommandInfo.CommandTypeEnum.humidity.value())) {
                                        c = c + new DecimalFormat("0.0").format(Double.parseDouble(commandInfos.get(j).getCommand())) + "%";
                                        break;
                                    }
                                }
                                deviceInfo.setLastCommand(c);
                            }
                        } else if (DeviceInfo.CaMenu.hongwaizhuanfaqi.value().equals(deviceInfo.getCa())) {
                            String command = deviceInfo.getLastCommand();
                            String mode = "";
                            String temprature = "";
                            if (command != null && !TextUtils.isEmpty(command)) {
                                if (command.length() < 4) {
                                    if (command.equals("on")) {
                                        mode = getString(R.string.hwzf_mode_ar);
                                        temprature = "26";
                                    } else if (command.equals("off")) {
                                        mode = getString(R.string.hwzf_mode_off);
                                    } else if (command.equals("aa")) {
                                        mode = getString(R.string.hwzf_mode_aa);
                                    } else if (command.equals("aw")) {
                                        mode = getString(R.string.hwzf_mode_aw);
                                    } else if (command.equals("ad")) {
                                        mode = getString(R.string.hwzf_mode_ad);
                                    }
                                    if (command.equals("on")) {
                                        deviceInfo.setLastCommand(mode + temprature + "℃");
                                    } else {
                                        deviceInfo.setLastCommand(mode);
                                    }
                                } else {
                                    if (command.contains("ar")) {
                                        mode = getString(R.string.hwzf_mode_ar);
                                    } else {
                                        mode = getString(R.string.hwzf_mode_ah);
                                    }
                                    temprature = command.substring(2, command.length());
                                    deviceInfo.setLastCommand(mode + temprature + "℃");
                                }
                            }

                        }
                        if (sortType == 0) {
                            if (deviceInfo.getAcceptMessage() == 0) {
                                deviceList_close.add(deviceInfo);
                            } else {
                                deviceLists.add(deviceInfo);
                            }
                        } else {
                            deviceLists.add(deviceInfo);
                        }
                    }
                    // 摄像头必须放在遥控器定住的逻辑前面不然会出现崩溃
                    if (shexiangtou != null) {
                        deviceLists.add(1, shexiangtou);
                    }
                    // 使操作的遥控器定住
                    if (operationDevice != null
                            && operationDevice.getControlType().contains(DeviceInfo.ControlTypeMenu.xiaxing.value())) {
                        for (int k = 0; k < deviceLists.size(); k++) {
                            if (operationDevice.getId() == deviceLists.get(k).getId()
                                    && deviceLists.size() >= operationDevice.getwIndex()
                                    && operationDevice.getwIndex() != -1) {
                                DeviceInfo opDevice = deviceLists.get(k);
                                deviceLists.remove(k);
                                deviceLists.add(operationDevice.getwIndex(), opDevice);
                            }
                        }
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
                deviceLists.addAll(deviceList_close);
            }
            Message m = defHandler.obtainMessage(this.what);
            m.obj = deviceLists;
            defHandler.sendMessage(m);
        }
    }
}
