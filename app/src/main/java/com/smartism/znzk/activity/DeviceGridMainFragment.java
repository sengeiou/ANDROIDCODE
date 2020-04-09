package com.smartism.znzk.activity;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.alert.ChooseAudioSettingMode;
import com.smartism.znzk.activity.camera.MainActivity;
import com.smartism.znzk.activity.common.SettingActivity;
import com.smartism.znzk.activity.device.DeviceDetailActivity;
import com.smartism.znzk.activity.device.DeviceInfoActivity;
import com.smartism.znzk.activity.device.DeviceMainActivity;
import com.smartism.znzk.activity.device.DeviceSetGSMPhoneActivity;
import com.smartism.znzk.activity.device.GroupInfoActivity;
import com.smartism.znzk.activity.device.ZhujiListFragment;
import com.smartism.znzk.activity.device.add.AddDeviceChooseActivity;
import com.smartism.znzk.activity.device.add.AddZhujiActivity;
import com.smartism.znzk.activity.scene.SceneActivity;
import com.smartism.znzk.activity.user.UserInfoActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.communication.service.AudioTipsService;
import com.smartism.znzk.communication.service.CoreService;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.GroupInfo;
import com.smartism.znzk.domain.ImageBannerInfo;
import com.smartism.znzk.domain.WeightUserInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.domain.camera.Account;
import com.smartism.znzk.domain.camera.CameraInfo;
import com.smartism.znzk.global.AccountPersist;
import com.smartism.znzk.global.AppConfig;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.global.FList;
import com.smartism.znzk.global.NpcCommon;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.LogUtil;
import com.smartism.znzk.util.NetworkUtils;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.util.camera.T;
import com.smartism.znzk.util.camera.Utils;
import com.smartism.znzk.util.camera.WifiUtils;
import com.smartism.znzk.view.TextViewAutoVerticalScroll;
import com.smartism.znzk.view.BadgeView;
import com.smartism.znzk.view.CheckSwitchButton;
import com.smartism.znzk.view.DevicesMenuPopupWindow;
import com.smartism.znzk.view.GridViewWithHeaderAndFooter;
import com.smartism.znzk.view.TextViewAutoHorizontalScroll;
import com.smartism.znzk.view.alertview.AlertView;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;
import com.youth.banner.listener.OnBannerListener;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by win7 on 2016/11/5.
 */
public class DeviceGridMainFragment extends Fragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    public static final String TAG = DeviceGridMainFragment.class.getSimpleName();
    private final int dHandlerWhat_initsuccess = 1,
            dHandlerWhat_loadsuccess = 2,
            dHandlerWhat_serverupdatetimeout = 3,
            dHandlerWhat_deletesuccess = 10,
            dHandler_timeout = 4,
            dHandler_timerc = 5,
            dHander_refresh = 6,
            dHandler_scenes = 7,
            dHandler_ipclogin = 8,
            dHandler_panic = 9,
            dHandler_weightInfo = 11,
            dHandler_initContast = 12,
            dHandler_startdiactivity = 13,
            dHandler_notice = 14,
            dHandler_xyjInfo = 15,
            dHandler_image_banner = 16;
    private String old_refulsh_device_id = "";

    private DeviceMainActivity mContext;
    private ZhujiInfo zhuji;
    private int sortType;
    private int deviceCount;
    private DeviceInfo operationDevice;
    private List<DeviceInfo> deviceInfos;
    //    private GridView deviceListView;
    private GridViewWithHeaderAndFooter deviceListView;
    private DeviceAdapter deviceAdapter;
    private DevicesMenuPopupWindow itemMenu;
    private Intent deviceIntent = null;
    private View listViewFooterView;
    private View listViewHeadView;
    private boolean checkUpdate = true;
    private boolean autoShowAddZhuji = true;
    private String[] adv;
    private int number = 0;
    private boolean initSuccess = false;
    // 下拉刷新控件
    private SwipeRefreshLayout mRefreshLayout;
    private TextViewAutoVerticalScroll textview_auto_roll;

    private LinearLayout ll_device_main, device_grid_heanview;
    private List<ImageBannerInfo.ImageBannerBean> bannerBeanList;

    // 显示图片的配置
    DisplayImageOptions options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.loading)
            .showImageOnFail(R.drawable.sorrow).cacheInMemory(true).cacheOnDisc(true)
            .bitmapConfig(Bitmap.Config.RGB_565).imageScaleType(ImageScaleType.EXACTLY_STRETCHED)// 设置图片以如何的编码方式显示
            .resetViewBeforeLoading(true)// 设置图片在下载前是否重置，复位
            // .displayer(new RoundedBitmapDisplayer(20))//是否设置为圆角，弧度为多少
            .displayer(new FadeInBitmapDisplayer(100))// 是否图片加载好后渐入的动画时间
            .build();
    // logo图片的配置
    DisplayImageOptions options_userlogo = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.loading)
            .showImageOnFail(R.drawable.sorrow).cacheInMemory(true).cacheOnDisc(true)
            .bitmapConfig(Bitmap.Config.RGB_565).imageScaleType(ImageScaleType.EXACTLY_STRETCHED)// 设置图片以如何的编码方式显示
            .resetViewBeforeLoading(true)// 设置图片在下载前是否重置，复位
            .displayer(new RoundedBitmapDisplayer(40))// 是否设置为圆角，弧度为多少
            .displayer(new FadeInBitmapDisplayer(100))// 是否图片加载好后渐入的动画时间
            .build();

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case dHandlerWhat_initsuccess: //页面创建,加载本地数据库的数据显示,并启动coreservice去连接服务器拉去最新的信息
                    deviceCount = msg.arg1;
                    deviceInfos.clear();
                    deviceInfos.addAll((List<DeviceInfo>) msg.obj);
//                    LogUtil.i(TAG, "设备列表，初始化数据集合大小为:" + deviceInfos.size());
                    deviceAdapter.notifyDataSetChanged();
                    try{
                        // 启动管理服务器连接的服务
                        Intent intent = new Intent();
                        intent.setClass(mContext.getApplicationContext(), CoreService.class);
                        mContext.startService(intent);
                    }catch(Exception ex){
                        //oppo 手机有些会调用异常
                        Toast.makeText(mContext,"service start failed",Toast.LENGTH_SHORT).show();
                    }
                    // cancelInProgress();
                    defaultHandler.sendEmptyMessageDelayed(dHandler_timerc, 60000);
                    mContext.setZhuji(zhuji);
                    mContext.menuWindow.updateMenu(mContext.dcsp, zhuji);
                    mContext.initLeftMenu();
                    break;
                case dHandlerWhat_loadsuccess: //页面刷新获取加载完成,包括变更刷新,推送刷新、去服务器拉取刷新等。
                    deviceCount = msg.arg1;
                    mContext.cancelInProgress();
                    //下拉刷新超时 和 关闭下拉刷新效果
                    defaultHandler.removeMessages(dHander_refresh);
                    mRefreshLayout.setRefreshing(false);
                    defaultHandler.removeMessages(dHandler_timeout);
                    deviceInfos.clear();
                    deviceInfos.addAll((List<DeviceInfo>) msg.obj);

                    // EventBus.getDefault().post(deviceInfos);
                    deviceAdapter.notifyDataSetChanged();
                    Intent intent = null;
                    try{
                        // 启动管理服务器连接的服务 已经启动则不会再次启动
                        intent = new Intent();
                        intent.setClass(mContext.getApplicationContext(), CoreService.class);
                        mContext.startService(intent);
                    }catch(Exception ex){
                        //oppo 手机有些会调用异常
                        Toast.makeText(mContext,"service start failed",Toast.LENGTH_SHORT).show();
                    }
                    if (zhuji == null && autoShowAddZhuji) {
                        autoShowAddZhuji = false;
                        intent.setClass(mContext.getApplicationContext(), AddZhujiActivity.class);
                        startActivity(intent);
                    }
                    if (zhuji != null) {
                        autoShowAddZhuji = true;
                    }
                    mContext.setZhuji(zhuji);
                    mContext.menuWindow.updateMenu(mContext.dcsp, zhuji);
                    mContext.initLeftMenu();
                    // 刷新服务器数据后提醒主机更新
                    if (checkUpdate) {
                        checkUpdate = false;
                        if (zhuji != null) {
                            if (zhuji.isOnline()) {
                                SyncMessageContainer.getInstance().sendMessageToServer(SyncMessage.CommandMenu.rq_checkpudate,
                                        SyncMessage.CodeMenu.zero, zhuji.getId(), null);
                            }
                        }
                    }
                    break;
                case dHandlerWhat_serverupdatetimeout:
                    mContext.cancelInProgress();
                    Toast.makeText(mContext.getApplicationContext(), getString(R.string.deviceslist_server_updating_timeout),
                            Toast.LENGTH_SHORT).show();
                    break;
                case dHandlerWhat_deletesuccess: // 删除成功
                    refreshData();
                    break;
                case dHandler_timeout:
                    defaultHandler.removeMessages(dHandler_timeout);
                    mContext.cancelInProgress();
                    Toast.makeText(mContext.getApplicationContext(), getString(R.string.timeout), Toast.LENGTH_SHORT).show();
                    break;
                case dHandler_timerc: // 1分钟执行一次，检查一键上行设备是否正常。
                    for (DeviceInfo info : deviceInfos) {
                        if (DeviceInfo.ControlTypeMenu.shangxing_1.value().equals(info.getControlType())) {
                            if (!"".equals(info.getLastUpdateTime())) {
                                if (info.getLastUpdateTime() < System.currentTimeMillis() - 12 * 60 * 60000) { // 指令超过12小时没有持续的包下来则表示正常了
                                    info.setLastUpdateTime(0);
                                    info.setLastCommand(getString(R.string.deviceslist_server_item_normal));
                                }
                            }
                        }
                    }
                    defaultHandler.sendEmptyMessageDelayed(dHandler_timerc, 60000);
                    break;
                case dHander_refresh: // 刷新超时

                    mRefreshLayout.setRefreshing(false);
                    Toast.makeText(mContext.getApplicationContext(), getString(R.string.timeout), Toast.LENGTH_SHORT).show();
                    break;
                case dHandler_scenes: // 场景操作完成
                    mContext.cancelInProgress();
                    break;
                case dHandler_panic: // panic操作完成
                    mContext.cancelInProgress();
                    break;
                case dHandler_ipclogin:// IPC登录完成
                    mContext.cancelInProgress();
                    intent = new Intent();
                    intent.setClass(mContext, MainActivity.class);
                    DeviceInfo device = (DeviceInfo) msg.obj;
                    intent.putExtra("device", device);
//                    Log.i(TAG, "打开摄像头：" + device.getIpc());
                    startActivity(intent);
                    break;
                case dHandler_weightInfo:
                    mContext.cancelInProgress();
                    intent = new Intent();
                    final DeviceInfo info = (DeviceInfo) msg.obj;
//                    intent.setClass(mContext, WeightPrimaryActivity.class);
                    intent.putExtra("device", info);
                    startActivity(intent);
                    break;
                case dHandler_xyjInfo:
                    mContext.cancelInProgress();
                    intent = new Intent();
                    final DeviceInfo info1 = (DeviceInfo) msg.obj;
