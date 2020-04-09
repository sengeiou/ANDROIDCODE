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
import android.media.MediaPlayer;
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
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
import com.p2p.core.GestureDetector;
import com.p2p.core.P2PHandler;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.SmartMedicine.SmartMedicineMainActivity;
import com.smartism.znzk.activity.alert.ChooseAudioSettingMode;
import com.smartism.znzk.activity.camera.MainActivity;
import com.smartism.znzk.activity.camera.RadarAddActivity;
import com.smartism.znzk.activity.common.CLDTimeSetActivity;
import com.smartism.znzk.activity.common.HongCaiSettingActivity;
import com.smartism.znzk.activity.common.HongCaiTantouSettingActivity;
import com.smartism.znzk.activity.common.SettingActivity;
import com.smartism.znzk.activity.common.ZldGSMSettingActivity;
import com.smartism.znzk.activity.device.BeiJingMaoYanActivity;
import com.smartism.znzk.activity.device.BeijingSuoActivity;
import com.smartism.znzk.activity.device.DeviceDetailActivity;
import com.smartism.znzk.activity.device.DeviceInfoActivity;
import com.smartism.znzk.activity.device.DeviceMainActivity;
import com.smartism.znzk.activity.device.DeviceSetGSMPhoneActivity;
import com.smartism.znzk.activity.device.GroupInfoActivity;
import com.smartism.znzk.activity.device.THHistoryActivity;
import com.smartism.znzk.activity.device.ZhujiListFragment;
import com.smartism.znzk.activity.device.add.AddDeviceChooseActivity;
import com.smartism.znzk.activity.device.add.AddGroupActivity;
import com.smartism.znzk.activity.device.add.AddZhujiActivity;
import com.smartism.znzk.activity.device.add.AddZhujiOldActivity;
import com.smartism.znzk.activity.scene.SceneActivity;
import com.smartism.znzk.activity.smartlock.LockMainActivity;
import com.smartism.znzk.activity.user.UserInfoActivity;
import com.smartism.znzk.activity.yaokan.YKDownLoadCodeActivity;
import com.smartism.znzk.adapter.recycleradapter.BaseRecyslerAdapter;
import com.smartism.znzk.adapter.recycleradapter.RecyclerItemBean;
import com.smartism.znzk.adapter.scene.ScenesAdapter;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.camera.P2PListener;
import com.smartism.znzk.camera.SettingListener;
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
import com.smartism.znzk.domain.PersInfo;
import com.smartism.znzk.domain.SceneInfo;
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
import com.smartism.znzk.util.CollectionsUtils;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.IntentUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.LogUtil;
import com.smartism.znzk.util.NetworkUtils;
import com.smartism.znzk.util.ToastUtil;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.util.camera.Utils;
import com.smartism.znzk.util.camera.WifiUtils;
import com.smartism.znzk.view.TextViewAutoVerticalScroll;
import com.smartism.znzk.view.BadgeView;
import com.smartism.znzk.view.DeviceMainTopLinearLyout;
import com.smartism.znzk.view.DevicesMenuPopupWindow;
import com.smartism.znzk.view.ExpandableView.ExpandableLayout;
import com.smartism.znzk.view.ScenePopupWindow;
import com.smartism.znzk.view.SwitchButton.SwitchButton;
import com.smartism.znzk.view.alertview.AlertView;
import com.smartism.znzk.view.alertview.OnItemClickListener;
import com.smartism.znzk.zhicheng.activities.ZCIRRemoteList;
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.smartism.znzk.domain.CommandInfo.CommandTypeEnum.weight;


/**
 * Created by win7 on 2016/11/5.
 */
public class DeviceMainFragment extends Fragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    public static final String TAG = DeviceMainFragment.class.getSimpleName();

    public static final String HONGCAI_TO_TANTOU_SETTING ="com.smartism.hkhctz.ACTION_TANTOU";
    public static  boolean hongcai_tiaozhuan = false ;
    ImageView hongcai_ic_volume ,hongcai_flash_off;
    boolean isHongcaiShowGroup = true ; //默认显示组
    ImageView hongcai_switch_show ;//宏才设备切换显示按钮
    List<DeviceInfo> hongcaiTempDeviceInfos = new ArrayList<>();//保存没被删除或添加设备之前的设备信息，实现跳转到设置界面功能
    List<DeviceInfo> hongcaiDeviceInfos = new ArrayList<>() ;//存放不考虑分组的设备
    private final int dHandlerWhat_initsuccess = 1,
            dHandlerWhat_loadsuccess = 2,
            dHandlerWhat_serverupdatetimeout = 3,
            dHandler_timeout = 4,
            dHandler_timerc = 5,
            dHander_refresh = 6,
            dHandler_scenes = 7,
            dHandler_ipclogin = 8,
            dHandler_panic = 9,
            dHandlerWhat_deletesuccess = 10,
            dHandler_weightInfo = 11,
            dHandler_initContast = 12,
            dHandler_startdiactivity = 13,
            dHandler_notice = 14,
            dHandler_xyjInfo = 15,
            dHandler_logincamera = 16,
            dHandler_scenelist = 17,
            dHandler_scenechoose = 18,
            dHandler_scenesave = 19,
            dHandler_daojishi = 20,
            dHandler_banner_top = 21,
            dHandler_banner_bottom = 22;
    private String old_refulsh_device_id = "";
    // 多主机列表
    private List<ZhujiInfo> zhujiList;

    private DeviceMainActivity mContext;
    private int sortType;
    public int deviceCount;
    private DeviceInfo operationDevice;
    public DeviceInfo deviceZhujiInfo;//主机属性转成对象
    private String operationSort;
    public List<DeviceInfo> deviceInfos;
    private DeviceAdapter deviceAdapter;
    private DevicesMenuPopupWindow itemMenu; //底部菜单
    private Intent deviceIntent = null;
    private View listViewFooterView;
    private View listViewHeadView;
    private boolean checkUpdate = true;
    //    private boolean autoShowAddZhuji = true;
    private String[] adv;
    private int number = 0;
    private boolean initSuccess = false;
    // 下拉刷新控件
    private SwipeRefreshLayout mRefreshLayout;
    private TextViewAutoVerticalScroll textview_auto_roll;

    private LinearLayout ll_device_main, ll_device, nonet_layout, ll_sence_top;
    private DeviceMainTopLinearLyout ll_top;
    private DataCallBack dataCallBack;

    private ScenePopupWindow sceneWindow;
    private List<RecyclerItemBean> sceneList;
    private RecyclerView recycle;
    private ScenesAdapter mAdapter;
    private int securityCount;//是否有安防设备
    View view;
    private LinearLayout ll_notice, ll_no_hub;
    private RelativeLayout ll_main;
    private ImageView iv_notice,iv_call_status;
    private RelativeLayout rl_hub;

    private TextView tv_hub_num, tv_call_status, tv_user_num, tv_current_device, tv_express_zhuji;
    private ImageView iv_add_sence, scene_arming, scene_disarming, scene_home, iv_sence;
    private RecyclerView recycle_main_scence;
    private ListView lv_device;
    private Button btn_add_hub;
    public ExpandableLayout expandable_layout;

    private ImageView iv_hub, iv_hub_line;
    public LinearLayout ll_sence_sl, ll_tel_sms_alarm;
    private ImageView scene_iv;
    private TextView scene_name;
    private MediaPlayer mp;
    private AlertView showAlert;
    private boolean isDelPers;
    private long mId;//当前成员ID
    private long xyjLastTime;
    private InputMethodManager imm;

    private AlertView controlConfrimAlert;//控制确认提示框
    private EditText etName; //催泪密码
    private AlertView mAlertViewExt;//催泪密码
    private AlertView mAnBaForceAlertTip ; //安霸禁用提示框

    private List<ImageBannerInfo.ImageBannerBean> bannerBeanTopList,bannerBeanBottomList; //轮播广告
    private Banner banner_bottom,banner_top;

    private Map<String,String> zhujiSetInfos = new HashMap<>();//主机设置信息



    public void setDataCallBack(DataCallBack dataCallBack) {
        this.dataCallBack = dataCallBack;
    }


    public interface DataCallBack {
        public void getData(Object a);
    }

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
                case dHandler_scenesave:
                    mContext.cancelInProgress();
//                    JavaThreadPool.getInstance().excute(new ScenesLoad());
                    break;
                case dHandler_scenechoose://智能场景列表
                    mContext.cancelInProgress();
                    List<SceneInfo> slist = (List<SceneInfo>) msg.obj;
                    initSelectScence(slist);
                    break;
                case dHandler_scenelist://所有智能场景列表
                    mContext.cancelInProgress();
                    List<SceneInfo> allscene = (List<SceneInfo>) msg.obj;
                    List<RecyclerItemBean> alllist = initDefaultScence(allscene, 1);
                    sceneWindow.updateMenu(alllist);
                    sceneWindow.showAsDropDown(iv_add_sence);
                    mContext.changeWindowAlfa(0.7f);
                    break;
                case dHandlerWhat_initsuccess: //页面创建,加载本地数据库的数据显示,并启动coreservice去连接服务器拉去最新的信息
                  //这里刚进来时从数据库加载，不会因为删除或者添加设备到这
//                    if(Actions.VersionType.CHANNEL_HCTZ.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
//                        hongcaiTempDeviceInfos.addAll(hongcaiDeviceInfos);
//                    }
                    if(MainApplication.app.getAppGlobalConfig().isShowNewAddGroup()){
                        hongcaiTempDeviceInfos.addAll(hongcaiDeviceInfos);
                    }
                    deviceCount = msg.arg1;
                    deviceInfos.clear();
                    deviceInfos.addAll((List<DeviceInfo>) msg.obj);
                    deviceAdapter.notifyDataSetChanged();
                    if (deviceZhujiInfo == null && deviceInfos.size() == 0) {
                        showHub(false);
//                        mContext.main_menu.setVisibility(View.GONE);
//                        mContext.device_main_scnce.setVisibility(View.GONE);
                    } else {
                        showHub(true);
//                        mContext.main_menu.setVisibility(View.VISIBLE);
//                        mContext.device_main_scnce.setVisibility(View.VISIBLE);
//                        mContext.device_main_scnce.setVisibility(View.VISIBLE);
                        if (mContext.getZhuji().isOnline()){
                            ImageLoader.getInstance()
                                    .displayImage(
                                            mContext.dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "") + "/devicelogo/"
                                                    + mContext.getZhuji().getLogo(),
                                            iv_hub, options, new MImageLoadingBar());
                        }else {
                            iv_hub.setImageResource(R.drawable.zhzj_host_lixian);
                        }
                        iv_hub_line.setVisibility(mContext.getZhuji().isOnline() ? View.GONE : View.VISIBLE);
                        tv_hub_num.setText(mContext.getZhuji().getMasterid() == null ? "" : mContext.getZhuji().getMasterid());
                        tv_user_num.setText(mContext.getZhuji().getUc() == 0 ? "" : String.valueOf(mContext.getZhuji().getUc()));
                        setScenceControll(securityCount > 0);
//                        ll_top.setShowSence(securityCount > 0 ? true : false);
                        if (securityCount > 0 || (Actions.VersionType.CHANNEL_ANBABAOQUAN.equals(MainApplication.app.getAppGlobalConfig().getVersion()))) {
                            initGestureDetector();
                        } else {
                            mGestureDetector = null;
                        }
                        if (!TextUtils.isEmpty(mContext.getZhuji().getScene())) {
                            setDefaultScenceBackGround(mContext.getZhuji().getScene());
                        }
                        if (mContext.getZhuji().getStatusCall() == 1 && mContext.getZhuji().getStatusSMS() == 1) {
                            tv_call_status.setText(getString(R.string.zhzj_main_scence_tel_sms));
                            iv_call_status.setImageResource(R.drawable.icon_call_status_green);
                        } else if (mContext.getZhuji().getStatusCall() == 1 && mContext.getZhuji().getStatusSMS() == 0) {
                            tv_call_status.setText(getString(R.string.zhzj_main_scence_tel));
                            iv_call_status.setImageResource(R.drawable.icon_call_status_yellow);
                        } else if (mContext.getZhuji().getStatusCall() == 0 && mContext.getZhuji().getStatusSMS() == 1) {
                            tv_call_status.setText(getString(R.string.zhzj_main_scence_sms));
                            iv_call_status.setImageResource(R.drawable.icon_call_status_yellow);
                        } else if (mContext.getZhuji().getStatusCall() == 0 && mContext.getZhuji().getStatusSMS() == 0) {
                            tv_call_status.setText(getString(R.string.zhzj_main_scence_app));
                            iv_call_status.setImageResource(R.drawable.icon_call_status_red);
                        }
                    }
                    tv_current_device.setText(deviceCount + "");
                    if (dataCallBack != null) {

                        dataCallBack.getData(deviceCount);
                    }
                    JavaThreadPool.getInstance().excute(new ScenesLoad());
                    //获取摄像头登录code 以保证不进入摄像头列表也能接收到移动侦测消息
                    if(Actions.VersionType.CHANNEL_HZYCZN.equals(MainApplication.app.getAppGlobalConfig().getVersion())||
                            Actions.VersionType.CHANNEL_ZHICHENG.equals(MainApplication.app.getAppGlobalConfig().getVersion())){

                    }else{
                        verifyIPCLogin(null);
                    }
                    try {
                        // 启动管理服务器连接的服务
                        Intent intent = new Intent();
                        intent.setClass(mContext.getApplicationContext(), CoreService.class);
                        mContext.startService(intent);
                    } catch (Exception ex) {
                        //oppo 手机有些会调用异常
                        Toast.makeText(mContext, "service start failed", Toast.LENGTH_SHORT).show();
                    }
                    // cancelInProgress();
                    defaultHandler.sendEmptyMessageDelayed(dHandler_timerc, 60000);
                    mContext.menuWindow.updateMenu(mContext.dcsp, mContext.getZhuji(),mContext.getMainShowFragment());
                    mContext.initLeftMenu();

                    showAilienBattery();
                    break;
                case dHandlerWhat_loadsuccess: //页面刷新获取加载完成,包括变更刷新,推送刷新、去服务器拉取刷新等。
                    //此段代码会重复多次调用，请务将不能多次调用的代码放置在此
                    deviceCount = msg.arg1;
                    mContext.cancelInProgress();
                    //下拉刷新超时 和 关闭下拉刷新效果
                    defaultHandler.removeMessages(dHander_refresh);
                    mRefreshLayout.setRefreshing(false);
                    defaultHandler.removeMessages(dHandler_timeout);
                    deviceInfos.clear();
                    deviceInfos.addAll((List<DeviceInfo>) msg.obj);
//                    if(Actions.VersionType.CHANNEL_HCTZ.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
//                        showOrHideHongcai();
//                        if(!isHongcaiShowGroup){
//                            //不显示分组
//                            deviceInfos.clear();
//                            deviceInfos.addAll(hongcaiDeviceInfos);
//                        }
//
//                        addDeviceToTanTou();
//                    }
                    if(MainApplication.app.getAppGlobalConfig().isShowNewAddGroup()){
                        if(!isHongcaiShowGroup){
                               //不显示分组
                              deviceInfos.clear();
                              deviceInfos.addAll(hongcaiDeviceInfos);
                          }
                        if(Actions.VersionType.CHANNEL_HCTZ.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
                            showOrHideHongcai();
                            addDeviceToTanTou();
                        }
                    }
                    if (deviceZhujiInfo == null && deviceInfos.size() == 0) {
                        showHub(false);
//                        mContext.main_menu.setVisibility(View.GONE);
//                        mContext.device_main_scnce.setVisibility(View.GONE);
                    } else {
                        showHub(true);
//                        mContext.main_menu.setVisibility(View.VISIBLE);
//                        mContext.device_main_scnce.setVisibility(View.VISIBLE);
                        if (mContext.getZhuji().isOnline()){
                            ImageLoader.getInstance()
                                    .displayImage(
                                            mContext.dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "") + "/devicelogo/"
                                                    + mContext.getZhuji().getLogo(),
                                            iv_hub, options, new MImageLoadingBar());
                        }else {
                            iv_hub.setImageResource(R.drawable.zhzj_host_lixian);
                        }
                        iv_hub_line.setVisibility(mContext.getZhuji().isOnline() ? View.GONE : View.VISIBLE);
                        tv_hub_num.setText(mContext.getZhuji().getMasterid() == null ? "" : mContext.getZhuji().getMasterid());
                        tv_user_num.setText(mContext.getZhuji().getUc() == 0 ? "" : String.valueOf(mContext.getZhuji().getUc()));
                        setScenceControll(securityCount > 0);
//                        ll_top.setShowSence(securityCount > 0 ? true : false);
                        if (securityCount > 0 || (Actions.VersionType.CHANNEL_ANBABAOQUAN.equals(MainApplication.app.getAppGlobalConfig().getVersion()))) {
                            initGestureDetector(); ////不显示场景的话不需要手势检测
                        } else {
                            mGestureDetector = null;
                        }
                        if (!TextUtils.isEmpty(mContext.getZhuji().getScene())) {
                            setDefaultScenceBackGround(mContext.getZhuji().getScene() == null ? "" : mContext.getZhuji().getScene());
                        }

                        if (mContext.getZhuji().getStatusCall() == 1 && mContext.getZhuji().getStatusSMS() == 1) {
                            tv_call_status.setText(getString(R.string.zhzj_main_scence_tel_sms));
                            iv_call_status.setImageResource(R.drawable.icon_call_status_green);
                        } else if (mContext.getZhuji().getStatusCall() == 1 && mContext.getZhuji().getStatusSMS() == 0) {
                            tv_call_status.setText(getString(R.string.zhzj_main_scence_tel));
                            iv_call_status.setImageResource(R.drawable.icon_call_status_yellow);
                        } else if (mContext.getZhuji().getStatusCall() == 0 && mContext.getZhuji().getStatusSMS() == 1) {
                            tv_call_status.setText(getString(R.string.zhzj_main_scence_sms));
                            iv_call_status.setImageResource(R.drawable.icon_call_status_yellow);
                        } else if (mContext.getZhuji().getStatusCall() == 0 && mContext.getZhuji().getStatusSMS() == 0) {
                            tv_call_status.setText(getString(R.string.zhzj_main_scence_app));
                            iv_call_status.setImageResource(R.drawable.icon_call_status_red);
                        }
                    }
                    tv_current_device.setText(deviceCount + "");
                    if (dataCallBack != null) {

                        dataCallBack.getData(deviceCount);
                    }

//                    devicesAdapter.notifyDataSetChanged();

                    // EventBus.getDefault().post(deviceInfos);
                    deviceAdapter.notifyDataSetChanged();
//                    if (isShowScence) {
//                    }
                    JavaThreadPool.getInstance().excute(new ScenesLoad());
                    Intent intent = null;
                    try {
                        // 启动管理服务器连接的服务 已经启动则不会再次启动
                        intent = new Intent();
                        intent.setClass(mContext.getApplicationContext(), CoreService.class);
                        mContext.startService(intent);
                    } catch (Exception ex) {
                        //oppo 手机有些会调用异常
                        Toast.makeText(mContext, "service start failed", Toast.LENGTH_SHORT).show();
                    }
//                    if (zhuji == null && autoShowAddZhuji) {
//                        mContext.changeFragment("main");
//                        autoShowAddZhuji = false;
//                        if (Actions.VersionType.CHANNEL_AIERFUDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
//                            intent.setClass(mContext.getApplicationContext(), AddZhujiOldActivity.class);
//                        } else {
//                            intent.setClass(mContext.getApplicationContext(), AddZhujiActivity.class);
//                        }
//                        startActivity(intent);
//                    }
//                    if (zhuji != null) {
//                        autoShowAddZhuji = true;
//                    }
                    mContext.menuWindow.updateMenu(mContext.dcsp, mContext.getZhuji(),mContext.getMainShowFragment());
                    mContext.initLeftMenu();
                    // 刷新服务器数据后提醒主机更新
                    if (checkUpdate) {
                        checkUpdate = false;
                        if (mContext.getZhuji() != null) {
                            if (mContext.getZhuji().isOnline()) {
                                SyncMessageContainer.getInstance().sendMessageToServer(SyncMessage.CommandMenu.rq_checkpudate,
                                        SyncMessage.CodeMenu.zero, mContext.getZhuji().getId(), null);
                            }
                        }
                    }
                    showAilienBattery();
                    //检查此时是否需要锁端设备授权
                    JavaThreadPool.getInstance().excute(new SuoAuthority(deviceInfos));
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
                        if (DeviceInfo.CakMenu.security.value().equals(info.getCak())) {
                            if (!"".equals(info.getLastUpdateTime())) {
                                if (info.getLastUpdateTime() < System.currentTimeMillis() - 12 * 60 * 60000) { // 指令超过12小时没有持续的包下来则表示正常了
                                    info.setLastUpdateTime(0);
                                    try {
                                        info.setLastCommand(getString(R.string.deviceslist_server_item_normal));
                                    } catch (IllegalStateException i) {
                                        return true;
                                    }
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
                    int position = msg.arg1;
                    selectScene(position);
                    setDefaultScenceBackGround(String.valueOf(msg.arg2));//这句可以不要，触发场景之后服务器会让APP刷新的
                    break;
                case dHandler_panic: // panic操作完成
                    mContext.cancelInProgress();
                    break;
                case dHandler_ipclogin:// IPC登录完成
                    mContext.cancelInProgress();
                    intent = new Intent();
                    DeviceInfo device = (DeviceInfo) msg.obj;
                    if (TextUtils.isEmpty(device.getIpc())) {
                        intent.setClass(mContext, RadarAddActivity.class);
                    } else {
                        JSONArray array = JSONArray.parseArray(device.getIpc());
                        if (CollectionsUtils.isEmpty(array)) {
                            intent.setClass(mContext, RadarAddActivity.class);
                            intent.putExtra("isMainList", false);
                            intent.putExtra("int", 3);
                        } else {
                            intent.setClass(mContext, MainActivity.class);
                        }
                    }
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
                    mContext.cancelInProgress();
                    Bundle bundle = (Bundle) msg.obj;
                    if (deviceIntent != null) {
                        Contact contact = (Contact) bundle.getSerializable("contact");
                        deviceIntent.putExtra("cameraPaiZi",bundle.getString("cameraPaiZi"));
                        deviceIntent.putExtra("contact", contact);
                    }
                    if(bundle.getString("cameraPaiZi").equals(CameraInfo.CEnum.xiongmai.value())){
                        startActivity(deviceIntent);
                    }else if(bundle.getString("cameraPaiZi").equals(CameraInfo.CEnum.jiwei.value())){
                        deviceIntent.putExtra("connectType", Constants.ConnectType.P2PCONNECT);
                        verifyIPCLogin(deviceIntent);
                    }
                    break;
                case dHandler_logincamera:
                    mContext.cancelInProgress();
                    Log.e("log", "dHandler_logincamera");
//                    P2PHandler.getInstance().p2pInit(mContext, new P2PListener(), new SettingListener());
                    NpcCommon.verifyNetwork(mContext);
                    connect();
                    if (msg.obj == null) return true;
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
                case dHandler_daojishi:
                    if (controlConfrimAlert!=null && controlConfrimAlert.isShowing()){
                        LinearLayout loAlertButtons = (LinearLayout) controlConfrimAlert.getContentContainer().findViewById(R.id.loAlertButtons);
                        TextView textView = (TextView) loAlertButtons.getChildAt(2).findViewById(R.id.tvAlert); //获取到按钮
                        textView.setText(getString(R.string.sure)+"("+msg.arg1+")");
                        if (msg.arg1 <= 0){
                            textView.setClickable(false);
                            textView.setTextColor(getResources().getColor(R.color.gray));
                        }else {
                            defaultHandler.sendMessageDelayed(defaultHandler.obtainMessage(dHandler_daojishi, msg.arg1 - 1, 0), 1000);
                        }
                    }else{
                        defaultHandler.removeMessages(dHandler_daojishi);
                    }
                    break;
                case dHandler_banner_top:
                    bannerBeanTopList = new ArrayList<>();
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
                            bannerBeanTopList.add(bean);
                        }
                        initBanner(bannerBeanTopList,banner_top);
                    }

                    break;
                case dHandler_banner_bottom:
                    bannerBeanBottomList = new ArrayList<>();
                    array = JSON.parseArray((String) msg.obj);
                    if (array != null && array.size() > 0) {
                        for (int i = 0; i < array.size(); i++) {
                            JSONObject object = array.getJSONObject(i);
                            ImageBannerInfo.ImageBannerBean bean = new ImageBannerInfo.ImageBannerBean();
                            bean.setContent(object.getString("content"));
                            bean.setLang(object.getString("lang"));
                            bean.setName(object.getString("name"));
                            bean.setUrl(object.getString("url"));
                            bean.setUrlType(object.getString("urlType"));
                            bannerBeanBottomList.add(bean);
                        }
                        initBanner(bannerBeanBottomList,banner_bottom);
                    }

                    break;
                default:
                    break;
            }
            return false;
        }
    };
    private Handler defaultHandler = new WeakRefHandler(mCallback);


    class SuoAuthority implements  Runnable{
        List<DeviceInfo> infos ;
        public SuoAuthority(@NonNull List<DeviceInfo> infos){
            infos = new ArrayList<>(infos);
            this.infos = infos ;
        }
        @Override
        public void run() {
            final ZhujiInfo zhujiInfo = mContext.getZhuji() ;
            if(zhujiInfo!=null&&zhujiInfo.isAdmin()&&zhujiInfo.isOnline()){
                for(final DeviceInfo deviceInfo:infos){
                    if(deviceInfo.getCa()!=null&&deviceInfo.getCa().equals(DeviceInfo.CaMenu.zhinengsuo.value())){
                        List<CommandInfo> infos = DatabaseOperator.getInstance().queryAllCommands(deviceInfo.getId());
                        for(final CommandInfo info :infos){
                            if(info.getCtype().equals("126")&&!info.getCommand().equals("0")&&(info.getCtime()+60000>System.currentTimeMillis())){
                                final DeviceInfo device_info = deviceInfo;
                                mContext.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showWIFILockRequestUser(zhujiInfo,device_info);
                                    }
                                });
                                break ;
                            }else if(info.getCtype().equals("126")){
                                mContext.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(permissonView!=null&&permissonView.isShowing()){
                                            permissonView.dismiss();
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
            }
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = (DeviceMainActivity) getActivity();
        deviceInfos = new ArrayList<>();
        imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        // 初始化右边菜单
        itemMenu = new DevicesMenuPopupWindow(mContext, this);
        persInfos = new ArrayList<>();
        sceneWindow = new ScenePopupWindow(mContext, new BaseRecyslerAdapter.RecyclerItemClickListener() {
            @Override
            public void onRecycleItemClick(View view, int position) {
                RecyclerItemBean bean = sceneWindow.itemBeans.get(position);
                SceneInfo sceneInfo = (SceneInfo) sceneWindow.itemBeans.get(position).getT();
                if (!bean.isFlag()) {
                    int total = 0;
                    for (RecyclerItemBean itemBean : sceneWindow.itemBeans) {
                        if (itemBean.isFlag())
                            total++;
                    }
                    Log.e("TAG_!!!", "this is " + total);
                    if (total >= 9) {
                        Toast.makeText(mContext, getString(R.string.scene_select_max_msg), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if (sceneInfo.getType() < 3) {
                    boolean flag = sceneWindow.itemBeans.get(position).isFlag();
                    sceneWindow.itemBeans.get(position).setFlag(!flag);
                    sceneWindow.mAdapter.notifyItemChanged(position);
                    mContext.showInProgress(getString(R.string.loading), false, true);
                    JavaThreadPool.getInstance().excute(new SaveScene());
                }
            }
        });
        sceneWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                JavaThreadPool.getInstance().excute(new ScenesLoad());
                mContext.changeWindowAlfa(1.0f);
            }
        });

        if(Actions.VersionType.CHANNEL_HZYCZN.equals(MainApplication.app.getAppGlobalConfig().getVersion())||
                Actions.VersionType.CHANNEL_ZHICHENG.equals(MainApplication.app.getAppGlobalConfig().getVersion())){

        }else{
            verifyIPCLogin(null);
        }


    }
    public void checkAndStartToDevice() {
        JavaThreadPool.getInstance().excute(new Runnable() {
            @Override
            public void run() {
                if (mContext != null && mContext.defaultStartDid != 0) {
                    final DeviceInfo dInfo = DatabaseOperator.getInstance(mContext).queryDeviceInfo(mContext.defaultStartDid);
                    if (dInfo != null) {
                        final ZhujiInfo zjInfo = DatabaseOperator.getInstance(mContext).queryDeviceZhuJiInfo(dInfo.getZj_id());
                        if (zjInfo != null) {
                            mContext.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    startDevicePage(zjInfo, dInfo);
                                    mContext.defaultStartDid = 0; //这个值需要启动后再变更为0，有设备要判断是否是点击通知进入的
                                }
                            });
//                            if (!zjInfo.getMasterid().equals(mContext.dcsp.getString(DataCenterSharedPreferences.Constant.APP_MASTERID, ""))) {
//                                mContext.dcsp.putString(DataCenterSharedPreferences.Constant.APP_MASTERID, zjInfo.getMasterid()).commit();
////                                Intent intent = new Intent();
////                                intent.setAction(Actions.REFRESH_DEVICES_LIST);
////                                mContext.sendBroadcast(intent);
//                            }
                            //替换
                            if(!zjInfo.getMasterid().equals(ZhujiListFragment.getMasterId())){
                                ZhujiListFragment.setMasterId(zjInfo.getMasterid());
                            }
                        }
                    }
                    mContext.showLock = true;
                    //移除设备ID，如果是没有关闭 getIntent是获取不到defaultStartDid的。
                    mContext.getIntent().removeExtra(DataCenterSharedPreferences.Constant.DEVICE_ID);
                }
            }
        });
    }

    private List<PersInfo> persInfos;

    private void initData() {
        JavaThreadPool.getInstance().excute(new loadAllDevicesInfo(dHandlerWhat_initsuccess));
        if (Actions.VersionType.CHANNEL_HONGTAI.equals(MainApplication.app.getAppGlobalConfig().getVersion())||
                Actions.VersionType.CHANNEL_HZYCZN.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
            JavaThreadPool.getInstance().excute(new GetHomeTopBannerImage());
        }
        JavaThreadPool.getInstance().excute(new GetHomeButtomBannerImage());
    }


    boolean isRegist = false;

    @Override
    public void onResume() {
        super.onResume();
        Log.w("jdm", "fragment onresume");
//        mContext.setTitle(mContext.dcsp.getString(DataCenterSharedPreferences.Constant.LOGIN_APPNAME, ""));
//        user_name.setText(mContext.dcsp.getString(DataCenterSharedPreferences.Constant.LOGIN_APPNAME, ""));
//        hometitle.setText(
//                dcsp.getString(DataCenterSharedPreferences.Constant.LOGIN_APPNAME, "") + getString(R.string.deviceslist_server_leftmenu_dtitle));
        if (initSuccess) {
            operationDevice = null;
            SyncMessageContainer.getInstance().produceSendMessage(new SyncMessage(SyncMessage.CommandMenu.rq_refresh));
            refreshData();
        }
        if (itemMenu.isShowing()) {
            itemMenu.dismiss();
        }
        if (!isRegist) {
            //可能会导致广播注册与解绑不一致产生异常
            isRegist = true;
         //   initRegisterReceiver();
        }
        initRegisterReceiver();
        //进入首页清空未读消息有点不合理，暂时屏蔽
//        NotificationUtil.cancelNotification(mContext.getApplicationContext(), DataCenterSharedPreferences.Constant.NOTIFICATIONID);
        initSuccess = true; //以前放在dHandlerWhat_initsuccess初始化完成中,发现这个肯定是可以执行完成的,第一次不用刷新,后面才要刷新
        checkAlertAudio();


        if(mContext.defaultStartDid!=0){
            //查看主机下设备信息
            checkAndStartToDevice();
        }
    }

    //Fragment的show hide方法触发下面回调
    @Override
    public void onHiddenChanged(boolean hidden) {//此方法只会在mainActivity中tab切换时触发
        if (!hidden) {
            mContext.menuWindow.updateMenu(mContext.dcsp, mContext.getZhuji(),mContext.getMainShowFragment());
            refreshData();
            expandableHide();
        }
        Log.d("onHiddenChanged","onHiddenChanged"+hidden);

    }

    @Override
    public void onPause() {
        super.onPause();
        mContext.unregisterReceiver(defaultReceiver);
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

    private void showHub(boolean flag) {
        if (flag) {
            ll_no_hub.setVisibility(View.GONE);
            btn_add_hub.setVisibility(View.GONE);
//            mRefreshLayout.setVisibility(View.VISIBLE);
            ll_main.setVisibility(View.VISIBLE);
        } else {
            btn_add_hub.setVisibility(View.VISIBLE);
            ll_no_hub.setVisibility(View.VISIBLE);
//            mRefreshLayout.setVisibility(View.GONE);
            ll_main.setVisibility(View.GONE);
        }
    }

    private void setDefaultScenceBackGround(String id) {
        if (id.equals(DataCenterSharedPreferences.Constant.SCENE_NOW_SF)) {
            if (expandable_layout.isExpanded()) {
                ll_sence_sl.setVisibility(View.GONE);
            } else if (!expandable_layout.isExpanded() && !isShowScence()) {
                ll_sence_sl.setVisibility(View.GONE);
            }
            scene_iv.setImageResource(R.drawable.zhzj_sl_shefang);
            scene_name.setText(getString(R.string.activity_scene_item_outside));
        } else if (id.equals(DataCenterSharedPreferences.Constant.SCENE_NOW_CF)) {
            if (expandable_layout.isExpanded()) {
                ll_sence_sl.setVisibility(View.GONE);
            } else if (!expandable_layout.isExpanded() && !isShowScence()) {
                ll_sence_sl.setVisibility(View.GONE);
            }
            scene_iv.setImageResource(R.drawable.zhzj_sl_chefang);
            scene_name.setText(getString(R.string.activity_scene_item_home));
        } else if (id.equals(DataCenterSharedPreferences.Constant.SCENE_NOW_HOME)) {
            if (expandable_layout.isExpanded()) {
                ll_sence_sl.setVisibility(View.GONE);
            } else if (!expandable_layout.isExpanded() && !isShowScence()) {
                ll_sence_sl.setVisibility(View.GONE);
            }
            scene_iv.setImageResource(R.drawable.zhzj_sl_zaijia);
            scene_name.setText(getString(R.string.activity_scene_item_inhome));
        } else {

            if (isShowScence() && expandable_layout.isExpanded()) {
                ll_sence_sl.setVisibility(View.GONE);
            } else if (!isShowScence()) {
                ll_sence_sl.setVisibility(View.VISIBLE);
            }
            if (mContext.getZhuji().getScenet() != null) {
                if (mContext.getZhuji().getScenet().equals("0")) {
                    scene_iv.setImageResource(R.drawable.zhzj_sl_zdy);
                } else if (mContext.getZhuji().getScenet().equals("1")) {
                    scene_iv.setImageResource(R.drawable.zhzj_sl_dingshi);
                } else if (mContext.getZhuji().getScenet().equals("2")) {
                    scene_iv.setImageResource(R.drawable.zhzj_sl_liandong);
                }
            } else {
                scene_iv.setImageResource(R.drawable.zhzj_sl_zdy);
            }

            scene_name.setText(mContext.getZhuji().getScene());
        }

        if((Actions.VersionType.CHANNEL_ZHILIDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                &&mContext.getZhuji().getCa().equals(DeviceInfo.CaMenu.bohaoqi.value()))
                ||Actions.VersionType.CHANNEL_WANGDUODUO.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            if(ll_sence_sl.getVisibility()==View.GONE){
                ll_sence_sl.setVisibility(View.VISIBLE);
            }
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_device_main1, container, false);
        return view;
    }

    //显示埃利恩电量
    ImageView ailien_battery_ima  ;
    private void showAilienBattery(){
        if(mContext.getZhuji().getMasterid().contains("FF3B")){
            ailien_battery_ima.setVisibility(View.VISIBLE);
            List<CommandInfo> commandInfos = DatabaseOperator.getInstance().queryAllCommands(mContext.getZhuji().getId());
            if (commandInfos.size()>0){
                for (CommandInfo c : commandInfos) {
                    if (CommandInfo.CommandTypeEnum.battery.value().equals(c.getCtype())){
                        /**
                         * < 20%     : 0格；
                         20% - 35%  : 1格；
                         35% - 50%  : 2格；
                         50% - 70%  : 3格；
                         70% - 100% : 4格；
                         */
                        if (Integer.parseInt(c.getCommand()) < 20){
                            ailien_battery_ima.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.dianliang0));
                        }else if(Integer.parseInt(c.getCommand()) >= 20 && Integer.parseInt(c.getCommand()) < 35){
                            ailien_battery_ima.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.dianliang1));
                        }else if(Integer.parseInt(c.getCommand()) >= 35 && Integer.parseInt(c.getCommand()) < 50){
                            ailien_battery_ima.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.dianliang2));
                        }else if(Integer.parseInt(c.getCommand()) >= 50 && Integer.parseInt(c.getCommand()) < 70){
                            ailien_battery_ima.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.dianliang3));
                        }else{
                            ailien_battery_ima.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.dianliang4));
                        }
                    }
                }
            }
        }else{
            ailien_battery_ima.setVisibility(View.GONE);
        }
    }

    //新增设备跳转至探头
    private void addDeviceToTanTou(){
        //通过广播进行的刷新,可能删除或新增了设备
        if(hongcaiTempDeviceInfos.size()<hongcaiDeviceInfos.size()&&hongcai_tiaozhuan&&isResumed()){
            hongcai_tiaozhuan = false ;//恢复默认值
            //新增了设备,找出增加的设备
            for(int i=0;i<hongcaiDeviceInfos.size();i++){
                if(!hongcaiTempDeviceInfos.contains(hongcaiDeviceInfos.get(i))){
                    //找到了新增的设备
                    operationDevice = hongcaiDeviceInfos.get(i);
                    //探头设备跳转
                    if(!operationDevice.getCa().equals(DeviceInfo.CaMenu.wenshiduji.value())){
                        mContext.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //跳转到主机触发探头设定
                                Intent intent = new Intent();
                                intent.setClass(mContext,HongCaiTantouSettingActivity.class);
                                intent.putExtra("device_id",operationDevice.getId());
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
                    }

                }
            }
        }
        hongcaiTempDeviceInfos.clear();
        hongcaiTempDeviceInfos.addAll(hongcaiDeviceInfos);//保持一致
    }
    //显示或者关闭宏才闪灯报警图标
    private void showOrHideHongcai(){
        List<CommandInfo> temp =  null ;
        if(mContext.defaultStartDid!=0){
            //防止通过通知进来时，mContext.getZhuji()==null的情况,在主线程查找数据库，感觉很难改嗄
            temp  = DatabaseOperator.getInstance().queryAllCommands(DatabaseOperator.getInstance().queryDeviceInfo(mContext.defaultStartDid).getZj_id());
        }else{
            temp = DatabaseOperator.getInstance().queryAllCommands(mContext.getZhuji().getId());
        }
        String command = "";
        if(temp!=null&&temp.size()>0){
            for(CommandInfo commandInfo :temp){
                if(commandInfo.getCtype().equals("109")){
                    command = commandInfo.getCommand();
                    break;
                }
            }
        }
        if(!TextUtils.isEmpty(command)){
            Long destValue = Long.valueOf(command,16);//变成十六进制
            for(int i=0;i<command.length()/2;i++) {
                int result = destValue.byteValue();
                destValue = destValue >>> 8;
                if(i==5){
                    //报警
                    if(result==1){
                        //打开
                        hongcai_ic_volume.setVisibility(View.GONE);
                    }else{
                        //关
                        hongcai_ic_volume.setVisibility(View.VISIBLE);
                    }
                }else if(i==6){
                    //闪灯
                    if(result==1){
                        //打开,不显示
                        hongcai_flash_off.setVisibility(View.GONE);
                    }else{
                        //关，显示提示，表示关了
                        hongcai_flash_off.setVisibility(View.VISIBLE);

                    }
                }
            }
        }else{
            hongcai_flash_off.setVisibility(View.GONE);
            hongcai_ic_volume.setVisibility(View.GONE);
        }
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        hongcai_switch_show = view.findViewById(R.id.hc_switch_show);
        hongcai_flash_off = view.findViewById(R.id.hc_flash_off);
        ailien_battery_ima = view.findViewById(R.id.al_battery_ima);
        hongcai_ic_volume = view.findViewById(R.id.hc_ic_volume);
//        if(Actions.VersionType.CHANNEL_HCTZ.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
//            showOrHideHongcai();
//            hongcai_switch_show.setVisibility(View.VISIBLE);
//            hongcai_switch_show.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if(isHongcaiShowGroup){
//                        isHongcaiShowGroup = false; //不显示宏才分组
//                        hongcai_switch_show.setImageResource(R.drawable.hongcai_hidegroup);
//                    }else{
//                        isHongcaiShowGroup = true ;//显示宏才分组
//                        hongcai_switch_show.setImageResource(R.drawable.hongcai_showgroup);
//                    }
//                    refreshData();
//                }
//            });
//        }

        if(MainApplication.app.getAppGlobalConfig().isShowNewAddGroup()){
            if(Actions.VersionType.CHANNEL_HCTZ.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
                showOrHideHongcai();
            }

            hongcai_switch_show.setVisibility(View.VISIBLE);
            hongcai_switch_show.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isHongcaiShowGroup){
                        isHongcaiShowGroup = false; //不显示宏才分组
                        hongcai_switch_show.setImageResource(R.drawable.hongcai_hidegroup);
                    }else{
                        isHongcaiShowGroup = true ;//显示宏才分组
                        hongcai_switch_show.setImageResource(R.drawable.hongcai_showgroup);
                    }
                    refreshData();
                }
            });
        }


        recycle = (RecyclerView) view.findViewById(R.id.recycle);
        iv_add_sence = (ImageView) view.findViewById(R.id.iv_add_sence);
        iv_add_sence.setOnClickListener(this);
        iv_sence = (ImageView) view.findViewById(R.id.iv_sence);
        iv_sence.setOnClickListener(this);
        expandable_layout = (ExpandableLayout) view.findViewById(R.id.expandable_layout);
        scene_iv = (ImageView) view.findViewById(R.id.sence_iv);
        scene_name = (TextView) view.findViewById(R.id.sence_name);
        //广告view
        ll_notice = (LinearLayout) view.findViewById(R.id.ll_notice);
        ll_sence_sl = (LinearLayout) view.findViewById(R.id.ll_sence_sl);
        iv_notice = (ImageView) view.findViewById(R.id.iv_notice);

        textview_auto_roll = (TextViewAutoVerticalScroll) view.findViewById(R.id.textview_auto_roll);
        lv_device = (ListView) view.findViewById(R.id.lv_device);
        listViewFooterView = LayoutInflater.from(mContext).inflate(R.layout.add_device_footerview, null);
        listViewHeadView = LayoutInflater.from(mContext).inflate(R.layout.activity_devices_list_item_nonet, null);
        listViewFooterView.findViewById(R.id.add_device_foot).setOnClickListener(this);
        mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh_ly);
        ll_device_main = (LinearLayout) view.findViewById(R.id.ll_device_main);
        nonet_layout = (LinearLayout) view.findViewById(R.id.nonet_layout);
        ll_device = (LinearLayout) view.findViewById(R.id.ll_device);
        ll_sence_top = (LinearLayout) view.findViewById(R.id.ll_sence_top);
        ll_sence_top.setOnClickListener(this);
        ll_top = (DeviceMainTopLinearLyout) view.findViewById(R.id.ll_top);
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setColorSchemeResources(R.color.green, R.color.green, R.color.green, R.color.green);
        ll_top.setmRefreshLayout(mRefreshLayout);
        deviceAdapter = new DeviceAdapter(mContext);
//        lv_device.addHeaderView(new View(mContext));
//        lv_device.addFooterView(new View(mContext));
        lv_device.setAdapter(deviceAdapter);

        ll_no_hub = (LinearLayout) view.findViewById(R.id.ll_no_hub);
        iv_hub = (ImageView) view.findViewById(R.id.iv_hub);
        iv_hub_line = (ImageView) view.findViewById(R.id.iv_hub_line);
        btn_add_hub = (Button) view.findViewById(R.id.btn_add_hub);

        ll_main = (RelativeLayout) view.findViewById(R.id.ll_main);
        ll_tel_sms_alarm = (LinearLayout) view.findViewById(R.id.ll_tel_sms_alarm);
        ll_tel_sms_alarm.setOnClickListener(this);
        if (MainApplication.app.getAppGlobalConfig().isShowCallAlarm()
                || MainApplication.app.getAppGlobalConfig().isShowSmsAlarm()) {
            ll_tel_sms_alarm.setVisibility(View.VISIBLE);
        }

        btn_add_hub.setOnClickListener(this);

        tv_hub_num = (TextView) view.findViewById(R.id.tv_hub_num);
        tv_call_status = (TextView) view.findViewById(R.id.tv_call_status);
        tv_user_num = (TextView) view.findViewById(R.id.tv_user_num);
        tv_express_zhuji = (TextView) view.findViewById(R.id.tv_express_zhuji);
        tv_express_zhuji.setOnClickListener(this);

        iv_call_status = (ImageView) view.findViewById(R.id.iv_call_status);

        rl_hub = (RelativeLayout) view.findViewById(R.id.rl_hub);
        rl_hub.setOnClickListener(this);

        scene_arming = (ImageView) view.findViewById(R.id.scene_arming);
        scene_arming.setOnClickListener(this);
        scene_disarming = (ImageView) view.findViewById(R.id.scene_disarming);
        scene_disarming.setOnClickListener(this);
        scene_home = (ImageView) view.findViewById(R.id.scene_home);
        scene_home.setOnClickListener(this);
        nonet_layout.setOnClickListener(this);

        tv_current_device = (TextView) view.findViewById(R.id.tv_user);

        banner_bottom = (Banner) view.findViewById(R.id.banner_bottom);
        banner_top = (Banner) view.findViewById(R.id.banner_top);

        initViewEvent();
        initRecycle();
        initSelectScence(new ArrayList<SceneInfo>());
        initData();
        mp = MediaPlayer.create(mContext, R.raw.sf);