//                    intent.setClass(mContext, XYJPrimaryActivity.class);
                    intent.putExtra("device", info1);
                    startActivity(intent);
                    break;
                case dHandler_initContast:
                    if (deviceIntent != null) {
//                        P2PHandler.getInstance().p2pInit(mContext, new P2PListener(), new SettingListener());
                        NpcCommon.verifyNetwork(mContext);
                        connect();
                        Contact contact = (Contact) msg.obj;
                        deviceIntent.putExtra("contact", contact);
                        deviceIntent.putExtra("connectType", Constants.ConnectType.P2PCONNECT);
                    }
                    startActivity(deviceIntent);
                    break;
                case dHandler_notice:
                    Map<Long, String> map = new HashMap<>();
                    map = (Map<Long, String>) msg.obj;
                    if (map != null && map.size() > 0) {
                        Iterator i = map.entrySet().iterator();

                        List<String> list = new ArrayList<>();
                        final List<Long> idList = new ArrayList<>();
                        while (i.hasNext()) {
                            Map.Entry entry = (Map.Entry) i.next();
                            long id = (long) entry.getKey();
                            String title = (String) entry.getValue();
                            list.add(title);
                            idList.add(id);
                        }
                        adv = list.toArray(new String[list.size()]);
                        textview_auto_roll.setText(adv[0]);
                        ll_notice.setVisibility(View.VISIBLE);
                        iv_notice.setVisibility(View.VISIBLE);
                        textview_auto_roll.setVisibility(View.VISIBLE);
                        textview_auto_roll.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(mContext, CommonWebViewActivity.class);
                                intent.putExtra("title", adv[number % adv.length]);
                                intent.putExtra("url", mContext.dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "")
                                        + "/jdm/notice?nid=" + idList.get((number % adv.length)) + "&lang=" + Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry());
                                startActivity(intent);

                            }
                        });
                        if (adv.length > 1) {
                            defaultHandler.sendEmptyMessageDelayed(99, 3000);
                        }
                    }
                    break;
                case 99:
                    textview_auto_roll.next();
                    number++;
                    textview_auto_roll.setText(adv[number % adv.length]);
                    defaultHandler.sendEmptyMessageDelayed(99, 3000);
                    break;
                case dHandler_image_banner:
                    bannerBeanList = new ArrayList<>();
                    JSONArray array = JSON.parseArray((String) msg.obj);
                    if (array != null && array.size() > 0) {
                        for (int i = 0; i < array.size(); i++) {
                            JSONObject object = array.getJSONObject(i);
                            ImageBannerInfo.ImageBannerBean bean = new ImageBannerInfo.ImageBannerBean();
                            bean.setContent(object.getString("content"));
                            bean.setLang(object.getString("lang"));
                            bean.setName(object.getString("name"));
                            bean.setUrl(object.getString("url"));
                            bean.setUrlType(object.getString("urlType"));
                            bannerBeanList.add(bean);
                        }
                        initBanner(bannerBeanList);
                        Log.e("ImageBanner :", bannerBeanList.toString());
                    }

                    break;
                default:
                    break;
            }
            return false;
        }
    };
    private Handler defaultHandler = new WeakRefHandler(mCallback);

    private boolean isFSNY;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = (DeviceMainActivity) getActivity();
        deviceInfos = new ArrayList<>();
        // 初始化右边菜单
        itemMenu = new DevicesMenuPopupWindow(mContext, this);
        initData();

    }

    private void initData() {
        JavaThreadPool.getInstance().excute(new loadAllDevicesInfo(dHandlerWhat_initsuccess));
        JavaThreadPool.getInstance().excute(new Runnable() {
            public void run() {
                String server = mContext.dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                try {
                    String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/notice/list", null, mContext);

                    if (result != null && !"".equals(result) && !"null".equals(result) && !"0".equals(result) && result.length() > 4) {
                        JSONArray jsonArray = JSON.parseArray(result);
                        Map<Long, String> map = new HashMap<>();
                        ArrayList<Map<Long, String>> arrayList = new ArrayList<>();
                        for (int j = 0; j < jsonArray.size(); j++) {
                            JSONObject v = jsonArray.getJSONObject(j);
//                            noticeStr = noticeStr + v.getString("title") + " , ";
                            map.put(v.getLong("id").longValue(), v.getString("title"));
                        }
                        Message notice = defaultHandler.obtainMessage(dHandler_notice);
                        notice.obj = map;
                        defaultHandler.sendMessage(notice);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        JavaThreadPool.getInstance().excute(new GetBannerImage());
    }

    @Override
    public void onResume() {
        super.onResume();

//        mContext.setTitle(mContext.dcsp.getString(DataCenterSharedPreferences.Constant.LOGIN_APPNAME, ""));
//        user_name.setText(mContext.dcsp.getString(DataCenterSharedPreferences.Constant.LOGIN_APPNAME, ""));
//        hometitle.setText(
//                dcsp.getString(DataCenterSharedPreferences.Constant.LOGIN_APPNAME, "") + getString(R.string.deviceslist_server_leftmenu_dtitle));

        initRegisterReceiver();
//        NotificationUtil.cancelNotification(mContext.getApplicationContext(), DataCenterSharedPreferences.Constant.NOTIFICATIONID);
        if (initSuccess) {
            operationDevice = null;
            refreshData();
        }
        initSuccess = true; //以前放在dHandlerWhat_initsuccess初始化完成中,发现这个肯定是可以执行完成的,第一次不用刷新,后面才要刷新
        checkAlertAudio();
        if (itemMenu.isShowing()) {
            itemMenu.dismiss();
        }
        mContext.menuWindow.updateMenu(mContext.dcsp, zhuji);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (defaultReceiver != null) {
            mContext.unregisterReceiver(defaultReceiver);
        }
    }

    @Override
    public void onRefresh() {
        // 下拉刷新 发送刷新指令
        if (NetworkUtils.CheckNetwork(mContext)) {
            SyncMessageContainer.getInstance().produceSendMessage(new SyncMessage(SyncMessage.CommandMenu.rq_refresh));
            defaultHandler.sendEmptyMessageDelayed(dHander_refresh, 5000);
        } else {
            mRefreshLayout.setRefreshing(false);
            Toast.makeText(mContext.getApplicationContext(), getString(R.string.net_error_nonet), Toast.LENGTH_SHORT).show();
        }
    }

    View view;
    private LinearLayout ll_notice;
    private ImageView iv_notice;
    private Banner banner;
//    List<Integer> images = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_device_gridmain, container, false);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
//        defaultHandler.sendEmptyMessage(0);
        deviceListView = (GridViewWithHeaderAndFooter) view.findViewById(R.id.dv);
        listViewFooterView = LayoutInflater.from(mContext).inflate(R.layout.add_device_footerview, null);

        banner = (Banner) view.findViewById(R.id.banner);
        ll_notice = (LinearLayout) view.findViewById(R.id.ll_notice);
        iv_notice = (ImageView) view.findViewById(R.id.iv_notice);
        textview_auto_roll = (TextViewAutoVerticalScroll) view.findViewById(R.id.textview_auto_roll);

        listViewHeadView = LayoutInflater.from(mContext).inflate(R.layout.activity_devices_list_item_nonet, null);
        listViewFooterView.findViewById(R.id.add_device_foot).setOnClickListener(this);
        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh_ly);
        ll_device_main = (LinearLayout) view.findViewById(R.id.ll_device_main);
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setColorSchemeResources(R.color.green, R.color.green, R.color.green, R.color.green);
        deviceAdapter = new DeviceAdapter(mContext);
        deviceListView.addHeaderView(listViewHeadView);
        deviceListView.addFooterView(listViewFooterView);
        deviceListView.setAdapter(deviceAdapter);
//        initBanner();
        initViewEvent();
    }

    class GetBannerImage implements Runnable {
        @Override
        public void run() {

            JSONObject o = new JSONObject();
            o.put("key", "home_bottom");
            String server = mContext.dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");

            String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/ad/list", o, mContext);
            if ("0".equals(result)) {
                defaultHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mContext.cancelInProgress();
//                        Toast.makeText(mContext, getString(R.string.net_error_nodata),
//                                Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (!TextUtils.isEmpty(result) && result.length() > 4) {
                Message message = defaultHandler.obtainMessage(dHandler_image_banner);
                message.obj = result;
                defaultHandler.sendMessage(message);
            }
        }
    }

    private void initBanner(List<ImageBannerInfo.ImageBannerBean> list) {
        List<String> images = new ArrayList<>();
        //设置banner样式
        banner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR);
        //设置图片加载器
        banner.setImageLoader(new GlideImageLoader());
        int width = getActivity().getResources().getDisplayMetrics().widthPixels;
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, width / 4);
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        banner.setLayoutParams(lp);
        //设置图片集合
//        images.add(R.drawable.image_viewpager_test1);
//        images.add(R.drawable.image_viewpager_test2);
//        images.add(R.drawable.image_viewpager_test3);
//        images.add(R.drawable.image_viewpager_test4);
//        images.add(R.drawable.image_viewpager_test5);
//        images.add(R.drawable.image_viewpager_test6);
//        Integer[] images = {R.drawable.image_viewpager_test1,R.drawable.image_viewpager_test2,R.drawable.image_viewpager_test3,
//                R.drawable.image_viewpager_test4,R.drawable.image_viewpager_test5,R.drawable.image_viewpager_test6};

        for (ImageBannerInfo.ImageBannerBean bean : bannerBeanList) {
            images.add(bean.getContent());
        }
        banner.setImages(images);
        //设置banner动画效果
        banner.setBannerAnimation(Transformer.Default);
        //设置标题集合（当banner样式有显示title时）
//        List<String> titles = new ArrayList<>();
//        titles.add("1");
//        titles.add("2");
//        titles.add("3");
//        titles.add("4");
//        titles.add("5");
//        titles.add("6");
//        banner.setBannerTitles(titles);
        //设置自动轮播，默认为true
        banner.isAutoPlay(true);
        //设置轮播时间
        banner.setDelayTime(15000);

        banner.setOnBannerListener(new OnBannerListener() {
            @Override
            public void OnBannerClick(int position) {
                Intent intent = new Intent(getActivity(), CommonWebViewActivity.class);
                intent.putExtra("bannerBean", bannerBeanList.get(position));
                startActivity(intent);
            }
        });

        //设置指示器位置（当banner模式中有指示器时）
        banner.setIndicatorGravity(BannerConfig.RIGHT);
        //banner设置方法全部调用完毕时最后调用
        banner.start();
    }

    public class GlideImageLoader extends com.youth.banner.loader.ImageLoader {

        @Override
        public void displayImage(Context context, Object path, ImageView imageView) {
            Glide.with(context).load(path).into(imageView);
        }
    }

    /**
     * 获取服务器上的设备信息
     */
    class loadAllDevicesInfo implements Runnable {
        private int what;

        public loadAllDevicesInfo() {
        }

        public loadAllDevicesInfo(int what) {
            this.what = what;
        }

        @Override
        public void run() {
            int count = 0;
            if (mContext.dcsp != null) {
//                String masterID = mContext.dcsp.getString(DataCenterSharedPreferences.Constant.APP_MASTERID, "");
                String masterID = ZhujiListFragment.getMasterId();
                zhuji = DatabaseOperator.getInstance(mContext)
                        .queryDeviceZhuJiInfo(masterID);
            }
            if (zhuji == null) {
                List<ZhujiInfo> tmpzhuji = DatabaseOperator.getInstance(mContext).queryAllZhuJiInfos();
                if (!tmpzhuji.isEmpty()) {
                    zhuji = tmpzhuji.get(0);
//                    mContext.dcsp.putString(DataCenterSharedPreferences.Constant.APP_MASTERID, zhuji.getMasterid()).commit();
                    ZhujiListFragment.setMasterId(zhuji.getMasterid());
                }
            }
            DeviceInfo shexiangtou = null;
            List<DeviceInfo> deviceList = new ArrayList<DeviceInfo>();
            List<DeviceInfo> deviceList_close = new ArrayList<DeviceInfo>();
            if (zhuji != null) {
                if (mContext.dcsp.getBoolean(DataCenterSharedPreferences.Constant.SHOW_ZHUJI, true)) {
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
                    deviceInfo.setLowb(zhuji.getBatteryStatus() == 1);//是否底电
                    deviceList.add(deviceInfo); // 主机实例化一个对象来代替
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
                        deviceList.add(dInfo);
                    }
                }
                sortType = mContext.dcsp.getString(DataCenterSharedPreferences.Constant.SHOW_DLISTSORT, "zhineng").equals("zhineng") ? 0 : 1;
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
                                && !DeviceInfo.ControlTypeMenu.zhuji.value().equals(deviceInfo.getControlType())
                                && !DeviceInfo.CaMenu.zhujifmq.value().equals(deviceInfo.getCa())) {
                            count++;
                        }
                        if ("zhuji_fmq".equals(deviceInfo.getCa())) {
                            continue;
                        }
                        if (DatabaseOperator.getInstance(mContext).isInGroup(deviceInfo)) {
                            continue;
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
                        } else if ("qwq".equals(deviceInfo.getCa())) {
                            List<CommandInfo> commandInfos = DatabaseOperator.getInstance(mContext).queryAllCommands(deviceInfo.getId());
                            String c = getString(R.string.qwq_battry_normal);
                            String d = "100";
                            if (commandInfos != null && commandInfos.size() > 0) {

                                for (int i = 0; i < commandInfos.size(); i++) {
                                    if (commandInfos.get(i).getCtype() .equals( CommandInfo.CommandTypeEnum.liquidMargin.value())) {
                                        c = commandInfos.get(i).getCommand();
                                        if (Double.parseDouble(c) == 1.0) {
                                            c = getString(R.string.qwq_battry_low);
                                        } else {
                                            c = getString(R.string.qwq_battry_normal);
                                        }
                                        deviceInfo.setLastUpdateTime(commandInfos.get(i).getCtime());
                                    } else if (commandInfos.get(i).getCtype() .equals( CommandInfo.CommandTypeEnum.battery.value())) {
                                        d = commandInfos.get(i).getCommand();
                                        deviceInfo.setLastUpdateTime(commandInfos.get(i).getCtime());
                                    }
                                }
                            }
                            deviceInfo.setLastCommand(d + " " + c);
                        } else if (deviceInfo.getControlType().equals(DeviceInfo.ControlTypeMenu.wenshiduji.value())
                                | deviceInfo.getControlType().equals(DeviceInfo.ControlTypeMenu.wenduji.value())) {
                            List<CommandInfo> commandInfos = DatabaseOperator.getInstance(mContext).queryAllCommands(deviceInfo.getId());
                            if (commandInfos != null && commandInfos.size() > 0) {
                                String c = "";
                                for (int j = 0; j < commandInfos.size(); j++) {
                                    if (commandInfos.get(j).getCtype() .equals( CommandInfo.CommandTypeEnum.temperature.value())) {
                                        c = commandInfos.get(j).getCommand() + "℃";
                                        deviceInfo.setLastUpdateTime(commandInfos.get(j).getCtime());
                                        break;
                                    }
                                }
                                for (int j = 0; j < commandInfos.size(); j++) {
                                    if (commandInfos.get(j).getCtype() .equals( CommandInfo.CommandTypeEnum.humidity.value())) {
                                        c = c + commandInfos.get(j).getCommand() + "%";
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
                                deviceList.add(deviceInfo);
                            }
                        } else {
                            deviceList.add(deviceInfo);
                        }
                    }
                    // 摄像头必须放在遥控器定住的逻辑前面不然会出现崩溃
                    if (shexiangtou != null) {
                        deviceList.add(1, shexiangtou);
                    }
                    // 使操作的遥控器定住
                    if (operationDevice != null
                            && operationDevice.getControlType().contains(DeviceInfo.ControlTypeMenu.xiaxing.value())) {
                        for (int k = 0; k < deviceList.size(); k++) {
                            if (operationDevice.getId() == deviceList.get(k).getId()
                                    && deviceList.size() >= operationDevice.getwIndex()
                                    && operationDevice.getwIndex() != -1) {
                                DeviceInfo opDevice = deviceList.get(k);
                                deviceList.remove(k);
                                deviceList.add(operationDevice.getwIndex(), opDevice);
                            }
                        }
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
                deviceList.addAll(deviceList_close);
            }
            Message m = defaultHandler.obtainMessage(this.what);
            m.obj = deviceList;
            m.arg1 = count;
            defaultHandler.sendMessage(m);
        }
    }

    /**
     * 设备列表适配器
     */
    class DeviceAdapter extends BaseAdapter {
        /**
         * 视图内部类
         *
         * @author Administrator
         */
        class DeviceInfoView {
            ImageView low, power, ioc, ioc_wendu, ioc_shidu, ioc_showright, ioc_arming, ioc_disarming, ioc_home, ioc_panic;
            TextView mode, name, time, command, command_shidu, type, type_left, type_right;
            ImageButton button;
            CheckSwitchButton switchButton;
            RelativeLayout device_item_layout, comm_layout;
            LinearLayout cLayout, rLayout, n_layout;
            LinearLayout sceneLayout;
            BadgeView badgeView;
            LinearLayout ucTech;
        }

        LayoutInflater layoutInflater;

        public DeviceAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
//            return deviceInfos.size()+1;
            return deviceInfos.size();
        }

        @Override
        public Object getItem(int arg0) {
            if (arg0 == deviceInfos.size()) {
                return new DeviceInfo();
            }
            return deviceInfos.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        // uctech=================第一个条目不可点击
        // @Override
        // public boolean areAllItemsEnabled() {
        // return false;
        // }
        //
        // @Override
        // public boolean isEnabled(int position) {
        // if (position == 0)
        // return false;
        // else
        // return true;
        // }

        // uctech=================第一个条目不可点击

        /**
         * 返回一个view视图，填充gridview的item
         */
        @SuppressLint("NewApi")
        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            DeviceInfoView viewCache = null;
            if (view == null) {
                viewCache = new DeviceInfoView();
                view = layoutInflater.inflate(R.layout.activity_devices_grid_item, null);
                viewCache.device_item_layout = (RelativeLayout) view.findViewById(R.id.device_item_layout);
                viewCache.comm_layout = (RelativeLayout) view.findViewById(R.id.comm_layout);
                viewCache.low = (ImageView) view.findViewById(R.id.device_low);
                viewCache.power = (ImageView) view.findViewById(R.id.device_power);
                viewCache.ioc = (ImageView) view.findViewById(R.id.device_logo);
                viewCache.badgeView = new BadgeView(mContext, viewCache.ioc);
                viewCache.badgeView.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
                viewCache.badgeView.setTextSize(10);
                viewCache.ioc_wendu = (ImageView) view.findViewById(R.id.wendu_img);
                viewCache.ioc_shidu = (ImageView) view.findViewById(R.id.wendu_shidu_img);
                viewCache.mode = (TextView) view.findViewById(R.id.device_mode);
                viewCache.mode.setBackground(Util.createReadBgShapeDrawable(mContext));
                viewCache.name = (TextView) view.findViewById(R.id.device_name);
                viewCache.time = (TextView) view.findViewById(R.id.last_time);
                viewCache.type = (TextView) view.findViewById(R.id.device_type);
                viewCache.type_left = (TextView) view.findViewById(R.id.device_type_left);
                viewCache.type_right = (TextView) view.findViewById(R.id.device_type_right);
                viewCache.command = (TextViewAutoHorizontalScroll) view.findViewById(R.id.last_command);
                viewCache.command_shidu = (TextView) view.findViewById(R.id.last_command_shidu);
                viewCache.switchButton = (CheckSwitchButton) view.findViewById(R.id.c_switchButton);
                viewCache.button = (ImageButton) view.findViewById(R.id.c_one_button);
                viewCache.ioc_showright = (ImageView) view.findViewById(R.id.c_img);
                viewCache.rLayout = (LinearLayout) view.findViewById(R.id.r_layout);
                viewCache.n_layout = (LinearLayout) view.findViewById(R.id.n_layout);
                viewCache.cLayout = (LinearLayout) view.findViewById(R.id.c_layout);
                viewCache.sceneLayout = (LinearLayout) view.findViewById(R.id.scene_layout);
                viewCache.ioc_arming = (ImageView) view.findViewById(R.id.scene_arming);
                viewCache.ioc_disarming = (ImageView) view.findViewById(R.id.scene_disarming);
                viewCache.ioc_home = (ImageView) view.findViewById(R.id.scene_home);
                viewCache.ioc_panic = (ImageView) view.findViewById(R.id.scene_panic);
                // 天气预报 UCTECH 单独有的布局
                viewCache.ucTech = (LinearLayout) view.findViewById(R.id.ll_header_uctech);
                // if (metric.widthPixels <= 600) {
                // LayoutParams layoutParams = (LayoutParams)
                // viewCache.ioc_shidu.getLayoutParams();
                // layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                // layoutParams.addRule(RelativeLayout.BELOW,viewCache.ioc_wendu.getId());
                // viewCache.ioc_shidu.setLayoutParams(layoutParams);
                // layoutParams = (LayoutParams)
                // viewCache.command_shidu.getLayoutParams();
                // layoutParams.addRule(RelativeLayout.RIGHT_OF,viewCache.ioc_shidu.getId());
                // layoutParams.addRule(RelativeLayout.BELOW,viewCache.command.getId());
                // viewCache.command_shidu.setLayoutParams(layoutParams);
                // }
                view.setTag(viewCache);
            } else {
                viewCache = (DeviceInfoView) view.getTag();
            }
//            if (deviceInfos.size() == i) {
//                viewCache.ioc.setImageResource(R.drawable.add_icon);
//                viewCache.power.setVisibility(View.INVISIBLE);
//                viewCache.low.setVisibility(View.INVISIBLE);
//                viewCache.comm_layout.setVisibility(View.INVISIBLE);
//                viewCache.cLayout.setVisibility(View.INVISIBLE);
//                viewCache.rLayout.setVisibility(View.INVISIBLE);
//                viewCache.comm_layout.setVisibility(View.INVISIBLE);
//                viewCache.n_layout.setVisibility(View.INVISIBLE);
//                return view;
//            }
            if (deviceInfos.get(i) == null) {
                return view;
            }
            initButtonEvent(viewCache, i);
            setDeviceLogoAndName(viewCache, i);
            setCommand(viewCache, i);
            setShowOrHide(viewCache, i);
            setTypeAndBackground(viewCache, i);
            setBadeNumber(viewCache, i);
            setModen(viewCache, i);
            return view;
        }

        /**
         * 设置设备logo图片和名称
         *
         * @param
         */
        private void setDeviceLogoAndName(DeviceInfoView viewCache, int i) {
            if (i != 0 && ("qwq").equals(deviceInfos.get(i).getCa())) {
                ImageLoader.getInstance().displayImage(mContext.dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "")
                        + "/devicelogo/" + deviceInfos.get(i).getLogo(), viewCache.ioc, options, new MImageLoadingBar());
                viewCache.type_left.setVisibility(View.INVISIBLE);
                viewCache.type_right.setVisibility(View.INVISIBLE);
                viewCache.type.setVisibility(View.INVISIBLE);
                viewCache.name.setText(deviceInfos.get(i).getName());
            } else if (DeviceInfo.ControlTypeMenu.wenduji.value().equals(deviceInfos.get(i).getControlType())) {
                // 设置图片
                if (Actions.VersionType.CHANNEL_UCTECH.equals(((MainApplication) mContext.getApplication()).getAppGlobalConfig().getVersion())) {
                    try {
                        viewCache.ioc.setImageBitmap(BitmapFactory.decodeStream(
                                mContext.getAssets().open("uctech/uctech_t_" + deviceInfos.get(i).getChValue() + ".png")));
                    } catch (IOException e) {
                        Log.e("uctech", "读取图片文件错误");
                    }
                } else {
                    ImageLoader.getInstance()
                            .displayImage(mContext.dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "") + "/devicelogo/"
                                            + deviceInfos.get(i).getLogo(),
                                    viewCache.ioc, options, new MImageLoadingBar());
                }
                viewCache.name.setText(deviceInfos.get(i).getName() + "CH" + deviceInfos.get(i).getChValue());
            } else if (DeviceInfo.ControlTypeMenu.wenshiduji.value().equals(deviceInfos.get(i).getControlType())) {
                if (Actions.VersionType.CHANNEL_UCTECH.equals(((MainApplication) mContext.getApplication()).getAppGlobalConfig().getVersion())) {
                    try {
                        viewCache.ioc.setImageBitmap(BitmapFactory.decodeStream(
                                mContext.getAssets().open("uctech/uctech_th_" + deviceInfos.get(i).getChValue() + ".png")));
                    } catch (IOException e) {
                        Log.e("uctech", "读取图片文件错误");
                    }
                } else {
                    ImageLoader.getInstance()
                            .displayImage(mContext.dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "") + "/devicelogo/"
                                            + deviceInfos.get(i).getLogo(),
                                    viewCache.ioc, options, new MImageLoadingBar());
                }
                viewCache.name.setText(deviceInfos.get(i).getName() + "CH" + deviceInfos.get(i).getChValue());
            } else {
                // 设置图片
                ImageLoader.getInstance().displayImage( mContext.dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "")
                        + "/devicelogo/" + deviceInfos.get(i).getLogo(), viewCache.ioc, options, new MImageLoadingBar());
                viewCache.name.setText(deviceInfos.get(i).getName());
            }
        }

        /**
         * 设置指令和时间信息
         *
         * @param viewCache
         * @param i
         */
        private void setCommand(DeviceInfoView viewCache, int i) {


//            viewCache.time.setText(formatTime(deviceInfos.get(i).getLastUpdateTime()));
            if (i != 0 && Actions.VersionType.CHANNEL_LILESI
                    .equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                viewCache.command.setText("");
                viewCache.command_shidu.setVisibility(View.GONE);
                viewCache.ioc_shidu.setVisibility(View.GONE);
                viewCache.ioc_wendu.setVisibility(View.GONE);
                return;
            }
            if ("qwq".equals(deviceInfos.get(i).getCa())) {
                viewCache.ioc_wendu.setVisibility(View.GONE);
                viewCache.command_shidu.setVisibility(View.GONE);
                viewCache.ioc_shidu.setVisibility(View.GONE);
                viewCache.time.setText("");

                String commmand = deviceInfos.get(i).getLastCommand();
                //100 正常

                String[] aa = commmand.split(" ");
//                String aaaaa = getString(R.string.qwq_dianlaing) + ":" + aa[0] + "% " + getString(R.string.qwq_yelaing) + ":" + aa[1];
//                SpannableStringBuilder builder = new SpannableStringBuilder(aaaaa);
//                //ForegroundColorSpan 为文字前景色，BackgroundColorSpan为文字背景色
//                ForegroundColorSpan greenspan = new ForegroundColorSpan(Color.GREEN);
//                ForegroundColorSpan redSpan = new ForegroundColorSpan(Color.RED);
//                ForegroundColorSpan span = new ForegroundColorSpan(Color.GREEN);

                String text1, text2;
                Html.fromHtml(getString(R.string.qwq_dianlaing) + ":" + "<font color='#ff0000'>" + aa[0] + "</font>");
                if (Double.parseDouble(aa[0]) > 10) {
                    text1 = "<font color='#00ff00'>" + aa[0] + "%" + "</font>";
                } else {
                    text1 = "<font color='#ff0000'>" + aa[0] + "%" + "</font>";
                }
                if (aa[1].equals(getString(R.string.qwq_battry_normal))) {
                    text2 = "<font color='#00ff00'>" + aa[1] + "</font>";

                } else {
                    text2 = "<font color='#ff0000'>" + aa[1] + "</font>";
                }

                viewCache.command.setText(Html.fromHtml(getString(R.string.qwq_dianlaing) + ":" + text1 + " " + getString(R.string.qwq_yelaing) + ":" + text2));

            } else if ("tzc".equals(deviceInfos.get(i).getCa())) {
                try {
                    String unitName = "";
                    double commandValue = Double.parseDouble(deviceInfos.get(i).getLastCommand());
                    if (deviceInfos.get(i).getDtype() .equals("2") ) {
                        if (mContext.dcsp.getString(DataCenterSharedPreferences.Constant.WEIGHT_UNIT, "gjin").equals("gjin")) {
                            unitName = "Kg";
                            viewCache.command.setText(commandValue + unitName);
                        } else {
                            unitName = "lb";
                            String value = String.valueOf(commandValue * 2.2046226);
                            viewCache.command.setText(value.substring(0, value.indexOf(".") + 2) + unitName);
                        }
                    }
                } catch (Exception e) {
                    viewCache.command.setText("0.0");
                }
            } else if (deviceInfos.get(i).getControlType().equals(DeviceInfo.ControlTypeMenu.wenshiduji.value())
                    | deviceInfos.get(i).getControlType().equals(DeviceInfo.ControlTypeMenu.wenduji.value())) {
                String command = deviceInfos.get(i).getLastCommand();
                if (command.contains("℃")) {
                    if (mContext.dcsp.getString(DataCenterSharedPreferences.Constant.SHOW_TEMPERATURE_UNIT, "ssd").equals("ssd")) {
                        viewCache.command.setText(command.substring(0, command.indexOf("℃") + 1));
                    } else if (mContext.dcsp.getString(DataCenterSharedPreferences.Constant.SHOW_TEMPERATURE_UNIT, "ssd").equals("hsd")) {
                        viewCache.command.setText(((float) Math
                                .round((Float.parseFloat(command.substring(0, command.indexOf("℃"))) * 1.8 + 32) * 10)
                                / 10) + "℉");
                    }
                    viewCache.ioc_wendu.setVisibility(View.VISIBLE);
                } else {
                    viewCache.ioc_wendu.setVisibility(View.GONE);
                }
                if (command.contains("%")) {
                    viewCache.command_shidu.setText(command.substring(command.indexOf("℃") + 1));
                    viewCache.command_shidu.setVisibility(View.VISIBLE);
                    viewCache.ioc_shidu.setVisibility(View.VISIBLE);
                } else {
                    viewCache.command_shidu.setVisibility(View.GONE);
                    viewCache.ioc_shidu.setVisibility(View.GONE);
                }
            } else if (deviceInfos.get(i).getControlType().equals(DeviceInfo.ControlTypeMenu.zhuji.value())) {
                viewCache.command.setText(zhuji.getUc() + " " + getString(R.string.deviceslist_server_totalonlineapps)
                        + "  " + deviceCount + " " + getString(R.string.deviceslist_server_totaldevices));
                viewCache.command_shidu.setVisibility(View.GONE);
                viewCache.ioc_shidu.setVisibility(View.GONE);
                viewCache.ioc_wendu.setVisibility(View.GONE);
            } else if (deviceInfos.get(i).getControlType().contains("shangxing")) {
                viewCache.command_shidu.setVisibility(View.GONE);
                viewCache.ioc_shidu.setVisibility(View.GONE);
                viewCache.ioc_wendu.setVisibility(View.GONE);
                if (deviceInfos.get(i).getStatus() == 0) {
                    viewCache.command.setText("".equals(deviceInfos.get(i).getLastCommand())
                            ? getString(R.string.deviceslist_server_item_normal) : deviceInfos.get(i).getLastCommand());
                } else if (deviceInfos.get(i).getStatus() == 1) { // 显示正常
                    viewCache.command.setText(getString(R.string.normal));
                    viewCache.time.setText("");
                }
            } else {
                // Log.e("点击", deviceInfos.get(i).getControlType());
                viewCache.command.setText("".equals(deviceInfos.get(i).getLastCommand())
                        ? getString(R.string.deviceslist_server_item_normal) : deviceInfos.get(i).getLastCommand());
                viewCache.command_shidu.setVisibility(View.GONE);
                viewCache.ioc_shidu.setVisibility(View.GONE);
                viewCache.ioc_wendu.setVisibility(View.GONE);
            }
        }

        private String formatTime(long time) { // 需要增加定时器晚上12点刷新一下
            String forString = "";
            if (time == 0) {
                return "";
            }
            int day = 0;
            Calendar calendar = Calendar.getInstance();
            day = calendar.get(Calendar.DAY_OF_YEAR);
            calendar.setTimeInMillis(time);
            int day_tmp = calendar.get(Calendar.DAY_OF_YEAR);
            switch (day - day_tmp) {
                case 0:
                    int m = calendar.get(Calendar.MINUTE);
                    if (m < 10) {
                        forString = calendar.get(Calendar.HOUR_OF_DAY) + ":0" + m;
                    } else {
                        forString = calendar.get(Calendar.HOUR_OF_DAY) + ":" + m;
                    }
                    break;
                case 1:
                    forString = getString(R.string.yesterday);
                    break;
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                    switch (calendar.get(Calendar.DAY_OF_WEEK)) {
                        case 1:
                            forString = getString(R.string.sunday);
                            break;
                        case 2:
                            forString = getString(R.string.monday);
                            break;
                        case 3:
                            forString = getString(R.string.tuesday);
                            break;
                        case 4:
                            forString = getString(R.string.wednesday);
                            break;
                        case 5:
                            forString = getString(R.string.thursday);
                            break;
                        case 6:
                            forString = getString(R.string.friday);
                            break;
                        case 7:
                            forString = getString(R.string.saturday);
                            break;
                        default:
                            break;
                    }
                    ;
                    break;
                default:
                    forString = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-"
                            + calendar.get(Calendar.DAY_OF_MONTH);
                    break;
            }
            return forString;
        }

        /**
         * 设置控件的显示也隐藏
         *
         * @param viewCache
         * @param i
         */
        private void setShowOrHide(DeviceInfoView viewCache, int i) {
            // 可控制的就和只接受的不一样
            if (deviceInfos.get(i).getControlType().contains("shangxing")
                    | deviceInfos.get(i).getControlType().equals("wenshiduji")
                    | deviceInfos.get(i).getControlType().equals("wenduji")
                    | deviceInfos.get(i).getControlType().contains("fangdiu")) {
                viewCache.cLayout.setVisibility(View.VISIBLE);
//                viewCache.ioc_showright.setVisibility(View.GONE);
//                viewCache.button.setVisibility(View.GONE);
//                viewCache.switchButton.setVisibility(View.VISIBLE);
//                viewCache.switchButton.changeButtonBg(mContext, R.drawable.checkswitch_bottom);
//                if (deviceInfos.get(i).getAcceptMessage() == 0) {
//                    viewCache.switchButton.setCheckedNotListener(false);
//                } else {
//                    viewCache.switchButton.setCheckedNotListener(true);
//                }
                viewCache.command.setVisibility(View.VISIBLE);
                viewCache.time.setVisibility(View.VISIBLE);
            } else {
                if (deviceInfos.get(i).getControlType().equals(DeviceInfo.ControlTypeMenu.xiaxing_1.value())) {
                    viewCache.cLayout.setVisibility(View.VISIBLE);
                    viewCache.ioc_showright.setVisibility(View.VISIBLE);
//                    viewCache.button.setVisibility(View.VISIBLE);
//                    viewCache.switchButton.setVisibility(View.GONE);
                    viewCache.command.setVisibility(View.VISIBLE);
                    viewCache.time.setVisibility(View.VISIBLE);
                    // 红外设备显示向右的箭头图标
                    if (deviceInfos.get(i).getCa().equals("hwzf")) {
                        viewCache.ioc_showright.setVisibility(View.VISIBLE);
                    }
                } else if (deviceInfos.get(i).getControlType().equals(DeviceInfo.ControlTypeMenu.xiaxing_2.value())) {
                    viewCache.cLayout.setVisibility(View.VISIBLE);
                    viewCache.ioc_showright.setVisibility(View.VISIBLE);
//                    viewCache.button.setVisibility(View.GONE);
//                    viewCache.switchButton.changeButtonBg(mContext,
//                            R.drawable.checkswitch_bottom_wenzi);
//                    viewCache.switchButton.setVisibility(View.VISIBLE);
//                    if (deviceInfos.get(i).getDr() == 1) {
//                        viewCache.switchButton.setCheckedNotListener(true);
//                    } else {
//                        viewCache.switchButton.setCheckedNotListener(false);
//                    }
                    viewCache.command.setVisibility(View.VISIBLE);
                    viewCache.time.setVisibility(View.VISIBLE);
                } else if (deviceInfos.get(i).getControlType().contains("xiaxing")) {
                    viewCache.cLayout.setVisibility(View.VISIBLE);
                    viewCache.ioc_showright.setVisibility(View.VISIBLE);
                    viewCache.button.setVisibility(View.GONE);
                    viewCache.switchButton.setVisibility(View.GONE);
                    viewCache.command.setVisibility(View.VISIBLE);
                    viewCache.time.setVisibility(View.VISIBLE);
                } else if (DeviceInfo.ControlTypeMenu.neiqian.value().equals(deviceInfos.get(i).getControlType())
                        || DeviceInfo.ControlTypeMenu.group.value().equals(deviceInfos.get(i).getControlType())) {
                    viewCache.cLayout.setVisibility(View.VISIBLE);
                    viewCache.ioc_showright.setVisibility(View.VISIBLE);
                    viewCache.button.setVisibility(View.GONE);
                    viewCache.switchButton.setVisibility(View.GONE);
                    viewCache.command.setVisibility(View.VISIBLE);
                    viewCache.time.setVisibility(View.INVISIBLE);
                    viewCache.command.setText(getResources().getString(R.string.activity_group_name));
                } else if (deviceInfos.get(i).getCak() != null && (deviceInfos.get(i).getCak().contains(DeviceInfo.CakMenu.health.value())
                        || deviceInfos.get(i).getCak().contains(DeviceInfo.CakMenu.detection.value()))) {
                    viewCache.switchButton.setVisibility(View.GONE);
                    viewCache.ioc_showright.setVisibility(View.VISIBLE);

                    viewCache.cLayout.setVisibility(View.VISIBLE);
                    viewCache.button.setVisibility(View.GONE);
                    viewCache.command.setVisibility(View.GONE);
                    viewCache.time.setVisibility(View.INVISIBLE);

                } else {// 只有主机了
                    viewCache.command.setVisibility(View.VISIBLE);
                    viewCache.cLayout.setVisibility(View.VISIBLE);
                    if (isFSNY) {
                        viewCache.time.setVisibility(View.INVISIBLE);
                    } else {
                        viewCache.time.setVisibility(View.VISIBLE);
                    }
                }
            }
            if (deviceInfos.get(i).isLowb()) { //低电
                viewCache.low.setVisibility(View.VISIBLE);
            } else {
                viewCache.low.setVisibility(View.GONE);
            }
            if (Actions.VersionType.CHANNEL_LILESI.equals(getJdmVersionType())) { //立乐斯显示电源状态
                if (DeviceInfo.ControlTypeMenu.zhuji.value().equals(deviceInfos.get(i).getControlType())) {
                    viewCache.power.setVisibility(View.VISIBLE);
                    if (deviceInfos.get(i).getPowerStatus() == 0) { //市电
                        viewCache.power.setImageResource(R.drawable.ic_power_normal);
                    } else { //电池
                        viewCache.power.setImageResource(R.drawable.ic_power_battery);
                    }
                } else {
                    viewCache.power.setVisibility(View.GONE);
                }
            }

            // 红外遥控不显示开关
            if (deviceInfos.get(i).getCa() != null) {
                if (deviceInfos.get(i).getCa().contentEquals("hwzf")) {
                    // Log.e("aaa", "第" + i + "个： " +
                    // deviceInfos.get(i).getCa());
                    viewCache.button.setVisibility(View.GONE);
                    viewCache.time.setVisibility(View.INVISIBLE);
                }
            }
        }

        /**
         * 设置控件的显示背景
         *
         * @param viewCache
         * @param i
         */
        private void setTypeAndBackground(DeviceInfoView viewCache, int i) {
            if (deviceInfos.get(i).getId() == zhuji.getId()) {
                viewCache.type
                        .setText((deviceInfos.get(i).getWhere() == null ? "" : (deviceInfos.get(i).getWhere() + " "))
                                + (zhuji.isOnline() ? getString(R.string.deviceslist_server_zhuji_online)
                                : getString(R.string.deviceslist_server_zhuji_offline)));

                if (zhuji.isOnline()) {
                    viewCache.device_item_layout.setBackgroundResource(R.drawable.device_item_click_bg);
                } else {
                    viewCache.device_item_layout.setBackgroundColor(Color.RED);
                }
                // 在线
                viewCache.type_left.setVisibility(View.GONE);
                viewCache.type_right.setVisibility(View.GONE);
            } else {
                // 主机是否在线
                if (!zhuji.isOnline()) { // 不在线
                    if (deviceInfos.get(i).getControlType().contains("xiaxing")) {
                        viewCache.button.setEnabled(false);
                        viewCache.switchButton.setEnabled(false);
                    } else {
                        viewCache.switchButton.setEnabled(true);
                    }
                } else {
                    viewCache.switchButton.setEnabled(true);
                    viewCache.button.setEnabled(true);
                    if (sortType == 0) { // 智能类型
                    }
                }
                if ("qwq".equals(deviceInfos.get(i).getCa())) {
                    deviceInfos.get(i).setWhere("");
                    deviceInfos.get(i).setType("");
                }
                if (!StringUtils.isEmpty(deviceInfos.get(i).getWhere())
                        || !StringUtils.isEmpty(deviceInfos.get(i).getType())) {
                    viewCache.type_left.setVisibility(View.VISIBLE);
                    viewCache.type_right.setVisibility(View.VISIBLE);
                    viewCache.type.setText(
                            (deviceInfos.get(i).getWhere() == null ? "" : (deviceInfos.get(i).getWhere() + " "))
                                    + deviceInfos.get(i).getType());
                } else {
                    viewCache.type_left.setVisibility(View.GONE);
                    viewCache.type_right.setVisibility(View.GONE);
                    viewCache.type.setText("");
                }
            }
        }


        /**
         * 初始化按钮点击事件
         *
         * @param viewCache
         */
        private void initButtonEvent(final DeviceInfoView viewCache, final int i) {

            // uctech头部天气预报点击事件
            // if (i == 0) {
            // viewCache.ucTech.findViewById(R.id.rl_windspeed).setOnClickListener(new
            // OnClickListener() {
            // @Override
            // public void onClick(View v) {
            // Log.e("aaa", "风速");
            // }
            // });
            // viewCache.ucTech.findViewById(R.id.rl_pressure).setOnClickListener(new
            // OnClickListener() {
            // @Override
            // public void onClick(View v) {
            // Log.e("aaa", "气压");
            // }
            // });
            // viewCache.ucTech.findViewById(R.id.rl_rain).setOnClickListener(new
            // OnClickListener() {
            // @Override
            // public void onClick(View v) {
            // Log.e("aaa", "雨量");
            // }
            // });
            // return;
            // }

            viewCache.button.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.e("aaa", "发送通知==");
                    if (Util.isFastClick()) {
                        Toast.makeText(mContext, getString(R.string.activity_devices_commandhistory_tip), Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("aaa", "发送通知==");
                        mContext.showInProgress(getString(R.string.operationing), false, false);
                        SyncMessage message = new SyncMessage();
                        message.setCommand(SyncMessage.CommandMenu.rq_control.value());
                        message.setDeviceid(deviceInfos.get(i).getId());
                        // 操作 这里的操作写死，需要在服务器的配置页面中配置此按钮对应的指令是什么
                        message.setSyncBytes(new byte[]{0x02});
                        SyncMessageContainer.getInstance().produceSendMessage(message);
                        operationDevice = deviceInfos.get(i);
                        operationDevice.setwIndex(i);
                    }
                }
            });
            viewCache.switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