//        initRecycle();
//
//        initSelectScence(new ArrayList<SceneInfo>());
//        JavaThreadPool.getInstance().excute(new ScenesLoad());
//        initDefaultScence(new ArrayList<SceneInfo>(), zhuji);//初始化智能场景

        //网多多需求
        if(Actions.VersionType.CHANNEL_WANGDUODUO.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            ll_sence_sl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (!mContext.getZhuji().isOnline()) {
                        Toast.makeText(mContext, getString(R.string.deviceslist_zhuji_offline), Toast.LENGTH_SHORT).show();
                        return ;
                    }
                    Intent intent = new Intent();
                    intent.setClass(mContext, SceneActivity.class);
                    startActivity(intent);
                }
            });
        }else if(Actions.VersionType.CHANNEL_ANBABAOQUAN.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            mAnBaForceAlertTip = new AlertView(getString(R.string.remind_msg), getString(R.string.abbq_ges_notice_force_tip),
                    null,
                    new String[]{getString(R.string.ready_guide_msg13)}, null,
                    mContext, AlertView.Style.Alert,
                    new OnItemClickListener() {
                        @Override
                        public void onItemClick(Object o, final int position) {
                            if(position!=-1){
                                Log.d(TAG,"点击我知道了");
                            }
                        }
                    });
            mAnBaForceAlertTip.setCancelable(false);
        }

        expandableHide();
    }

    private void expandableHide(){
        ViewGroup.LayoutParams lp = expandable_layout.getLayoutParams() ;
        if(Actions.VersionType.CHANNEL_ZHILIDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            if(mContext.getZhuji()!=null&&mContext.getZhuji().getCa().equals(DeviceInfo.CaMenu.bohaoqi.value())){
                if (MainApplication.app.getAppGlobalConfig().isShowCallAlarm()
                        || MainApplication.app.getAppGlobalConfig().isShowSmsAlarm())
                    ll_tel_sms_alarm.setVisibility(View.VISIBLE);


                lp.height =  0 ;
                ll_sence_sl.setVisibility(View.VISIBLE);
                expandable_layout.setExpanded(false);
            }else{
                //wifi主机隐藏ll_tel_sms_alarm
                ll_tel_sms_alarm.setVisibility(View.GONE);

                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                ll_sence_sl.setVisibility(View.GONE);
                expandable_layout.setExpanded(true);
            }
            expandable_layout.setLayoutParams(lp);
        }else if(Actions.VersionType.CHANNEL_WANGDUODUO.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            lp.height =  0 ;
            expandable_layout.setLayoutParams(lp);
            ll_sence_sl.setVisibility(View.VISIBLE);
            expandable_layout.setExpanded(false);
        }
    }


    /**
     * 手势检测初始化
     */

    private void initGestureDetector() {
        if (mGestureDetector == null) {
            mGestureDetector = new GestureDetector(this.getActivity(), new MyOnGestureListener());
        }
        mRefreshLayout.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mGestureDetector == null)
                    return false;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        if (event.getY() > downY && !expandable_layout.isExpanded()) {//下滑且关闭
                            mRefreshLayout.setEnabled(false);
                        } else {
                            mRefreshLayout.setEnabled(true);
                        }
                        break;
                }
//                if (!expandable_layout.isExpanded()){
//                    mRefreshLayout.setEnabled(false);
//                }else {
//                    mRefreshLayout.setEnabled(true);
//                }
                return (mGestureDetector != null) ? mGestureDetector.onTouchEvent(event) : false;
            }
        });
        ll_top.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return (mGestureDetector != null) ? mGestureDetector.onTouchEvent(event) : false;
            }
        });
        ll_device.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return (mGestureDetector != null) ? mGestureDetector.onTouchEvent(event) : false;
            }
        });
        rl_hub.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return (mGestureDetector != null) ? mGestureDetector.onTouchEvent(event) : false;
            }
        });

        recycle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return (mGestureDetector != null) ? mGestureDetector.onTouchEvent(event) : false;
            }
        });
    }

    /**
     * 初始化Recycler 设备列表 场景点击
     */
    private void initRecycle() {
        if (sceneList == null) {
            sceneList = new ArrayList<>();
        }
        mAdapter = new ScenesAdapter(sceneList);
        mAdapter.setRecyclerItemClickListener(new BaseRecyslerAdapter.RecyclerItemClickListener() {
            @Override
            public void onRecycleItemClick(View view, int position) {
                if (!mContext.getZhuji().isOnline()) {
                    Toast.makeText(getActivity(), getString(R.string.deviceslist_zhuji_offline), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (position < 0 || position >= sceneList.size()) {
                    return;
                }
                SceneInfo sceneInfo = (SceneInfo) sceneList.get(position).getT();

                if (!MainApplication.app.getAppGlobalConfig().isRepeatClickSence()) {
                    if (sceneList.get(position).isFlag()) return;
                }
                switch (sceneInfo.getType()) {
                    case 0:
                        mContext.showInProgress(getString(R.string.loading),false,false);
                        triggerScene(sceneInfo, position);
                        break;
                    case 3:
                        mContext.showInProgress(getString(R.string.loading),false,false);
                        JavaThreadPool.getInstance().excute(new TriggerScene(-3, position));
                        break;
                    case 4:
                        mContext.showInProgress(getString(R.string.loading),false,false);
                        JavaThreadPool.getInstance().excute(new TriggerScene(-1, position));
                        break;
                    case 5:
                        mContext.showInProgress(getString(R.string.loading),false,false);
                        JavaThreadPool.getInstance().excute(new TriggerScene(0, position));
                        break;
                    case 100:
                        if (Actions.VersionType.CHANNEL_ANBABAOQUAN.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                            sendCuiLeiCommand();
                        } else {
                            mContext.showInProgress(getString(R.string.loading),false,false);
                            JavaThreadPool.getInstance().excute(new TriggerPanic());
                        }
                        break;
                }

            }
        });
        //创建默认线性LinearLayoutManager
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, 4, GridLayoutManager.VERTICAL, false);
        recycle.setLayoutManager(gridLayoutManager);  //设置布局管理器
        recycle.setItemAnimator(new DefaultItemAnimator()); //设置Item增加、移除动画
        recycle.setAdapter(mAdapter);
    }



    public void sendCuiLeiCommand() {
        //判断是否设置催泪面积
        List<CommandInfo> commandInfos = DatabaseOperator.getInstance(mContext).queryCommandsByCT(mContext.getZhuji().getId(),"108");
        boolean showTip = false;
        if (!CollectionsUtils.isEmpty(commandInfos)){
            CommandInfo commandInfo = commandInfos.get(0);
            if (Integer.parseInt(commandInfo.getCommand()) == 0){
                showTip = true;
            }
        }else{
            showTip = true;
        }
        if (showTip){
            new AlertView(getString(R.string.activity_weight_notice), mContext.getZhuji().isAdmin() ? getString(R.string.abbq_ges_notice_nomianji) : getString(R.string.abbq_ges_notice_nomianjinoadmin), mContext.getZhuji().isAdmin() ? getString(R.string.deviceslist_server_leftmenu_delcancel) : getString(R.string.sure),
                    mContext.getZhuji().isAdmin() ? new String[]{getString(R.string.abbq_ges_notice_toset)} : null, null, DeviceMainActivity.mthis, AlertView.Style.Alert,
                    new OnItemClickListener() {

                        @Override
                        public void onItemClick(Object o, final int position) {
                            if (position != -1) {
                                Intent intent = new Intent(mContext, CLDTimeSetActivity.class);
                                intent.putExtra("zhuji",mContext.getZhuji());
                                startActivity(intent);
                            }
                        }
                    }).show();
        }else {
            final List<CommandInfo> commandPwds = DatabaseOperator.getInstance(mContext).queryCommandsByCT(mContext.getZhuji().getId(), "pwd_cl");
            if (CollectionsUtils.isEmpty(commandPwds)) {//未设置密码
                new AlertView(getString(R.string.activity_weight_notice), mContext.getZhuji().isAdmin() ? getString(R.string.abbq_ges_pwd_nomianji) : getString(R.string.abbq_ges_pwd_nomianjinoadmin), getString(R.string.sure),
                        null, null, DeviceMainActivity.mthis, AlertView.Style.Alert, null).show();
            } else{
                mAlertViewExt = new AlertView(null, getString(R.string.abbq_cld_pwd_title), getString(R.string.cancel), null, new String[]{getString(R.string.sure)}, mContext, AlertView.Style.Alert, new OnItemClickListener() {
                    @Override
                    public void onItemClick(Object o, int position) {
                        if (position != -1){ //确认密码
                            if (etName.getText().length() != 6){
                                Toast.makeText(mContext,R.string.abbq_update_cld_pwd_length,Toast.LENGTH_SHORT).show();
                            }else if (etName.getText().toString().equals(commandPwds.get(0).getCommand())) {
                                SyncMessage message1 = new SyncMessage();
                                message1.setCommand(SyncMessage.CommandMenu.rq_control.value());
                                message1.setDeviceid(mContext.getZhuji().getId());
                                // 操作
                                mContext.showInProgress(getString(R.string.operationing));
                                defaultHandler.sendEmptyMessageDelayed(dHandler_timeout, 10 * 1000);
                                message1.setSyncBytes(new byte[]{(byte) 101}); //主机固定101 发射催泪弹
                                SyncMessageContainer.getInstance().produceSendMessage(message1);
                            }else{
                                Toast.makeText(mContext,R.string.password_error,Toast.LENGTH_SHORT).show();
                            }
                        }
                        //关闭软键盘
                        imm.hideSoftInputFromWindow(etName.getWindowToken(), 0);
                        //恢复位置
                        mAlertViewExt.setMarginBottom(0);
                    }
                });
                ViewGroup extView = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.alert_password_form, null);
                etName = (EditText) extView.findViewById(R.id.etName);
                CheckBox cbLaws = (CheckBox) extView.findViewById(R.id.cbLaws);
                etName.setText("");
                etName.setHint(getString(R.string.abbq_cld_pwd_title));
                etName.setInputType(InputType.TYPE_CLASS_NUMBER
                        | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                cbLaws.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        String psw = etName.getText().toString();

                        if (isChecked) {
                            etName.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
                        } else {
                            etName.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                        }
                        etName.setSelectAllOnFocus(true);
                        etName.setSelection(psw.length());
                    }
                });
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
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == mContext.RESULT_OK) {

        }
    }

    private float downY = 0, upY;
    private GestureDetector mGestureDetector;

    private class MyOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            downY = e.getY();
            if (securityCount > 0 && !expandable_layout.isExpanded()) {
                mRefreshLayout.setEnabled(false);
            }
//            Log.e(TAG, "touchDown:" + downY);
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            upY = e2.getY();
//            Log.e(TAG, "touchuP: " + upY + " fling e1:" + e1.getY() + ";" + downY);
            if (upY - downY > 20) {
                if (!expandable_layout.isExpanded()) {
                    expandable_layout.expand(true);
                    ll_sence_sl.setVisibility(View.GONE);
                }
            } else if (downY - upY > 20) {
                if (expandable_layout.isExpanded()) {
                    expandable_layout.collapse(true);
                    ll_sence_sl.setVisibility(View.VISIBLE);
                }
            }
            if((Actions.VersionType.CHANNEL_ZHILIDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                    &&mContext.getZhuji().getCa().equals(DeviceInfo.CaMenu.bohaoqi.value()))
                    ||Actions.VersionType.CHANNEL_WANGDUODUO.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
                if(ll_sence_sl.getVisibility()==View.GONE){
                    ll_sence_sl.setVisibility(View.VISIBLE);
                }
            }
            return false;
        }

    }

    /**
     * 加载本地数据库
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
            securityCount = 0;
            persInfos.clear();
            if (mContext.getZhuji() == null || DatabaseOperator.getInstance(mContext).queryDeviceZhuJiInfo(mContext.getZhuji().getId()) == null) {
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mContext.changeFragment("main");//设备列表为空跳转到主机列表
                    }
                });
                return;
            }
            mContext.setZhuji(DatabaseOperator.getInstance(mContext).queryDeviceZhuJiInfo(mContext.getZhuji().getId()));//重新赋值。。状态好刷新
            zhujiSetInfos = DatabaseOperator.getInstance().queryZhujiSets(mContext.getZhuji().getId());
            DeviceInfo shexiangtou = null;
            List<DeviceInfo> deviceList = new ArrayList<DeviceInfo>();
            List<DeviceInfo> deviceList_close = new ArrayList<DeviceInfo>();
            if (mContext.getZhuji() != null) {
                if (MainApplication.app.getAppGlobalConfig().isShowDevicesPermisson() && !mContext.getZhuji().isAdmin()) {
                    persInfos = DatabaseOperator.getInstance(mContext).queryAllPersInfos(mContext.getZhuji().getId());
                    if (persInfos != null && persInfos.size() > 0) {
                        for (PersInfo info : persInfos) {
                            if (PersInfo.UserPerssionKey.p_add.value().equals(info.getK())) {
                                if (info.getV().equals("0")) {
                                    mContext.menuWindow.setAddPers(false);
                                } else {
                                    mContext.menuWindow.setAddPers(true);
                                }
                            } else if (PersInfo.UserPerssionKey.p_del.value().equals(info.getK())) {
                                if (info.getV().equals("0")) {
                                    itemMenu.setDelPers(false);
                                } else {
                                    itemMenu.setDelPers(true);
                                }
                            }
                        }
                    } else {
                        itemMenu.setDelPers(true);
                        mContext.menuWindow.setAddPers(true);
                    }
                }
//                if (mContext.dcsp.getBoolean(DataCenterSharedPreferences.Constant.SHOW_ZHUJI, true)) {
                // 设置属性
//                    DeviceInfo deviceInfo = new DeviceInfo();
//                    deviceInfo.setId(zhuji.getId());
//                    deviceInfo.setName(zhuji.getName());
//                    deviceInfo.setWhere(zhuji.getWhere());
//                    deviceInfo.setStatus(zhuji.getUpdateStatus());
//                    deviceInfo.setControlType(DeviceInfo.ControlTypeMenu.zhuji.value());
//                    deviceInfo.setLogo(zhuji.getLogo());
//                    deviceInfo.setGsm(zhuji.getGsm());
//                    deviceInfo.setFlag(zhuji.isAdmin()); // 利用deviceInfo的flag存主机的是否admin信息
//                    deviceInfo.setPowerStatus(zhuji.getPowerStatus());
//                    deviceInfo.setLowb(zhuji.getBatteryStatus() == 1);//是否底电
                //不加主机对象
//                    deviceList.add(deviceInfo); // 主机实例化一个对象来代替
                deviceZhujiInfo = new DeviceInfo();
                deviceZhujiInfo.setId(mContext.getZhuji().getId());
                deviceZhujiInfo.setZj_id(mContext.getZhuji().getId());
                deviceZhujiInfo.setName(mContext.getZhuji().getName());
                deviceZhujiInfo.setWhere(mContext.getZhuji().getWhere());
                deviceZhujiInfo.setStatus(mContext.getZhuji().getUpdateStatus());
                deviceZhujiInfo.setControlType(DeviceInfo.ControlTypeMenu.zhuji.value());
                deviceZhujiInfo.setLogo(mContext.getZhuji().getLogo());
                deviceZhujiInfo.setGsm(mContext.getZhuji().getGsm());
                deviceZhujiInfo.setFlag(mContext.getZhuji().isAdmin()); // 利用deviceInfo的flag存主机的是否admin信息
                deviceZhujiInfo.setPowerStatus(mContext.getZhuji().getPowerStatus());
                deviceZhujiInfo.setLowb(mContext.getZhuji().getBatteryStatus() == 1);//是否底电
                deviceZhujiInfo.setCa(mContext.getZhuji().getCa());
                deviceZhujiInfo.setCak(mContext.getZhuji().getCak());
//                }
//                if (zhuji.getCa() != DeviceInfo.CakMenu.zhuji.value()) {
//                    deviceList.add(deviceZhujiInfo);
//                }
                List<GroupInfo> gInfos = DatabaseOperator.getInstance(mContext)
                        .queryAllGroups(mContext.getZhuji().getId());
                if (gInfos != null && !gInfos.isEmpty()) {
                    for (GroupInfo g : gInfos) {
                        //宏才
//                        if(Actions.VersionType.CHANNEL_HCTZ.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
//                            //这些组下必须要有设备才将组添加进来
//                            boolean temp = DatabaseOperator.getInstance(mContext).queryGroupsHasDevice(g.getId());
//                            if(!temp){
//                                //组下没有设备就不添加进来
//                                continue;
//                            }
//                        }
                        //宏才的分组方式
                        if(MainApplication.app.getAppGlobalConfig().isShowNewAddGroup()){
                            //这些组下必须要有设备才将组添加进来
                            boolean temp = DatabaseOperator.getInstance(mContext).queryGroupsHasDevice(g.getId());
                            if(!temp){
                                //组下没有设备就不添加进来
                                continue;
                            }
                        }
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
                //通过主机id查询主机下设备
                Cursor cursor = DatabaseOperator.getInstance(mContext).getReadableDatabase().rawQuery(
                        "select * from DEVICE_STATUSINFO where zj_id = ? " + ordersql,
                        new String[]{String.valueOf(mContext.getZhuji().getId())});

                if (cursor != null && cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        DeviceInfo deviceInfo = DatabaseOperator.getInstance(mContext)
                                .buildDeviceInfo(cursor);
                        if (!DeviceInfo.ControlTypeMenu.group.value().equals(deviceInfo.getControlType())
                                && !DeviceInfo.ControlTypeMenu.zhuji.value().equals(deviceInfo.getControlType())
                                && !DeviceInfo.CaMenu.zhujifmq.value().equals(deviceInfo.getCa())) {
                            count++;
                        }
                        if (DeviceInfo.CakMenu.security.value().equals(deviceInfo.getCak())) {
                            securityCount++;
                        }
                        if ("zhuji_fmq".equals(deviceInfo.getCa())) {
                            continue;
                        }

                        if (DatabaseOperator.getInstance(mContext).isInGroup(deviceInfo)) {
                           // continue;
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
                        } else if (DeviceInfo.CaMenu.xueyaji.value().equals(deviceInfo.getCa())) {
                            List<CommandInfo> commandInfos = DatabaseOperator.getInstance(mContext).queryAllCommands(deviceInfo.getId());
                            double h = 0.0, b = 0.0, bh = 0.0;
                            if (commandInfos != null && commandInfos.size() > 0) {
                                for (int i = 0; i < commandInfos.size(); i++) {
                                    if (commandInfos.get(i).getCtype().equals(CommandInfo.CommandTypeEnum.heartrate.value())) { //心率
                                //        h = Double.parseDouble(commandInfos.get(i).getCommand());
                                        h = Util.parseDouble(commandInfos.get(i).getCommand());
//                                        deviceInfo.setLastUpdateTime(commandInfos.get(i).getCtime());
                                    } else if (commandInfos.get(i).getCtype().equals(CommandInfo.CommandTypeEnum.bloodpressure.value())) {//高压
                                      //  b = Double.parseDouble(commandInfos.get(i).getCommand());
                                        b = Util.parseDouble(commandInfos.get(i).getCommand());
//                                        deviceInfo.setLastUpdateTime(commandInfos.get(i).getCtime());
                                    } else if (commandInfos.get(i).getCtype().equals(CommandInfo.CommandTypeEnum.bloodpressureh.value())) {//低压
                                      //  bh = Double.parseDouble(commandInfos.get(i).getCommand());
                                        bh = Util.parseDouble(commandInfos.get(i).getCommand());
//                                        deviceInfo.setLastUpdateTime(commandInfos.get(i).getCtime());
                                    }
                                    mId = commandInfos.get(i).getmId();
                                }
//                                if (mId != 0) {
//                                    mContext.dcsp.putLong(DataCenterSharedPreferences.Constant.USER_ID, mId).commit();
//                                }
                            }
                            deviceInfo.setLastCommand(getString(R.string.deviceslist_device_heartrate) + (int) h + " " + getString(R.string.deviceslist_device_bloodpressure) + (int) b + " " + getString(R.string.deviceslist_device_bloodpressureh) + (int) bh);
                        } else if (DeviceInfo.CaMenu.tizhongceng.value().equals(deviceInfo.getCa())) {
                            List<CommandInfo> commandInfos = DatabaseOperator.getInstance(mContext).queryAllCommands(deviceInfo.getId());
                            double w = 0.0;
                            if (commandInfos != null && commandInfos.size() > 0) {
                                for (int i = 0; i < commandInfos.size(); i++) {
                                    if (commandInfos.get(i).getCtype().equals(weight.value())) { //tizhog
                                     //   w = Double.parseDouble(commandInfos.get(i).getCommand());
                                        w = Util.parseDouble(commandInfos.get(i).getCommand());
                                    }
                                }
                            }
                            deviceInfo.setLastCommand(String.format(Locale.ENGLISH,"%.2f",w));
                        } else if (DeviceInfo.CaMenu.xuetangyi.value().equals(deviceInfo.getCa())) {
                            List<CommandInfo> commandInfos = DatabaseOperator.getInstance(mContext).queryAllCommands(deviceInfo.getId());
                            double w = 0.0;
                            if (commandInfos != null && commandInfos.size() > 0) {
                                for (int i = 0; i < commandInfos.size(); i++) {
                                    if (commandInfos.get(i).getCtype().equals(CommandInfo.CommandTypeEnum.bloodsuggar.value())) { //血糖
                                      //  w = Double.parseDouble(commandInfos.get(i).getCommand());
                                        w = Util.parseDouble(commandInfos.get(i).getCommand());
                                    }
                                }
                            }
                            deviceInfo.setLastCommand(String.format(Locale.ENGLISH,"%.1f",w));
                        } else if (deviceInfo.getControlType().equals(DeviceInfo.ControlTypeMenu.wenshiduji.value())
                                | deviceInfo.getControlType().equals(DeviceInfo.ControlTypeMenu.wenduji.value())) {
                            List<CommandInfo> commandInfos = DatabaseOperator.getInstance(mContext).queryAllCommands(deviceInfo.getId());
                            if (commandInfos != null && commandInfos.size() > 0) {
                                String c = "";
                                for (int j = 0; j < commandInfos.size(); j++) {
                                    if (commandInfos.get(j).getCtype().equals(String.valueOf(CommandInfo.CommandTypeEnum.temperature.value()))) {
                                  //      c = new DecimalFormat("0.0").format(Double.parseDouble(commandInfos.get(j).getCommand())) + "℃";
                                        c = String.format(Locale.ENGLISH,"%.1f",Util.parseDouble(commandInfos.get(j).getCommand())) + "℃";
//                                        deviceInfo.setLastUpdateTime(commandInfos.get(j).getCtime());
                                        break;
                                    }
                                }
                                for (int j = 0; j < commandInfos.size(); j++) {
                                    if (commandInfos.get(j).getCtype().equals(String.valueOf(CommandInfo.CommandTypeEnum.humidity.value()))) {
                                  //      c = c + new DecimalFormat("0.0").format(Double.parseDouble(commandInfos.get(j).getCommand())) + "%";
                                        c = c + String.format(Locale.ENGLISH,"%.1f",Util.parseDouble(commandInfos.get(j).getCommand())) + "%";
                                        break;
                                    }
                                }
                                deviceInfo.setLastCommand(c);
                            }
                        } else if (DeviceInfo.CaMenu.hongwaizhuanfaqi.value().equals(deviceInfo.getCa())) {
                            if (!TextUtils.isEmpty(deviceInfo.getEids())) {
                                JSONArray array = JSONArray.parseArray(deviceInfo.getEids());
                                if (array != null) {
                                    deviceInfo.setStatus(0); // 显示指令
                                    deviceInfo.setLastCommand(
                                            array.size() + getString(R.string.deviceslist_camera_count));
                                }
                            } else {
                                deviceInfo.setLastCommand(
                                        0 + getString(R.string.deviceslist_camera_count));
                            }
                        }
                        //多余的判断
//                        else {
//                            List<CommandInfo> commandInfos = DatabaseOperator.getInstance(mContext).queryAllCommands(deviceInfo.getId());
//                            if (commandInfos != null && commandInfos.size() > 0) {
//                                deviceInfo.setLastCommand(commandInfos.get(0).getCommand());
//                                deviceInfo.setDtype(commandInfos.get(0).getCtype());
//                                deviceInfo.setLastUpdateTime(commandInfos.get(0).getCtime());
//                            }
//                        }
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
                        //原先position为1
//                        deviceList.add(1, shexiangtou);
                        deviceList.add(0, shexiangtou);
                    } else if (Actions.VersionType.CHANNEL_ANBABAOQUAN.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                        shexiangtou = new DeviceInfo();
                        shexiangtou.setName(getString(R.string.zhzj_zhujiipc_title));
                        shexiangtou.setControlType(DeviceInfo.ControlTypeMenu.ipcamera.value());
                        shexiangtou.setCa(DeviceInfo.CaMenu.ipcamera.value());
                        shexiangtou.setAcceptMessage(1);//显示不变灰
                        shexiangtou.setStatus(0); // 显示指令
                        shexiangtou.setLastCommand(
                                0 + getString(R.string.deviceslist_camera_count));// 显示摄像头个数
                        shexiangtou.setLogo("category/FF05/sst_zhzj.png");
                        deviceList.add(0, shexiangtou);
                    }
                    // 使操作的遥控器定住
                    if (operationDevice != null
                            && operationDevice.getControlType().contains(DeviceInfo.ControlTypeMenu.xiaxing.value())) {
                        for (int k = 0; k < deviceList.size(); k++) {
                            if (operationDevice.getId() == deviceList.get(k).getId()&& operationDevice.getwIndex() != -1) {
                                DeviceInfo opDevice = deviceList.get(k);
                                deviceList.remove(k);
                                if (deviceList.size() > operationDevice.getwIndex()) {
                                    deviceList.add(operationDevice.getwIndex(), opDevice);
                                }else{
                                    deviceList.add(opDevice);
                                }
                            }
                        }
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
                deviceList.addAll(deviceList_close);
                //广告显示
                List<DeviceInfo> adsInfo = DatabaseOperator.getInstance(mContext).queryAllAdsInfoWithDeviceInfo(mContext.getZhuji().getId());
                if (!CollectionsUtils.isEmpty(adsInfo)) {
                    if (!deviceList.isEmpty()) {//服务器是随机取一个的。app需要判断是否存在，过滤。
                        for (DeviceInfo dInfo : deviceList) {
                            for (int i = 0; i < adsInfo.size(); i++) {
                                if (dInfo.getCa().equals(adsInfo.get(i).getCa())) {
                                    adsInfo.remove(i);
                                    i--;
                                }
                            }
                        }
                        if (!CollectionsUtils.isEmpty(adsInfo)) {
                            if (DeviceInfo.CaMenu.ipcamera.value().equals(deviceList.get(0).getCa())) {
                                if (deviceList.size() >= 2){
                                    deviceList.addAll(1,adsInfo);
                                }else{
                                    deviceList.addAll(adsInfo);
                                }
                            } else {
                                deviceList.addAll(0,adsInfo);
                            }
                        }
                    } else {
                        deviceList.addAll(adsInfo);
                    }
                }
            } else {
                //没有主机的话 主机对象置为空  否则会崩溃
                deviceZhujiInfo = null;
            }
            mContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //埃利恩隐藏控件
                    if(mContext.getZhuji().getMasterid().contains("FF3B")){
                        iv_add_sence.setVisibility(View.GONE);
                    }else{
                        iv_add_sence.setVisibility(View.VISIBLE);
                    }
                }
            });

//            if(Actions.VersionType.CHANNEL_HCTZ.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
//             hongcaiDeviceInfos.clear();//清空以前设备
//            if(deviceList!=null&&deviceList.size()>0){
//                for(DeviceInfo temp : deviceList){
//                    //保存所有的设备，不管是否在组里面
//                    if(!temp.getControlType().equals("group")){
//                        //不是组设备，是真实的设备，保存起来
//                        hongcaiDeviceInfos.add(temp);
//                    }
//                }
//
//                for(int i=0;i<deviceList.size();i++){
//                    //去掉已经分组的设备
//                    if(DatabaseOperator.getInstance(mContext).isInGroup(deviceList.get(i))){
//                        deviceList.remove(i);
//                        i--;
//                    }
//                }
//              }
//            }
            if(MainApplication.app.getAppGlobalConfig().isShowNewAddGroup()){
                hongcaiDeviceInfos.clear();//清空以前设备
                if(deviceList!=null&&deviceList.size()>0){
                    for(DeviceInfo temp : deviceList){
                        //保存所有的设备，不管是否在组里面
                        if(!temp.getControlType().equals("group")){
                            //不是组设备，是真实的设备，保存起来
                            hongcaiDeviceInfos.add(temp);
                        }
                    }

                    for(int i=0;i<deviceList.size();i++){
                        //去掉已经分组的设备
                        if(DatabaseOperator.getInstance(mContext).isInGroup(deviceList.get(i))){
                            deviceList.remove(i);
                            i--;
                        }
                    }
              }
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
            ImageView low, power, ioc, ioc_wendu, ioc_shidu, ioc_showright, ioc_arming, ioc_disarming, ioc_home, ioc_panic, zhuji_anim, mode;
            TextView name, time, command, command_shidu, type, type_left, type_right, tv_chvalue;
            ImageButton button;
            RelativeLayout device_item_layout;
            LinearLayout cLayout, rLayout;
            LinearLayout sceneLayout;
            FrameLayout fLayout;
            BadgeView badgeView;

            SwitchButton switchButton;
        }

        LayoutInflater layoutInflater;

        public DeviceAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return deviceInfos.size();
        }

        @Override
        public Object getItem(int arg0) {
            return deviceInfos.get(arg0);
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
            DeviceInfoView viewCache = null;
            if (view == null) {
                viewCache = new DeviceInfoView();
//                view = layoutInflater.inflate(R.layout.activity_devices_list_item, null);

                view = LayoutInflater.from(mContext).inflate(R.layout.item_device_main_devices, viewGroup, false);
                viewCache.ioc = (ImageView) view.findViewById(R.id.iv_device_logo);
                viewCache.name = (TextView) view.findViewById(R.id.tv_device_name);
                viewCache.switchButton = (SwitchButton) view.findViewById(R.id.c_switchButton);

                viewCache.device_item_layout = (RelativeLayout) view.findViewById(R.id.device_item_layout);
                viewCache.low = (ImageView) view.findViewById(R.id.device_low);
//                viewCache.power = (ImageView) view.findViewById(R.id.device_power);
//                viewCache.ioc = (ImageView) view.findViewById(R.id.device_logo);
                viewCache.badgeView = new BadgeView(mContext, viewCache.ioc);
                viewCache.badgeView.setBadgeMargin(0, 0);
                viewCache.badgeView.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
                viewCache.badgeView.setTextSize(10);
                viewCache.ioc_wendu = (ImageView) view.findViewById(R.id.wendu_img);
                viewCache.ioc_shidu = (ImageView) view.findViewById(R.id.wendu_shidu_img);
                viewCache.mode = (ImageView) view.findViewById(R.id.device_mode);
//                viewCache.mode.setBackground(Util.createReadBgShapeDrawable(mContext));
//                viewCache.name = (TextView) view.findViewById(R.id.device_name);
                viewCache.time = (TextView) view.findViewById(R.id.last_time);
                viewCache.type = (TextView) view.findViewById(R.id.device_type);
                viewCache.type_left = (TextView) view.findViewById(R.id.device_type_left);
                viewCache.type_right = (TextView) view.findViewById(R.id.device_type_right);
                viewCache.command = (TextView) view.findViewById(R.id.last_command);
                viewCache.command_shidu = (TextView) view.findViewById(R.id.last_command_shidu);
                viewCache.button = (ImageButton) view.findViewById(R.id.c_one_button);
                viewCache.ioc_showright = (ImageView) view.findViewById(R.id.c_img);
//                viewCache.rLayout = (LinearLayout) view.findViewById(R.id.r_layout);
//                viewCache.cLayout = (LinearLayout) view.findViewById(R.id.c_layout);
//                viewCache.sceneLayout = (LinearLayout) view.findViewById(R.id.scene_layout);
//                viewCache.ioc_arming = (ImageView) view.findViewById(R.id.scene_arming);
//                viewCache.ioc_disarming = (ImageView) view.findViewById(R.id.scene_disarming);
//                viewCache.ioc_home = (ImageView) view.findViewById(R.id.scene_home);
//                viewCache.ioc_panic = (ImageView) view.findViewById(R.id.scene_panic);
//                viewCache.zhuji_anim = (ImageView) view.findViewById(R.id.zhuji_anim);
                viewCache.tv_chvalue = (TextView) view.findViewById(R.id.tv_chvalue);
                view.setTag(viewCache);
            } else {
                viewCache = (DeviceInfoView) view.getTag();
            }


            setDeviceLogoAndName(viewCache, i);
//            viewCache.logo.setImageResource(R.drawable.zhzj_menci);
//            viewCache.device_name.setText(deviceInfos.get(i).getName());

            initButtonEvent(viewCache, i);
            setCommand(viewCache, i);
            setShowOrHide(viewCache, i);
            setTypeAndBackground(viewCache, i);
            setBadeNumber(viewCache, i);
//            setModen(viewCache, i);

            return view;
        }

        /**
         * 设置设备logo图片和名称
         *
         * @param
         */
        private void setDeviceLogoAndName(DeviceInfoView viewCache, int i) {
            DeviceInfo deviceInfo = deviceInfos.get(i);
            if (DeviceInfo.ControlTypeMenu.adsinfo.value().equals(deviceInfos.get(i).getControlType())) {//广告的logo
                ImageLoader.getInstance().displayImage(deviceInfos.get(i).getLogo(), viewCache.ioc, options, new MImageLoadingBar());
                viewCache.name.setText(deviceInfos.get(i).getName());
            }else if (DeviceInfo.ControlTypeMenu.wenduji.value().equals(deviceInfos.get(i).getControlType())) {
//                // 设置图片
//                if (Actions.VersionType.CHANNEL_UCTECH.equals(((MainApplication) mContext.getApplication()).getAppGlobalConfig().getVersion())) {
//                    try {
//                        viewCache.ioc.setImageBitmap(BitmapFactory.decodeStream(
//                                mContext.getAssets().open("uctech/uctech_t_" + deviceInfos.get(i).getChValue() + ".png")));
//                    } catch (IOException e) {
//                        Log.e("uctech", "读取图片文件错误");
//                    }
//                } else {
//                }

                ImageLoader.getInstance()
                        .displayImage(mContext.dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "") + "/devicelogo/"
                                        + deviceInfos.get(i).getLogo(),
                                viewCache.ioc, options, new MImageLoadingBar());
                viewCache.name.setText(deviceInfos.get(i).getName());
            } else if (DeviceInfo.ControlTypeMenu.wenshiduji.value().equals(deviceInfos.get(i).getControlType())) {
//                if (Actions.VersionType.CHANNEL_UCTECH.equals(((MainApplication) mContext.getApplication()).getAppGlobalConfig().getVersion())) {
//                    try {
//                        viewCache.ioc.setImageBitmap(BitmapFactory.decodeStream(
//                                mContext.getAssets().open("uctech/uctech_th_" + deviceInfos.get(i).getChValue() + ".png")));
//                    } catch (IOException e) {
//                        Log.e("uctech", "读取图片文件错误");
//                    }
//                } else {
//                }
                ImageLoader.getInstance()
                        .displayImage(mContext.dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "") + "/devicelogo/"
                                        + deviceInfos.get(i).getLogo(),
                                viewCache.ioc, options, new MImageLoadingBar());
                viewCache.name.setText(deviceInfos.get(i).getName());
            }else {
                // 设置图片
                ImageLoader.getInstance().displayImage(mContext.dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "")
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
            if (Actions.VersionType.CHANNEL_LILESI
                    .equals(MainApplication.app.getAppGlobalConfig().getVersion())
                    || Actions.VersionType.CHANNEL_AIERFUDE
                    .equals(MainApplication.app.getAppGlobalConfig().getVersion())
                    || Actions.VersionType.CHANNEL_UHOME.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                viewCache.time.setText(""); //立乐斯屏蔽时间
            } else {
                viewCache.time.setText(formatTime(deviceInfos.get(i).getLastUpdateTime()));
            }
            if (DeviceInfo.ControlTypeMenu.adsinfo.value().equals(deviceInfos.get(i).getControlType())) {//广告的logo
                viewCache.command.setText(deviceInfos.get(i).getLastCommand());
                viewCache.command_shidu.setVisibility(View.GONE);
                viewCache.ioc_shidu.setVisibility(View.GONE);
                viewCache.ioc_wendu.setVisibility(View.GONE);
            }else if (DeviceInfo.CaMenu.tizhongceng.value().equals(deviceInfos.get(i).getCa())) {
                try {
                 //   double commandValue = Double.parseDouble(deviceInfos.get(i).getLastCommand());
                    double commandValue = Util.parseDouble(deviceInfos.get(i).getLastCommand());
                    if (commandValue == 0) {
                        viewCache.command.setText("");
                    } else if (mContext.dcsp.getString(Constant.WEIGHT_UNIT, Constant.WEIGHT_UNIT_KG).equals(Constant.WEIGHT_UNIT_KG)) {
                        viewCache.command.setText(commandValue + Constants.WeightUnit.WEIGHT_KG);
                    } else {
                        viewCache.command.setText(Util.kgTolbs(commandValue) + Constants.WeightUnit.WEIGHT_LB);
                    }
                } catch (Exception e) {
                    viewCache.command.setText("0.0");
                    viewCache.command.setText("");
                }
                viewCache.command_shidu.setVisibility(View.GONE);
                viewCache.ioc_shidu.setVisibility(View.GONE);
                viewCache.ioc_wendu.setVisibility(View.GONE);
            } else if (DeviceInfo.CaMenu.hongwaizhuanfaqi.value().equals(deviceInfos.get(i).getCa())) {
//                viewCache.command.setText("".equals(deviceInfos.get(i).getLastCommand())
//                        ? "" : deviceInfos.get(i).getLastCommand());
                viewCache.command.setText(deviceInfos.get(i).getLastCommand());
                viewCache.command_shidu.setVisibility(View.GONE);
                viewCache.ioc_shidu.setVisibility(View.GONE);
                viewCache.ioc_wendu.setVisibility(View.GONE);
            } else if (DeviceInfo.CaMenu.wenduji.value().equals(deviceInfos.get(i).getCa())
                | DeviceInfo.CaMenu.wenshiduji.value().equals(deviceInfos.get(i).getCa())) {
                if (!StringUtils.isEmpty(deviceInfos.get(i).getChValue())) {
                    viewCache.tv_chvalue.setText("CH" + deviceInfos.get(i).getChValue());
                }else{
                    viewCache.tv_chvalue.setText("");
                }
//                20.39℃65%
                String command = deviceInfos.get(i).getLastCommand();
                if (!StringUtils.isEmpty(command)) {
                    if (command.contains("℃")) {
                        if (mContext.dcsp.getString(DataCenterSharedPreferences.Constant.SHOW_TEMPERATURE_UNIT, "ssd").equals("ssd")) {
                        //    viewCache.command.setText(format.format(Double.parseDouble(command.substring(0, command.indexOf("℃")))) + "℃");
                            viewCache.command.setText(String.format(Locale.ENGLISH,"%.1f",Util.parseDouble(command.substring(0, command.indexOf("℃")))) + "℃");
                        } else if (mContext.dcsp.getString(DataCenterSharedPreferences.Constant.SHOW_TEMPERATURE_UNIT, "ssd").equals("hsd")) {
//                            viewCache.command.setText(format.format((Math
//                                    .round((Float.parseFloat(command.substring(0, command.indexOf("℃"))) * 1.8 + 32) * 10)
//                                    / 10)) + "℉");
                            viewCache.command.setText(String.format(Locale.ENGLISH,"%.1f",(float) ((Float.parseFloat(command.substring(0, command.indexOf("℃"))) * 1.8 + 32) * 10
                                    / 10)) + "℉");
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
                } else {
                    viewCache.command_shidu.setVisibility(View.GONE);
                    viewCache.ioc_shidu.setVisibility(View.GONE);
                    viewCache.ioc_wendu.setVisibility(View.GONE);
                    viewCache.command.setText("");
                }
            } else if (deviceInfos.get(i).getControlType().equals(DeviceInfo.ControlTypeMenu.zhuji.value())) {
                viewCache.command.setText(mContext.getZhuji().getUc() + " " + getString(R.string.deviceslist_server_totalonlineapps)
                        + "  " + deviceCount + " " + getString(R.string.deviceslist_server_totaldevices));
                viewCache.command_shidu.setVisibility(View.GONE);
                viewCache.ioc_shidu.setVisibility(View.GONE);
                viewCache.ioc_wendu.setVisibility(View.GONE);
            } else if (DeviceInfo.CakMenu.security.value().equals(deviceInfos.get(i).getCak())) {
                viewCache.command_shidu.setVisibility(View.GONE);
                viewCache.ioc_shidu.setVisibility(View.GONE);
                viewCache.ioc_wendu.setVisibility(View.GONE);
                if(DeviceInfo.CaMenu.ybq.value().equals(deviceInfos.get(i).getCa())){
                    List<CommandInfo> infos = deviceInfos.get(i).getdCommands() ;
                    if(deviceInfos.get(i).getAcceptMessage()==2){
                        if(infos.size()>0){
                            for(CommandInfo info:infos){
                                if(info.getCtype().equals("101")){
                                    viewCache.command.setText(info.getCommand());
                                    break  ;
                                }
                            }
                        }
                    }else{
                        viewCache.command.setText(deviceInfos.get(i).getLastCommand());
                    }

                }else {
                    if (deviceInfos.get(i).getStatus() == 0) {
                        viewCache.command.setText("".equals(deviceInfos.get(i).getLastCommand())
                                ? getString(R.string.deviceslist_server_item_normal) : deviceInfos.get(i).getLastCommand());
                    } else if (deviceInfos.get(i).getStatus() == 1) { // 显示正常
                        viewCache.command.setText(getString(R.string.normal));
                        viewCache.time.setText("");
                    }
                    //网多多燃气报警器在添加上来之后会发数值，屏蔽数值
                    if(Actions.VersionType.CHANNEL_WANGDUODUO.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                            &&deviceInfos.get(i).getCa().equals(DeviceInfo.CaMenu.rangqibaojing.value())){
                        String commands = deviceInfos.get(i).getLastCommand() ;
                        if(commands!=null){
                            try{
                                Integer.parseInt(commands);
                                viewCache.command.setText("");
                            }catch (NumberFormatException e){

                            }
                        }
                    }
                }
            } else {
                String lastCommand = "".equals(deviceInfos.get(i).getLastCommand())
                        ? getString(R.string.deviceslist_server_item_normal) : deviceInfos.get(i).getLastCommand();
                viewCache.command.setText(lastCommand);
                viewCache.command_shidu.setVisibility(View.GONE);
                viewCache.ioc_shidu.setVisibility(View.GONE);
                viewCache.ioc_wendu.setVisibility(View.GONE);
                if(Actions.VersionType.CHANNEL_AIERFUDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())&&deviceInfos.get(i).getCa()!=null
                        &&deviceInfos.get(i).getCa().equals(DeviceInfo.CaMenu.zhinengsuo.value())){
                    viewCache.command.setText("");
                }

                if(deviceInfos.get(i).getCa()!=null&&(deviceInfos.get(i).getCa().equals("fj")||deviceInfos.get(i).getCa().equals("qf"))){
                    if(deviceInfos.get(i).getLastCommand()!=null&&!deviceInfos.get(i).getLastCommand().equals("")){
                        viewCache.command.setText("");
                    }
                    List<CommandInfo> infos = deviceInfos.get(i).getdCommands() ;
                    if(infos!=null&&infos.size()>0){
                        for(CommandInfo info:infos){
                            if(info.getCtype().equals("95")){
                                if(info.getCommand().equals("1")){
                                    viewCache.command.setText(getString(R.string.gensture_open));
                                }else if(info.getCommand().equals("0")){
                                    viewCache.command.setText(getString(R.string.gensture_off));
                                }
                            }
                        }
                    }
                }else if(DeviceInfo.CaMenu.ybq.value().equals(deviceInfos.get(i).getCa())){
                    List<CommandInfo> infos = deviceInfos.get(i).getdCommands() ;
                    if(infos!=null&&infos.size()>0){
                        for(CommandInfo info:infos){
                            if(info.getCtype().equals("101")){
                                viewCache.command.setText(info.getCommand());
                                break  ;
                            }
                        }
                    }
                }
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
            if (DeviceInfo.ControlTypeMenu.adsinfo.value().equals(deviceInfos.get(i).getControlType())) { //广告的需要放在第一个if
//                viewCache.cLayout.setVisibility(View.VISIBLE);
                viewCache.ioc_showright.setVisibility(View.VISIBLE);
                viewCache.button.setVisibility(View.GONE);
                viewCache.switchButton.setVisibility(View.GONE);
                viewCache.tv_chvalue.setVisibility(View.GONE);
                viewCache.time.setText("");
            }else if (DeviceInfo.ControlTypeMenu.wenduji.value().equals(deviceInfos.get(i).getControlType()) ||
                    DeviceInfo.ControlTypeMenu.wenshiduji.value().equals(deviceInfos.get(i).getControlType())) {
//                deviceInfo.setChValue(cursor.getString(cursor.getColumnIndex("re_1")));
                viewCache.ioc_showright.setVisibility(View.GONE);
                viewCache.switchButton.setVisibility(View.GONE);
                viewCache.tv_chvalue.setVisibility(View.VISIBLE);
            } else if (DeviceInfo.CaMenu.zhinengsuo.value().equals(deviceInfos.get(i).getCa())
                    | DeviceInfo.CaMenu.maoyan.value().equals(deviceInfos.get(i).getCa())) {//智能锁 猫眼
//                viewCache.cLayout.setVisibility(View.VISIBLE);
                viewCache.ioc_showright.setVisibility(View.VISIBLE);
                viewCache.button.setVisibility(View.GONE);
                viewCache.switchButton.setVisibility(View.GONE);
                viewCache.command.setVisibility(View.GONE);
                viewCache.time.setText("");
                viewCache.tv_chvalue.setVisibility(View.GONE);
            } else if (DeviceInfo.CaMenu.znyx.value().equals(deviceInfos.get(i).getCa())) {
                viewCache.ioc_showright.setVisibility(View.VISIBLE);
                viewCache.switchButton.setVisibility(View.GONE);
                viewCache.command.setVisibility(View.VISIBLE);
                viewCache.tv_chvalue.setVisibility(View.GONE);
//                viewCache.time.setText("");
            } else if (DeviceInfo.CaMenu.hongwaizhuanfaqi.value().equals(deviceInfos.get(i).getCa())) { //红外转发器
//                viewCache.cLayout.setVisibility(View.VISIBLE);
                viewCache.ioc_showright.setVisibility(View.VISIBLE);
                viewCache.button.setVisibility(View.GONE);
                viewCache.switchButton.setVisibility(View.GONE);
                viewCache.command.setVisibility(View.VISIBLE);
                viewCache.time.setText("");
                viewCache.tv_chvalue.setVisibility(View.GONE);
            } else if (deviceInfos.get(i).getControlType().equals(DeviceInfo.ControlTypeMenu.xiaxing_1.value())) {
//                viewCache.cLayout.setVisibility(View.VISIBLE);
                viewCache.ioc_showright.setVisibility(View.GONE);

                if(Actions.VersionType.CHANNEL_WANGDUODUO.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
                    viewCache.button.setVisibility(View.VISIBLE);
                }else{
                    viewCache.button.setVisibility(View.GONE);
                }

                viewCache.switchButton.setVisibility(View.GONE);
                viewCache.command.setVisibility(View.VISIBLE);
                viewCache.time.setVisibility(View.VISIBLE);
                viewCache.tv_chvalue.setVisibility(View.GONE);
            } else if (deviceInfos.get(i).getControlType().equals(DeviceInfo.ControlTypeMenu.xiaxing_2.value())) {
//                viewCache.cLayout.setVisibility(View.VISIBLE);
                viewCache.ioc_showright.setVisibility(View.GONE);
                viewCache.button.setVisibility(View.GONE);
             //   viewCache.switchButton.setCheckedImmediatelyNoEvent(true);
                viewCache.switchButton.setVisibility(View.VISIBLE);
                List<CommandInfo> commandInfos = deviceInfos.get(i).getdCommands() ;
                boolean isHave95 = false;
                if(commandInfos!=null&&commandInfos.size()>0){
                    for(CommandInfo info : commandInfos){
                        if(info.getCtype().equals("95")){
                            isHave95 = true;
                            if(info.getCommand().equals("1")){
                                viewCache.switchButton.setCheckedImmediatelyNoEvent(true);
                            }else{
                                viewCache.switchButton.setCheckedImmediatelyNoEvent(false);
                            }
                            break ;
                        }
                    }
                }
                if (!isHave95){
                    if (deviceInfos.get(i).getDr() == 1) {
                        viewCache.switchButton.setCheckedImmediatelyNoEvent(true);
                    } else {
                        viewCache.switchButton.setCheckedImmediatelyNoEvent(false);
                    }
                }
                viewCache.command.setVisibility(View.VISIBLE);
                viewCache.time.setVisibility(View.VISIBLE);
                viewCache.tv_chvalue.setVisibility(View.GONE);
            } else if (deviceInfos.get(i).getControlType().contains("xiaxing")) {
//                viewCache.cLayout.setVisibility(View.VISIBLE);
                viewCache.ioc_showright.setVisibility(View.VISIBLE);
                viewCache.button.setVisibility(View.GONE);
                viewCache.switchButton.setVisibility(View.GONE);
                viewCache.command.setVisibility(View.VISIBLE);
                viewCache.time.setVisibility(View.VISIBLE);
                viewCache.tv_chvalue.setVisibility(View.GONE);
            } else if (DeviceInfo.ControlTypeMenu.neiqian.value().equals(deviceInfos.get(i).getControlType())
                    || DeviceInfo.ControlTypeMenu.group.value().equals(deviceInfos.get(i).getControlType())) {
//                viewCache.cLayout.setVisibility(View.VISIBLE);
                viewCache.ioc_showright.setVisibility(View.VISIBLE);
               viewCache.button.setVisibility(View.GONE);
                viewCache.switchButton.setVisibility(View.GONE);
                viewCache.command.setVisibility(View.GONE);
                viewCache.tv_chvalue.setVisibility(View.GONE);
                viewCache.time.setText("");
            } else if (deviceInfos.get(i).getCak() != null && (deviceInfos.get(i).getCak().contains(DeviceInfo.CakMenu.health.value())
                    || deviceInfos.get(i).getCak().contains(DeviceInfo.CakMenu.detection.value()))) {
//                viewCache.cLayout.setVisibility(View.VISIBLE);
                viewCache.ioc_showright.setVisibility(View.VISIBLE);
                //迎宾器
                if(deviceInfos.get(i).getCa().equals("ybq")&&!deviceInfos.get(i).isFa()){
                    viewCache.ioc_showright.setVisibility(View.GONE);
                    viewCache.switchButton.setVisibility(View.VISIBLE);
                    //初始状态设置
                    if (deviceInfos.get(i).getAcceptMessage() == 0) {
                        viewCache.switchButton.setCheckedImmediatelyNoEvent(false);
                    } else {
                        viewCache.switchButton.setCheckedImmediatelyNoEvent(true);
                    }
                }else{
                    viewCache.switchButton.setVisibility(View.GONE);
                }
               viewCache.button.setVisibility(View.GONE);
                viewCache.tv_chvalue.setVisibility(View.GONE);
                //不需要
                viewCache.command.setVisibility(View.VISIBLE);
                viewCache.time.setVisibility(View.VISIBLE);

            } else if ("sst".equals(deviceInfos.get(i).getCa())) {
                viewCache.switchButton.setVisibility(View.GONE);
                viewCache.ioc_showright.setVisibility(View.VISIBLE);
                viewCache.tv_chvalue.setVisibility(View.GONE);
            } else if (DeviceInfo.CaMenu.ldtsq.value().equals(deviceInfos.get(i).getCa())
                    ||DeviceInfo.CaMenu.yxj.value().equals(deviceInfos.get(i).getCa())
                    ||DeviceInfo.CaMenu.menling.value().equalsIgnoreCase(deviceInfos.get(i).getCa())) {//门铃需要显示开关按钮
//                viewCache.cLayout.setVisibility(View.VISIBLE);
                viewCache.ioc_showright.setVisibility(View.GONE);
               viewCache.button.setVisibility(View.GONE);
                viewCache.switchButton.setVisibility(View.VISIBLE);
                viewCache.tv_chvalue.setVisibility(View.GONE);
                if (deviceInfos.get(i).getAcceptMessage() == 0) {
                    viewCache.switchButton.setCheckedImmediatelyNoEvent(false);
                } else {
                    viewCache.switchButton.setCheckedImmediatelyNoEvent(true);
                }
                viewCache.command.setVisibility(View.VISIBLE);
//                viewCache.time.setVisibility(View.VISIBLE);
            }else if (deviceInfos.get(i).getControlType().contains("shangxing")
                    || deviceInfos.get(i).getControlType().contains("fangdiu")
                    || deviceInfos.get(i).getCak().contains(DeviceInfo.CakMenu.security.value())) {
//                viewCache.cLayout.setVisibility(View.VISIBLE);
                viewCache.ioc_showright.setVisibility(View.VISIBLE);
               viewCache.button.setVisibility(View.GONE);
                viewCache.switchButton.setVisibility(View.GONE);
                viewCache.tv_chvalue.setVisibility(View.GONE);
                if (deviceInfos.get(i).getAcceptMessage() == 0) {
                    viewCache.switchButton.setCheckedImmediatelyNoEvent(false);
                } else {
                    viewCache.switchButton.setCheckedImmediatelyNoEvent(true);
                }
                viewCache.command.setVisibility(View.VISIBLE);
//                viewCache.time.setVisibility(View.VISIBLE);
            }else{
                viewCache.ioc_showright.setVisibility(View.VISIBLE);
                viewCache.button.setVisibility(View.GONE);
                viewCache.switchButton.setVisibility(View.GONE);
                viewCache.tv_chvalue.setVisibility(View.GONE);
                viewCache.command.setVisibility(View.VISIBLE);
                viewCache.time.setVisibility(View.VISIBLE);
            }


            if(deviceInfos.get(i).getCa()!=null){
                if(deviceInfos.get(i).getCa().equals(DeviceInfo.CaMenu.wifizns.value())||
                        deviceInfos.get(i).getCa().equals(DeviceInfo.CaMenu.zhinengsuo.value())){
                    if(!Actions.VersionType.CHANNEL_UHOME.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
                        viewCache.command.setVisibility(View.VISIBLE);
                    }
                }
            }
            boolean isHaveBattery = false;
            if (!CollectionsUtils.isEmpty(deviceInfos.get(i).getdCommands())){
                for (CommandInfo c : deviceInfos.get(i).getdCommands()) {
                    if (CommandInfo.CommandTypeEnum.battery.value().equals(c.getCtype())){
                        isHaveBattery = true;
                        viewCache.low.setVisibility(View.VISIBLE);
                        /**
                         * < 20%     : 0格；
                         20% - 35%  : 1格；
                         35% - 50%  : 2格；
                         50% - 70%  : 3格；
                         70% - 100% : 4格；
                         */
                        if (Integer.parseInt(c.getCommand()) < 20){
                            viewCache.low.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.dianliang0));
                        }else if(Integer.parseInt(c.getCommand()) >= 20 && Integer.parseInt(c.getCommand()) < 35){
                            viewCache.low.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.dianliang1));
                        }else if(Integer.parseInt(c.getCommand()) >= 35 && Integer.parseInt(c.getCommand()) < 50){
                            viewCache.low.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.dianliang2));
                        }else if(Integer.parseInt(c.getCommand()) >= 50 && Integer.parseInt(c.getCommand()) < 70){
                            viewCache.low.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.dianliang3));
                        }else{
                            viewCache.low.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.dianliang4));
                        }
                    }
                }
            }
            if (!isHaveBattery) {
                if (deviceInfos.get(i).isLowb()) { //低电
                    viewCache.low.setVisibility(View.VISIBLE);
                    viewCache.low.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.low));
                } else {
                    viewCache.low.setVisibility(View.GONE);
                }
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
            if (deviceInfos.get(i).isFa()) {
                viewCache.switchButton.setVisibility(View.GONE);
            }