//                    getWindow().setLocalFocus(false, false);
                    if (Util.isFastClick()) {
                        Toast.makeText(mContext, getString(R.string.activity_devices_commandhistory_tip), Toast.LENGTH_SHORT).show();
                    } else {
                        SyncMessage message = new SyncMessage();
                        message.setCommand(SyncMessage.CommandMenu.rq_control.value());
                        message.setDeviceid(deviceInfos.get(i).getId());

                        if (arg1) { // 开关操作
                            // 这里的操作写死，需要在服务器的配置页面中配置此按钮对应的指令是什么
                            // 开操作
                            Log.e("aaa", "发送通知===开指令");
                            message.setSyncBytes(new byte[]{0x01});
                        } else {
                            // 关操作
                            Log.e("aaa", "发送通知===关指令");
                            message.setSyncBytes(new byte[]{0x00});
                        }

                        // 点击后显示进度条
                        mContext.showInProgress(getString(R.string.operationing), false, false);
                        defaultHandler.sendEmptyMessageDelayed(dHandler_timeout, 8 * 1000);
                        SyncMessageContainer.getInstance().produceSendMessage(message);
                        operationDevice = deviceInfos.get(i);
                        operationDevice.setwIndex(i);
                    }
                }
            });

            if (Actions.VersionType.CHANNEL_JKD.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                //吉开达的三个按钮放在一起轮播
                viewCache.ioc_arming.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // 设防按钮点击进入撤防
                        mContext.showInProgress(getString(R.string.operationing), false, true);
                        JavaThreadPool.getInstance().excute(new TriggerScene(0));
                        showBtton(viewCache.ioc_disarming, viewCache);
//                        viewCache.ioc_arming.setVisibility(View.GONE);
//                        viewCache.ioc_disarming.setVisibility(View.VISIBLE);
                    }
                });
                viewCache.ioc_disarming.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // 撤防按钮点击进入在家
                        mContext.showInProgress(getString(R.string.operationing), false, true);
                        JavaThreadPool.getInstance().excute(new TriggerScene(-3));
                        showBtton(viewCache.ioc_home, viewCache);