//            if(Actions.VersionType.CHANNEL_HCTZ.equals(MainApplication.app.getAppGlobalConfig().getVersion())
//                    &&deviceInfos.get(i).getControlType().equals(DeviceInfo.ControlTypeMenu.group.value())){
//                 //分组命令显示
//                List<DeviceInfo> temp = DatabaseOperator.getInstance().queryAllDevicesByGroups(deviceInfos.get(i).getId());
//                if(temp.size()>0){
//                    viewCache.command.setText(temp.size()+getString(R.string.hongcai_count_device));
//                    viewCache.command.setVisibility(View.VISIBLE);
//                }else{
//                    viewCache.command.setVisibility(View.GONE);
//                }
//
//            }

            if(MainApplication.app.getAppGlobalConfig().isShowNewAddGroup()
                    &&deviceInfos.get(i).getControlType().equals(DeviceInfo.ControlTypeMenu.group.value())){
                //分组命令显示
                List<DeviceInfo> temp = DatabaseOperator.getInstance().queryAllDevicesByGroups(deviceInfos.get(i).getId());
                if(temp.size()>0){
                    viewCache.command.setText(temp.size()+getString(R.string.hongcai_count_device));
                    viewCache.command.setVisibility(View.VISIBLE);
                }else{
                    viewCache.command.setVisibility(View.GONE);
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
//            if (deviceInfos.get(i).getId() == zhuji.getId()) {
//                viewCache.zhuji_anim.setVisibility(View.VISIBLE);
//                viewCache.type
//                        .setText((deviceInfos.get(i).getWhere() == null ? "" : (deviceInfos.get(i).getWhere() + " "))
//                                + (zhuji.isOnline() ? getString(R.string.deviceslist_server_zhuji_online)
//                                : getString(R.string.deviceslist_server_zhuji_offline)));
//                // 在线
//                if (zhuji.isOnline()) {
//                    viewCache.device_item_layout.setBackgroundResource(R.drawable.device_item_click_bg);
//                    if (Actions.VersionType.CHANNEL_JUJIANG.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
////                        viewCache.zhuji_anim.setBackgroundRefsource(R.drawable.icon_online);
//                        viewCache.zhuji_anim.setBackgroundResource(R.drawable.zhujiline);
//                        animationDrawable = (AnimationDrawable) viewCache.zhuji_anim.getBackground();
//                        if (!animationDrawable.isRunning()) {
//                            animationDrawable.start();
//                        }
//                    }
//                } else {
//                    if (Actions.VersionType.CHANNEL_JKD.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
//                        viewCache.device_item_layout.setBackgroundResource(R.color.grayl);
//                    } else {
//                        if (Actions.VersionType.CHANNEL_JUJIANG.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
//                            viewCache.device_item_layout.setBackgroundResource(R.drawable.device_item_click_bg);
//                            viewCache.zhuji_anim.setBackgroundResource(R.drawable.icon_offline);
//                        } else {
//                            viewCache.device_item_layout.setBackgroundColor(Color.RED);
//                        }
//                    }
//
//                }
//                viewCache.type_left.setVisibility(View.GONE);
//                viewCache.type_right.setVisibility(View.GONE);
//            } else {
//                viewCache.zhuji_anim.setVisibility(View.GONE);
            // 主机是否在线
            if (!mContext.getZhuji().isOnline()) { // 不在线
                viewCache.device_item_layout.setBackgroundResource(R.color.common_bg_normal);
                if (deviceInfos.get(i).getControlType().contains("xiaxing")) {
                       viewCache.button.setEnabled(false);
                    viewCache.switchButton.setEnabled(false);
                } else {
                    viewCache.switchButton.setEnabled(true);
                }
            } else {
                if(deviceInfos.get(i).getCa()!=null&&!deviceInfos.get(i).getCa().equals("ybq")){
                    viewCache.switchButton.setEnabled(true);
                }
                viewCache.button.setEnabled(true);
                viewCache.device_item_layout.setBackgroundResource(R.drawable.device_item_click_bg);
                if (sortType == 0) { // 智能类型
                    if (deviceInfos.get(i).getAcceptMessage() > 0) {
                        // if(dcsp.getLong(Constant.DEVICE_BG_CHANGE_TYPE,
                        // 0) ==
                        // 1){ //0为啥都没有，1为蓝色背景 2闪烁
                        // if(String.valueOf(deviceInfos.get(i).getId()).equals(old_refulsh_device_id)){
                        // viewCache.device_item_layout.setBackgroundColor(Color.BLUE);
                        // }else{
                        // viewCache.device_item_layout.setBackgroundResource(R.drawable.device_item_click_bg);
                        // }
                        // }else{
                        viewCache.device_item_layout.setBackgroundResource(R.drawable.device_item_click_bg);
                        // }
                    } else {
                        viewCache.device_item_layout.setBackgroundResource(R.color.common_bg_normal);
                    }
                }
            }
            if (!StringUtils.isEmpty(deviceInfos.get(i).getWhere())
                    || !StringUtils.isEmpty(deviceInfos.get(i).getType())) {
                if ("1".equalsIgnoreCase(zhujiSetInfos.get(ZhujiInfo.GNSetNameMenu.showTypeInDeviceList.value()))){
                    viewCache.type.setText(deviceInfos.get(i).getWhere() == null ? "" : deviceInfos.get(i).getWhere());
                }else {
                    viewCache.type.setText(
                            (deviceInfos.get(i).getWhere() == null ? "" : (deviceInfos.get(i).getWhere() + " "))
                                    + deviceInfos.get(i).getType());
                }
                if (!"".equals(viewCache.type.getText().toString())){
                    viewCache.type_left.setVisibility(View.VISIBLE);
                    viewCache.type_right.setVisibility(View.VISIBLE);
                }else{
                    viewCache.type_left.setVisibility(View.GONE);
                    viewCache.type_right.setVisibility(View.GONE);
                }
            } else {
                viewCache.type_left.setVisibility(View.GONE);
                viewCache.type_right.setVisibility(View.GONE);
                viewCache.type.setText("");
            }
//            }
        }


        /**
         * 初始化按钮点击事件
         *
         * @param viewCache
         */
        private void initButtonEvent(final DeviceInfoView viewCache, final int i) {


            viewCache.button.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (MainApplication.app.getAppGlobalConfig().isClickDeviceItem() && !mContext.getZhuji().isOnline())
                        return;//主机离线，设备列表不可点击
                    if (Util.isFastClick()) {
                        Toast.makeText(mContext, getString(R.string.activity_devices_commandhistory_tip), Toast.LENGTH_SHORT).show();
                    } else {
                        mContext.showInProgress(getString(R.string.operationing), false, false);
                        defaultHandler.sendEmptyMessageDelayed(dHandler_timeout, 8 * 1000);
                        SyncMessage message = new SyncMessage();
                        message.setCommand(SyncMessage.CommandMenu.rq_control.value());
                        message.setDeviceid(deviceInfos.get(i).getId());
                        // 操作 这里的操作写死，需要在服务器的配置页面中配置此按钮对应的指令是什么
                        message.setSyncBytes(new byte[]{0x02});
                        SyncMessageContainer.getInstance().produceSendMessage(message);
                        operationDevice = deviceInfos.get(i);
                        operationDevice.setwIndex(i);
                        operationSort = "2";
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
                            operationSort = "1";
                        } else {
                            // 关操作
                            Log.e("aaa", "发送通知===关指令");
                            message.setSyncBytes(new byte[]{0x00});
                            operationSort = "0";
                        }
                        // 点击后显示进度条
                        mContext.showInProgress(getString(R.string.operationing));
                        defaultHandler.sendEmptyMessageDelayed(dHandler_timeout, 8 * 1000);
                        SyncMessageContainer.getInstance().produceSendMessage(message);
                        operationDevice = deviceInfos.get(i);
                        operationDevice.setwIndex(i);
                    }
                }
            });


//                viewCache.ioc_arming.setOnClickListener(new View.OnClickListener() {
//
//                    @Override
//                    public void onClick(View v) {
//                        if (MainApplication.app.getAppGlobalConfig().isClickDeviceItem() && !zhuji.isOnline())
//                            return;//主机离线，设备列表不可点击
//                        // 设防按钮点击
//                        mContext.showInProgress(getString(R.string.operationing), false, true);
//                        JavaThreadPool.getInstance().excute(new TriggerScene(-1));
//                    }
//                });
//                viewCache.ioc_disarming.setOnClickListener(new View.OnClickListener() {
//
//                    @Override
//                    public void onClick(View v) {
//                        if (MainApplication.app.getAppGlobalConfig().isClickDeviceItem() && !zhuji.isOnline())
//                            return;//主机离线，设备列表不可点击
//                        // 撤防按钮点击
//                        mContext.showInProgress(getString(R.string.operationing), false, true);
//                        JavaThreadPool.getInstance().excute(new TriggerScene(0));
//                    }
//                });
//                viewCache.ioc_home.setOnClickListener(new View.OnClickListener() {
//
//                    @Override
//                    public void onClick(View v) {
//                        if (MainApplication.app.getAppGlobalConfig().isClickDeviceItem() && !zhuji.isOnline())
//                            return;//主机离线，设备列表不可点击
//                        // 在家按钮点击
//                        mContext.showInProgress(getString(R.string.operationing), false, true);
//                        JavaThreadPool.getInstance().excute(new TriggerScene(-3));
//                    }
//                });

//            viewCache.ioc_panic.setOnClickListener(new View.OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
//                    if (MainApplication.app.getAppGlobalConfig().isClickDeviceItem() && !zhuji.isOnline())
//                        return;//主机离线，设备列表不可点击
//                    // panic按钮点击
//                    mContext.showInProgress(getString(R.string.operationing), false, true);
//                    JavaThreadPool.getInstance().excute(new TriggerPanic());
//                }
//            });
        }

        /**
         * 设置未读消息数
         *
         * @param viewCache
         * @param i
         */
        private void setBadeNumber(DeviceInfoView viewCache, int i) {
            //显示组标
//            if(Actions.VersionType.CHANNEL_HCTZ.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
//                if(deviceInfos.get(i).getControlType().equals(DeviceInfo.ControlTypeMenu.group.value())){
//                    //组,查询组下的设备的未读消息数
//                    List<DeviceInfo> temp =DatabaseOperator.getInstance().queryAllDevicesByGroups(deviceInfos.get(i).getId());
//                    if(temp!=null){
//                        int nr = 0 ;
//                        for(int j=0;j<temp.size();j++){
//                            nr+=temp.get(j).getNr();
//                        }
//                        if(nr!=0){
//                            deviceInfos.get(i).setNr(nr);
//                        }
//                    }
//                }
//            }

            if(MainApplication.app.getAppGlobalConfig().isShowNewAddGroup()){
                if(deviceInfos.get(i).getControlType().equals(DeviceInfo.ControlTypeMenu.group.value())){
                    //组,查询组下的设备的未读消息数
                    List<DeviceInfo> temp =DatabaseOperator.getInstance().queryAllDevicesByGroups(deviceInfos.get(i).getId());
                    if(temp!=null){
                        int nr = 0 ;
                        for(int j=0;j<temp.size();j++){
                            nr+=temp.get(j).getNr();
                        }
                        if(nr!=0){
                            deviceInfos.get(i).setNr(nr);
                        }
                    }
                }
            }

            if (deviceInfos.get(i).getNr() == 0) {
                viewCache.badgeView.setVisibility(View.GONE);
            } else {
                viewCache.badgeView.setText(String.valueOf(deviceInfos.get(i).getNr()));
                viewCache.badgeView.show();
            }

            if (deviceInfos.get(i).getAcceptMessage() == 3) {
//                viewCache.mode.setText(getString(R.string.shefang));
                if (Actions.VersionType.CHANNEL_LILESI
                        .equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
//                    viewCache.mode.setVisibility(View.GONE);
                    viewCache.mode.setVisibility(View.INVISIBLE);
                } else {
                    viewCache.mode.setVisibility(View.VISIBLE);
                }
            } else {
                viewCache.mode.setVisibility(View.INVISIBLE);
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
                if (DataCenterSharedPreferences.Constant.SCENE_NOW_CF.equals(mContext.getZhuji().getScene())
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
//                if (MainApplication.app.getAppGlobalConfig().isShowFortification()) {
//                    viewCache.sceneLayout.setVisibility(View.VISIBLE);
//                    if (Actions.VersionType.CHANNEL_LILESI
//                            .equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
//                        viewCache.ioc_panic.setVisibility(View.VISIBLE);
//                    } else {
//                        viewCache.ioc_panic.setVisibility(View.GONE);
//                    }
//                } else {
//                    viewCache.sceneLayout.setVisibility(View.GONE);
//                }

                boolean isJKD = Actions.VersionType.CHANNEL_JKD.equals(MainApplication.app.getAppGlobalConfig().getVersion());
                if (DataCenterSharedPreferences.Constant.SCENE_NOW_SF.equals(mContext.getZhuji().getScene())) {
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
                } else if (DataCenterSharedPreferences.Constant.SCENE_NOW_CF.equals(mContext.getZhuji().getScene())) {
                    viewCache.time.setText(getString(R.string.activity_scene_item_home_moden));
                    if (isJKD) {
                        viewCache.rLayout.setBackgroundColor(Color.TRANSPARENT);
                        showBtton(viewCache.ioc_disarming, viewCache);
//                        viewCache.ioc_disarming.setVisibility(View.VISIBLE);
                        viewCache.time.setTextColor(getResources().getColor(R.color.bg_devices_modle));
                    } else if (Actions.VersionType.CHANNEL_AIERFUDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                            || Actions.VersionType.CHANNEL_UHOME.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                        viewCache.time.setTextColor(Color.WHITE);
                        viewCache.rLayout.setBackgroundColor(Color.BLUE);
                    } else {
                        viewCache.time.setTextColor(Color.WHITE);
                        viewCache.rLayout.setBackgroundColor(Color.GREEN);
                    }
                    viewCache.ioc_arming.setImageResource(R.drawable.scene_item_arming_normal);
                    viewCache.ioc_disarming.setImageResource(R.drawable.scene_item_disarming_pressed);
                    viewCache.ioc_home.setImageResource(R.drawable.scene_item_home_normal);
                } else if (DataCenterSharedPreferences.Constant.SCENE_NOW_HOME.equals(mContext.getZhuji().getScene())) {

                    viewCache.time.setText(getString(R.string.activity_scene_item_inhome_moden));
                    if (isJKD) {
                        viewCache.rLayout.setBackgroundColor(Color.TRANSPARENT);
                        showBtton(viewCache.ioc_home, viewCache);
//                        viewCache.ioc_home.setVisibility(View.VISIBLE);
                        viewCache.time.setTextColor(getResources().getColor(R.color.bg_devices_modle));
                    } else if (Actions.VersionType.CHANNEL_AIERFUDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                            || Actions.VersionType.CHANNEL_UHOME.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                        viewCache.time.setTextColor(Color.WHITE);
                        viewCache.rLayout.setBackgroundColor(Color.BLUE);
                    } else {
                        viewCache.time.setTextColor(Color.WHITE);
                        viewCache.rLayout.setBackgroundColor(Color.GREEN);
                    }
                    viewCache.ioc_arming.setImageResource(R.drawable.scene_item_arming_normal);
                    viewCache.ioc_disarming.setImageResource(R.drawable.scene_item_disarming_normal);
                    viewCache.ioc_home.setImageResource(R.drawable.scene_item_home_pressed);
                } else if (!mContext.getZhuji().getScene().equals("")) {
                    viewCache.rLayout.setBackgroundColor(Color.GREEN);
                    viewCache.time.setText(mContext.getZhuji().getScene());
                    SceneActivity.test = mContext.getZhuji().getScene();
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
            o.put("did", mContext.getZhuji().getId());
            String server = mContext.dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");

            String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/d/panic", o, mContext);
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
            if (arg1 != null)
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
        private int position;

        public TriggerScene() {
        }

        public TriggerScene(int sId, int position) {
            this.sId = sId;
            this.position = position;
        }

        @Override
        public void run() {

            JSONObject o = new JSONObject();
            o.put("id", sId);
            o.put("did", mContext.getZhuji().getId());
            String server = mContext.dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            String result = HttpRequestUtils
                    .requestoOkHttpPost(server + "/jdm/s3/scenes/trigger", o, mContext);
            if ("0".equals(result)) {
                Message m = defaultHandler.obtainMessage(dHandler_scenes);
                m.arg1 = position;//此处posiontion
                m.arg2 = sId;
                defaultHandler.sendMessage(m);
            } else {
                mContext.cancelInProgress();
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
            case R.id.ll_sence_top:
                if (!mContext.getZhuji().isOnline()) {
                    Toast.makeText(mContext, getString(R.string.deviceslist_zhuji_offline), Toast.LENGTH_SHORT).show();
                    break;
                }
                intent.setClass(mContext, SceneActivity.class);
                startActivity(intent);
                break;
            case R.id.nonet_layout:
                if (nonet_layout.getVisibility() == View.VISIBLE) {
                    startActivityForResult(new Intent(Settings.ACTION_WIRELESS_SETTINGS), 0);
                }
                break;
            case R.id.iv_add_sence:
                //智能场景选择
//                mContext.showInProgress(getString(R.string.loading), false, true);
                JavaThreadPool.getInstance().excute(new SceneAllLoad());
                break;
            case R.id.iv_sence:
                if (!mContext.getZhuji().isOnline()) {
                    Toast.makeText(mContext, getString(R.string.deviceslist_zhuji_offline), Toast.LENGTH_SHORT).show();
                    break;
                }
                intent.setClass(mContext, SceneActivity.class);
                startActivity(intent);
                break;
            case R.id.rl_hub:
                // 主机栏点击事件
                if (deviceZhujiInfo == null || deviceZhujiInfo.getId() == 0)
                    return;
                intent.setClass(mContext.getApplicationContext(), DeviceDetailActivity.class);
                intent.putExtra("device", deviceZhujiInfo);
                startActivity(intent);
                break;

//            case R.id.scene_arming:
//                mContext.showInProgress(getString(R.string.operationing), false, true);
//                JavaThreadPool.getInstance().excute(new TriggerScene(-1));
//                break;
//            case R.id.scene_disarming:
//                if (MainApplication.app.getAppGlobalConfig().isClickDeviceItem() && !zhuji.isOnline())
//                    return;//主机离线，设备列表不可点击
//                // 撤防按钮点击
//                mContext.showInProgress(getString(R.string.operationing), false, true);
//                JavaThreadPool.getInstance().excute(new TriggerScene(0));
//                break;
//            case R.id.scene_home:
//                if (MainApplication.app.getAppGlobalConfig().isClickDeviceItem() && !zhuji.isOnline())
//                    return;//主机离线，设备列表不可点击
//                // 在家按钮点击
//                mContext.showInProgress(getString(R.string.operationing), false, true);
//                JavaThreadPool.getInstance().excute(new TriggerScene(-3));
//                break;

            case R.id.btn_add_hub:
                if (Actions.VersionType.CHANNEL_AIERFUDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                        || Actions.VersionType.CHANNEL_UHOME.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                    intent.setClass(mContext.getApplicationContext(), AddZhujiOldActivity.class);
                } else {
                    intent.setClass(mContext.getApplicationContext(), AddZhujiActivity.class);
                }
                startActivity(intent);
                break;
            case R.id.menu_icon:
                mContext.menuWindow.showAtLocation(v, Gravity.TOP | Gravity.RIGHT, 0,
                        Util.dip2px(mContext.getApplicationContext(), 55) + Util.getStatusBarHeight(mContext));
                break;
            case R.id.iv_icon:
//                dl.open();
                break;
            case R.id.tv_setting:
                intent.setClass(mContext.getApplicationContext(), SettingActivity.class);
                intent.putExtra("zhuji_Id", mContext.getZhuji().getId());
                break;
            case R.id.iv_bottom:
                intent.setClass(mContext.getApplicationContext(), UserInfoActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_setdevice: // 设置设备
                itemMenu.dismiss();
                if(DeviceInfo.ControlTypeMenu.group.value().equals(operationDevice.getControlType())){
                    intent.setClass(mContext.getApplicationContext(), AddGroupActivity.class);
                }else{
                    intent.setClass(mContext.getApplicationContext(), ChooseAudioSettingMode.class);
                }
                intent.putExtra("device", operationDevice);
                startActivity(intent);
                break;
            case R.id.btn_setgsm: // 设置主机GSM号码
                itemMenu.dismiss();
                intent.setClass(mContext.getApplicationContext(), DeviceSetGSMPhoneActivity.class);
                intent.putExtra("device", operationDevice);
                intent.putExtra("type", 0);
                startActivity(intent);
                break;
            case R.id.btn_setscall: // 设置主机电话报警号码
                itemMenu.dismiss();
                intent.setClass(mContext.getApplicationContext(), DeviceSetGSMPhoneActivity.class);
                intent.putExtra("device", operationDevice);
                if(Actions.VersionType.CHANNEL_RUNLONG.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
                    intent.putExtra("type", 0);
                }else{
                    intent.putExtra("type", 1);
                }
                startActivity(intent);
                break;
            case R.id.btn_checkversion: // 固件检查更新
                itemMenu.dismiss();
                if (mContext.getZhuji() != null && !mContext.getZhuji().isOnline())
                    Toast.makeText(mContext, getString(R.string.update_zhuji_gujian), Toast.LENGTH_SHORT).show();
                SyncMessageContainer.getInstance().sendMessageToServer(SyncMessage.CommandMenu.rq_checkpudate, SyncMessage.CodeMenu.zero,
                        operationDevice.getId(), null);
                mContext.showInProgress(getString(R.string.loading));
                defaultHandler.sendEmptyMessageDelayed(dHandler_timeout, 8 * 1000);
                break;
            case R.id.btn_accept_auto_strongshow: // 强力提醒模式 3
                itemMenu.dismiss();
                SyncMessageContainer.getInstance().sendMessageToServer(SyncMessage.CommandMenu.rq_controlRemind, SyncMessage.CodeMenu.zero,
                        operationDevice.getId(), new byte[]{0x03});
                break;
            case R.id.btn_accept_autoshow: // 自动提醒 短信音 2
                itemMenu.dismiss();
                SyncMessageContainer.getInstance().sendMessageToServer(SyncMessage.CommandMenu.rq_controlRemind, SyncMessage.CodeMenu.zero,
                        operationDevice.getId(), new byte[]{0x02});
                break;
            case R.id.btn_acceptnotshow: // 接收消息不提醒  1
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
                                                        List<DeviceInfo> allDevices = DatabaseOperator.getInstance(mContext).queryAllDeviceInfos(operationDevice.getId());
                                                        if (!allDevices.isEmpty()) {
                                                            for (DeviceInfo d : allDevices) {
                                                                if (DeviceInfo.CaMenu.ipcamera.value()
                                                                        .equals(operationDevice.getCa())) {
                                                                    String ipc = operationDevice.getIpc();
                                                                    List<CameraInfo> camera = (List<CameraInfo>) JSON
                                                                            .parseArray(ipc, CameraInfo.class);
                                                                    if (!camera.isEmpty()) {
                                                                        for (CameraInfo c : camera) {
                                                                            //清空推送用户
                                                                            if (CameraInfo.CEnum.jiwei.value().equals(c.getC())) {
                                                                                P2PHandler.getInstance().setBindAlarmId(c.getId(), c.getP(), 0, new String[]{},MainApplication.GWELL_LOCALAREAIP);
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        DatabaseOperator.getInstance().getWritableDatabase().delete(
                                                                "DEVICE_STATUSINFO", "zj_id = ?",
                                                                new String[]{String.valueOf(operationDevice.getId())});

                                                        DatabaseOperator.getInstance().getWritableDatabase().delete(
                                                                "ZHUJI_STATUSINFO", "id = ?",
                                                                new String[]{String.valueOf(operationDevice.getId())});
//                                                        List<ZhujiInfo> zhujis = DatabaseOperator.getInstance()
//                                                                .queryAllZhuJiInfos();
//
//                                                        if (zhujis != null && !zhujis.isEmpty()) {
//                                                            mContext.setZhuji(zhujis.get(0));
//                                                            mContext.dcsp.putString(DataCenterSharedPreferences.Constant.APP_MASTERID,
//                                                                    zhujis.get(0).getMasterid()).commit();
//                                                        } else {
//                                                            mContext.dcsp.remove(DataCenterSharedPreferences.Constant.APP_MASTERID).commit();
//                                                        }
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                    mContext.runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            mContext.changeFragment("main");
                                                        }
                                                    });
                                                } else if (DeviceInfo.CaMenu.ipcamera.value()
                                                        .equals(operationDevice.getCa())) {
                                                    String ipc = operationDevice.getIpc();
                                                    List<CameraInfo> camera = (List<CameraInfo>) JSON
                                                            .parseArray(ipc, CameraInfo.class);
                                                    if (!camera.isEmpty()) {
                                                        for (CameraInfo c : camera) {
                                                            //清空推送用户
                                                            if (CameraInfo.CEnum.jiwei.value().equals(c.getC())) {
                                                                P2PHandler.getInstance().setBindAlarmId(c.getId(), c.getP(), 0, new String[]{},MainApplication.GWELL_LOCALAREAIP);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            mContext.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mContext.cancelInProgress();
                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        }).show();
                break;
            case R.id.add_device_foot:
                if (mContext.getZhuji() != null) {
                    intent.setClass(mContext.getApplicationContext(), AddDeviceChooseActivity.class);
                    intent.putExtra("zhuji",mContext.getZhuji());
                } else {
                    if (Actions.VersionType.CHANNEL_AIERFUDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                            || Actions.VersionType.CHANNEL_UHOME.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                        intent.setClass(mContext.getApplicationContext(), AddZhujiOldActivity.class);
                    } else {
                        intent.setClass(mContext.getApplicationContext(), AddZhujiActivity.class);
                    }
                }
                startActivity(intent);
                break;
            case R.id.tantou_chufa_setting:
                //探头触发主机设定
                itemMenu.dismiss();
                intent.setClass(mContext,HongCaiTantouSettingActivity.class);
                intent.putExtra("device_id",operationDevice.getId());
                startActivity(intent);
                break;
            case R.id.hongcai_alarm_setting:
                itemMenu.dismiss();
                //主机报警设定
                intent.setClass(mContext,HongCaiSettingActivity.class);
                intent.putExtra("whatsetting",1);
                intent.putExtra("zhuji_id",mContext.getZhuji().getId());
                startActivity(intent);
                break;
            case R.id.hongcai_naozhong_setting:
                itemMenu.dismiss();
                //主机闹钟设定
                intent.setClass(mContext,HongCaiSettingActivity.class);
                intent.putExtra("whatsetting",2);
                intent.putExtra("zhuji_id",mContext.getZhuji().getId());
                startActivity(intent);
                break;
            case R.id.ll_tel_sms_alarm:
                //设备主机对象判断是否是主账户（后面需求可能改成非管理员也可以显示报警电话这里的判断是否是主账户需要去掉）
                if ((MainApplication.app.getAppGlobalConfig().isShowCallAlarm()
                        || MainApplication.app.getAppGlobalConfig().isShowSmsAlarm()) && mContext.getZhuji().isAdmin()) {
                    if(Actions.VersionType.CHANNEL_ZHILIDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                            &&mContext.getZhuji().getCa().equals(DeviceInfo.CaMenu.bohaoqi.value())){
                        intent.setClass(mContext, ZldGSMSettingActivity.class);
                        intent.putExtra("zhuji_id",mContext.getZhuji().getId());
                    }else{
                        int alarmType = -1;
                        //设备主机对象判断是否是主账户（后面需求可能改成非管理员也可以显示报警电话这里的判断是否是主账户需要去掉）
                        if ((MainApplication.app.getAppGlobalConfig().isShowCallAlarm()
                                || MainApplication.app.getAppGlobalConfig().isShowSmsAlarm()) && mContext.getZhuji().getGsm() == 0 && mContext.getZhuji().isAdmin()) {
                            alarmType = 1;//报警电话
                        }
                        if (mContext.getZhuji().getGsm() == 1&&mContext.getZhuji().isAdmin()){
                            alarmType = 0;//Gsm电话
                        }

                        intent.setClass(mContext, DeviceSetGSMPhoneActivity.class);
                        intent.putExtra("device", Util.getZhujiDevice(mContext.getZhuji()));
                        intent.putExtra("type", alarmType);
                        intent.putExtra("isAdmin", mContext.getZhuji().isAdmin());
                    }
                    startActivity(intent);
                }
                break;
            default:

                break;
        }
    }

    private boolean isResponseGesture() {
        boolean flag = true;
        //无安防设备
        if (securityCount <= 0 || mGestureDetector == null)
            return false;
        return flag;
    }

    private void initViewEvent() {
        lv_device.setOnTouchListener(new View.OnTouchListener() {
            boolean isMove = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!isResponseGesture())
                    return false;
//
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_MOVE:
//                        isMove = true;
//                        break;
//                }
//
//                if (expandable_layout.isExpanded()) {
////                    mGestureDetector.onTouchEvent(event);
//                    mRefreshLayout.setEnabled(true);
//                    if (isMove) {
//                        return true;
//                    } else {
//                        return false;
//                    }
//                } else if (!expandable_layout.isExpanded()) {
////                    mGestureDetector.onTouchEvent(event);
//                    mRefreshLayout.setEnabled(false);
//                }
                return mGestureDetector.onTouchEvent(event);
            }
        });
        lv_device.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                boolean enable = false;
                if (lv_device != null && lv_device.getChildCount() > 0) {
                    // check if the first item of the list is visible
                    boolean firstItemVisible = lv_device.getFirstVisiblePosition() == 0;
                    // check if the top of the first item is visible
                    boolean topOfFirstItemVisible = lv_device.getChildAt(0).getTop() == 0;
                    // enabling or disabling the refresh layout
                    enable = firstItemVisible && topOfFirstItemVisible;
                } else if (lv_device != null && lv_device.getChildCount() == 0) {
                    enable = true;
                }
//                if (mGestureDetector!=null && !expandable_layout.isExpanded()){
//                    mRefreshLayout.setEnabled(false);
//                }else {
//
//                }
                mRefreshLayout.setEnabled(enable);

            }
        });

        lv_device.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            private DeviceInfo device;
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                //宏才版本
                if(Actions.VersionType.CHANNEL_HCTZ.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
                    if(!mContext.getZhuji().isOnline()){
                        //提示主机已离线
                        Toast toast = Toast.makeText(mContext,"",Toast.LENGTH_SHORT);
                        toast.setText(getString(R.string.deviceslist_zhuji_offline));
                        toast.show();
                        return ;
                    }
                }
                // 吉凯达listview与footer间有一段间距 点击间距抛出异常
                try {
//                    device = deviceInfos.get(position - 1); //-1是因为增加了一个headview
                    device = deviceInfos.get(position); //无headview
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                startDevicePage(mContext.getZhuji(),device);
            }
        });

        rl_hub.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // 吉凯达listview与footer间有一段间距 点击间距抛出异常

//                operationDevice.setwIndex(position);
                operationDevice = deviceZhujiInfo;
                itemMenu.updateDeviceMenu(mContext, deviceZhujiInfo, mContext.dcsp, mContext.getZhuji());
                itemMenu.showAtLocation(ll_device_main, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置
                mContext.changeWindowAlfa(0.7f);
                return true;

            }
        });

        lv_device.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
             @Override
             public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                 // 吉凯达listview与footer间有一段间距 点击间距抛出异常
                 try {
                     operationDevice = deviceInfos.get(position);
//                   operationDevice = deviceInfos.get(position - 1);

                 } catch (IndexOutOfBoundsException e) {
                     return true;
                 }
                 //宏才分组设备不可以长按
                 if(Actions.VersionType.CHANNEL_HCTZ.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
                     if(!hongcaiDeviceInfos.contains(operationDevice)){
                         return true;
                     }
                 }else if(Actions.VersionType.CHANNEL_RUNLONG.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
                     ZhujiInfo zhujiInfo = mContext.getZhuji()  ;
                     String rolek = zhujiInfo.getRolek() ;
                     if(rolek.equals("lock_num_baby")||rolek.equals("lock_num_guest")||rolek.equals("lock_num_temp")){
                         //小孩子、客人、临时不支持长按设备
                            return true;
                     }else{
//                                                             if(rolek.equals("lock_num_old")&&!(operationDevice!=null&&operationDevice.getCa().equals(DeviceInfo.CaMenu.zhinengsuo.value()))){
//                                                                //老人不能长按非锁设备
//                                                                 return true ;
//                                                             }
                     }
                 }
                 operationDevice.setwIndex(position);
                 if (!operationDevice.getControlType().equals(DeviceInfo.ControlTypeMenu.adsinfo.value())) { //广告条不显示长按菜单
                     itemMenu.updateDeviceMenu(mContext, operationDevice, mContext.dcsp, mContext.getZhuji());
                     itemMenu.showAtLocation(ll_device_main, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置
                     mContext.changeWindowAlfa(0.7f);
                 }
                 return true;
             }

        });
        listViewHeadView.setOnClickListener(new View.OnClickListener()

                                            {

                                                @Override
                                                public void onClick(View v) {
                                                    if (v.findViewById(R.id.nonet_layout).getVisibility() == View.VISIBLE) {
                                                        startActivityForResult(new Intent(Settings.ACTION_WIRELESS_SETTINGS), 0);
                                                    }
                                                }
                                            }

        );
    }

    private void startDevicePage(ZhujiInfo zhuji, DeviceInfo device) {
        //巨將在主機離線的時候只能打開攝像頭，其餘的提示主機不在線
        if (MainApplication.app.getAppGlobalConfig().getVersion().equals(Actions.VersionType.CHANNEL_JUJIANG)
                && !zhuji.isOnline()) {
            if (device.getControlType().equals(DeviceInfo.ControlTypeMenu.zhuji.value())) {
                // 主机栏点击事件
                Intent intent = new Intent();
                intent.setClass(mContext.getApplicationContext(), DeviceDetailActivity.class);
                intent.putExtra("device", device);
                startActivity(intent);
            } else if ("sst".equals(device.getCa())) {
                // 打开摄像头
                verifyIPCLoginAndStart(device);
            } else {
                Toast.makeText(mContext, getString(R.string.deviceslist_zhuji_offline),
                        Toast.LENGTH_LONG).show();
            }
            return;
        }
        if (MainApplication.app.getAppGlobalConfig().getVersion().equals(Actions.VersionType.CHANNEL_LILESI)
                && !zhuji.isOnline()) {
            if (device.getControlType().equals(DeviceInfo.ControlTypeMenu.zhuji.value())) {
                // 主机栏点击事件
                Intent intent = new Intent();
                intent.setClass(mContext.getApplicationContext(), DeviceDetailActivity.class);
                intent.putExtra("device", device);
                startActivity(intent);
            } else {
                Toast.makeText(mContext, getString(R.string.deviceslist_zhuji_offline),
                        Toast.LENGTH_LONG).show();
            }

            return;
        }
        if (DeviceInfo.ControlTypeMenu.adsinfo.value().equals(device.getControlType())) { //广告的需要放在第一个if
            if (!StringUtils.isEmpty(device.getAdsUrl())) {
                if ((device.getAdsUrl().indexOf(".taobao.") > -1 || device.getAdsUrl().indexOf(".tmall.") > -1) && IntentUtils.getInstance().existPackage(mContext,"com.taobao.taobao")){
                    Intent intent = new Intent();
                    intent.setAction("Android.intent.action.VIEW");
                    Uri uri = Uri.parse(device.getAdsUrl()); // 商品地址
                    intent.setData(uri);
                    intent.setClassName("com.taobao.taobao", "com.taobao.tao.detail.activity.DetailActivity");
                    startActivity(intent);
                }else {
                    Intent intent = new Intent();
                    intent.setClass(mContext, CommonWebViewActivity.class);
                    intent.putExtra("url", device.getAdsUrl());
                    startActivity(intent);
                }
            }
        }else if (device.getCa() != null && device.getCa().contentEquals("hwzf")) {
            Intent mIntent = new Intent();
            mIntent.putExtra("did", device.getId());
            if(Actions.VersionType.CHANNEL_ZHICHENG.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
                mIntent.setClass(mContext, ZCIRRemoteList.class);
                mIntent.putExtra("bipc",device.getBipc());
                mIntent.putExtra("deviceName",device.getName());
                mIntent.putExtra("zhujiID",device.getZj_id());
            }else{
                mIntent.setClass(mContext, YKDownLoadCodeActivity.class);
                mIntent.putExtra("masterId", zhuji.getMasterid());
            }
            startActivity(mIntent);
            return;
        }else if (device.getControlType().equals(DeviceInfo.ControlTypeMenu.neiqian.value())) {
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
                        //resourct = R.raw.p2pipcam_hvcipc_6_5;
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
        } else if (DeviceInfo.CaMenu.ipcamera.value().equals(device.getCa())) {
            //惠通智能主机离线不让打开摄像头
            if (DeviceInfo.CaMenu.ipcamera.value().equals(device.getCa())) {
                if (MainApplication.app.getAppGlobalConfig().getVersion().equals(Actions.VersionType.CHANNEL_HTZN)
                        && !zhuji.isOnline()) {
                    Toast.makeText(mContext, getString(R.string.activity_zhuji_not), Toast.LENGTH_SHORT).show();
                    return;
                }
                // 打开摄像头
                verifyIPCLoginAndStart(device);
            }
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

        } else if (device.getCa()!=null&&device.getCa().equals("tzc")) { //体重秤
//                    mContext.showInProgress(getString(R.string.loading), false, true);
//                    JavaThreadPool.getInstance().excute(new InitFamilyMemberThread(device, dHandler_weightInfo));
            deviceIntent = new Intent();
//            deviceIntent.setClass(mContext.getApplicationContext(), WeightMainActivity.class);
            deviceIntent.putExtra("zhuji", zhuji);
            deviceIntent.putExtra("device", device);
            deviceIntent.putExtra("mid", mId);
            startActivity(deviceIntent);
        } else if (DeviceInfo.CaMenu.xueyaji.value().equals(device.getCa())) {
//                    mContext.showInProgress(getString(R.string.loading), false, true);
//                    JavaThreadPool.getInstance().excute(new InitFamilyMemberThread(device, dHandler_xyjInfo));
//                       mContext.showInProgress(getString(R.string.loading), false, true);
//                       JavaThreadPool.getInstance().excute(new InitXYJMemberThread(device));
            deviceIntent = new Intent();
//            deviceIntent.setClass(mContext.getApplicationContext(), XYJMainActivity.class);
            deviceIntent.putExtra("zhuji", zhuji);
            deviceIntent.putExtra("device", device);
            deviceIntent.putExtra("mid", mId);
            startActivity(deviceIntent);
        }
//                else if (DeviceInfo.CaMenu.xuetangyi.value().equals(device.getCa())) {
//                    deviceIntent = new Intent();
//                    deviceIntent.setClass(mContext.getApplicationContext(), XTYMainActivity.class);
//                    deviceIntent.putExtra("zhuji", zhuji);
//                    deviceIntent.putExtra("device", device);
//                    startActivity(deviceIntent);
//                }
        else if (device.getCa()!=null&&device.getCa().equals("qwq")) {
            deviceIntent = new Intent();
            deviceIntent.setClass(mContext.getApplicationContext(), QWQActivity.class);
            deviceIntent.putExtra("device", device);
            startActivity(deviceIntent);
        } else if (device.getCa()!=null&&device.getCa().equals(DeviceInfo.CaMenu.maoyan.value())) {
            deviceIntent = new Intent();
            deviceIntent.setClass(mContext.getApplicationContext(), BeiJingMaoYanActivity.class);
            deviceIntent.putExtra("device", device);
            startActivity(deviceIntent);
        } else if (device.getCa()!=null&&device.getCa().equals(DeviceInfo.CaMenu.znyx.value())) {
            deviceIntent = new Intent();
            deviceIntent.setClass(mContext.getApplicationContext(), SmartMedicineMainActivity.class);
            deviceIntent.putExtra("device", device);
            startActivity(deviceIntent);
        } else if (Actions.VersionType.CHANNEL_RUNLONG.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                && device.getCa().equals(DeviceInfo.CaMenu.zhinengsuo.value())) {
            deviceIntent = new Intent();
            deviceIntent.setClass(mContext.getApplicationContext(), BeijingSuoActivity.class);
            deviceIntent.putExtra("device", device);
            startActivity(deviceIntent);
        } else if ((Actions.VersionType.CHANNEL_AIERFUDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                || Actions.VersionType.CHANNEL_ZNZK.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                || Actions.VersionType.CHANNEL_UHOME.equals(MainApplication.app.getAppGlobalConfig().getVersion()))
                && device.getCa().equals(DeviceInfo.CaMenu.zhinengsuo.value())) {
            deviceIntent = new Intent();
            deviceIntent.setClass(mContext.getApplicationContext(), ZSSuoNewActivity.class);
            deviceIntent.putExtra("device", device);
            startActivity(deviceIntent);
        }else if (device.getCa()!=null&&device.getCa().equals(DeviceInfo.CaMenu.zhinengsuo.value())/*!StringUtils.isEmpty(device.getMc()) && device.getMc().startsWith(DeviceInfo.TypeCompanyMenu.zhicheng.getValue())*/) {//锁 统一进入此锁。有特定需求时再设定不同的入口
           //这个智能锁，范围比下面的大，换个位置
            deviceIntent = new Intent();
            deviceIntent.setClass(mContext.getApplicationContext(), LockMainActivity.class);
            deviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            deviceIntent.putExtra("device", device);
            deviceIntent.putExtra("zhuji",zhuji);
            startActivity(deviceIntent);
        }/* else if ((Actions.VersionType.CHANNEL_AIERFUDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                || Actions.VersionType.CHANNEL_ZNZK.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                || Actions.VersionType.CHANNEL_UHOME.equals(MainApplication.app.getAppGlobalConfig().getVersion()))
                && device.getCa().equals(DeviceInfo.CaMenu.zhinengsuo.value())) {
            deviceIntent = new Intent();
            deviceIntent.setClass(mContext.getApplicationContext(), ZSSuoNewActivity.class);
            deviceIntent.putExtra("device", device);
            startActivity(deviceIntent);
        }*/
//        else if ((!Actions.VersionType.CHANNEL_AIERFUDE.equals(MainApplication.app.getAppGlobalConfig().getVersion()) && !Actions.VersionType.CHANNEL_ZHILIDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())) &&
//                (device.getCa().equals(DeviceInfo.CaMenu.yangan.value()) || device.getCa().equals(DeviceInfo.CaMenu.cazuo.value()) || device.getCa().equals(DeviceInfo.CaMenu.menci.value()))) {
//            //门磁、烟感、排插
//            deviceIntent = new Intent();
//            deviceIntent.setClass(mContext.getApplicationContext(), SecurityInfoActivity.class);
//            deviceIntent.putExtra("device", device);
//            mContext.showInProgress(getString(R.string.loading), false, true);
//            if (!StringUtils.isEmpty(device.getBipc()) && !"0".equals(device.getBipc())) {
//                JavaThreadPool.getInstance().excute(new BindingCameraLoad(device.getBipc()));
//            } else {
//                mContext.cancelInProgress();
//                startActivity(deviceIntent);
//            }
//        }
        else if (device.getCa().equals(DeviceInfo.CaMenu.wenshiduji.value()) ||
                device.getCa().equals(DeviceInfo.CaMenu.wenduji.value())) {
            deviceIntent = new Intent();
            deviceIntent.setClass(mContext.getApplicationContext(), THHistoryActivity.class);
            deviceIntent.putExtra("device", device);
            startActivity(deviceIntent);
//                } else if (device.getCa().equals("ybq") && DataCenterSharedPreferences.Constant.SCENE_NOW_HOME.equals(zhuji.getScene())) {
        } else if ((DeviceInfo.CakMenu.detection.value().equals(device.getCak()) && DeviceInfo.CaMenu.ybq.value().equals(device.getCa()) && mContext.defaultStartDid == 0) || (DeviceInfo.CakMenu.security.value().equals(device.getCak()) && DeviceInfo.CaMenu.ybq.value().equals(device.getCa()) && (device.getAcceptMessage() == 1 || device.getAcceptMessage() == 2))) {
            //进入统计页面的逻辑：专有迎宾器 是 正常点击直接进入统计页面，点击通知进入历史记录
            Intent deviceIntent = new Intent();
//            deviceIntent.setClass(mContext.getApplicationContext(), YBQChartActivity.class);
            deviceIntent.putExtra("device", device);
            startActivity(deviceIntent);
        } else { // 其他
            deviceIntent = new Intent();
            deviceIntent.setClass(mContext.getApplicationContext(), DeviceInfoActivity.class);
            deviceIntent.putExtra("device", device);
            mContext.showInProgress(getString(R.string.loading), false, true);
            if (!StringUtils.isEmpty(device.getBipc()) && !"0".equals(device.getBipc())) {
                //表明设备绑定了摄像头
                JavaThreadPool.getInstance().excute(new BindingCameraLoad(device.getBipc()));
            } else {
                mContext.cancelInProgress();
                startActivity(deviceIntent);
            }
        }
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
//                MainApplication.app.setAlertMessageActivity(null);//不设置为null好像不会释放会导致重影。不可能吧持续观察
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
        String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/infr/list", object, mContext);
        return result;
    }

    private void verifyIPCLoginAndStart(DeviceInfo device) {
        Account activeUser = AccountPersist.getInstance().getActiveAccountInfo(mContext);

        if (activeUser != null && !activeUser.three_number.equals("0517401")) {
            NpcCommon.mThreeNum = activeUser.three_number;
            Intent intent = new Intent();
            if (TextUtils.isEmpty(device.getIpc())) {
                intent.setClass(mContext, RadarAddActivity.class);
            } else {
                JSONArray array = JSONArray.parseArray(device.getIpc());
                if (CollectionsUtils.isEmpty(array)) {
                    intent.setClass(mContext, RadarAddActivity.class);
                    intent.putExtra("isMainList", false);
                    intent.putExtra("int", 3);
                } else {
                    intent.setClass(mContext, MainActivity.class);
                }
            }
            intent.putExtra("device", device);
            startActivity(intent);
            return;
        }
      //  Toast.makeText(mContext, R.string.net_error_loginoutofdayipc, Toast.LENGTH_SHORT).show();
    }

    /**
     * 验证IP摄像头是否登录，并打开摄像头
     */
    private void verifyIPCLogin(Intent intent) {
        if (!com.smartism.znzk.util.StringUtils.isEmpty(MainApplication.app.getAppGlobalConfig().getAPPID())) {
            Account activeUser = AccountPersist.getInstance().getActiveAccountInfo(mContext);
            Log.e("log", "verifyIPCLogin" + (activeUser == null));
            if (activeUser != null && !activeUser.three_number.equals("0517401")) {
                if (intent == null) return;
                NpcCommon.mThreeNum = activeUser.three_number;
//            P2PHandler.getInstance().p2pInit(mContext, new P2PListener(), new SettingListener());
                NpcCommon.verifyNetwork(mContext);
//                P2PHandler.getInstance().p2pInit(getActivity(), new P2PListener(),
//                        new SettingListener());
                connect();
                startActivity(intent);
                return;
            }
         //   Toast.makeText(mContext, R.string.net_error_loginoutofdayipc, Toast.LENGTH_SHORT).show();
        }
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
            object.put("did", mContext.getZhuji().getId());
            String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/f/list", object, mContext);
            if ("0".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        mContext.cancelInProgress();
                        Intent intent = new Intent();
                        DatabaseOperator.getInstance(mContext).getWritableDatabase().execSQL("delete from FAMINY_MEMBER");
                        if (mInfo.getCa().equals("tzc")) {
//                            intent.setClass(mContext, WeightPrepareActivity.class);
                        } else if (mInfo.getCa().equals("yyj")) {
//                            intent.setClass(mContext, XYJPrepareActivity.class);
                        }
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
     * 智能场景操作
     *
     * @param info
     */
    public void triggerScene(final SceneInfo info, final int position) {
        final long cid = info.getId();
        final long did = mContext.getZhuji().getId();
        JavaThreadPool.getInstance().excute(new Runnable() {
            @Override
            public void run() {
                String server = mContext.dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                JSONObject pJsonObject = new JSONObject();
                pJsonObject.put("did", did);
                pJsonObject.put("id", cid);
                String result = HttpRequestUtils
                        .requestoOkHttpPost(server + "/jdm/s3/scenes/trigger", pJsonObject, mContext);

                // 0成功设置 -1参数为空 -2校验失败 -3id不存在
                if ("0".equals(result)) {
                    defaultHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mContext.cancelInProgress();
                            Toast.makeText(mContext, getString(R.string.activity_editscene_set_success),
                                    Toast.LENGTH_LONG).show();
                        }
                    });

                    Message m = defaultHandler.obtainMessage(dHandler_scenes);
                    m.arg1 = position;
                    defaultHandler.sendMessage(m);
                } else if ("-1".equals(result)) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            mContext.cancelInProgress();
                            Toast.makeText(mContext, getString(R.string.register_tip_empty),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-2".equals(result)) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            mContext.cancelInProgress();
                            Toast.makeText(mContext, getString(R.string.device_check_failure),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-3".equals(result)) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            mContext.cancelInProgress();
                            Toast.makeText(mContext, getString(R.string.activity_editscene_id_empty),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-4".equals(result)) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            mContext.cancelInProgress();
                            Toast.makeText(mContext, getString(R.string.activity_editscene_isdisable),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }
        });
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
            List<CameraInfo> cameraInfos = DatabaseOperator.getInstance(mContext.getApplicationContext()).queryAllCameras(mContext.getZhuji());
            //由于要将主机下添加的雄迈添加到了主机列表，而没有添加到设备下，因此需要从主机列表拿出雄迈
            List<ZhujiInfo> infos = DatabaseOperator.getInstance(mContext.getApplicationContext()).queryAllZhuJiInfos();
            for(ZhujiInfo zhujiInfo :infos){
                if(zhujiInfo.getCa().equals(DeviceInfo.CaMenu.ipcamera.value())){
                     cameraInfos.add(zhujiInfo.getCameraInfo());
                }
            }
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
                Bundle bundle = new Bundle();
                bundle.putSerializable("contact",contact);
                bundle.putString("cameraPaiZi",c.getC());
                Message message = new Message();
                message.what = dHandler_initContast;
                /*
                message.obj = contact;
                */
                message.obj = bundle;
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
     * 注册广播 onpuse 中 需要解注册，意思是当页面跳走了不再接受广播
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
        receiverFilter.addAction(Actions.ACCETP_MAIN_SHOW_SCENCE);
        receiverFilter.addAction(Actions.CONTROL_BACK_MESSAGE);
        receiverFilter.addAction(HONGCAI_TO_TANTOU_SETTING);
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

    public boolean isShowScence() {
        return isShowScence;
    }

    public void setShowScence(boolean showScence) {
        isShowScence = showScence;
    }

    private String regId;//小米regID
    private boolean isShowScence;
    private AlertView permissonView;
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
                        if (wifiinfo == null || wifiinfo.getSSID() == null) {
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
//                        Intent intentNew = new Intent();
//                        intentNew.setAction(Constants.Action.NET_WORK_TYPE_CHANGE);
//                        mContext.sendBroadcast(intentNew);
                        WifiUtils.getInstance().isApDevice();
                    } else {
                        ToastUtil.shortMessage(getString(R.string.network_error) + " " + activeNetInfo.getTypeName());
                    }

                    if (activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        NpcCommon.mNetWorkType = NpcCommon.NETWORK_TYPE.NETWORK_WIFI;
                    } else {
                        NpcCommon.mNetWorkType = NpcCommon.NETWORK_TYPE.NETWORK_2GOR3G;
                    }
                } else {
                    ToastUtil.shortMessage(getString(R.string.network_error));
                }

                NpcCommon.setNetWorkState(isNetConnect);

            } else if (Actions.ACCETP_ONEDEVICE_MESSAGE.equals(intent.getAction()) && !isHidden()) { // 某一个设备的推送广播
                if (intent.getStringExtra("zhuji_id") != null) {//主机收到的数据
                    String zhuji_id = intent.getStringExtra("zhuji_id");
                    if (mContext.getZhuji() != null && zhuji_id.equals(String.valueOf(mContext.getZhuji().getId()))) {
                        String data = (String) intent.getSerializableExtra("zhuji_info");
                        if (data != null) {
                            try {
                                JSONObject object = JSONObject.parseObject(data);
                                if (object != null && CommandInfo.CommandTypeEnum.requestAddUser.value().equals(object.getString("dt")) && !TextUtils.isEmpty(object.getString("dtv"))) {
                                    if (mContext.getZhuji() != null && mContext.getZhuji().isAdmin()) {
                                        try {
                                            if (permissonView == null || !permissonView.isShowing()) {
                                                permissonView = new AlertView(getString(R.string.activity_beijingsuo_reqkeyauthtitle), getString(R.string.jjsuo_request_adduser,object.getString("dtv")),
                                                        null, new String[]{getString(R.string.activity_beijingsuo_reqkeyauth), getString(R.string.activity_beijingsuo_notauth)
                                                        , getString(R.string.cancel)}, null,
                                                        mContext, AlertView.Style.Alert,
                                                        new com.smartism.znzk.view.alertview.OnItemClickListener() {

                                                            @Override
                                                            public void onItemClick(Object o, int position) {
                                                                if (position == 0) {
                                                                    mContext.showInProgress(mContext.getString(R.string.operationing), false, true);
                                                                    //开始授权
                                                                    JavaThreadPool.getInstance().excute(new AuthorseKey(100, Long.parseLong(old_refulsh_device_id),CommandInfo.CommandTypeEnum.requestAddUser.value()));
                                                                } else if (position == 1) {
                                                                    //取消授权
                                                                    mContext.showInProgress(mContext.getString(R.string.operationing), false, true);
                                                                    JavaThreadPool.getInstance().excute(new AuthorseKey(0, Long.parseLong(old_refulsh_device_id),CommandInfo.CommandTypeEnum.requestAddUser.value()));
                                                                }
                                                            }
                                                        });
                                                permissonView.show();
                                            }
                                        } catch (Exception ex) {
                                            //防止key不是json崩溃
                                        }
                                    }
                                } else if (object != null && "101".equals(object.getString("sort"))) {
                                    long uid = mContext.dcsp.getLong(DataCenterSharedPreferences.Constant.LOGIN_APPID, 0);
                                    if (object.getString("send").equals(String.valueOf(uid))) {
                                        mContext.cancelInProgress();
                                        defaultHandler.removeMessages(dHandler_timeout);
                                        Toast.makeText(mContext, mContext.getString(R.string.cld_send_succ),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } catch (Exception ex) {
                                //防止json无数据崩溃
                            }
                        }
                    }
                } else if (intent.getStringExtra("device_id") != null) {
                    old_refulsh_device_id = intent.getStringExtra("device_id");
                    if (intent.getStringExtra("device_id") != null) {
                        String data = (String) intent.getSerializableExtra("device_info");
                        if (data != null) {
                            try {
                                JSONObject object = JSONObject.parseObject(data);
                                DeviceInfo deviceInfo = DatabaseOperator.getInstance().queryDeviceInfo(Long.parseLong(old_refulsh_device_id));
                                ZhujiInfo zhuji = DatabaseOperator.getInstance().queryDeviceZhuJiInfo(deviceInfo.getZj_id());
                                if (operationSort != null && operationSort.equals(object.getString("sort")) && mContext.progressIsShowing()) {
                                    Toast.makeText(mContext, getString(R.string.rq_control_sendsuccess),
                                            Toast.LENGTH_SHORT).show();
                                    mContext.cancelInProgress();
                                    defaultHandler.removeMessages(dHandler_timeout);
                                }else if(object != null && CommandInfo.CommandTypeEnum.requestAddUser.value().equals(object.getString("dt"))
                                        && !TextUtils.isEmpty(object.getString("deviceCommand")) && !"0".equals(object.getString("deviceCommand"))){
                                        if(zhuji!=null&&zhuji.isAdmin()){
                                           showWIFILockRequestUser(zhuji,deviceInfo);
                                        }
                                }else if(CommandInfo.CommandTypeEnum.reqLockNumberAuthorization.value().equals(object.getString("dt"))
                                        && !TextUtils.isEmpty(object.getString("dtv"))){
                                    if(zhuji!=null&&zhuji.isAdmin()) {
                                        showRunlongSuoRequestUser(object.getString("dtv"), deviceInfo);
                                    }
                                }
                            } catch (Exception ex) {
                                //防止json无数据崩溃
                            }
                        }
                    }
                }
                refreshData();
            } else if (Actions.CONNECTION_FAILED.equals(intent.getAction())) { // 连接断开
//                LogUtil.i(TAG, "Actions.CONNECTION_FAILED");
                listViewHeadView.findViewById(R.id.nonet_layout).setVisibility(View.VISIBLE);
                nonet_layout.setVisibility(View.VISIBLE);
//                startShowConnLoading();
            } else if (Actions.CONNECTION_SUCCESS.equals(intent.getAction())) { // 连接成功
//                LogUtil.i(TAG, "Actions.CONNECTION_SUCCESS");
                listViewHeadView.findViewById(R.id.nonet_layout).setVisibility(View.GONE);
                nonet_layout.setVisibility(View.GONE);
//                stopShowConnLoading();
            } else if (Actions.CONNECTION_ING.equals(intent.getAction())) { // 连接中
//                LogUtil.i(TAG, "Actions.CONNECTION_ING");
                listViewHeadView.findViewById(R.id.nonet_layout).setVisibility(View.GONE);
                nonet_layout.setVisibility(View.VISIBLE);
//                startShowConnLoading();
            } else if (Actions.CONNECTION_NONET.equals(intent.getAction())) { // 无网络
//                LogUtil.i(TAG, "Actions.CONNECTION_NONET");
                listViewHeadView.findViewById(R.id.nonet_layout).setVisibility(View.VISIBLE);
                nonet_layout.setVisibility(View.VISIBLE);
//                stopShowConnLoading();
            } else if (Actions.CONNECTION_FAILED_SENDFAILED.equals(intent.getAction()) && !isHidden()) { // 发送失败
                Toast.makeText(mContext, getString(R.string.rq_control_sendfailed),
                        Toast.LENGTH_SHORT).show();
                refreshData();
            } else if (Actions.SHOW_SERVER_MESSAGE.equals(intent.getAction()) && !isHidden()) { // 显示服务器信息
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
            }else if (Actions.CONTROL_BACK_MESSAGE.equals(intent.getAction()) && isResumed()) { // 控制返回
                //这里采用isResumed方法来判断Fragment是否出于resume状态
                defaultHandler.removeMessages(dHandler_timeout);
                if (intent.getIntExtra("code",0) == SyncMessage.CodeMenu.rp_control_needconfirm.value()) { //需要授权
                    final String keyStr = intent.getStringExtra("data_info");
                    defaultHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mContext.cancelInProgress();
                            controlConfrimAlert = new AlertView(getString(R.string.warning), getString(R.string.abbq_ges_notice_warning), getString(R.string.cancel),
                                    new String[]{getString(R.string.sure) + "(20)"}, null, mContext, AlertView.Style.Alert,
                                    new OnItemClickListener() {

                                        @Override
                                        public void onItemClick(Object o, final int position) {
                                            if (position != -1) {
                                                //发送控制
                                                mContext.showInProgress(getString(R.string.cld_send_ing));
                                                SyncMessage message = new SyncMessage();
                                                message.setCommand(SyncMessage.CommandMenu.rq_controlConfirm.value());
                                                message.setDeviceid(mContext.getZhuji().getId());
                                                // 操作 这里的操作写死，需要在服务器的配置页面中配置此按钮对应的指令是什么
                                                message.setSyncBytes(keyStr.getBytes());
                                                SyncMessageContainer.getInstance().produceSendMessage(message);
                                                defaultHandler.sendEmptyMessageDelayed(dHandler_timeout, 8000);//8秒超时
                                                controlConfrimAlert.dismiss();
                                            }
                                        }
                                    });
                            LinearLayout loAlertButtons = (LinearLayout) controlConfrimAlert.getContentContainer().findViewById(R.id.loAlertButtons);
                            TextView textView = (TextView) loAlertButtons.getChildAt(2).findViewById(R.id.tvAlert); //获取到按钮
                            textView.setTextColor(getResources().getColor(R.color.red));
                            controlConfrimAlert.show();
                            defaultHandler.sendMessageDelayed(defaultHandler.obtainMessage(dHandler_daojishi, 19, 0), 1000);//改变倒计时
                        }
                    },1000);
                }else if(intent.getIntExtra("code",0) == SyncMessage.CodeMenu.rp_control_verifyerror.value()){ //密码校验失败
                      //弹出提示框
                      mContext.cancelInProgress();//隐藏进度条
                     //ViewGroup频繁的remove会导致空指针异常.
                    if(mAnBaForceAlertTip!=null&&!mAnBaForceAlertTip.isShowing()){
                        lv_device.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(mAnBaForceAlertTip!=null&&!mAnBaForceAlertTip.isShowing()){
                                    mAnBaForceAlertTip.show();
                                }
                            }
                        },300);
                    }

                }
            } else if (Actions.ZHUJI_CHECKUPDATE.equals(intent.getAction()) && isResumed()) { // 检查主机版本
                mContext.cancelInProgress();
                boolean to = defaultHandler.hasMessages(dHandler_timeout);
                defaultHandler.removeMessages(dHandler_timeout);
                if (SyncMessage.CodeMenu.rp_checkpudate_nonew.value() == intent.getIntExtra("data", 0)) {
                    if (to) { // 当页面数据初始化完成会检测主机的固件版本，这个时候是不需要显示固件是最新提示的
                        String data_info = intent.getStringExtra("data_info");
                        if (data_info != null && !"".equals(data_info)) {
                            JSONObject object = JSON.parseObject(data_info);
                            Toast.makeText(mContext, getString(R.string.deviceslist_server_noupdatev, object.getString("ov")),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mContext, getString(R.string.deviceslist_server_noupdatev),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                } else if (SyncMessage.CodeMenu.rp_checkpudate_havenew.value() == intent.getIntExtra("data", 0)) {
                    String data_info = intent.getStringExtra("data_info");
                    if (data_info != null && !"".equals(data_info)) {
                        JSONObject object = JSON.parseObject(data_info);
                        data_info = getString(R.string.deviceslist_server_update_havenewv, object.getString("ov"), object.getString("nv"));
                    } else {
                        data_info = getString(R.string.deviceslist_server_update_havenew);
                    }
                    if (showAlert == null || !showAlert.isShowing()) {
                        showAlert = new AlertView(getString(R.string.deviceslist_server_update),
                                data_info,
                                getString(R.string.deviceslist_server_leftmenu_delcancel),
                                new String[]{getString(R.string.deviceslist_server_update_button)}, null,
                                mContext, AlertView.Style.Alert,
                                new com.smartism.znzk.view.alertview.OnItemClickListener() {

                                    @Override
                                    public void onItemClick(Object o, int position) {
                                        if (position != -1) {
                                            mContext.showInProgress(getString(R.string.ongoing));
                                            defaultHandler.sendEmptyMessageDelayed(dHandlerWhat_serverupdatetimeout, 20000);
                                            SyncMessage message1 = new SyncMessage();
                                            message1.setCommand(SyncMessage.CommandMenu.rq_pudate.value());
                                            message1.setDeviceid(mContext.getZhuji().getId());
                                            SyncMessageContainer.getInstance().produceSendMessage(message1);
                                        }
                                    }
                                });
                        showAlert.show();
                    }
                }
            } else if (Actions.ZHUJI_UPDATE.equals(intent.getAction()) && !isHidden()) { // 主机更新
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
            } else if (Actions.ACCETP_MAIN_SHOW_SCENCE.equals(intent.getAction())) {
                Log.e(TAG, ":isShowScence");
                isShowScence = intent.getBooleanExtra("is_show", false);
                setScenceControll(isShowScence());
            }else if (Actions.REFRESH_DEVICES_LIST.equals(intent.getAction())) { // 数据刷新完成广播
                refreshData();
            }else if(HongCaiSettingActivity.FLASH_AND_ALARM.equals(intent.getAction())){
                //宏才设置变更闪光与报警图标显示,这是设置修改后同步，刚开始也要同步一下
                showOrHideHongcai();
            }
        }
    };
    private void showWIFILockRequestUser(ZhujiInfo zhuji,final DeviceInfo deviceInfo){
        if (isHidden() || !mContext.isSee()){
            return;
        }
        try {
            if (permissonView == null || !permissonView.isShowing()) {
                permissonView = new AlertView(zhuji.getMasterid()+" "+deviceInfo.getName()+getString(R.string.activity_beijingsuo_reqkeyauthtitle), getString(R.string.jjsuo_request_adduser),
                        null, new String[]{getString(R.string.activity_beijingsuo_reqkeyauth), getString(R.string.activity_beijingsuo_notauth)
                        , getString(R.string.cancel)}, null,
                        mContext, AlertView.Style.Alert,
                        new com.smartism.znzk.view.alertview.OnItemClickListener() {

                            @Override
                            public void onItemClick(Object o, int position) {
                                if (position == 0) {
                                    mContext.showInProgress(mContext.getString(R.string.operationing), false, true);
                                    //开始授权
                                    JavaThreadPool.getInstance().excute(new AuthorseKey(100,deviceInfo.getId(),CommandInfo.CommandTypeEnum.requestAddUser.value()));
                                } else if (position == 1) {
                                    //取消授权
                                    mContext.showInProgress(mContext.getString(R.string.operationing), false, true);
                                    JavaThreadPool.getInstance().excute(new AuthorseKey(0, deviceInfo.getId(),CommandInfo.CommandTypeEnum.requestAddUser.value()));
                                }
                            }
                        });
                    permissonView.show();
            }
        } catch (Exception ex) {
            //防止key不是json崩溃
        }
    }
    private void showRunlongSuoRequestUser(String key, final DeviceInfo deviceInfo){
//        if (isHidden() || !mContext.isSee()){
//            return;
//        }
        try {
            JSONObject object = JSON.parseObject(key);
            if (object != null) {
                if (permissonView == null || !permissonView.isShowing()) {
                    permissonView = new AlertView(getString(R.string.activity_beijingsuo_reqkeyauthtitle), object.getString("lname"),
                            null,
                            new String[]{getString(R.string.activity_beijingsuo_reqkeyauth), getString(R.string.activity_beijingsuo_notauth)}, null,
                            mContext, AlertView.Style.Alert,
                            new OnItemClickListener() {

                                @Override
                                public void onItemClick(Object o, int position) {
                                    if (position == 0) {
                                        //开始授权
                                        mContext.showInProgress(mContext.getString(R.string.operationing), false, true);
                                        JavaThreadPool.getInstance().excute(new AuthorseKey(1,deviceInfo.getId(),CommandInfo.CommandTypeEnum.authorizationLockNumber.value()));
                                    } else if (position == 1) {
                                        //取消授权
                                        mContext.showInProgress(mContext.getString(R.string.operationing), false, true);
                                        JavaThreadPool.getInstance().excute(new AuthorseKey(0,deviceInfo.getId(),CommandInfo.CommandTypeEnum.authorizationLockNumber.value()));
                                    }
                                }
                            });
                    permissonView.show();
                }
            }
        } catch (Exception ex) {
            //防止key不是json崩溃
        }
    }

    private class AuthorseKey implements Runnable {
        int author = 0;
        long deviceId;
        String key = "";

        public AuthorseKey(int author, long deviceId,String key) {
            this.author = author;
            this.deviceId = deviceId;
            this.key = key;
        }

        @Override
        public void run() {
            String server = mContext.dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("did", deviceId);
            JSONArray array = new JSONArray();
            JSONObject o = new JSONObject();
            o.put("vkey", key);
            o.put("value", author);
            array.add(o);
            object.put("vkeys", array);
            String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/d/p/set", object, mContext);

            if (result != null && result.equals("0")) {
                defaultHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mContext.cancelInProgress();
                        Toast.makeText(mContext, getString(R.string.success),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                defaultHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mContext.cancelInProgress();
                        Toast.makeText(mContext, getString(R.string.operator_error),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    /**
     * @param flag 是否显示在家、设防、撤防场景
     */
    private void setScenceControll(boolean flag) {
        //测试要求有锁显示场景
        for(DeviceInfo temp : deviceInfos){
            if(temp.getCa()!=null){
                if(temp.getCa().equals(DeviceInfo.CaMenu.zhinengsuo.value())
                        ||temp.getCa().equals(DeviceInfo.CaMenu.wifizns.value())){
                    flag = true ;
                    break;
                }
            }
        }
        if(ZhujiListFragment.getMasterId().contains("FF3A")){
            //艾佳wifi主机不管有没有设备显示设防、布防按钮
            flag = true ;
        }
        if (flag || (Actions.VersionType.CHANNEL_ANBABAOQUAN.equals(MainApplication.app.getAppGlobalConfig().getVersion()))) {
            setShowScence(true);
            expandable_layout.setVisibility(View.VISIBLE);
//            if (!mContext.isZhujiFragment)
//                mContext.device_main_scnce.setVisibility(View.VISIBLE);
            if (!expandable_layout.isExpanded()) {
                ll_sence_sl.setVisibility(View.VISIBLE);
            }
        } else {
            setShowScence(false);
            expandable_layout.setVisibility(View.GONE);
//            mContext.device_main_scnce.setVisibility(View.GONE);
//                    ll_sence_sl.setVisibility(View.GONE);
        }
    }

    private void selectScene(int position) {
        for (int i = 0; i < sceneList.size(); i++) {
            if (sceneList.get(i).isFlag()) {
                sceneList.get(i).setFlag(false);
                mAdapter.notifyItemChanged(i);
            }
        }
        sceneList.get(position).setFlag(true);
        playMedia(position);
        mAdapter.notifyItemChanged(position);
    }

    private void playMedia(int position) {
        if (position == 0 || position == 1 || position == 2) {
            try {
                mp.reset();
                if (position == 0) {
                    mp = MediaPlayer.create(mContext, R.raw.sf);
                } else if (position == 1) {
                    mp = MediaPlayer.create(mContext, R.raw.cf);
                } else if (position == 2) {
                    mp = MediaPlayer.create(mContext, R.raw.zj);
                }
                mp.start();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "主机播放崩溃");
            }
        }

    }

    @Override
    public void onDestroy() {
        if (mp != null) {
            if (mp.isPlaying())
                mp.stop();
            mp.release();
        }
        super.onDestroy();
    }

    /**
     * 初始化只能场景列表
     *
     * @param list
     * @param flag 0的时候是首页显示的 ， 1的时候是弹出框的所有场景
     * @return
     */
    private List<RecyclerItemBean> initDefaultScence(List<SceneInfo> list, int flag) {

        List<RecyclerItemBean> slist = new ArrayList<>();
        String scene = (mContext.getZhuji() != null && mContext.getZhuji().getScene() != null) ? mContext.getZhuji().getScene() : "";
        SceneInfo sfScene = new SceneInfo();
        sfScene.setType(4);
        sfScene.setName(getString(R.string.activity_scene_item_outside));
        SceneInfo cfScene = new SceneInfo();
        cfScene.setType(5);
        cfScene.setName(getString(R.string.activity_scene_item_home));
        SceneInfo zjScene = new SceneInfo();
        zjScene.setType(3);
        zjScene.setName(getString(R.string.activity_scene_item_inhome));
        SceneInfo emergencyScene = new SceneInfo();
        emergencyScene.setType(100);//默认值
        emergencyScene.setName(getString(R.string.activity_scene_emergency));
        if (flag == 0) {
            if (DataCenterSharedPreferences.Constant.SCENE_NOW_SF.equals(scene)) {
                slist.add(new RecyclerItemBean(sfScene, 0, true));
                slist.add(new RecyclerItemBean(cfScene, 0, false));
                slist.add(new RecyclerItemBean(zjScene, 0, false));
            } else if (DataCenterSharedPreferences.Constant.SCENE_NOW_CF.equals(scene)) {
                slist.add(new RecyclerItemBean(sfScene, 0, false));
                slist.add(new RecyclerItemBean(cfScene, 0, true));
                slist.add(new RecyclerItemBean(zjScene, 0, false));
            } else if (DataCenterSharedPreferences.Constant.SCENE_NOW_HOME.equals(scene)) {
                slist.add(new RecyclerItemBean(sfScene, 0, false));
                slist.add(new RecyclerItemBean(cfScene, 0, false));
                slist.add(new RecyclerItemBean(zjScene, 0, true));
            } else {
                slist.add(new RecyclerItemBean(sfScene, 0, false));
                slist.add(new RecyclerItemBean(cfScene, 0, false));
                slist.add(new RecyclerItemBean(zjScene, 0, false));
            }
            slist.add(new RecyclerItemBean(emergencyScene, 0, false));
            for (SceneInfo sceneInfo : list) {
                if (sceneInfo.getType() < 3) {//代表是其他场景
                    if (sceneInfo.getName()!=null&&sceneInfo.getName().equals(mContext.getZhuji().getScene())) {
                        slist.add(new RecyclerItemBean(sceneInfo, 0, true));
                    } else {
                        slist.add(new RecyclerItemBean(sceneInfo, 0, false));
                    }
                }
            }
        } else if (flag == 1) {
            slist.add(new RecyclerItemBean(sfScene, 0, true));
            slist.add(new RecyclerItemBean(cfScene, 0, true));
            slist.add(new RecyclerItemBean(zjScene, 0, true));

            for (SceneInfo sceneInfo : list) {
                if (sceneInfo.getType() < 3) {
                    slist.add(new RecyclerItemBean(sceneInfo, 0, isSelect(sceneInfo)));
                }
            }
        }
        return slist;
    }

    //判断是否是是在首页显示的
    public boolean isSelect(SceneInfo info) {
        if (sceneList != null && !sceneList.isEmpty()) {
            for (RecyclerItemBean bean : sceneList) {
                SceneInfo sceneInfo = (SceneInfo) bean.getT();
                if (info.getId() == sceneInfo.getId())
                    return true;
            }

        }
        return false;
    }

    /*
      初始化的智能场景
     */
    private void initSelectScence(List<SceneInfo> list) {
        sceneList.clear();
        List<RecyclerItemBean> lists = initDefaultScence(list, 0);
        sceneList.addAll(lists);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 所有智能场景
     */
    class SaveScene implements Runnable {
        @Override
        public void run() {
            if (sceneWindow == null && sceneWindow.itemBeans == null || sceneWindow.itemBeans.isEmpty())
                return;
            String server = mContext.dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject pJsonObject = new JSONObject();
            pJsonObject.put("did", mContext.getZhuji().getId());
            JSONArray jsonArray = new JSONArray();
            for (RecyclerItemBean bean : sceneWindow.itemBeans) {
                SceneInfo info = (SceneInfo) bean.getT();
                if (info.getType() < 3 && bean.isFlag()) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id", info.getId());
                    jsonArray.add(jsonObject);
                }
            }
            pJsonObject.put("scenes", jsonArray);
            Log.e("pJsonObject", pJsonObject.toString());
            String result = HttpRequestUtils
                    .requestoOkHttpPost(server + "/jdm/s3/scenes/index/update", pJsonObject, mContext);
            List<SceneInfo> sceneInfos = new ArrayList<>();
            if ("0".equals(result)) {
                defaultHandler.sendEmptyMessage(dHandler_scenesave);
            } else if ("-3".equals(result)) {
                defaultHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mContext.cancelInProgress();
                    }
                });
            } else {
                defaultHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mContext.cancelInProgress();
                    }
                });
            }
        }
    }

    /**
     * 所有智能场景
     */
    class SceneAllLoad implements Runnable {
        @Override
        public void run() {
            String server = mContext.dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
//            String mId = mContext.dcsp.getString(DataCenterSharedPreferences.Constant.APP_MASTERID, "");
            //替换
            String mId =ZhujiListFragment.getMasterId();
            JSONObject pJsonObject = new JSONObject();
            pJsonObject.put("m", mId);
            String result = HttpRequestUtils
                    .requestoOkHttpPost(server + "/jdm/s3/scenes/all", pJsonObject, mContext);
            List<SceneInfo> sceneInfos = new ArrayList<>();
            if (!StringUtils.isEmpty(result) && result.length() > 2) {
                JSONArray ll = null;
                try {
                    ll = JSON.parseArray(result);
                } catch (Exception e) {
                    LogUtil.e(mContext, TAG, "解密错误：：", e);
                }
                if (ll == null) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(mContext, getString(R.string.device_set_tip_responseerr), Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }

                for (int j = 0; j < ll.size(); j++) {
                    SceneInfo info = new SceneInfo();
                    info.setId(((JSONObject) ll.get(j)).getLongValue("id"));
                    info.setName(((JSONObject) ll.get(j)).getString("name"));
                    info.setType(((JSONObject) ll.get(j)).getIntValue("type"));
                    info.setType(((JSONObject) ll.get(j)).getIntValue("type"));
                    info.setStatus(((JSONObject) ll.get(j)).getIntValue("status"));
                    sceneInfos.add(info);
                }
                Message m = defaultHandler.obtainMessage(dHandler_scenelist);
                m.obj = sceneInfos;
                defaultHandler.sendMessage(m);
            } else {
                defaultHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mContext.cancelInProgress();
                    }
                });
            }
        }
    }

    /**
     * 被选中的智能场景
     */
    class ScenesLoad implements Runnable {
        @Override
        public void run() {
            if (mContext.getZhuji() == null) return;
            String server = mContext.dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject pJsonObject = new JSONObject();
            pJsonObject.put("did", mContext.getZhuji().getId());
            Log.e("pJsonObject", pJsonObject.toString());
            String result = HttpRequestUtils
                    .requestoOkHttpPost(server + "/jdm/s3/scenes/index/all", pJsonObject, mContext);
            List<SceneInfo> sceneInfos = new ArrayList<SceneInfo>();
            Log.e("JSONObject", "result:" + result);
            if (result != null && result.startsWith("[")) {
                JSONArray ll = JSON.parseArray(result);
                if (ll == null) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(mContext, getString(R.string.device_set_tip_responseerr), Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }
                for (int j = 0; j < ll.size(); j++) {
                    SceneInfo info = new SceneInfo();
                    info.setId(((JSONObject) ll.get(j)).getLongValue("id"));
                    info.setName(((JSONObject) ll.get(j)).getString("name"));
                    info.setType(((JSONObject) ll.get(j)).getIntValue("type"));
                    info.setType(((JSONObject) ll.get(j)).getIntValue("type"));
                    info.setStatus(((JSONObject) ll.get(j)).getIntValue("status"));
                    sceneInfos.add(info);
                }
                Message m = defaultHandler.obtainMessage(dHandler_scenechoose);
                m.obj = sceneInfos;
                defaultHandler.sendMessage(m);
            } else {
                defaultHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mContext.cancelInProgress();
                    }
                });
            }
        }
    }


    public void moreHubChange(ZhujiInfo mZhuji) {
//        mContext.dcsp.putString(DataCenterSharedPreferences.Constant.APP_MASTERID, mZhuji.getMasterid()).commit();
        //替换
        ZhujiListFragment.setMasterId(mZhuji.getMasterid());
        refreshData();
    }

    public void moreHubChange() {
        zhujiList = DatabaseOperator.getInstance(mContext.getApplicationContext()).queryAllZhuJiInfos();
        List<String> is = new ArrayList<String>();
        int select = 0;
        for (int j = 0; j < zhujiList.size(); j++) {
            if (mContext.getZhuji().getId() == zhujiList.get(j).getId()) {
                select = j;
            }
            is.add(zhujiList.get(j).getName() + zhujiList.get(j).getWhere());
        }
        new AlertDialog.Builder(mContext).setTitle(getString(R.string.deviceslist_morehub_title))
                .setSingleChoiceItems(is.toArray(new String[zhujiList.size()]), select, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        mContext.dcsp.putString(DataCenterSharedPreferences.Constant.APP_MASTERID, zhujiList.get(which).getMasterid()).commit();
//                        替换
                        ZhujiListFragment.setMasterId(zhujiList.get(which).getMasterid());
                        refreshData();
                        dialog.dismiss();
                    }
                }).setNegativeButton(getString(R.string.setting_activity_cancel), null).show();
    }

    /**
     * 首页广告轮播图
     */
    class GetHomeTopBannerImage implements Runnable {
        @Override
        public void run() {
            JSONObject o = new JSONObject();
            o.put("key", "home_top");
            String server = mContext.dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");

            String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/ad/list", o, mContext);
            Message message = defaultHandler.obtainMessage(dHandler_banner_top);
            if (!TextUtils.isEmpty(result) && result.length() > 4) {
                message.obj = result;
                defaultHandler.sendMessage(message);
            }
        }
    }
    /**
     * 首页底部轮播图
     */
    class GetHomeButtomBannerImage implements Runnable {
        @Override
        public void run() {

            JSONObject o = new JSONObject();
            o.put("key", "home_bottom");
            String server = mContext.dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");

            String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/ad/list", o, mContext);
            if (!TextUtils.isEmpty(result) && result.length() > 4) {
                Message message = defaultHandler.obtainMessage(dHandler_banner_bottom);
                message.obj = result;
                defaultHandler.sendMessage(message);
            }
        }
    }

    private void initBanner(final List<ImageBannerInfo.ImageBannerBean> list, Banner banner) {
        banner.setVisibility(View.VISIBLE);
        List<String> images = new ArrayList<>();
        //设置banner样式
        banner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR);
        //设置图片加载器
        banner.setImageLoader(new GlideImageLoader());
        int width = getActivity().getResources().getDisplayMetrics().widthPixels;
        if (banner.getId() == R.id.banner_bottom) {
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, width / 4);
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            banner.setLayoutParams(lp);
        }else if(banner.getId() == R.id.banner_top){
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Integer.parseInt(String.valueOf((int)(width * 0.45))));
            lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            banner.setLayoutParams(lp);
        }
        //设置图片集合
        for (ImageBannerInfo.ImageBannerBean bean : list) {
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
        //设置自动轮播
        banner.isAutoPlay(images.size() > 1);
        //设置轮播时间
        banner.setDelayTime(15000);

        banner.setOnBannerListener(new OnBannerListener() {
            @Override
            public void OnBannerClick(int position) {
                Intent intent = new Intent(getActivity(), CommonWebViewActivity.class);
                intent.putExtra("url", list.get(position).getUrl());
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
}