//                        viewCache.ioc_disarming.setVisibility(View.GONE);
//                        viewCache.ioc_home.setVisibility(View.VISIBLE);
                    }
                });
                viewCache.ioc_home.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // 在家按钮点击进入设防
                        mContext.showInProgress(getString(R.string.operationing), false, true);
                        JavaThreadPool.getInstance().excute(new TriggerScene(-1));
                        showBtton(viewCache.ioc_arming, viewCache);
//                        viewCache.ioc_home.setVisibility(View.GONE);
//                        viewCache.ioc_arming.setVisibility(View.VISIBLE);
                    }
                });
            } else {
                viewCache.ioc_arming.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // 设防按钮点击
                        mContext.showInProgress(getString(R.string.operationing), false, true);
                        JavaThreadPool.getInstance().excute(new TriggerScene(-1));
                    }
                });
                viewCache.ioc_disarming.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // 撤防按钮点击
                        mContext.showInProgress(getString(R.string.operationing), false, true);
                        JavaThreadPool.getInstance().excute(new TriggerScene(0));
                    }
                });
                viewCache.ioc_home.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // 在家按钮点击
                        mContext.showInProgress(getString(R.string.operationing), false, true);
                        JavaThreadPool.getInstance().excute(new TriggerScene(-3));
                    }
                });
            }

            viewCache.ioc_panic.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // panic按钮点击
                    mContext.showInProgress(getString(R.string.operationing), false, true);
                    JavaThreadPool.getInstance().excute(new TriggerPanic());
                }
            });
        }

        /**
         * 设置未读消息数
         *
         * @param viewCache
         * @param i
         */
        private void setBadeNumber(DeviceInfoView viewCache, int i) {
            if (deviceInfos.get(i).getNr() == 0) {
                viewCache.badgeView.setVisibility(View.GONE);
            } else {
                viewCache.badgeView.setText(String.valueOf(deviceInfos.get(i).getNr()));
                viewCache.badgeView.show();
            }
            if (deviceInfos.get(i).getAcceptMessage() == 3) {
                viewCache.mode.setText(getString(R.string.shefang));
                if (Actions.VersionType.CHANNEL_LILESI
                        .equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                    viewCache.mode.setVisibility(View.GONE);
                } else {
                    viewCache.mode.setVisibility(View.VISIBLE);
                }
            } else {
                viewCache.mode.setVisibility(View.GONE);
            }
        }

        /**
         * jkd按钮轮播，用于变换轮播按钮
         *
         * @param view
         * @param viewCache
         */
        public void showBtton(View view, DeviceInfoView viewCache) {
            viewCache.ioc_arming.setVisibility(View.GONE);
            viewCache.ioc_disarming.setVisibility(View.GONE);
            viewCache.ioc_home.setVisibility(View.GONE);
            view.setVisibility(View.VISIBLE);
        }

        /**
         * 设置主机模式
         *
         * @param viewCache
         * @param i
         */

        private void setModen(DeviceInfoView viewCache, int i) {
            if (Actions.VersionType.CHANNEL_LILESI
                    .equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                if (DataCenterSharedPreferences.Constant.SCENE_NOW_CF.equals(zhuji.getScene())
                        && !deviceInfos.get(i).getControlType().contains("xiaxing")) { // 撤防，探头不可编辑接收模式
                    // 立乐斯要求
                    viewCache.switchButton.setEnabled(false);
                } else {
                    viewCache.switchButton.setEnabled(true);
                }
            }
            if (i == 0 && mContext.dcsp.getBoolean(DataCenterSharedPreferences.Constant.SHOW_ZHUJI, true)) {
                // 吉凯达不显示主机下面的三个按钮
//                if (VersionType.CHANNEL_JKD.equals(MainApplication.app.getVersionType())) {
//                    viewCache.sceneLayout.setVisibility(View.GONE);
//                    return;
//                }
//                // 巨将无按钮
                if (Actions.VersionType.CHANNEL_JUJIANG
                        .equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                    viewCache.sceneLayout.setVisibility(View.GONE);
                } else {
                    viewCache.sceneLayout.setVisibility(View.VISIBLE);
                    if (Actions.VersionType.CHANNEL_LILESI
                            .equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                        viewCache.ioc_panic.setVisibility(View.VISIBLE);
                    } else {
                        viewCache.ioc_panic.setVisibility(View.GONE);
                    }
                }

                if (isFSNY) {
                    viewCache.rLayout.setVisibility(View.INVISIBLE);
                } else {
                    viewCache.rLayout.setVisibility(View.VISIBLE);
                }
                boolean isJKD = Actions.VersionType.CHANNEL_JKD.equals(MainApplication.app.getAppGlobalConfig().getVersion());
                if (DataCenterSharedPreferences.Constant.SCENE_NOW_SF.equals(zhuji.getScene())) {
                    viewCache.time.setText(getString(R.string.activity_scene_item_outside_moden));
                    if (isJKD) {
                        viewCache.rLayout.setBackgroundColor(Color.TRANSPARENT);
                        showBtton(viewCache.ioc_arming, viewCache);
//                        viewCache.ioc_arming.setVisibility(View.VISIBLE);
                        viewCache.time.setTextColor(getResources().getColor(R.color.bg_devices_modle));
                    } else {
                        viewCache.time.setTextColor(Color.WHITE);
                        viewCache.rLayout.setBackgroundColor(Color.RED);
                    }
                    viewCache.ioc_arming.setImageResource(R.drawable.scene_item_arming_pressed);
                    viewCache.ioc_disarming.setImageResource(R.drawable.scene_item_disarming_normal);
                    viewCache.ioc_home.setImageResource(R.drawable.scene_item_home_normal);
                } else if (DataCenterSharedPreferences.Constant.SCENE_NOW_CF.equals(zhuji.getScene())) {
                    viewCache.time.setText(getString(R.string.activity_scene_item_home_moden));
                    if (isJKD) {
                        viewCache.rLayout.setBackgroundColor(Color.TRANSPARENT);
                        showBtton(viewCache.ioc_disarming, viewCache);
//                        viewCache.ioc_disarming.setVisibility(View.VISIBLE);
                        viewCache.time.setTextColor(getResources().getColor(R.color.bg_devices_modle));
                    } else {
                        viewCache.time.setTextColor(Color.WHITE);
                        viewCache.rLayout.setBackgroundColor(Color.GREEN);
                    }
                    viewCache.ioc_arming.setImageResource(R.drawable.scene_item_arming_normal);
                    viewCache.ioc_disarming.setImageResource(R.drawable.scene_item_disarming_pressed);
                    viewCache.ioc_home.setImageResource(R.drawable.scene_item_home_normal);
                } else if (DataCenterSharedPreferences.Constant.SCENE_NOW_HOME.equals(zhuji.getScene())) {

                    viewCache.time.setText(getString(R.string.activity_scene_item_inhome_moden));
                    if (isJKD) {
                        viewCache.rLayout.setBackgroundColor(Color.TRANSPARENT);
                        showBtton(viewCache.ioc_home, viewCache);
//                        viewCache.ioc_home.setVisibility(View.VISIBLE);
                        viewCache.time.setTextColor(getResources().getColor(R.color.bg_devices_modle));
                    } else {
                        viewCache.time.setTextColor(Color.WHITE);
                        viewCache.rLayout.setBackgroundColor(Color.GREEN);
                    }
                    viewCache.ioc_arming.setImageResource(R.drawable.scene_item_arming_normal);
                    viewCache.ioc_disarming.setImageResource(R.drawable.scene_item_disarming_normal);
                    viewCache.ioc_home.setImageResource(R.drawable.scene_item_home_pressed);
                } else if (!zhuji.getScene().equals("")) {
                    viewCache.rLayout.setBackgroundColor(Color.GREEN);
                    viewCache.time.setText(zhuji.getScene());
                    SceneActivity.test = zhuji.getScene();
                    viewCache.time.setTextColor(Color.BLACK);
                    viewCache.ioc_arming.setImageResource(R.drawable.scene_item_arming_normal);
                    viewCache.ioc_disarming.setImageResource(R.drawable.scene_item_disarming_normal);
                    viewCache.ioc_home.setImageResource(R.drawable.scene_item_home_normal);
                } else {
                    viewCache.rLayout.setBackgroundColor(Color.TRANSPARENT);
                    viewCache.time.setVisibility(View.GONE);
                    viewCache.ioc_arming.setImageResource(R.drawable.scene_item_arming_normal);
                    viewCache.ioc_disarming.setImageResource(R.drawable.scene_item_disarming_normal);
                    viewCache.ioc_home.setImageResource(R.drawable.scene_item_home_normal);
                }
            } else {
                viewCache.rLayout.setBackgroundColor(Color.TRANSPARENT);
                viewCache.time.setTextColor(Color.GRAY);
                viewCache.time.setVisibility(View.VISIBLE);
                viewCache.sceneLayout.setVisibility(View.GONE);
            }
        }
    }

    class TriggerPanic implements Runnable {
        @Override
        public void run() {

            JSONObject o = new JSONObject();
            o.put("did", zhuji.getId());
            String server = mContext.dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");

            String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/d/panic", o, mContext);
            if ("0".equals(result)) {
                Message m = defaultHandler.obtainMessage(dHandler_panic);
                defaultHandler.sendMessage(m);
            } else {
                defaultHandler.post(new Runnable() {
                    public void run() {
                        mContext.cancelInProgress();
                        Toast.makeText(mContext, getString(R.string.net_error_operationfailed),
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

    class TriggerScene implements Runnable {
        private int sId;

        public TriggerScene() {
        }

        public TriggerScene(int sId) {
            this.sId = sId;
        }

        @Override
        public void run() {

            JSONObject o = new JSONObject();
            o.put("id", sId);
            o.put("did", zhuji.getId());
//            o.put("uid", dcsp.getLong(Constant.LOGIN_APPID, 0));
//            o.put("code", dcsp.getString(Constant.LOGIN_CODE, ""));
            String server = mContext.dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            String result = HttpRequestUtils
                    .requestoOkHttpPost(server + "/jdm/s3/scenes/trigger", o, mContext);
//            String result = HttpRequestUtils.requestHttpServer(
//                     server + "/jdm/service/scenes/trigger?v="
//                            + URLEncoder.encode(SecurityUtil.crypt(o.toJSONString(), Constant.KEY_HTTP)),
//                    DevicesListActivity.this, defaultHandler);
            if ("0".equals(result)) {
                Message m = defaultHandler.obtainMessage(dHandler_scenes);
                m.arg1 = sId;
                defaultHandler.sendMessage(m);
            } else {
                defaultHandler.post(new Runnable() {
                    public void run() {
                        mContext.cancelInProgress();
                        Toast.makeText(mContext, getString(R.string.net_error_operationfailed),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    protected String getJdmVersionType() {
        return mContext.getJdmApplication().getAppGlobalConfig().getVersion();
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.menu_icon:
                mContext.menuWindow.showAtLocation(v, Gravity.TOP | Gravity.RIGHT, 0,
                        Util.dip2px(mContext.getApplicationContext(), 55) + Util.getStatusBarHeight(mContext));
                break;
            case R.id.iv_icon:
//                dl.open();
                break;
            case R.id.tv_setting:
                intent.setClass(mContext.getApplicationContext(), SettingActivity.class);
                intent.putExtra("zhuji_Id", zhuji.getId());
                break;
            case R.id.iv_bottom:
                intent.setClass(mContext.getApplicationContext(), UserInfoActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_setdevice: // 设置设备
                itemMenu.dismiss();
                intent.setClass(mContext.getApplicationContext(), ChooseAudioSettingMode.class);
                intent.putExtra("device", operationDevice);
                startActivity(intent);
                break;
            case R.id.btn_setgsm: // 设置主机GSM号码
                itemMenu.dismiss();
                intent.setClass(mContext.getApplicationContext(), DeviceSetGSMPhoneActivity.class);
                intent.putExtra("device", operationDevice);
                startActivity(intent);
                break;
            case R.id.btn_checkversion: // 固件检查更新
                itemMenu.dismiss();
                SyncMessageContainer.getInstance().sendMessageToServer(SyncMessage.CommandMenu.rq_checkpudate, SyncMessage.CodeMenu.zero,
                        operationDevice.getId(), null);
                mContext.showInProgress(getString(R.string.loading), false, false);
                defaultHandler.sendEmptyMessageDelayed(dHandler_timeout, 8 * 1000);
                break;
            case R.id.btn_accept_auto_strongshow: // 强力提醒模式
                itemMenu.dismiss();
                SyncMessageContainer.getInstance().sendMessageToServer(SyncMessage.CommandMenu.rq_controlRemind, SyncMessage.CodeMenu.zero,
                        operationDevice.getId(), new byte[]{0x03});
                break;
            case R.id.btn_accept_autoshow: // 自动提醒
                itemMenu.dismiss();
                SyncMessageContainer.getInstance().sendMessageToServer(SyncMessage.CommandMenu.rq_controlRemind, SyncMessage.CodeMenu.zero,
                        operationDevice.getId(), new byte[]{0x02});
                break;
            case R.id.btn_acceptnotshow: // 接收消息不提醒
                itemMenu.dismiss();
                SyncMessageContainer.getInstance().sendMessageToServer(SyncMessage.CommandMenu.rq_controlRemind, SyncMessage.CodeMenu.zero,
                        operationDevice.getId(), new byte[]{0x01});
                break;
            case R.id.btn_notaccept: // 关操作
                itemMenu.dismiss();
                SyncMessageContainer.getInstance().sendMessageToServer(SyncMessage.CommandMenu.rq_controlRemind, SyncMessage.CodeMenu.zero,
                        operationDevice.getId(), new byte[]{0x00});
                break;
            case R.id.btn_deldevice:
                // 删除设备
                itemMenu.dismiss();
                new AlertView(getString(R.string.deviceslist_server_leftmenu_deltitle),
                        DeviceInfo.ControlTypeMenu.zhuji.value().equals(operationDevice.getControlType())
                                ? getString(R.string.deviceslist_server_leftmenu_delmessage_zhuji)
                                : getString(R.string.deviceslist_server_leftmenu_delmessage),
                        getString(R.string.deviceslist_server_leftmenu_delcancel),
                        new String[]{getString(R.string.deviceslist_server_leftmenu_delbutton)}, null,
                        mContext, AlertView.Style.Alert,
                        new com.smartism.znzk.view.alertview.OnItemClickListener() {

                            @Override
                            public void onItemClick(Object o, int position) {

                                if (position != -1) {
                                    mContext.showInProgress(getString(R.string.deviceslist_server_leftmenu_deltips), false, true);
                                    JavaThreadPool.getInstance().excute(new Runnable() {

                                        @Override
                                        public void run() {

                                            String server = mContext.dcsp.getString(
                                                    DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                                            JSONObject object = new JSONObject();
                                            object.put("id", operationDevice.getId());
                                            if (DeviceInfo.ControlTypeMenu.group.value()
                                                    .equals(operationDevice.getControlType())) {
                                                server = server + "/jdm/s3/dg/del";
                                            } else {
                                                server = server + "/jdm/s3/d/del";
                                            }
                                            String result = HttpRequestUtils.requestoOkHttpPost(server, object, mContext);
                                            // -1参数为空，0删除成功
                                            if (result != null && result.equals("0")) {
                                                if (DeviceInfo.ControlTypeMenu.zhuji.value()
                                                        .equals(operationDevice.getControlType())) {
                                                    try {
                                                        DatabaseOperator.getInstance().getWritableDatabase().delete(
                                                                "DEVICE_STATUSINFO", "zj_id = ?",
                                                                new String[]{String.valueOf(operationDevice.getId())});

                                                        DatabaseOperator.getInstance().getWritableDatabase().delete(
                                                                "ZHUJI_STATUSINFO", "id = ?",
                                                                new String[]{String.valueOf(operationDevice.getId())});
                                                        List<ZhujiInfo> zhujis = DatabaseOperator.getInstance()
                                                                .queryAllZhuJiInfos();

                                                        if (zhujis != null && !zhujis.isEmpty()) {
//                                                            mContext.dcsp.putString(DataCenterSharedPreferences.Constant.APP_MASTERID,
//                                                                    zhujis.get(0).getMasterid()).commit();
                                                            ZhujiListFragment.setMasterId(zhujis.get(0).getMasterid());
                                                        } else {
//                                                            mContext.dcsp.remove(DataCenterSharedPreferences.Constant.APP_MASTERID).commit();
                                                            ZhujiListFragment.resetMasterId();
                                                        }
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                        }
                                    });
                                }
                            }
                        }).show();

                                            /*try {

                                                String server = dcsp.getString(
                                                        Constant.HTTP_DATA_SERVERS, "");
                                                JSONObject object = new JSONObject();
                                                object.put("id", operationDevice.getId());
                                                object.put("uid", dcsp.getLong(Constant.LOGIN_APPID, 0));
                                                object.put("code", dcsp.getString(Constant.LOGIN_CODE, ""));
                                                if (ControlTypeMenu.group.value()
                                                        .equals(operationDevice.getControlType())) {
                                                    server = server + "/jdm/service/dg/del?v=";
                                                } else {
                                                    server = server + "/jdm/service/del?v=";
                                                }
                                                String result = HttpRequestUtils.requestHttpServer(
                                                         server
                                                                + URLEncoder.encode(SecurityUtil.crypt(
                                                                object.toJSONString(), Constant.KEY_HTTP), "UTF-8"),
                                                        DevicesListActivity.this, defaultHandler);
                                                // -1参数为空，0删除成功
                                                if (result != null && result.equals("0")) {
                                                    if (ControlTypeMenu.zhuji.value()
                                                            .equals(operationDevice.getControlType())) {
                                                        DatabaseOperator.getInstance().getWritableDatabase().delete(
                                                                "DEVICE_STATUSINFO", "zj_id = ?",
                                                                new String[]{String.valueOf(operationDevice.getId())});
                                                        DatabaseOperator.getInstance().getWritableDatabase().delete(
                                                                "ZHUJI_STATUSINFO", "id = ?",
                                                                new String[]{String.valueOf(operationDevice.getId())});
                                                        List<ZhujiInfo> zhujis = DatabaseOperator.getInstance()
                                                                .queryAllZhuJiInfos();
                                                        if (zhujis != null && !zhujis.isEmpty()) {
                                                            dcsp.putString(Constant.APP_MASTERID,
                                                                    zhujis.get(0).getMasterid()).commit();
                                                        } else {
                                                            dcsp.remove(Constant.APP_MASTERID).commit();
                                                        }
                                                    } else if (ControlTypeMenu.group.value()
                                                            .equals(operationDevice.getControlType())) {
                                                        DatabaseOperator.getInstance().getWritableDatabase().delete(
                                                                "GROUP_STATUSINFO", "id = ?",
                                                                new String[]{String.valueOf(operationDevice.getId())});
                                                        DatabaseOperator.getInstance().getWritableDatabase().delete(
                                                                "GROUP_DEVICE_RELATIOIN", "gid = ?",
                                                                new String[]{String.valueOf(operationDevice.getId())});
                                                    } else {

                                                        DatabaseOperator.getInstance().getWritableDatabase().delete(
                                                                "DEVICE_STATUSINFO", "id = ?",
                                                                new String[]{String.valueOf(operationDevice.getId())});
                                                        if (operationDevice.getCak().equals("surveillance")) {
                                                            String ipc = operationDevice.getIpc();
                                                            Log.e("ipc", ipc);
                                                            List<CameraInfo> camera = (List<CameraInfo>) JSON
                                                                    .parseArray(ipc, CameraInfo.class);
                                                            Log.e("camera", camera.size() + "");
                                                            Looper.prepare();
                                                            for (int i = 0; i < camera.size(); i++) {
                                                                if (FList.getInstance() != null
                                                                        && FList.getInstance().size() > 0) {
                                                                    FList.getInstance().delete(camera.get(i).getId());
                                                                    Log.e("删除了", camera.get(i).getId());
                                                                    File file = new File(Constants.Image.USER_HEADER_PATH
                                                                            + NpcCommon.mThreeNum + "/"
                                                                            + camera.get(i).getId());
                                                                    Utils.deleteFile(file);
                                                                    if (i == 0 && FList.getInstance().size() == 0
                                                                            && FList.getInstance().apListsize() == 0) {
                                                                        Intent it = new Intent();
                                                                        it.setAction(Constants.Action.DELETE_DEVICE_ALL);
                                                                        MainApplication.app.sendBroadcast(it);
                                                                    }
                                                                    camera.remove(i);
                                                                    i--;
                                                                }
                                                            }
                                                            Looper.loop();
                                                        }
                                                    }
                                                    defaultHandler.sendEmptyMessage(dHandlerWhat_deletesuccess);

                                                } else if ("-1".equals(result)) {
                                                    defaultHandler.post(new Runnable() {

                                                        @Override
                                                        public void run() {
                                                            cancelInProgress();
                                                            Toast.makeText(DevicesListActivity.this,
                                                                    getString(
                                                                            R.string.deviceslist_server_leftmenu_delfaultedtips),
                                                                    Toast.LENGTH_LONG).show();
                                                        }
                                                    });
                                                } else if ("-2".equals(result)) {
                                                    defaultHandler.post(new Runnable() {

                                                        @Override
                                                        public void run() {
                                                            cancelInProgress();
                                                            Toast.makeText(DevicesListActivity.this,
                                                                    getString(R.string.net_error_illegal_request),
                                                                    Toast.LENGTH_LONG).show();
                                                            LogUtil.e(DevicesListActivity.this, TAG,
                                                                    "非法的http请求，id+code校验失败，触发端为android");
                                                        }
                                                    });
                                                } else if ("-4".equals(result)) {
                                                    defaultHandler.post(new Runnable() {

                                                        @Override
                                                        public void run() {
                                                            cancelInProgress();
                                                            Toast.makeText(DevicesListActivity.this,
                                                                    getString(R.string.net_error_operationfailed),
                                                                    Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                } else if (!StringUtils.isEmpty(result)) {
                                                    defaultHandler.post(new Runnable() {

                                                        @Override
                                                        public void run() {
                                                            cancelInProgress();
                                                            Toast.makeText(DevicesListActivity.this,
                                                                    getString(R.string.net_error_weizhi), Toast.LENGTH_LONG)
                                                                    .show();
                                                        }
                                                    });
                                                }
                                            } catch (

                                                    Exception e)

                                            {
                                                Log.e(TAG, "异常", e);
                                            }*/

                break;
            case R.id.add_device_foot:
                if (zhuji != null) {
//                    if (VersionType.CHANNEL_JUJIANG //巨将+号同样修改为从设备添加页面进入
//                            .equals(MainApplication.app.getJdmVersionByPrefix(zhuji.getMasterid().substring(0, 4)))) { // 默认打开遥控器
//                        intent.setClass(getApplicationContext(), DeviceCategoryActivity.class);
//                        intent.putExtra("filter", 2); // 2为遥控器
//                        startActivityForResult(intent, 112);
//                        break;
//                    } else {
                    intent.setClass(mContext.getApplicationContext(), AddDeviceChooseActivity.class);
//                    }
                } else {
                    intent.setClass(mContext.getApplicationContext(), AddZhujiActivity.class);
                }
                startActivity(intent);
                break;
            default:
                Toast.makeText(mContext, getString(R.string.deviceslist_server_leftmenu_unknownbutton),
                        Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void initViewEvent() {
        isFSNY = Actions.VersionType.CHANNEL_FSNY.equals(MainApplication.app.getAppGlobalConfig().getVersion());
        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            private DeviceInfo device;

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

// 添加按钮在块状布局的最后面
//                if (deviceInfos.size() == position) {
//                    Intent intent = new Intent();
//                    if (zhuji != null) {
//                        intent.setClass(mContext.getApplicationContext(), AddDeviceChooseActivity.class);
//                    } else {
//                        intent.setClass(mContext.getApplicationContext(), AddZhujiActivity.class);
//                    }
//                    startActivity(intent);
//                }
                // 吉凯达listview与footer间有一段间距 点击间距抛出异常
                try {
                    device = deviceInfos.get(position);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }

                // 如果点击的是红外设备
                if (device.getCa() != null && device.getCa().contentEquals("hwzf")) {

                    return;
                }

                if (device.getControlType().equals(DeviceInfo.ControlTypeMenu.neiqian.value())) {
                    String cpackage = device.getApppackage();
                    if (Util.appIsInstalled(mContext, cpackage)) {
                        // 已经安装，打开
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setComponent(new ComponentName(cpackage.substring(0, cpackage.lastIndexOf("/")),
                                cpackage.replace("/", ".")));
                        startActivity(intent);
                    } else {
                        // 提示安装
                        Toast.makeText(mContext, getString(R.string.deviceslist_server_addnewapptips),
                                Toast.LENGTH_LONG).show();
                        String downloadString = device.getAppdownload();
                        if (downloadString.startsWith("jdmapk://")) {
                            // 内嵌apk
                            File file = new File(
                                    Environment.getExternalStorageDirectory().getAbsolutePath() + "/jdm_app_tmp.apk");
                            int resourct = 0;
                            if (downloadString.substring(9).equals("p2pipcam_hvcipc_6_5")) {
//                                resourct = R.raw.p2pipcam_hvcipc_6_5;
                            }
                            try {
                                FileUtils.copyInputStreamToFile(getResources().openRawResource(resourct), file);
                            } catch (Resources.NotFoundException e) {
                            } catch (IOException e) {
                            }
                            Util.install(mContext, Uri.fromFile(file));
                        } else if (downloadString.startsWith("http")) {
                            // http连接，需要下载。
                        }
                    }
                } else if (device.getControlType().equals(DeviceInfo.ControlTypeMenu.zhuji.value())) {

                    // 主机栏点击事件
                    Intent intent = new Intent();
                    intent.setClass(mContext.getApplicationContext(), DeviceDetailActivity.class);
                    intent.putExtra("device", device);
                    startActivity(intent);
                } else {
                    /**
                     * dakai
                     */
                    if ("sst".equals(device.getCa())) {
                        // 打开摄像头
                        verifyIPCLoginAndStart(device);
                    } else if (DeviceInfo.ControlTypeMenu.group.value().equals(device.getControlType())) {
                        //   ---群组-------->GroupInfoActivity

                        deviceIntent = new Intent();
                        deviceIntent.setClass(mContext.getApplicationContext(), GroupInfoActivity.class);
                        deviceIntent.putExtra("device", device);
                        mContext.showInProgress(getString(R.string.loading), false, true);
                        if (!StringUtils.isEmpty(device.getBipc()) && !"0".equals(device.getBipc())) {
                            JavaThreadPool.getInstance().excute(new BindingCameraLoad(device.getBipc()));
                        } else {
                            mContext.cancelInProgress();
                            startActivity(deviceIntent);
                        }

                    } else if (device.getCa().equals("tzc")) { //体重秤
                        mContext.showInProgress(getString(R.string.loading), false, true);
                        JavaThreadPool.getInstance().excute(new InitFamilyMemberThread(device, dHandler_weightInfo));
//                        Intent intent = new Intent();
//                        intent.setClass(mContext, WeightPrepareActivity.class);
//                        intent.putExtra("device", device);
//                        startActivity(intent);
                    } else if (device.getCa().equals("yyj")) {
                        mContext.showInProgress(getString(R.string.loading), false, true);
                        JavaThreadPool.getInstance().excute(new InitFamilyMemberThread(device, dHandler_xyjInfo));
//                       mContext.showInProgress(getString(R.string.loading), false, true);
//                       JavaThreadPool.getInstance().excute(new InitXYJMemberThread(device));
//                        Intent intent = new Intent();
//                        intent.setClass(mContext, XYJPrepareActivity.class);
//                        intent.putExtra("device", device);
//                        startActivity(intent);
                    } else if (device.getCa().equals("qwq")) {
                        deviceIntent = new Intent();
                        deviceIntent.setClass(mContext.getApplicationContext(), QWQActivity.class);
                        deviceIntent.putExtra("devices", device);
                        startActivity(deviceIntent);
                    } else { // 其他

                        deviceIntent = new Intent();
                        deviceIntent.setClass(mContext.getApplicationContext(), DeviceInfoActivity.class);
                        deviceIntent.putExtra("device", device);
                        mContext.showInProgress(getString(R.string.loading), false, true);
                        if (!StringUtils.isEmpty(device.getBipc()) && !"0".equals(device.getBipc())) {
                            JavaThreadPool.getInstance().excute(new BindingCameraLoad(device.getBipc()));
                        } else {
                            mContext.cancelInProgress();
                            startActivity(deviceIntent);
                        }

                    }
                }
            }
        });


        deviceListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                // 吉凯达listview与footer间有一段间距 点击间距抛出异常
                try {
                    operationDevice = deviceInfos.get(position);

                } catch (IndexOutOfBoundsException e) {
                    return true;
                }

                operationDevice.setwIndex(position);
                itemMenu.updateDeviceMenu(mContext, operationDevice, mContext.dcsp, zhuji);
                itemMenu.showAtLocation(ll_device_main, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置
                return true;
            }

        });
        listViewHeadView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (v.findViewById(R.id.nonet_layout).getVisibility() == View.VISIBLE) {
                    startActivityForResult(new Intent(Settings.ACTION_WIRELESS_SETTINGS), 0);
                }
            }
        });
    }

    private void checkAlertAudio() {
        ActivityManager a = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> sInfos = a.getRunningServices(Integer.MAX_VALUE);
        boolean isRunning = false;
        for (ActivityManager.RunningServiceInfo rServiceInfo : sInfos) {
            if (AudioTipsService.class.getCanonicalName().equals(rServiceInfo.service.getClassName())) {
                isRunning = true;
                break;
            }
        }
        if (isRunning) {
            if (MainApplication.app.getAlertMessageActivity() != null) {
                MainApplication.app.getAlertMessageActivity().finish();
                LogUtil.e(mContext.getApplicationContext(), TAG, "打开DevicesList，发现有AlertAudio正在运行的情况，finish掉它");
            }
        }
    }

    /**
     * 获取红外设备的指令信息
     *
     * @param did 红外设备id号
     * @param b   是否下载码
     * @return
     */
    private String getYaokanCode(long did, Boolean b) {

        JSONObject object = new JSONObject();
        object.put("did", did);
        object.put("c", b);
        String server = mContext.dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
        String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/infr/get", object, mContext);
        return result;
    }

    /**
     * 验证IP摄像头是否登录，并打开摄像头
     */
    private void verifyIPCLoginAndStart(DeviceInfo device) {
        Account activeUser = AccountPersist.getInstance().getActiveAccountInfo(mContext);

        if (activeUser != null && !activeUser.three_number.equals("0517401")) {
            NpcCommon.mThreeNum = activeUser.three_number;
//            P2PHandler.getInstance().p2pInit(mContext, new P2PListener(), new SettingListener());
            NpcCommon.verifyNetwork(mContext);
            connect();
            return;
        }
        Toast.makeText(mContext, R.string.net_error_loginoutofdayipc, Toast.LENGTH_SHORT).show();
    }


    class InitFamilyMemberThread implements Runnable {
        public final DeviceInfo mInfo;
        public int type;

        public InitFamilyMemberThread(DeviceInfo deviceInfo, int type) {
            this.mInfo = deviceInfo;
            this.type = type;
        }

        @Override
        public void run() {

            String server = mContext.dcsp.getString(
                    DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("did", zhuji.getId());
            String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/f/list", object, mContext);
            if ("0".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        mContext.cancelInProgress();
                        DatabaseOperator.getInstance(mContext).getWritableDatabase().execSQL("delete from FAMINY_MEMBER");
                        Intent intent = new Intent(mContext, null);
                        intent.putExtra("device", mInfo);
                        startActivity(intent);
                    }
                });
            } else if (!StringUtils.isEmpty(result) && result.length() > 4) {
                JSONArray resultBack = null;
                try {
                    resultBack = JSON.parseArray(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (resultBack == null) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            mContext.cancelInProgress();
                            Toast.makeText(
                                    mContext,
                                    getString(R.string.device_set_tip_responseerr),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }

                JSONArray array = (JSONArray) JSON.parse(resultBack.toJSONString());
                ArrayList<WeightUserInfo> userInfos = new ArrayList<>();
                for (int i = 0; i < array.size(); i++) {
                    WeightUserInfo userInfo = new WeightUserInfo();
                    JSONObject object1 = array.getJSONObject(i);
                    userInfo.setUserBirthday(object1.getString("birthday"));
                    userInfo.setUserSex((0 == object1.getIntValue("sex")) ? "女" : "男");
                    userInfo.setUserHeight(object1.getIntValue("height"));
                    userInfo.setUserObjectiveWeight(object1.getString("objectiveWeight"));
                    userInfo.setUserName(object1.getString("name"));
                    userInfo.setUserId(object1.getLong("id"));
                    userInfo.setUserLogo(object1.getString("logo"));
                    DatabaseOperator.getInstance(mContext.getApplicationContext()).insertOrUpdateFamilyMember(userInfo);
//                    userInfos.add(userInfo);
                }
                Message m = defaultHandler.obtainMessage(type);
                m.obj = mInfo;
                defaultHandler.sendMessage(m);
            }
        }
    }

    /**
     * 请求数据子线程
     */
    class BindingCameraLoad implements Runnable {
        private long uid;
        private String code;
        private String bIpc;

        public BindingCameraLoad(String bIpc) {
            this.bIpc = bIpc;
        }

        @Override
        public void run() {
            CameraInfo c = null;
            List<CameraInfo> cameraInfos = DatabaseOperator.getInstance(mContext.getApplicationContext()).queryAllCameras(zhuji);
            if (!cameraInfos.isEmpty()) {
                for (CameraInfo cs : cameraInfos) {
                    if (cs.getIpcid() == Long.parseLong(bIpc)) {
                        c = cs;
                        break;
                    }
                }
            }
            if (c != null) {
                Contact contact = new Contact();
                contact.contactId = c.getId();
                contact.contactName = c.getN();
                contact.contactPassword = c.getP();
                contact.userPassword = c.getOriginalP();
                try {
                    contact.ipadressAddress = InetAddress.getByName("192.168.1.1");
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                Message message = new Message();
                message.what = dHandler_initContast;
                message.obj = contact;
                defaultHandler.sendMessage(message);
            } else {
                //设备不存在的时候跳转，但是不显示视屏
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        mContext.cancelInProgress();
                        startActivity(deviceIntent);
                    }
                });
            }
        }
    }

    /**
     * 注册广播
     */
    private void initRegisterReceiver() {
        IntentFilter receiverFilter = new IntentFilter();
        receiverFilter.addAction(Actions.REFRESH_DEVICES_LIST);
        receiverFilter.addAction(Actions.ACCETP_ONEDEVICE_MESSAGE);
        receiverFilter.addAction(Actions.CONNECTION_FAILED);
        receiverFilter.addAction(Actions.CONNECTION_ING);
        receiverFilter.addAction(Actions.CONNECTION_NONET);
        receiverFilter.addAction(Actions.CONNECTION_SUCCESS);
        receiverFilter.addAction(Actions.CONNECTION_FAILED_SENDFAILED);
        receiverFilter.addAction(Actions.SHOW_SERVER_MESSAGE);
        receiverFilter.addAction(Actions.ZHUJI_CHECKUPDATE);
        receiverFilter.addAction(Actions.ZHUJI_UPDATE);
        receiverFilter.addAction(Constants.Action.ACTION_NETWORK_CHANGE);
        mContext.registerReceiver(defaultReceiver, receiverFilter);
    }


    /**
     * 更新数据
     */
    private void refreshData() {
        JavaThreadPool.getInstance().excute(new loadAllDevicesInfo(dHandlerWhat_loadsuccess));
    }

    private void connect() {
        Intent service = new Intent(MainApplication.MAIN_SERVICE_START);
        service.setPackage(mContext.getPackageName());
        mContext.startService(service);
        if (AppConfig.DeBug.isWrightAllLog) {
            Intent log = new Intent(MainApplication.LOGCAT);
            log.setPackage(mContext.getPackageName());
            mContext.startService(log);
        }
    }

    private String regId;//小米regID
    private BroadcastReceiver defaultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (intent.getAction().equals(Constants.Action.ACTION_NETWORK_CHANGE)) {
                boolean isNetConnect = false;
                ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
                if (activeNetInfo != null) {
                    if (activeNetInfo.isConnected()) {
                        isNetConnect = true;
                        WifiManager wifimanager = (WifiManager) mContext.getApplicationContext().getSystemService(mContext.WIFI_SERVICE);
                        if (wifimanager == null) {
                            return;
                        }
                        WifiInfo wifiinfo = wifimanager.getConnectionInfo();
                        if (wifiinfo == null) {
                            return;
                        }
                        if (wifiinfo.getSSID().length() > 0) {
                            String wifiName = Utils.getWifiName(wifiinfo.getSSID());
                            if (wifiName.startsWith(AppConfig.Relese.APTAG)) {
                                String id = wifiName.substring(AppConfig.Relese.APTAG.length());
                                FList.getInstance().setIsConnectApWifi(id, true);
                            } else {
                                FList.getInstance().setAllApUnLink();
                            }
                        }
//                        try {
//                            Thread.sleep(1000);
//                        } catch (InterruptedException e) {
//                            // TODO Auto-generated catch block
//                            e.printStackTrace();
//                        }
//                        Intent intentNew = new Intent();
//                        intentNew.setAction(Constants.Action.NET_WORK_TYPE_CHANGE);
//                        mContext.sendBroadcast(intentNew);
                        WifiUtils.getInstance().isApDevice();
                    } else {
                        T.showShort(mContext, getString(R.string.network_error) + " " + activeNetInfo.getTypeName());
                    }

                    if (activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        NpcCommon.mNetWorkType = NpcCommon.NETWORK_TYPE.NETWORK_WIFI;
                    } else {
                        NpcCommon.mNetWorkType = NpcCommon.NETWORK_TYPE.NETWORK_2GOR3G;
                    }
                } else {
                    Toast.makeText(mContext, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                }

                NpcCommon.setNetWorkState(isNetConnect);

            } else if (Actions.REFRESH_DEVICES_LIST.equals(intent.getAction())) { // 数据刷新完成广播
                refreshData();
            } else if (Actions.ACCETP_ONEDEVICE_MESSAGE.equals(intent.getAction())) { // 某一个设备的推送广播
                old_refulsh_device_id = intent.getStringExtra("device_id");
                if (mContext.progressIsShowing()) {
                    Toast.makeText(mContext, getString(R.string.rq_control_sendsuccess),
                            Toast.LENGTH_SHORT).show();
                }
                mContext.cancelInProgress();
                refreshData();
            } else if (Actions.CONNECTION_FAILED.equals(intent.getAction())) { // 连接断开
//                LogUtil.i(TAG, "Actions.CONNECTION_FAILED");
                listViewHeadView.findViewById(R.id.nonet_layout).setVisibility(View.VISIBLE);
                deviceAdapter.notifyDataSetChanged();
//                startShowConnLoading();
            } else if (Actions.CONNECTION_SUCCESS.equals(intent.getAction())) { // 连接成功
//                LogUtil.i(TAG, "Actions.CONNECTION_SUCCESS");
                listViewHeadView.findViewById(R.id.nonet_layout).setVisibility(View.GONE);
                deviceAdapter.notifyDataSetChanged();
//                stopShowConnLoading();
            } else if (Actions.CONNECTION_ING.equals(intent.getAction())) { // 连接中
//                LogUtil.i(TAG, "Actions.CONNECTION_ING");
                listViewHeadView.findViewById(R.id.nonet_layout).setVisibility(View.GONE);
                deviceAdapter.notifyDataSetChanged();
//                startShowConnLoading();
            } else if (Actions.CONNECTION_NONET.equals(intent.getAction())) { // 无网络
//                LogUtil.i(TAG, "Actions.CONNECTION_NONET");
                listViewHeadView.findViewById(R.id.nonet_layout).setVisibility(View.VISIBLE);
                deviceAdapter.notifyDataSetChanged();
//                stopShowConnLoading();
            } else if (Actions.CONNECTION_FAILED_SENDFAILED.equals(intent.getAction())) { // 发送失败
                Toast.makeText(mContext, getString(R.string.rq_control_sendfailed),
                        Toast.LENGTH_SHORT).show();
                refreshData();
            } else if (Actions.SHOW_SERVER_MESSAGE.equals(intent.getAction())) { // 显示服务器信息
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
                refreshData();
            } else if (Actions.ZHUJI_CHECKUPDATE.equals(intent.getAction())) { // 检查主机版本
                mContext.cancelInProgress();
                boolean to = defaultHandler.hasMessages(dHandler_timeout);
                defaultHandler.removeMessages(dHandler_timeout);
                if (SyncMessage.CodeMenu.rp_checkpudate_nonew.value() == intent.getIntExtra("data", 0)) {
                    if (to) { // 当页面数据初始化完成会检测主机的固件版本，这个时候是不需要显示固件是最新提示的
                        Toast.makeText(mContext, getString(R.string.deviceslist_server_noupdate),
                                Toast.LENGTH_SHORT).show();
                    }
                } else if (SyncMessage.CodeMenu.rp_checkpudate_havenew.value() == intent.getIntExtra("data", 0)) {
                    new AlertView(getString(R.string.deviceslist_server_update),
                            getString(R.string.deviceslist_server_update_havenew),
                            getString(R.string.deviceslist_server_leftmenu_delcancel),
                            new String[]{getString(R.string.deviceslist_server_update_button)}, null,
                            mContext, AlertView.Style.Alert,
                            new com.smartism.znzk.view.alertview.OnItemClickListener() {

                                @Override
                                public void onItemClick(Object o, int position) {
                                    if (position != -1) {
                                        mContext.showInProgress(getString(R.string.ongoing), false, false);
                                        defaultHandler.sendEmptyMessageDelayed(dHandlerWhat_serverupdatetimeout, 20000);
                                        SyncMessage message1 = new SyncMessage();
                                        message1.setCommand(SyncMessage.CommandMenu.rq_pudate.value());
                                        message1.setDeviceid(zhuji.getId());
                                        SyncMessageContainer.getInstance().produceSendMessage(message1);
                                    }
                                }
                            }).show();
                }
            } else if (Actions.ZHUJI_UPDATE.equals(intent.getAction())) { // 主机更新
                System.out.println(
                        "max:" + intent.getIntExtra("max", 0) + "progress:" + intent.getIntExtra("progress", 0));
                defaultHandler.removeMessages(dHandlerWhat_serverupdatetimeout);
                if (SyncMessage.CodeMenu.rp_pupdate_into.value() == intent.getIntExtra("data", 0)) {
                    mContext.cancelInProgress();
                    mContext.showOrUpdateProgressBar(getString(R.string.deviceslist_server_updating), true, 1, 100);
                } else if (SyncMessage.CodeMenu.rp_pupdate_success.value() == intent.getIntExtra("data", 0)) {
                    mContext.cancelInProgress();
                    mContext.cancelInProgressBar();
                    Toast.makeText(mContext, getString(R.string.deviceslist_server_update_success),
                            Toast.LENGTH_LONG).show();
                } else if (SyncMessage.CodeMenu.rp_pupdate_progress.value() == intent.getIntExtra("data", 0)) {
                    mContext.showOrUpdateProgressBar(getString(R.string.deviceslist_server_updating), true,
                            intent.getIntExtra("progress", 0) + 1, intent.getIntExtra("max", 0));
                    if (intent.getIntExtra("progress", 0) + 1 == intent.getIntExtra("max", 0)
                            && intent.getIntExtra("progress", 0) != 0) {
                        mContext.showInProgress(getString(R.string.deviceslist_server_update_reboot), false, true);
                    }
                }
            }
        }
    };

    public void moreHubChange(ZhujiInfo mZhuji) {
//        mContext.dcsp.putString(DataCenterSharedPreferences.Constant.APP_MASTERID, mZhuji.getMasterid()).commit();
        ZhujiListFragment.setMasterId(mZhuji.getMasterid());
        refreshData();
    }

    // 多主机列表
    private List<ZhujiInfo> zhujiList;

    public void moreHubChange() {
        zhujiList = DatabaseOperator.getInstance(mContext.getApplicationContext()).queryAllZhuJiInfos();
        List<String> is = new ArrayList<String>();
        int select = 0;
        for (int j = 0; j < zhujiList.size(); j++) {
            if (zhuji.getId() == zhujiList.get(j).getId()) {
                select = j;
            }
            is.add(zhujiList.get(j).getName() + zhujiList.get(j).getWhere());
        }
        new AlertDialog.Builder(mContext).setTitle(getString(R.string.deviceslist_morehub_title))
                .setSingleChoiceItems(is.toArray(new String[zhujiList.size()]), select, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        mContext.dcsp.putString(DataCenterSharedPreferences.Constant.APP_MASTERID, zhujiList.get(which).getMasterid()).commit();
                        ZhujiListFragment.setMasterId(zhujiList.get(which).getMasterid());
                        refreshData();
                        dialog.dismiss();
                    }
                }).setNegativeButton(getString(R.string.setting_activity_cancel), null).show();
        // zhujiList =
        // DatabaseOperator.getInstance(getApplicationContext()).queryAllZhuJiInfos();
        // AlertView moreHub = new
        // AlertView(getString(R.string.deviceslist_morehub_title), null,
        // getString(R.string.cancel), new String[] { getString(R.string.sure)
        // }, null, DevicesListActivity.this,
        // AlertView.Style.Alert, new
        // com.smartism.znzk.view.alertview.OnItemClickListener() {
        //
        // @Override
        // public void onItemClick(Object o, int position) { // o的值为字符串
        // // StringBuffer b = new StringBuffer();
        // // if (position != -1) {
        // // cycleTime = o.toString();
        // //
        // // }
        // // if (cycleTime.equals("00000001")) {
        // // b.append(getString(R.string.everyday));
        // // } else if (cycleTime.equals("00000010")) {
        // // b.append(getString(R.string.sundays) + " ");
        // // } else {
        // // for (int zhujiList = 0; zhujiList < cycleTime.length(); zhujiList++) {
        // //
        // // if (cycleTime.charAt(zhujiList) == '1') {
        // // b.append(getString(R.string.week) + (zhujiList + 1) + " ");
        // // }
        // // }
        // //
        // // }
        // //
        // // cycle.setText(b);
        // }
        //
        // });
        // View extView =
        // LayoutInflater.from(DevicesListActivity.this).inflate(R.layout.layout_alertview_alert_vertical,
        // null);
        // moreHub.addExtView(extView);
        // moreHub.show();
        // ListView alertButtonListView = (ListView)
        // extView.findViewById(R.id.alertButtonListView);
        // AlertViewAdapter adapter = new AlertViewAdapter(zhujiList, null);
        // alertButtonListView.setAdapter(adapter);
        // alertButtonListView.setOnItemClickListener(new
        // AdapterView.OnItemClickListener() {
        // @Override
        // public void onItemClick(AdapterView<?> adapterView, View view, int
        // position, long l) {
        // if (position < mDatas.size()) {
        // if (position == mDatas.size() - 1) { // 点击了每天
        // for (int zhujiList = 0; zhujiList < mDatas.size() - 1; zhujiList++) {
        // JSONObject o = JSONObject.parseObject(mDatas.get(zhujiList));
        // o.put("o", 0);
        // mDatas.remove(zhujiList);
        // mDatas.add(zhujiList, o.toJSONString());
        // }
        // } else {
        // JSONObject o = JSONObject.parseObject(mDatas.get(mDatas.size() - 1));
        // o.put("o", 0);
        // mDatas.remove(mDatas.size() - 1);
        // mDatas.add(o.toJSONString());
        // }
        // JSONObject o = JSONObject.parseObject(mDatas.get(position));
        // if (o.getIntValue("o") == 0) {
        // o.put("o", 1);
        // } else {
        // o.put("o", 0);
        // }
        // mDatas.remove(position);
        // if (position != mDatas.size()) {
        // mDatas.add(position, o.toJSONString());
        // } else {
        // mDatas.add(o.toJSONString());
        // }
        // adapter.notifyDataSetChanged();
        // StringBuffer b = new StringBuffer();
        // for (int zhujiList = 0; zhujiList < mDatas.size(); zhujiList++) {
        // o = JSONObject.parseObject(mDatas.get(zhujiList));
        // if (o.getIntValue("o") == 1) {
        // b.append("1");
        // } else {
        // b.append("0");
        // }
        // }
        // onItemClickListener.onItemClick(b.toString(), position);
        // // mDatas.get(position)
        // } else {
        // dismiss();
        // }
        // }
        // });
        // dcsp.putString(Constant.APP_MASTERID, "").commit();
        // refreshData();
    }
}
