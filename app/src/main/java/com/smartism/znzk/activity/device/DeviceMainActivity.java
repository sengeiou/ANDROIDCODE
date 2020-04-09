package com.smartism.znzk.activity.device;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.amazonaws.amplify.generated.graphql.CreateCtrGroupDevicesMutation;
import com.amazonaws.amplify.generated.graphql.CreateCtrUserInfoTableMutation;
import com.amazonaws.amplify.generated.graphql.ListCtrUserInfoTablesQuery;
import com.amazonaws.amplify.generated.graphql.UpdateCtrUserGroupMutation;
import com.amazonaws.amplify.generated.graphql.UpdateCtrUserInfoTableMutation;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest;
import com.amazonaws.services.sns.model.CreatePlatformEndpointResult;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.nineoldandroids.view.ViewHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.CommonWebViewActivity;
import com.smartism.znzk.activity.DeviceGridMainFragment;
import com.smartism.znzk.activity.DeviceMainFragment;
import com.smartism.znzk.activity.FragmentParentActivity;
import com.smartism.znzk.activity.MessageCenterFragment;
import com.smartism.znzk.activity.MineFragment;
import com.smartism.znzk.activity.ServiceFragment;
import com.smartism.znzk.activity.ServiceNewFragment;
import com.smartism.znzk.activity.SettingSpeechActivity;
import com.smartism.znzk.activity.camera.AddContactTypeActivity;
import com.smartism.znzk.activity.camera.RadarAddActivity;
import com.smartism.znzk.activity.common.AboutActivity;
import com.smartism.znzk.activity.common.SettingActivity;
import com.smartism.znzk.activity.common.XZSWAboutActivity;
import com.smartism.znzk.activity.developer.DeveCommandShowActivity;
import com.smartism.znzk.activity.device.add.AddDeviceChooseActivity;
import com.smartism.znzk.activity.device.add.AddGroupActivity;
import com.smartism.znzk.activity.device.add.AddVirtualRemoteControlActivity;
import com.smartism.znzk.activity.device.add.AddZhujiActivity;
import com.smartism.znzk.activity.device.add.AddZhujiOldActivity;
import com.smartism.znzk.activity.device.add.AddZhujiWayChooseActivity;
import com.smartism.znzk.activity.device.share.ShareDevicesActivity;
import com.smartism.znzk.activity.scene.SceneActivity;
import com.smartism.znzk.activity.scene.SelectSceneTypeActivity;
import com.smartism.znzk.activity.smartlock.LockMainActivity;
import com.smartism.znzk.activity.smartlock.WifiLockMainActivity;
import com.smartism.znzk.activity.user.LoginActivity;
import com.smartism.znzk.activity.user.UserInfoActivity;
import com.smartism.znzk.activity.user.factory.FactoryAddDevicesActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.awsClient.AWSClients;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.communication.service.CoreService;
import com.smartism.znzk.communication.service.FloatService;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.CategoryInfo;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.hipermission.HiPermission;
import com.smartism.znzk.hipermission.PermissionCallback;
import com.smartism.znzk.hipermission.PermissionItem;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.AndroidRomUtil;
import com.smartism.znzk.util.BaiduLBSUtils;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.DateUtil;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.LogUtil;
import com.smartism.znzk.util.NotificationUtil;
import com.smartism.znzk.util.SecurityUtil;
import com.smartism.znzk.util.ToastUtil;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.util.update.UpdateAgent;
import com.smartism.znzk.util.update.UpdateListener;
import com.smartism.znzk.util.update.UpdateResponse;
import com.smartism.znzk.util.update.UpdateStatus;
import com.smartism.znzk.view.CircleImageView;
import com.smartism.znzk.view.DialogView;
import com.smartism.znzk.view.MenuInteractionPopupWindow;
import com.smartism.znzk.view.SelectAddPopupWindow;
import com.smartism.znzk.view.ZhzjAddPopupWindow;
import com.smartism.znzk.view.alertview.AlertView;
import com.smartism.znzk.view.alertview.OnItemClickListener;
import com.smartism.znzk.view.zbarscan.ScanCaptureActivity;
import com.smartism.znzk.zhicheng.tasks.LoadZhujiAndDeviceTask;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import type.CreateCtrGroupDevicesInput;
import type.CreateCtrUserInfoTableInput;
import type.TableCtrUserInfoTableFilterInput;
import type.TableStringFilterInput;
import type.UpdateCtrUserInfoTableInput;

import static com.smartism.znzk.activity.device.add.AddDeviceChooseActivity.NET_TYPE;


/**
 * Created by win7 on 2016/11/4.
 */
public class DeviceMainActivity extends FragmentParentActivity implements NavigationView.OnNavigationItemSelectedListener,
        View.OnClickListener, AdapterView.OnItemClickListener, DeviceMainFragment.DataCallBack {
    public static int OVERLAY_PERMISSION_REQ_CODE = 12;
    public static final String TAG = DeviceMainActivity.class.getSimpleName();
    public long defaultStartDid = 0;//报警设备id,这个值会从远程推送的通知栏上传递过来
    public static DeviceMainActivity mthis;
    private String snsEndpointArn = "";
    private ZhujiInfo zhuji;
    private TabLayout tabLayout;
    private Toolbar toolbar;
    private View headerView;
    private Menu navMenu;
    private String[] titles;
    private CircleImageView toolBar_icom, headerImage;
    private int[] unImage = {R.drawable.abbq_tab_shebei, R.drawable.unshop,
            R.drawable.uninteraction, R.drawable.unservice, R.drawable.abbq_tab_fuwu, R.drawable.abbq_tab_message, R.drawable.tab_found, R.drawable.tab_shop, R.drawable.tab_mine, R.drawable.abbq_tab_wode}; // Tab未选中图标数组
    private int[] image = {R.drawable.abbq_tab_shebei_h, R.drawable.shop,
            R.drawable.interaction, R.drawable.service, R.drawable.abbq_tab_fuwu_h, R.drawable.abbq_tab_message_h, R.drawable.tab_found_h, R.drawable.tab_shop_h, R.drawable.tab_mine_h, R.drawable.abbq_tab_wode_h}; // Tab选中图标数组

    private FragmentManager fragmentManager;
    private DeviceMainFragment mainFragment;
    private ZhujiListFragment zhujiFragment;
//    private ShopMainFragment shopMainFragment; //整个商城为app的一个tab页签
//    private ShopTabMainFragment shopTabMainFragment; //商城为APP第一层时 - 首页
//    private ShopTabFindFragment shopTabFindFragment; //商城为APP第一层时 - 发现 从ServiceNewFragment拷贝过来就可以。从ShopMainFragment拷贝过来有问题不会切换只会显示在第一个tab页签上
//    private ShopTabMerchantFragment shopTabMerchantFragment; //商城为APP第一层时 - 商家
//    private ShopTabMineFragment shopTabMineFragment; //商城为APP第一层时 - 我的
//    private InteractionFragment interactionFragment;
    private ServiceFragment serviceFragment;
    //    private ServiceABBQFragment serviceABBQFragment;
    private ServiceNewFragment serviceNewFragment;
    private MineFragment mineFragment;
    private MessageCenterFragment messageCenterFragment;
    private long lastTime = 0;
    public TextView toolbar_title;
    private AlertView mAlertView;
    public ZhzjAddPopupWindow menuWindow; // 右上角弹出框
    public SelectAddPopupWindow zjMenuWindow; // 弹出框
    public MenuInteractionPopupWindow bbsMenuWindow; // 弹出框 互动模块的菜单
    public ImageView main_menu, conn_icon, zhujiMenu, device_main_scnce, img_back,set_menu;
    private RelativeLayout rl_user;
    public TextView userName, user_devices;
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private DrawerLayout.DrawerListener drawerListener;
    private boolean isRight = false; //是否是右侧滑
    public boolean isShowTab = false;

    private DeviceGridMainFragment gridMainFragment;
    public final static String ACTION_CHANGE_FRAGMENT = "com.smartism.znzk.changefragment";//切换设备列表和主机列表
    private ImageView iv_menu_qrcode;

    public boolean isNoticeAddMobile;

    private Fragment mainShowFragment; //首页tab当前显示的fragment

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_main1);

        AWSClients.getInstance().init(this);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            Util.setStatusBarColor(this,getResources().getColor(R.color.device_main_bg));
        }
//        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mthis = this;
        //根据这个设置显示侧滑的位置
        isRight = MainApplication.app.getAppGlobalConfig().isRightMenu();
        initView();
        initDrawerLyout();
        isNoticeAddMobile = getIntent().getBooleanExtra("isNoticeAddMobile", false);
//        if (Actions.VersionType.CHANNEL_WOAIJIA.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
//            initfragment();
//        } else {
//            initTab();
//        }
//        initPermission(); 欢迎页做了权限申请，这里不再申请
        initTab();
        initPush();
        initData();
        initLocation();
        //保存启动app时间，下次启动不显示欢迎页
        dcsp.putString(DataCenterSharedPreferences.Constant.START_APP_TIME, DateUtil.formatUnixTime(System.currentTimeMillis(), "yyyy-MM-dd")).commit();

        if (MainApplication.app.getAppGlobalConfig().isSowSpeech()) {
            if (getIntent().getBooleanExtra("isNotCamera", true)) {
                showFloatingButton();
            }
        }

        if (!LogUtil.isDebug && MainApplication.app.getAppGlobalConfig().isAutomaticUpdates()) { // 只有正式环境才让更新
            //从摄像头列表跳转当前界面之后会提示更新，为了避免从摄像头列表跳转弹出，添加一个判断
//            if (getIntent().getBooleanExtra("isNotCamera", true) && getIntent().getBooleanExtra(Constant.IS_UPDATE_AGAIN, true)) {
            if (getIntent().getBooleanExtra("isNotCamera", true) && MainApplication.app.isNotice()) {
                UpdateAgent.update(this);
            }
        }
        autoOpenDeviceActivity(getIntent());
        initCoreService();
//        changeFragment("zhuji");
    }

    private void initCoreService(){
        try {
            // 启动管理服务器连接的服务
            Intent intent = new Intent();
            intent.setClass(mContext.getApplicationContext(), CoreService.class);
            mContext.startService(intent);
        } catch (Exception ex) {
            //oppo 手机有些会调用异常
            Toast.makeText(mContext, "service start failed", Toast.LENGTH_SHORT).show();
        }
    }

    public LocationClient mLocationClient = null;



    private void initLocation() {
        if (!Actions.VersionType.CHANNEL_ZHZJ.equals(MainApplication.app.getAppGlobalConfig().getVersion()) &&
                !Actions.VersionType.CHANNEL_WANGDUODUO.equals(MainApplication.app.getAppGlobalConfig().getVersion()) &&
                !Actions.VersionType.CHANNEL_AIERFUDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            return;
        }
        //百度定位方式
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.setLocOption(BaiduLBSUtils.createClientOption());
        mLocationClient.registerLocationListener(new BDAbstractLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
                //以下只列举部分获取经纬度相关（常用）的结果信息
                //更多结果信息获取说明，请参照类参考中BDLocation类中的说明
                double latitude = bdLocation.getLatitude();    //获取纬度信息
                double longitude = bdLocation.getLongitude();    //获取经度信息
                float radius = bdLocation.getRadius();    //获取定位精度，默认值为0.0f

                String coorType = bdLocation.getCoorType();
                //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准
                String addr = bdLocation.getAddrStr();    //获取详细地址信息
                String country = bdLocation.getCountry();    //获取国家
                String province = bdLocation.getProvince();    //获取省份
                String city = bdLocation.getCity();    //获取城市
                String district = bdLocation.getDistrict();    //获取区县
                String street = bdLocation.getStreet();    //获取街道信息

                double[] temp = BaiduLBSUtils.gcjToWgs(latitude, longitude);
                bdLocation.setLatitude(temp[0]);
                bdLocation.setLongitude(temp[1]);
                int errorCode = bdLocation.getLocType();
                getJdmApplication().setLocation(bdLocation);
//                if (shopTabMainFragment != null) {
//                    shopTabMainFragment.initWebViewData();
//                }
//                if (shopTabFindFragment != null) {
//                    shopTabFindFragment.initWebViewData();
//                }
//                if (shopTabMerchantFragment != null) {
//                    shopTabMerchantFragment.initWebViewData();
//                }
//                Toast.makeText(mContext,"经度:"+location.getLongitude()+",纬度："+location.getLatitude(),Toast.LENGTH_SHORT).show();
                //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明
                if (BaiduLBSUtils.judgeLocationSucess(bdLocation.getLocType())){
                    initWeather();
                    mLocationClient.stop();//只定位一次 就调用stop
                }
            }
        });
        mLocationClient.start(); //开始
    }


    public void changeFragment(String flag) {
        Log.d("changeFragment",flag);
        if (zhujiFragment == null) zhujiFragment = new ZhujiListFragment();

        if ("zhuji".equals(flag)) {//进入设备
//            toolBar_icom.setVisibility(View.VISIBLE);
            toolbar_title.setVisibility(View.VISIBLE);
            if (zhuji!=null && !StringUtils.isEmpty(zhuji.getBrandName())) {
                toolbar_title.setText(Html.fromHtml(zhuji.getBrandNameText()));
            }else{
                toolbar_title.setText("");
            }
            img_back.setVisibility(View.VISIBLE);
            showDeviceFragment();
            //刷新主机切换
            Intent frush = new Intent();
            frush.setAction(Actions.REFRESH_DEVICES_LIST);
            sendBroadcast(frush);
        } else if("main".equals(flag)){ //回到主机列表
            toolbar_title.setVisibility(View.GONE);
            main_menu.setVisibility(View.GONE);
            iv_menu_qrcode.setVisibility(View.VISIBLE);
            set_menu.setVisibility(View.GONE);
            if (MainApplication.app.getAppGlobalConfig().isShowShoTabMain()){ //主机上层是否还有fragment显示
                img_back.setVisibility(View.VISIBLE);
            }else {
                img_back.setVisibility(View.GONE);
            }
            switchContent(mainShowFragment,zhujiFragment);
            mainShowFragment = zhujiFragment;
        } else { //首页默认显示的页面 可以是主机列表 也可以是其它
//            if (MainApplication.app.getAppGlobalConfig().isShowShoTabMain()) { //主机上层是否还有fragment显示
//                switchContent(mainShowFragment,shopTabMainFragment);
//                mainShowFragment = shopTabMainFragment;
//                img_back.setVisibility(View.GONE);
//                main_menu.setVisibility(View.GONE);
//                iv_menu_qrcode.setVisibility(View.GONE);
//                set_menu.setVisibility(View.GONE);
//            }
        }
        initLeftMenu();
        menuWindow.updateMenu(dcsp, zhuji, mainShowFragment);
    }

    private void showDeviceFragment(){
        if (mainFragment == null) mainFragment = new DeviceMainFragment();
        main_menu.setVisibility(View.GONE);
        iv_menu_qrcode.setVisibility(View.VISIBLE);
        set_menu.setVisibility(View.GONE);
        switchContent(mainShowFragment, mainFragment);
        mainShowFragment = mainFragment;
        this.mainFragment.setDataCallBack(this);
    }

    private void hideDeviceFragment(FragmentTransaction transaction){
        if (mainFragment != null) {
            transaction.hide(mainFragment);
        }
    }

    private void initDrawerLyout() {
        if (Actions.VersionType.CHANNEL_ANBABAOQUAN.equals(MainApplication.app.getAppGlobalConfig().getVersion()) ||
                Actions.VersionType.CHANNEL_ZHZJ.equals(MainApplication.app.getAppGlobalConfig().getVersion()))
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        if (isRight) {
            DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) navigationView.getLayoutParams();
            params.gravity = Gravity.END;
            navigationView.setLayoutParams(params);
        }

        drawerListener = new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                View mContent = drawer.getChildAt(0);
                View mMenu = drawerView;
                float scale = 1 - slideOffset;
                float rightScale = 0.9f + scale * 0.1f;
                if (!isRight) {

                    //改变DrawLayout侧栏透明度，若不需要效果可以不设置
                    ViewHelper.setAlpha(mMenu, 0.6f + 0.4f * (1 - scale));
                    ViewHelper.setTranslationX(mContent,
                            mMenu.getMeasuredWidth() * (1 - scale));
                    ViewHelper.setPivotX(mContent, 0);
                    ViewHelper.setPivotY(mContent, mContent.getMeasuredHeight() / 2);
                    mContent.invalidate();
                    ViewHelper.setScaleX(mContent, rightScale);
                    ViewHelper.setScaleY(mContent, rightScale);
                } else {
                    ViewHelper.setTranslationX(mContent,
                            -mMenu.getMeasuredWidth() * slideOffset);
                    ViewHelper.setPivotX(mContent, mContent.getMeasuredWidth());
                    ViewHelper.setPivotY(mContent,
                            mContent.getMeasuredHeight() / 2);
                    mContent.invalidate();
                    ViewHelper.setScaleX(mContent, rightScale);
                    ViewHelper.setScaleY(mContent, rightScale);
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {

            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        };

        drawer.addDrawerListener(drawerListener);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.about_s, R.string.about_s);
        drawer.setDrawerListener(toggle);


        navigationView.setNavigationItemSelectedListener(this);
        //侧滑菜单列表
        navMenu = navigationView.getMenu();
        headerView = LayoutInflater.from(this).inflate(R.layout.activity_nav_hander, null);
        navigationView.addHeaderView(headerView);
        //这样设置后icon和title的颜色就是默认的了
        navigationView.setItemTextColor(null);
        navigationView.setItemIconTintList(null);
        headerImage = (CircleImageView) headerView.findViewById(R.id.sideslip_header_img);
        userName = (TextView) headerView.findViewById(R.id.sideslip_username_tv);
        user_devices = (TextView) headerView.findViewById(R.id.sideslip_device_num);
        rl_user = (RelativeLayout) headerView.findViewById(R.id.rl_user);
//        headerImage.setOnClickListener(this);
        rl_user.setOnClickListener(this);
        userName.setText(dcsp.getString(DataCenterSharedPreferences.Constant.LOGIN_APPNAME, ""));

        showUserLogo();
    }

    /**
     * 侧滑菜单列表项的显示与隐藏\
     * 0、个人信息
     * 1、主机交换
     * 2、智能场景
     * 3、更新
     * 4、帮助
     * 5、关于
     * 6、设置
     * 7、推出
     * 8、语音设置
     * 9、退出
     */
    public void initLeftMenu() {
        if (navMenu == null) {
            return;
        }
        boolean[] showmenu = new boolean[navMenu.size()];
        zhujiMenu.setVisibility(View.GONE);
        if (mainShowFragment instanceof ZhujiListFragment) {
            navMenu.getItem(0).setVisible(false);
            navMenu.getItem(1).setVisible(false);
            navMenu.getItem(2).setVisible(false);
            if (MainApplication.app.getAppGlobalConfig().isAutomaticUpdates()) {
                navMenu.getItem(3).setVisible(true);
            } else {
                navMenu.getItem(3).setVisible(false);
            }
            navMenu.getItem(4).setVisible(false);
            navMenu.getItem(5).setVisible(true);
            navMenu.getItem(6).setVisible(false);
            navMenu.getItem(7).setVisible(false);
            navMenu.getItem(8).setVisible(false);
            navMenu.getItem(9).setVisible(true);

        } else if (mainShowFragment instanceof DeviceMainFragment){
            navMenu.getItem(0).setVisible(false);
            navMenu.getItem(1).setVisible(false);
            navMenu.getItem(2).setVisible(false);
            if (MainApplication.app.getAppGlobalConfig().isAutomaticUpdates()) {
                navMenu.getItem(3).setVisible(true);
            } else {
                navMenu.getItem(3).setVisible(false);
            }
            if (MainApplication.app.getAppGlobalConfig().isShowHeldWeb()) {
                navMenu.getItem(4).setVisible(true);
            } else {
                navMenu.getItem(4).setVisible(false);
            }

            navMenu.getItem(5).setVisible(true);

            if (Actions.VersionType.CHANNEL_FSNY.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
//                if (zhuji != null && zhuji.isAdmin()){
//                    navMenu.getItem(7).setVisible(true);
//                }else {
//                    navMenu.getItem(7).setVisible(false);
//                }
                navMenu.getItem(6).setVisible(false);
            } else {
                navMenu.getItem(6).setVisible(true);
            }
            if (Actions.VersionType.CHANNEL_FSNY.equals(MainApplication.app.getAppGlobalConfig().getVersion()) && zhuji != null && zhuji.isAdmin()) {
                navMenu.getItem(7).setVisible(true);
            } else {
                navMenu.getItem(7).setVisible(false);
            }
            if (MainApplication.app.getAppGlobalConfig().isSowSpeech()) {
                navMenu.getItem(8).setVisible(true);
            } else {
                navMenu.getItem(8).setVisible(false);
            }
            navMenu.getItem(9).setVisible(true);
        }else{
            navMenu.getItem(0).setVisible(false);
            navMenu.getItem(1).setVisible(false);
            navMenu.getItem(2).setVisible(false);
            if (MainApplication.app.getAppGlobalConfig().isAutomaticUpdates()) {
                navMenu.getItem(3).setVisible(true);
            } else {
                navMenu.getItem(3).setVisible(false);
            }
            navMenu.getItem(4).setVisible(false);
            navMenu.getItem(5).setVisible(true);
            navMenu.getItem(6).setVisible(false);
            navMenu.getItem(7).setVisible(false);
            navMenu.getItem(8).setVisible(false);
            navMenu.getItem(9).setVisible(true);
        }
    }

    public void showUserLogo() {
        if (!"".equals(dcsp.getString(Constant.LOGIN_LOGO, ""))) {
            ImageLoader.getInstance().displayImage(dcsp.getString(Constant.LOGIN_LOGO, ""), toolBar_icom,
                    options_userlogo);
            ImageLoader.getInstance().displayImage(dcsp.getString(Constant.LOGIN_LOGO, ""), headerImage, options_userlogo);
            if (mineFragment != null && mineFragment.userinfo_logo != null) {
                ImageLoader.getInstance().displayImage(dcsp.getString(Constant.LOGIN_LOGO, ""), mineFragment.userinfo_logo, options_userlogo);
            }

        } else {
            toolBar_icom.setImageResource(R.drawable.h0);
            headerImage.setImageResource(R.drawable.h0);
            if (mineFragment != null && mineFragment.userinfo_logo != null) {
                ImageLoader.getInstance().displayImage(dcsp.getString(Constant.LOGIN_LOGO, ""), mineFragment.userinfo_logo, options_userlogo);
            }
        }
    }

    @Override
    public void onBackPressed() {
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (mainShowFragment!=null){
                if (mainShowFragment instanceof DeviceMainFragment && !mainShowFragment.isHidden()){
                    changeFragment("main");//返回主机列表
                    return;
                }
            }
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastTime < 2000) {
                moveTaskToBack(true);
            } else {
                Toast.makeText(this, getString(R.string.deviceslist_server_exit_again), Toast.LENGTH_SHORT).show();
            }
            lastTime = System.currentTimeMillis();
        }
//        if (!isFinishing())
//            super.onBackPressed();
    }

    /**
     * 设置默认tab样式
     *
     * @param tab
     */

    private void setTab(TabLayout.Tab tab) {
        View view = tab.getCustomView();
        TextView tvTabTitle = (TextView) view.findViewById(R.id.tvTabTitle);
//        tvTabTitle.setTextColor(getResources().getColor(R.color.dodgerblue));
        ImageView ivTabImage = (ImageView) view.findViewById(R.id.ivTabImage);
        ivTabImage.setImageResource(image[(int) tab.getTag()]);
    }

    /**
     * 设置未选中Tab样式
     *
     * @param tab 目标Tab
     */
    private void unselectTab(TabLayout.Tab tab) {
        View view = tab.getCustomView();
        TextView tvTabTitle = (TextView) view.findViewById(R.id.tvTabTitle);
//        tvTabTitle.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.tab_unselected));
        ImageView ivTabImage = (ImageView) view.findViewById(R.id.ivTabImage);
        ivTabImage.setImageResource(unImage[(int) tab.getTag()]);
    }

    /**
     * 显示选中Fragment
     *
     * @param tag Fragment别名
     */
    private void selectFragment(int tag) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        switch (tag) {
            case 0:
                if (MainApplication.app.getAppGlobalConfig().isGridFragment()) {
                    transaction.show(gridMainFragment);
                } else {
                    if (mainShowFragment instanceof ZhujiListFragment) {
                        if (MainApplication.app.getAppGlobalConfig().isShowShoTabMain()) {
                            img_back.setVisibility(View.VISIBLE);
                        }else{
                            img_back.setVisibility(View.GONE);
                        }
                        toolbar_title.setVisibility(View.GONE);
                        main_menu.setVisibility(View.GONE);
                        iv_menu_qrcode.setVisibility(View.VISIBLE);
                        set_menu.setVisibility(View.GONE);
                        transaction.show(mainShowFragment);
//                        if (mainFragment != null) {
//                            transaction.hide(mainFragment);
//                        }
                    } else if(mainShowFragment instanceof DeviceMainFragment) {
                        toolbar_title.setVisibility(View.VISIBLE);
                        img_back.setVisibility(View.VISIBLE);
                        if (mainShowFragment instanceof DeviceMainFragment){
                            main_menu.setVisibility(View.GONE);
                            iv_menu_qrcode.setVisibility(View.VISIBLE);
                            set_menu.setVisibility(View.GONE);
                        }else{
                            main_menu.setVisibility(View.GONE);
                            iv_menu_qrcode.setVisibility(View.GONE);
                            set_menu.setVisibility(View.VISIBLE);
                        }
                        transaction.show(mainShowFragment);
//                        if (zhujiFragment != null) {
//                            transaction.hide(zhujiFragment);
//                        }
                        if (zhuji!=null && !StringUtils.isEmpty(zhuji.getBrandName())) {
                            toolbar_title.setText(Html.fromHtml(zhuji.getBrandNameText()));
                        }else{
                            toolbar_title.setText("");
                        }
                    }else{
                        transaction.show(mainShowFragment);
                        hideDeviceFragment(transaction);
                        if (zhujiFragment != null) {
                            transaction.hide(zhujiFragment);
                        }
                        toolbar_title.setVisibility(View.GONE);
                        img_back.setVisibility(View.GONE);
                        iv_menu_qrcode.setVisibility(View.GONE);
                        main_menu.setVisibility(View.GONE);
                        set_menu.setVisibility(View.GONE);
                    }
                }
//                if (serviceNewFragment != null)
//                    transaction.hide(serviceNewFragment);
//                if (mineFragment != null)
//                    transaction.hide(mineFragment); 这是要做什么。看不懂 注释掉。有bug
                toolbar.setVisibility(View.VISIBLE);
                toolBar_icom.setVisibility(View.VISIBLE);
//                device_main_scnce.setVisibility(View.GONE);//2017814
                break;
            case 1:
//                if (shopMainFragment == null) {
//                    FragmentTransaction t = fragmentManager.beginTransaction();
//                    shopMainFragment = new ShopMainFragment();
//                    t.add(R.id.frame, shopMainFragment);
////                    t.addToBackStack(null);
//                    t.commit();
//                }
//                transaction.show(shopMainFragment);
                toolbar.setVisibility(View.GONE);
                break;
            case 2:
//                if (interactionFragment == null) {
//                    FragmentTransaction t = fragmentManager.beginTransaction();
//                    interactionFragment = new InteractionFragment();
//                    t.add(R.id.frame, interactionFragment);
////                    t.addToBackStack(null);
//                    t.commit();
//                }
//                transaction.show(interactionFragment);
                toolbar.setVisibility(View.VISIBLE);
                main_menu.setVisibility(View.GONE);
                set_menu.setVisibility(View.GONE);
                break;
            case 3:
                if (serviceFragment == null) {
                    FragmentTransaction t = fragmentManager.beginTransaction();
                    serviceFragment = new ServiceFragment();
                    t.add(R.id.frame, serviceFragment);
//                    t.addToBackStack(null);
                    t.commit();
                }
                transaction.show(serviceFragment);
                toolbar.setVisibility(View.VISIBLE);
                if (DataCenterSharedPreferences.Constant.ROLE_ASADMIN.equals(dcsp.getString(DataCenterSharedPreferences.Constant.LOGIN_ROLE, DataCenterSharedPreferences.Constant.ROLE_NORMAL))
                        || DataCenterSharedPreferences.Constant.ROLE_ASSERVICE.equals(dcsp.getString(DataCenterSharedPreferences.Constant.LOGIN_ROLE, DataCenterSharedPreferences.Constant.ROLE_NORMAL))) {
                    main_menu.setVisibility(View.GONE);
                } else {
                    main_menu.setVisibility(View.GONE);
                }
                break;
            case 4:
                if (serviceNewFragment == null) {
                    FragmentTransaction t = fragmentManager.beginTransaction();
                    serviceNewFragment = new ServiceNewFragment();
                    t.add(R.id.frame, serviceNewFragment);
//                    t.addToBackStack(null);
                    t.commit();
                }
                transaction.show(serviceNewFragment);
                img_back.setVisibility(View.GONE);
                toolBar_icom.setVisibility(View.GONE);
                main_menu.setVisibility(View.GONE);
                iv_menu_qrcode.setVisibility(View.GONE);
                set_menu.setVisibility(View.GONE);
                toolbar_title.setVisibility(View.VISIBLE);
                toolbar_title.setText(getString(R.string.tab_service));
                break;
            case 5:
                if (messageCenterFragment == null) {
                    FragmentTransaction t = fragmentManager.beginTransaction();
                    messageCenterFragment = new MessageCenterFragment();
                    t.add(R.id.frame, messageCenterFragment);
//                    t.addToBackStack(null);
                    t.commit();
                }
                transaction.show(messageCenterFragment);
                img_back.setVisibility(View.GONE);
                toolBar_icom.setVisibility(View.GONE);
                main_menu.setVisibility(View.GONE);
                iv_menu_qrcode.setVisibility(View.GONE);
                set_menu.setVisibility(View.GONE);
                toolbar_title.setVisibility(View.VISIBLE);
                toolbar_title.setText(getString(R.string.tab_message));
                break;
            case 6:
//                if (shopTabFindFragment == null) {
//                    FragmentTransaction t = fragmentManager.beginTransaction();
//                    shopTabFindFragment = new ShopTabFindFragment();
//                    t.add(R.id.frame, shopTabFindFragment);
//                    t.commit();
//                }
//                transaction.show(shopTabFindFragment);
                img_back.setVisibility(View.GONE);
                toolBar_icom.setVisibility(View.GONE);
                main_menu.setVisibility(View.GONE);
                iv_menu_qrcode.setVisibility(View.GONE);
                set_menu.setVisibility(View.GONE);
                toolbar_title.setVisibility(View.VISIBLE);
                toolbar_title.setText(getString(R.string.tab_item_shopfind));
                break;
            case 7:
//                if (shopTabMerchantFragment == null) {
//                    FragmentTransaction t = fragmentManager.beginTransaction();
//                    shopTabMerchantFragment = new ShopTabMerchantFragment();
//                    t.add(R.id.frame, shopTabMerchantFragment);
//                    t.commit();
//                }
//                transaction.show(shopTabMerchantFragment);
                img_back.setVisibility(View.GONE);
                toolBar_icom.setVisibility(View.GONE);
                main_menu.setVisibility(View.GONE);
                iv_menu_qrcode.setVisibility(View.GONE);
                set_menu.setVisibility(View.GONE);
                toolbar_title.setVisibility(View.VISIBLE);
                toolbar_title.setText(getString(R.string.tab_item_shopmerchant));
                break;
            case 8:
//                if (shopTabMineFragment == null) {
//                    FragmentTransaction t = fragmentManager.beginTransaction();
//                    shopTabMineFragment = new ShopTabMineFragment();
//                    t.add(R.id.frame, shopTabMineFragment);
//                    t.commit();
//                }
//                transaction.show(shopTabMineFragment);
                img_back.setVisibility(View.GONE);
                toolBar_icom.setVisibility(View.GONE);
                main_menu.setVisibility(View.GONE);
                iv_menu_qrcode.setVisibility(View.GONE);
                set_menu.setVisibility(View.GONE);
                toolbar_title.setVisibility(View.VISIBLE);
                toolbar_title.setText(getString(R.string.tab_item_mine));
                break;
            case 9:
                if (mineFragment == null) {
                    FragmentTransaction t = fragmentManager.beginTransaction();
                    mineFragment = new MineFragment();
                    t.add(R.id.frame, mineFragment);
                    t.commit();
                }
                transaction.show(mineFragment);
                img_back.setVisibility(View.GONE);
                toolBar_icom.setVisibility(View.GONE);
                main_menu.setVisibility(View.GONE);
                iv_menu_qrcode.setVisibility(View.GONE);
                set_menu.setVisibility(View.GONE);
                toolbar_title.setVisibility(View.VISIBLE);
                toolbar_title.setText(getString(R.string.tab_item_mine));
                break;
        }
        transaction.commitAllowingStateLoss();
    }

    private void initTab() {
        int initTabCount = 0;
        //初始化第一个tab tag不能为空
        TabLayout.Tab tab = tabLayout.newTab().setTag(0).setCustomView(getTabView(0));
        tabLayout.addTab(tab);
        setTab(tab);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
//        if (MainApplication.app.getAppGlobalConfig().isShowTabHostShop() && Util.isChinaSimCard(mthis)) {//商城页签 只有中国才启用商城
//            //初始化第二个tab
//            tab = tabLayout.newTab().setTag(1).setCustomView(getTabView(1));
//            tabLayout.addTab(tab);
//            if (shopMainFragment == null) {
//                shopMainFragment = new ShopMainFragment();
//                transaction.add(R.id.frame, shopMainFragment);
//                initTabCount++;
////                transaction.hide(shopMainFragment); //默认添加进来需要隐藏
//            }
//        }
//        if (MainApplication.app.getAppGlobalConfig().isShowTabHostInteraction()) { //互动页签
//            //初始化第三个个tab
//            tab = tabLayout.newTab().setTag(2).setCustomView(getTabView(2));
//            tabLayout.addTab(tab);
//            if (interactionFragment == null) {
//                interactionFragment = new InteractionFragment();
//                transaction.add(R.id.frame, interactionFragment);
//                initTabCount++;
////                transaction.hide(interactionFragment);
//            }
//        }
        if (MainApplication.app.getAppGlobalConfig().isShowTabHostService()) {//服务页签
            //初始化第四个tab
            tab = tabLayout.newTab().setTag(3).setCustomView(getTabView(3));
            tabLayout.addTab(tab);
            if (serviceFragment == null) {
                serviceFragment = new ServiceFragment();
                transaction.add(R.id.frame, serviceFragment);
                initTabCount++;
            }
        }
        if (MainApplication.app.getAppGlobalConfig().isShowABBQService()) {//巨将服务页签
            tab = tabLayout.newTab().setTag(4).setCustomView(getTabView(4));
            tabLayout.addTab(tab);
            initTabCount++;
//            if (serviceABBQFragment == null)
//                serviceABBQFragment = new ServiceABBQFragment();
//            transaction.add(R.id.frame, serviceABBQFragment);
        }
        if (MainApplication.app.getAppGlobalConfig().isShowMessages()) {//消息页签
            tab = tabLayout.newTab().setTag(5).setCustomView(getTabView(5));
            tabLayout.addTab(tab);
            initTabCount++;
//            if (mineFragment == null)
//                mineFragment = new MineFragment();
//            transaction.add(R.id.frame, mineFragment);
        }
        if (MainApplication.app.getAppGlobalConfig().isShowShoTabFind()) {//商城版 发现页签
            tab = tabLayout.newTab().setTag(6).setCustomView(getTabView(6));
            tabLayout.addTab(tab);
            initTabCount++;
        }
        if (MainApplication.app.getAppGlobalConfig().isShowShoTabMerchant()) {//商城版 商家页签
            tab = tabLayout.newTab().setTag(7).setCustomView(getTabView(7));
            tabLayout.addTab(tab);
            initTabCount++;
        }
        if (MainApplication.app.getAppGlobalConfig().isShowShoTabMine()) {//商城版 我的页签
            tab = tabLayout.newTab().setTag(8).setCustomView(getTabView(8));
            tabLayout.addTab(tab);
            initTabCount++;
        }
        if (MainApplication.app.getAppGlobalConfig().isShowMine()) {//我的 页签
            tab = tabLayout.newTab().setTag(9).setCustomView(getTabView(9));
            tabLayout.addTab(tab);
            initTabCount++;
        }

        if (MainApplication.app.getAppGlobalConfig().isGridFragment()) {
            //网格布局
            gridMainFragment = new DeviceGridMainFragment();
            transaction.add(R.id.frame, gridMainFragment);
            initTabCount++;
        } else {
            if (MainApplication.app.getAppGlobalConfig().isShowShoTabMain()){//商城布局
//                shopTabMainFragment = new ShopTabMainFragment();
//                transaction.add(R.id.frame, shopTabMainFragment);
//                initTabCount++;
//                mainShowFragment = shopTabMainFragment;
            }else {
                zhujiFragment = new ZhujiListFragment();
                transaction.add(R.id.frame, zhujiFragment);
                initTabCount++;
                mainShowFragment = zhujiFragment;
            }
        }
        transaction.commitAllowingStateLoss();
        if (initTabCount > 1){
            isShowTab = true;
        }
        initLeftMenu();
        selectFragment(0);
    }

    private View getTabView(int index) {
        View view = View.inflate(DeviceMainActivity.this, R.layout.layout_tab, null);
        ImageView image = (ImageView) view.findViewById(R.id.ivTabImage);
        TextView tv = (TextView) view.findViewById(R.id.tvTabTitle);
        tv.setText(titles[index]);
        image.setBackgroundResource(unImage[index]);
        return view;
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        zhujiFragment.newMasterId = intent.getStringExtra("masterId");
        autoOpenDeviceActivity(intent);
    }

    /**
     * 自动在打开页面时 跳转到设备详情
     * @param intent
     */
    private void autoOpenDeviceActivity(Intent intent){
        //defaultStartDid 为点击通知栏或者重新打开首页，如果传入了设备ID，则需要主动跳转到设备详情页，这里切换到主机设备详情
        //在详情页面刷新页面，然后再判断此字段进行跳转
        defaultStartDid = intent.getLongExtra(DataCenterSharedPreferences.Constant.DEVICE_ID, 0);
        ZhujiInfo wifiLock = DatabaseOperator.getInstance(mContext).queryDeviceZhuJiInfo(defaultStartDid);
        Log.i("defaultStartDid",String.valueOf(defaultStartDid));
        DeviceInfo dInfo = DatabaseOperator.getInstance(mContext).queryDeviceInfo(defaultStartDid);
        if(wifiLock!=null){
            //说明是主机
            Intent deviceIntent = new Intent();
            defaultStartDid = 0 ;
              if(wifiLock.getCa().equals(DeviceInfo.CaMenu.wifizns.value())){
                  deviceIntent.setClass(mContext.getApplicationContext(), WifiLockMainActivity.class);
              } else if(wifiLock.getCa().equals(DeviceInfo.CaMenu.zhinengsuo.value())){
                  deviceIntent.setClass(mContext.getApplicationContext(), LockMainActivity.class);
              }
            deviceIntent.putExtra("device", Util.getZhujiDevice(wifiLock));
            deviceIntent.putExtra("zhuji",wifiLock);
            deviceIntent.putExtra("notification","true");//标识点击通知打开的
            if(deviceIntent.resolveActivity(getPackageManager())!=null){
                startActivity(deviceIntent);
            }
        }else{
            if (dInfo != null) {
                setZhuji(DatabaseOperator.getInstance().queryDeviceZhuJiInfo(dInfo.getZj_id()));
                changeFragment("zhuji");//切换到设备列表
                showLock = false; //首页不显示手势密码，跳转到设备信息里面再显示
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
//        GoogleApiAvailability.makeGooglePlayServicesAvailable(this);
        setTitle("");
        if (!isShowTab) {
            tabLayout.setVisibility(View.GONE);
        }
//        toolbar_title.setText(dcsp.getString(DataCenterSharedPreferences.Constant.LOGIN_APPNAME, ""));
        userName.setText(dcsp.getString(DataCenterSharedPreferences.Constant.LOGIN_APPNAME, ""));
        checkNotificationSet();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("defaultStartDid","Destory");
        if (defaultReceiver != null && isRegist) {
            isRegist = false;
            unregisterReceiver(defaultReceiver);
            defaultReceiver = null;
        }
    }

    boolean isRegist = false;
    private String regId;
    private String jiguangRegId;
    private BroadcastReceiver defaultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_CHANGE_FRAGMENT.equals(intent.getAction())) {
                String fragment = intent.getStringExtra("fragment");
                if (fragment != null && !"".equals(fragment)) {
                    changeFragment(fragment);
                }
            } else if (Actions.ACCETP_HUAWEIPUSH_MESSAGE.equals(intent.getAction())) {
                String hwToken = (String) intent.getSerializableExtra("huaweiToken");
                JavaThreadPool.getInstance().excute(new PushToken(hwToken, 3));
            } else if (Actions.ACCETP_MIPUSH_MESSAGE.equals(intent.getAction())) {
                regId = (String) intent.getSerializableExtra("regId");
                JavaThreadPool.getInstance().excute(new PushToken(regId, 2));
            } else if (Actions.ACCETP_JIGUANGPUSH_MESSAGE.equals(intent.getAction())) {
                jiguangRegId = (String) intent.getSerializableExtra("regId");
                JavaThreadPool.getInstance().excute(new PushToken(jiguangRegId, 4));
            } else if (Actions.CONNECTION_FAILED.equals(intent.getAction())) { // 连接断开
//                LogUtil.i(TAG, "Actions.CONNECTION_FAILED");
                startShowConnLoading();
            } else if (Actions.CONNECTION_SUCCESS.equals(intent.getAction())) { // 连接成功
//                LogUtil.i(TAG, "Actions.CONNECTION_SUCCESS");
                stopShowConnLoading();
            } else if (Actions.CONNECTION_ING.equals(intent.getAction())) { // 连接中
//                LogUtil.i(TAG, "Actions.CONNECTION_ING");
                startShowConnLoading();
            } else if (Actions.CONNECTION_NONET.equals(intent.getAction())) { // 无网络
//                LogUtil.i(TAG, "Actions.CONNECTION_NONET");
                stopShowConnLoading();
            } else if (Actions.UPDATE_USER_LOGO.equals(intent.getAction())) {
                showUserLogo();
            }else if (Actions.ACCETP_ONEDEVICE_MESSAGE.equals(intent.getAction())){
                if(!Actions.VersionType.CHANNEL_ANBABAOQUAN.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
                    return  ;
                }
                if(intent.getStringExtra("device_id") != null){
                    String device_id = intent.getStringExtra("device_id");
                    if (intent.getStringExtra("device_id") != null) {
                        String data = (String) intent.getSerializableExtra("device_info");
                        if (data != null) {
                            try {
                                JSONObject object = JSONObject.parseObject(data);
                                if(object != null &&"150".equals(object.getString("dt"))&&"4096".equals(object.getString("deviceCommand"))){
                                    new LoadZhujiAndDeviceTask().queryDeviceInfoByDevice(Long.parseLong(device_id), new LoadZhujiAndDeviceTask.ILoadResult<DeviceInfo>() {
                                        @Override
                                        public void loadResult(DeviceInfo result) {
                                            anBaoCheckWiredError(result.getName());
                                        }
                                    });
                                }
                            }catch (Exception ex) {
                                //防止json无数据崩溃
                            }
                        }
                    }
                }
            }
        }

    };

    public void stopShowConnLoading() {
        Log.i(TAG, "stopShowConnLoading: 停止显示loading");
        conn_icon.setVisibility(View.GONE);
        conn_icon.clearAnimation();
        cancelInProgress();
    }

    public void startShowConnLoading() {
        showInProgress(getString(R.string.ongoing));
        //播放动画需要配合onWindowFocusChanged使用,不然会播放不了
        Log.i(TAG, "startShowConnLoading: 开始显示loading");
        if (conn_icon != null && conn_icon.getVisibility() != View.VISIBLE) {
            conn_icon.setVisibility(View.VISIBLE);
            Animation imgloading_animation = AnimationUtils.loadAnimation(DeviceMainActivity.this,
                    R.anim.loading_revolve);
            imgloading_animation.setInterpolator(new LinearInterpolator());
            conn_icon.startAnimation(imgloading_animation);
        }
    }

    public boolean connLoadingVisible() {
        if (conn_icon != null && conn_icon.getVisibility() == View.VISIBLE)
            return true;
        return false;
    }

    private AlertView mWiredErrorTipView ;
    //安霸有线防区故障弹框提醒
    private void anBaoCheckWiredError(String deviceName){
        if(mWiredErrorTipView!=null&&mWiredErrorTipView.isShowing()){
            mWiredErrorTipView.dismissImmediately();
        }
        mWiredErrorTipView = new AlertView(getString(R.string.remind_msg), getString(R.string.adwa_wired_zone_errordevice,deviceName),
                null, new String[]{getString(R.string.ready_guide_msg13)}, null,
                mContext, AlertView.Style.Alert, null);
        mWiredErrorTipView.show();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.i(TAG, "onWindowFocusChanged: 当前的显示状态是" + conn_icon.getVisibility());
        if (conn_icon.getVisibility() == View.VISIBLE) {
            Animation imgloading_animation = AnimationUtils.loadAnimation(DeviceMainActivity.this,
                    R.anim.loading_revolve);
            imgloading_animation.setInterpolator(new LinearInterpolator());
            conn_icon.startAnimation(imgloading_animation);
        }
    }

    public void switchContent(Fragment from, Fragment to) {
        if (mainShowFragment != to) {
            mainShowFragment = to;
            FragmentTransaction transaction = fragmentManager.beginTransaction().setCustomAnimations(
                    android.R.anim.fade_in, android.R.anim.fade_out);
            if (!to.isAdded()) {    // 先判断是否被add过
//                transaction.hide(from).add(R.id.frame, to).commit(); // 隐藏当前的fragment，add下一个到Activity中
                transaction.hide(from).add(R.id.frame, to).show(to).commitAllowingStateLoss(); // 隐藏当前的fragment，add下一个到Activity中
            } else {
//                transaction.hide(from).show(to).commit(); // 隐藏当前的fragment，显示下一个
                transaction.hide(from).show(to).commitAllowingStateLoss(); // 隐藏当前的fragment，显示下一个
            }
        }
    }

    /**
     * 初始化需要的权限
     */
    private void initPermission(){
        //
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<PermissionItem> permissonItems = new ArrayList<>();
            permissonItems.add(new PermissionItem(Manifest.permission.WRITE_EXTERNAL_STORAGE, getString(R.string.permission_storage), R.drawable.permission_ic_storage));
//            permissonItems.add(new PermissionItem(Manifest.permission.CAMERA, getString(R.string.permission_camera), R.drawable.permission_ic_camera));
//            permissonItems.add(new PermissionItem(Manifest.permission.ACCESS_FINE_LOCATION, getString(R.string.permission_location), R.drawable.permission_ic_location));
            permissonItems.add(new PermissionItem(Manifest.permission.READ_PHONE_STATE, getString(R.string.permission_phone), R.drawable.permission_ic_phone));
//            if (MainApplication.app.getAppGlobalConfig().isShowCallAlarm()) {
              //  permissonItems.add(new PermissionItem(Manifest.permission.WRITE_CONTACTS, getString(R.string.permission_contacts), R.drawable.permission_ic_contacts));
               // permissonItems.add(new PermissionItem(Manifest.permission.READ_CONTACTS, getString(R.string.permission_contacts), R.drawable.permission_ic_contacts));
//            }

            HiPermission.create(mContext)
                    .animStyle(R.style.PermissionAnimFade)//设置动画
                    .permissions(permissonItems)
                    .checkMutiPermission(new PermissionCallback() {
                        @Override
                        public void onClose() {}

                        @Override
                        public void onFinish() {}

                        @Override
                        public void onDeny(String permission, int position) {}

                        @Override
                        public void onGuarantee(String permission, int position) {
                            Log.e("22222222222","完成授权："+permission);
                            if (permission.equals(Manifest.permission.WRITE_CONTACTS)){
                                //initAlarmContacts();
                            }
                        }
                    });
//            if(MainApplication.app.getAppGlobalConfig().isShowCallAlarm()){
//                if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CONTACTS)!=PackageManager.PERMISSION_GRANTED||
//                        ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_CONTACTS)!=PackageManager.PERMISSION_GRANTED){
//                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_CONTACTS,Manifest.permission.WRITE_CONTACTS},
//                            99);
//                }else{
//                    initAlarmContacts();
//                }
//            }

        }else{
//           initAlarmContacts(); 不再写入号码，由于有通讯录权限，去掉这个权限
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==99){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED&&grantResults[1]==PackageManager.PERMISSION_GRANTED){
               initAlarmContacts();
            }
        }
    }

    /**
     * 初始化或者刷新天气信息
     */
    private void initWeather(){
        mContext.sendBroadcast(new Intent(Actions.WEATHER_GET));
    }

    boolean isHigh = true;//是否是紧急程度的通知
    private void checkNotificationSet(){
        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        // areNotificationsEnabled方法的有效性官方只最低支持到API 19，低于19的仍可调用此方法不过只会返回true，即默认为用户已经开启了通知。
        boolean isOpened = manager.areNotificationsEnabled();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            //检测通知设定 要给出提示框用于跳转
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel1 = notificationManager.getNotificationChannel(NotificationUtil.CHANNEL_NOTIFICATION_ID);
            if (channel1.getImportance() < NotificationManager.IMPORTANCE_HIGH) {
                isOpened = false;
                isHigh = false;
            }
        }
        if (!isOpened) {
            if (dcsp.getLong("lastOpenNotificationTipView",0) + (3*24*3600) > System.currentTimeMillis()/1000){
                return;
            }
            dcsp.putLong("lastOpenNotificationTipView",System.currentTimeMillis()/1000).commit();
            if (mAlertView==null || !mAlertView.isShowing()) {
                mAlertView = new AlertView(getString(R.string.notification_default) + " " +getString(R.string.set),
                        getString(R.string.permission_notification), getString(R.string.permission_cancel),
                        new String[]{getString(R.string.permission_go_to_setting)}, null, DeviceMainActivity.this,
                        AlertView.Style.Alert, new com.smartism.znzk.view.alertview.OnItemClickListener() {

                    @Override
                    public void onItemClick(Object o, int position) {
                        if (position != -1) {
                            try {
                                Intent intent = new Intent();
                                if (AndroidRomUtil.isMIUI()){//小米手机进不去的，直接去往设置页面
                                    intent.setAction(Settings.ACTION_SETTINGS);
                                }else {
                                    // 根据isOpened结果，判断是否需要提醒用户跳转AppInfo页面，去打开App通知权限
                                    intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                                    //这种方案适用于 API 26, 即8.0（含8.0）以上可以用
                                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                                    if (!isHigh) {
                                        intent.setAction(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                                        intent.putExtra(Settings.EXTRA_CHANNEL_ID, NotificationUtil.CHANNEL_NOTIFICATION_ID);
                                    }

                                    //这种方案适用于 API21——25，即 5.0——7.1 之间的版本可以使用
                                    intent.putExtra("app_package", getPackageName());
                                    intent.putExtra("app_uid", getApplicationInfo().uid);

                                    // 小米6 -MIUI9.6-8.0.0系统，是个特例，通知设置界面只能控制"允许使用通知圆点"——然而这个玩意并没有卵用，我想对雷布斯说：I'm not ok!!!
                                    //  if ("MI 6".equals(Build.MODEL)) {
                                    //      intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    //      Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    //      intent.setData(uri);
                                    //      // intent.setAction("com.android.settings/.SubSettings");
                                    //  }
                                }
                                startActivity(intent);
                            } catch (Exception e) {
                                // 出现异常则跳转到应用设置界面：锤子坚果3——OC105 API25
                                Intent intent = new Intent();

                                //下面这种方案是直接跳转到当前应用的设置界面。
                                //https://blog.csdn.net/ysy950803/article/details/71910806
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            }
                        }
                    }
                });
                mAlertView.show();
            }
        }
    }

    private void initAlarmContacts(){
        if (MainApplication.app.getAppGlobalConfig().isShowCallAlarm()) {
//            //通过本地文件记录是否需要写入号码的方式，在覆盖安装时新添加的号码将无法写入
//            if (!dcsp.getBoolean(Constant.ALARM_CENTER_ADD, false)) {
//                Util.addContacts(getApplicationContext());
//                dcsp.putBoolean(Constant.ALARM_CENTER_ADD, true).commit();
//            }
            int previous = dcsp.getInt(Constant.ALARM_VERSIONCODE,0);
            final int current  = dcsp.getInt(Constant.APP_VERSIONCODE,0);
            if(current>previous){
                //首先判断报警号码是否全
                if(!Util.hasAlarmNumber(this)){
                    //进来说明号码不全，不是最新，需要更新
                    AlertView temp = new AlertView(getString(R.string.alarm_center_delete_title), getString(R.string.alarm_center_delete_message),
                            getString(R.string.alarm_center_delete_no), new String[]{getString(R.string.alarm_center_delete_yes)},
                            null, this, AlertView.Style.Alert, new OnItemClickListener() {
                        @Override
                        public void onItemClick(Object o, int position) {
                            if(position!=-1){
                                Util.deletePreviousContacts(DeviceMainActivity.this);
                            }else{
                                Util.addContacts(DeviceMainActivity.this);
                            }
                        }
                    });
                    temp.setCancelable(false);
                    temp.show();
                }
                dcsp.putInt(Constant.ALARM_VERSIONCODE,current).commit();
            }
        }
    }

    private void initData() {
        toolBar_icom.setOnClickListener(this);
        zhujiMenu.setOnClickListener(this);
        if (!isRegist) {
            isRegist = true;
            IntentFilter receiverFilter = new IntentFilter();
            receiverFilter.addAction(Actions.ACCETP_MIPUSH_MESSAGE);
            receiverFilter.addAction(Actions.ACCETP_HUAWEIPUSH_MESSAGE);
            receiverFilter.addAction(Actions.ACCETP_JIGUANGPUSH_MESSAGE);
            receiverFilter.addAction(Actions.REFRESH_DEVICES_LIST);
            receiverFilter.addAction(Actions.ACCETP_ONEDEVICE_MESSAGE);
            receiverFilter.addAction(Actions.CONNECTION_FAILED);
            receiverFilter.addAction(Actions.CONNECTION_ING);
            receiverFilter.addAction(Actions.CONNECTION_NONET);
            receiverFilter.addAction(Actions.CONNECTION_SUCCESS);
            receiverFilter.addAction(Actions.CONNECTION_FAILED_SENDFAILED);
            receiverFilter.addAction(Actions.SHOW_SERVER_MESSAGE);
            receiverFilter.addAction(Actions.ZHUJI_UPDATE);
            receiverFilter.addAction(Constants.Action.ACTION_NETWORK_CHANGE);
            receiverFilter.addAction(ACTION_CHANGE_FRAGMENT);
            receiverFilter.addAction(Actions.UPDATE_USER_LOGO);
            mContext.registerReceiver(defaultReceiver, receiverFilter);
        }

        menuWindow.updateMenu(dcsp, zhuji, mainShowFragment);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setTab(tab);
                selectFragment((int) tab.getTag());
                for (int i = 0; i < tabLayout.getTabCount(); i++) {
                    int other = (int) tabLayout.getTabAt(i).getTag();
                    if (other != (int) tab.getTag()) {
                        unselectFragment(other);
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                unselectTab(tab);
//                unselectFragment((int) tab.getTag()); 在onTabSelected方法处理了
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void initPush(){
        JavaThreadPool.getInstance().excute(() -> {
            FirebaseApp.initializeApp(getApplicationContext());
            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener((@NonNull Task<InstanceIdResult> task)-> {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        if (!StringUtils.isEmpty(token)){
                            JavaThreadPool.getInstance().excute(new PushToken(token, 4));
                        }
                    });
        });
    }

    /**
     * 隐藏各fragment
     * @param tag
     */
    private void unselectFragment(int tag) {
        FragmentTransaction trans = fragmentManager.beginTransaction();
        switch (tag) {
            case 0:
                if (MainApplication.app.getAppGlobalConfig().isGridFragment()) {
                    trans.hide(gridMainFragment);
                } else {
                    trans.hide(mainShowFragment);
                }
                break;
            case 1:
//                if (shopMainFragment != null) {
//                    trans.hide(shopMainFragment);
//                }
                break;
            case 2:
//                if (interactionFragment != null) {
//                    trans.hide(interactionFragment);
//                }
                break;
            case 3:
                if (serviceFragment != null) {
                    trans.hide(serviceFragment);
                }
                break;
            case 4:
                if (serviceNewFragment != null)
                    trans.hide(serviceNewFragment);
                break;
            case 5:
                if (messageCenterFragment != null)
                    trans.hide(messageCenterFragment);
                break;
            case 6:
//                if (shopTabFindFragment != null)
//                    trans.hide(shopTabFindFragment);
                break;
            case 7:
//                if (shopTabMerchantFragment != null)
//                    trans.hide(shopTabMerchantFragment);
                break;
            case 8:
//                if (shopTabMineFragment != null)
//                    trans.hide(shopTabMineFragment);
                break;
            case 9:
                if (mineFragment != null)
                    trans.hide(mineFragment);
                break;
        }
        trans.commitAllowingStateLoss();
    }

    private void initView() {
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        img_back = (ImageView) findViewById(R.id.img_back);//返回按钮
        img_back.setOnClickListener(this);
        iv_menu_qrcode = (ImageView) findViewById(R.id.iv_menu_qrcode);//扫码添加按钮
        iv_menu_qrcode.setOnClickListener(this);
        device_main_scnce = (ImageView) findViewById(R.id.device_main_scnce);
        device_main_scnce.setOnClickListener(this);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        toolBar_icom = (CircleImageView) findViewById(R.id.toolbar_header_img);
        conn_icon = (ImageView) findViewById(R.id.toolbar_conn_icon);
        titles = new String[]{getString(R.string.tab_home), getString(R.string.tab_shop), getString(R.string.tab_interactive), getString(R.string.tab_service), getString(R.string.tab_service), getString(R.string.tab_message),getString(R.string.tab_item_shopfind),getString(R.string.tab_item_shopmerchant),getString(R.string.tab_item_mine),getString(R.string.tab_item_mine)};
        tabLayout = (TabLayout) findViewById(R.id.tab);
        fragmentManager = getSupportFragmentManager();
        toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        menuWindow = new ZhzjAddPopupWindow(DeviceMainActivity.this, this);
        zjMenuWindow = new SelectAddPopupWindow(DeviceMainActivity.this, this, zhuji);
        bbsMenuWindow = new MenuInteractionPopupWindow(DeviceMainActivity.this, this);
        main_menu = (ImageView) findViewById(R.id.device_main_menu);
        main_menu.setOnClickListener(this);
        set_menu = (ImageView) findViewById(R.id.device_main_setting);
        set_menu.setOnClickListener(this);
//        main_menu.setVisibility(View.GONE);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        titleTab = (TabLayout) findViewById(R.id.title_tab);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        zhujiMenu = (ImageView) findViewById(R.id.device_main_zhujimenu);

    }


//    public void back(View view) {
//        this.onDestroy();
//        finish();
//    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent = new Intent();
        switch (id) {
            case R.id.sideslip_user:
                intent.setClass(getApplicationContext(), UserInfoActivity.class);
                startActivity(intent);
                break;
            case R.id.sideslip_scene:
                if (!zhuji.isOnline()) {
                    Toast.makeText(this, getString(R.string.deviceslist_zhuji_offline), Toast.LENGTH_SHORT).show();
                    break;
                }
                intent.setClass(getApplicationContext(), SceneActivity.class);
                startActivity(intent);
                break;
            case R.id.sideslip_showzhuji:
                intent.putExtra("pattern", "status_forver");
                intent.setClass(getApplicationContext(), ShareDevicesActivity.class);
                intent.putExtra("shareid",zhuji!=null?zhuji.getId():0);
                startActivity(intent);
                break;
            case R.id.sideslip_update:
                checkUpdate();
                break;
            case R.id.sideslip_replacehost:
                intent.setAction(DeviceMainActivity.ACTION_CHANGE_FRAGMENT);
                intent.putExtra("fragment", "main");
                mContext.sendBroadcast(intent);
                drawer.closeDrawer(GravityCompat.START);
//                if (Actions.VersionType.CHANNEL_WOAIJIA.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
//                    if (zhujiFragment == null) zhujiFragment = new ZhujiListFragment();
//                    if (mainFragment == null) mainFragment = new DeviceMainFragment();
//                    switchContent(mainFragment, zhujiFragment);
//                    drawer.closeDrawer(GravityCompat.START);
//                } else {
//                    if (mainFragment != null) {
//                        mainFragment.moreHubChange();
//                    } else if (gridMainFragment != null) {
//                        gridMainFragment.moreHubChange();
//                    }
//                }
                break;
            case R.id.sideslip_exitlogin:
                new AlertView(getString(R.string.activity_user_info_logout_tip),
                        getString(R.string.activity_user_info_logout_mes), getString(R.string.cancel),
                        new String[]{getString(R.string.sure)}, null, DeviceMainActivity.this,
                        AlertView.Style.Alert, new com.smartism.znzk.view.alertview.OnItemClickListener() {

                    @Override
                    public void onItemClick(Object o, int position) {
                        if (position != -1) {
                            logout();
                        }
                    }
                }).show();
                break;
            case R.id.sideslip_help:
                intent.setClass(getApplicationContext(), CommonWebViewActivity.class);
                intent.putExtra("title", getString(R.string.deviceslist_server_leftmenu_help));
                intent.putExtra("url", dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "")
                        + "/jdm/help?uid=" + dcsp.getLong(DataCenterSharedPreferences.Constant.LOGIN_APPID, 0));
                startActivity(intent);
                break;
            case R.id.sideslip_about:
                intent = new Intent();
                if (Actions.VersionType.CHANNEL_QYJUNHONG.equals(MainApplication.app.getAppGlobalConfig().getVersion()) ||
                        Actions.VersionType.CHANNEL_ANBABAOQUAN.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                    intent.setClass(getApplicationContext(), XZSWAboutActivity.class);
                } else {
                    intent.setClass(getApplicationContext(), AboutActivity.class);
                }
                startActivity(intent);
                break;

            case R.id.sideslip_voice_setting:
                intent.setClass(getApplicationContext(), SettingSpeechActivity.class);
                startActivity(intent);
                break;
            case R.id.sideslip_setting:
                intent.setClass(getApplicationContext(), SettingActivity.class);
                if (zhuji != null) {
                    intent.putExtra("zhuji_Id", zhuji.getId());
                }
                startActivity(intent);
                break;
        }

        return true;
    }

    public void checkUpdate() {
        UpdateAgent.setUpdateListener(new UpdateListener() {

            @Override
            public void onUpdateReturned(int updateStatus, UpdateResponse updateInfo) {
                UpdateAgent.setUpdateListener(null);
                switch (updateStatus) {
                    case UpdateStatus.YES: // has update
                        UpdateAgent.showUpdateDialog(DeviceMainActivity.this, updateInfo);
                        break;
                    case UpdateStatus.NO: // has no update
                        Toast.makeText(DeviceMainActivity.this,
                                getString(R.string.deviceslist_server_leftmenu_islastversion),
                                Toast.LENGTH_SHORT).show();
                        break;
                    // case UpdateStatus.NoneWifi: // none wifi
                    // Toast.makeText(DevicesListActivity.this,
                    // getString(R.string.deviceslist_server_leftmenu_notwifi),
                    // Toast.LENGTH_SHORT)
                    // .show();
                    // break;
                    case UpdateStatus.TIMEOUT: // time out
                        Toast.makeText(DeviceMainActivity.this,
                                getString(R.string.deviceslist_server_leftmenu_chaoshi), Toast.LENGTH_SHORT)
                                .show();
                        break;
                    case UpdateStatus.ERROR: // 错误
                        Toast.makeText(DeviceMainActivity.this, getString(R.string.net_error_requestfailed),
                                Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        });
        UpdateAgent.update(DeviceMainActivity.this);
    }

    public void startActivityForResult(String action, boolean isVisible) {
        if (isVisible) {
            startActivityForResult(new Intent(Settings.ACTION_WIRELESS_SETTINGS), 0);
        }
    }

    List<ZhujiInfo> zhujiList;

    /*
     更改屏幕窗口透明度
  */
    public void changeWindowAlfa(float alfa) {
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.alpha = alfa;
        getWindow().setAttributes(params);
    }

    private String key, pattern;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 3333 && resultCode == Constant.CAPUTRE_ADDRESULT) {
            key = data.getStringExtra("value");
            pattern = data.getStringExtra("pattern");
            if (key == null) {
                Toast.makeText(this, getString(R.string.net_error_programs), Toast.LENGTH_SHORT).show();
                return;
            }
            showInProgress(getString(R.string.activity_add_zhuji_havezhu_ongoing), false, false);
            JavaThreadPool.getInstance().excute(new Matching());
        }
    }

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 10) {
                if (updateCount == 3) {
                    return true;
                }
                JSONObject object = (JSONObject) msg.obj;
                JavaThreadPool.getInstance().excute(new PushToken(object.getString("token"), object.getInteger("t").intValue()));
            }
            return false;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);

    class Matching implements Runnable {
        @Override
        public void run() {
            String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
            JSONObject pJsonObject = new JSONObject();
            pJsonObject.put("uid", dcsp.getLong(Constant.LOGIN_APPID, 0));
            pJsonObject.put("code", dcsp.getString(Constant.LOGIN_CODE, ""));
            pJsonObject.put("key", key);
            String result = "";
            // 长久添加
            if (pattern.equals("status_forver")) {
                result = HttpRequestUtils
                        .requestoOkHttpPost(
                                server + "/jdm/s3/d/mshare", pJsonObject,
                                DeviceMainActivity.this);
                // 临时添加
            } else if (pattern.equals("status_temp")) {
                result = HttpRequestUtils
                        .requestoOkHttpPost(
                                server + "/jdm/s3/d/tmshare", pJsonObject,
                                DeviceMainActivity.this);
            }
            if ("-3".equals(result)) {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(DeviceMainActivity.this, getString(R.string.activity_add_zhuji_havezhu_shixiao), Toast.LENGTH_LONG).show();
                    }
                });
            } else if ("0".equals(result)) {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        //重新获取设备列表
                        SyncMessageContainer.getInstance()
                                .produceSendMessage(new SyncMessage(SyncMessage.CommandMenu.rq_refresh));
//                        finish();
                        Toast.makeText(DeviceMainActivity.this, getString(R.string.activity_add_zhuji_havezhu_addsuccess), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = getIntent();
        switch (v.getId()) {
            case R.id.pop_exper: // 体验主机
                menuWindow.dismiss();
                break;
            case R.id.img_back:
                if (!mainFragment.isHidden() && zhujiFragment.isHidden()) {
                    changeFragment("main");
                }else{
                    changeFragment("top");
                }
                break;
            case R.id.iv_dismiss:
                menuWindow.dismiss();
                break;
            case R.id.device_main_scnce:
                if (!this.mainFragment.isShowScence()) {
                    return;
                }
                if (this.mainFragment.expandable_layout.isExpanded()) {
                    this.mainFragment.ll_sence_sl.setVisibility(View.VISIBLE);
                } else {
                    this.mainFragment.ll_sence_sl.setVisibility(View.GONE);
                }
                this.mainFragment.expandable_layout.toggle();
                break;
            case R.id.pop_addscence:
                intent.setClass(getApplicationContext(), SelectSceneTypeActivity.class);
                startActivity(intent);
                menuWindow.dismiss();
                break;
            case R.id.iv_menu_qrcode:
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.setClass(this, ScanCaptureActivity.class);
                intent.putExtra("isZhujiFragment", mainShowFragment instanceof ZhujiListFragment);
                if (mainShowFragment instanceof ZhujiListFragment) {
                    startActivityForResult(intent, 3333);
                } else if (this.mainFragment.deviceZhujiInfo == null) {
//                    intent.putExtra("type",1);
                    startActivityForResult(intent, 3333);
                } else {
//                    intent.putExtra("type",2);
                    startActivity(intent);
                }
//                intent.putExtra(Constant.CAPUTRE_REQUESTCOE, Constant.CAPUTRE_ADDDEVICE);

                break;
            case R.id.device_main_zhujimenu:
                if (mainFragment != null) {
                    zjMenuWindow.showAtLocation(v, Gravity.TOP | Gravity.RIGHT, 0,
                            Util.dip2px(getApplicationContext(), 55) + Util.getStatusBarHeight(this));
                    zhujiList = DatabaseOperator.getInstance(mContext.getApplicationContext()).queryAllZhuJiInfos();
                    zjMenuWindow.updateMenu(zhuji, zhujiList);
                } else if (gridMainFragment != null) {
                    zjMenuWindow.showAtLocation(v, Gravity.TOP | Gravity.RIGHT, 0,
                            Util.dip2px(getApplicationContext(), 55) + Util.getStatusBarHeight(this));
                    zhujiList = DatabaseOperator.getInstance(mContext.getApplicationContext()).queryAllZhuJiInfos();
                    zjMenuWindow.updateMenu(zhuji, zhujiList);
                }
                break;
            case R.id.toolbar_header_img: //头像点击
                if (MainApplication.app.getAppGlobalConfig().isShowMine()) {
                    intent.setClass(getApplicationContext(), UserInfoActivity.class);
                    startActivity(intent);
                    return;
                }
                if (isRight) {
                    intent.setClass(getApplicationContext(), UserInfoActivity.class);
                    startActivity(intent);
                } else {
                    drawer.openDrawer(GravityCompat.START);
                }
                break;
            case R.id.rl_user: //头像点击
                intent.setClass(getApplicationContext(), UserInfoActivity.class);
                startActivity(intent);
                break;
            case R.id.device_main_menu://右上角菜单点击
                if (!isRight) {
                    try {
                        if ("0".equals(tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getTag().toString())) {
                            if ((Actions.VersionType.CHANNEL_AIERFUDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                                    || Actions.VersionType.CHANNEL_UHOME.equals(MainApplication.app.getAppGlobalConfig().getVersion())) && mainShowFragment instanceof ZhujiListFragment) {
                                intent.setClass(getApplicationContext(), AddZhujiOldActivity.class);
                                intent.putExtra("isMainList", mainShowFragment instanceof ZhujiListFragment);
                                startActivity(intent);
                            } else {
                                changeWindowAlfa(0.7f);
                                menuWindow.showAtLocation(v, Gravity.TOP, 0,
                                        Util.getStatusBarHeight(this));
                            }
                        } else if ("2".equals(tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getTag().toString()) || "3".equals(tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).getTag().toString())) {
                            bbsMenuWindow.showAtLocation(v, Gravity.TOP | Gravity.RIGHT, 0,
                                    Util.dip2px(getApplicationContext(), 55) + Util.getStatusBarHeight(this));
                        }
                    } catch (Exception ex) {
                        //getTag和toString可能为空
                    }
                } else {
                    drawer.openDrawer(GravityCompat.END);
                }
                break;
            case R.id.device_main_setting: //头部设置点击
                intent.setClass(mContext.getApplicationContext(), DeviceDetailActivity.class);
                intent.putExtra("device", Util.getZhujiDevice(zhuji));
                startActivity(intent);
                break;
            case R.id.iv_icon:
                break;
            case R.id.tv_setting:
                intent.setClass(getApplicationContext(), SettingActivity.class);
                intent.putExtra("zhuji_Id", zhuji.getId());
                startActivity(intent);
                break;
            case R.id.iv_bottom:
                intent.setClass(getApplicationContext(), UserInfoActivity.class);
                startActivity(intent);
                break;
            case R.id.pop_addzhuji: // 新增主机
                menuWindow.dismiss();
                if (Actions.VersionType.CHANNEL_AIERFUDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                        || Actions.VersionType.CHANNEL_UHOME.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                    intent.setClass(getApplicationContext(), AddZhujiOldActivity.class);
                    intent.putExtra("isMainList", mainShowFragment instanceof ZhujiListFragment);
                    startActivity(intent);
                } else {
                    if (Actions.VersionType.CHANNEL_QYJUNHONG.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                        ||Actions.VersionType.CHANNEL_HCTZ.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                        intent.setClass(mContext, AddZhujiActivity.class);
                    } else {
                        intent.setClass(mContext, AddZhujiWayChooseActivity.class);
                    }
//                    intent.putExtra("isZhujiFragment", true);
//                    startActivityForResult(intent, 3333);
                    startActivity(intent);
                }
                break;
            case R.id.pop_adddevice: // 新增设备
                menuWindow.dismiss();
                if (zhuji != null) {
                    intent.putExtra(NET_TYPE, CategoryInfo.NetTypeEnum.rf.value());
                    intent.setClass(getApplicationContext(), AddDeviceChooseActivity.class);
                    intent.putExtra("zhuji",zhuji);
                } else {
                    if (Actions.VersionType.CHANNEL_AIERFUDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                            || Actions.VersionType.CHANNEL_UHOME.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                        intent.setClass(getApplicationContext(), AddZhujiOldActivity.class);
                    } else {
                        intent.setClass(getApplicationContext(), AddZhujiActivity.class);
                    }
                }
                startActivity(intent);
                break;
            //批量操作菜单
            case R.id.batch_control_menu:
                menuWindow.dismiss();
                break ;
            case R.id.pop_adddnewgroup:
            case R.id.pop_adddgroup:// 新增群组
                menuWindow.dismiss();
                if (Util.isHaveZhuji(getApplicationContext())) {
                    intent.setClass(getApplicationContext(), AddGroupActivity.class);
                } else {
                    if (Actions.VersionType.CHANNEL_AIERFUDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                            || Actions.VersionType.CHANNEL_UHOME.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                        intent.setClass(getApplicationContext(), AddZhujiOldActivity.class);
                    } else {
                        intent.setClass(getApplicationContext(), AddZhujiActivity.class);
                    }
                }
                startActivity(intent);
                break;
            case R.id.pop_showss: // 分享设备
                menuWindow.dismiss();
                intent.putExtra("pattern", "status_forver");
                intent.setClass(getApplicationContext(), ShareDevicesActivity.class);
                intent.putExtra("shareid",zhuji!=null?zhuji.getId():0);
                startActivity(intent);
                // try {
                // intent.setClass(getApplicationContext(), ScanCaptureAct.class);
                // startActivity(intent);
                // } catch (Exception e) {
                // Toast.makeText(DevicesListActivity.this,
                // getString(R.string.deviceslist_server_camera_error),
                // Toast.LENGTH_LONG).show();
                // }
                break;
            case R.id.pop_showss_temp:// 临时分享主机10分钟
                menuWindow.dismiss();
                intent.putExtra("pattern", "status_temp");
                intent.setClass(getApplicationContext(), ShareDevicesActivity.class);
                intent.putExtra("shareid",zhuji!=null?zhuji.getId():0);
                startActivity(intent);
                break;

            case R.id.pop_addfromfactory: // 厂商添加设备到服务器后台
                menuWindow.dismiss();
                intent.setClass(getApplicationContext(), FactoryAddDevicesActivity.class);
                intent.putExtra(FactoryAddDevicesActivity.MODEL_FACTORY_TYPE,
                        FactoryAddDevicesActivity.MODEL_FACTORY_TYPE_FACTORY);
                startActivity(intent);
                break;
            case R.id.pop_devetc: // 厂商测试透传
                menuWindow.dismiss();
                intent.setClass(getApplicationContext(), DeveCommandShowActivity.class);
                startActivity(intent);
                break;
            case R.id.pop_addy: // 添加任意遥控器
                menuWindow.dismiss();
                intent.setClass(getApplicationContext(), DeviceCategoryActivity.class);
                intent.putExtra("zhuji",zhuji);
                intent.putExtra("filter", 2); // 2为遥控器
                startActivity(intent);
                break;
            case R.id.pop_addyx: // 添加虚拟遥控器
                menuWindow.dismiss();
                intent.setClass(getApplicationContext(), AddVirtualRemoteControlActivity.class);
                intent.putExtra("zhuji", zhuji);
                startActivity(intent);
                break;
            case R.id.pop_camera: // 添加技威的摄像头
                menuWindow.dismiss();
                intent.setClass(mContext.getApplicationContext(), RadarAddActivity.class);
                //判断是否是主机列表Fragment
                intent.putExtra("isMainList", mainShowFragment instanceof ZhujiListFragment);
                //技威武摄像头标识码
                intent.putExtra("int", 3);
                startActivity(intent);
                break;
            case R.id.pop_vcamera: // 添加V380摄像头
                menuWindow.dismiss();
                if (MainApplication.app.getAppGlobalConfig().getVersion().equals(Actions.VersionType.CHANNEL_JUJIANG)) {
                    intent.setClass(mContext.getApplicationContext(), RadarAddActivity.class);
                    intent.putExtra("isCameraList", 0);
                } else {
                    intent.setClass(mContext.getApplicationContext(), AddContactTypeActivity.class);
                }
                intent.putExtra("int", 5);
                startActivity(intent);
                break;
            case R.id.pop_addt: // 添加任意探头
                menuWindow.dismiss();
                intent.setClass(getApplicationContext(), DeviceCategoryActivity.class);
                intent.putExtra("zhuji",zhuji);
                intent.putExtra("filter", 1); // 1为探头
                startActivity(intent);
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
                    intent.setClass(getApplicationContext(), AddDeviceChooseActivity.class);
                    intent.putExtra("zhuji",zhuji);
//                    }
                } else {
                    intent.setClass(getApplicationContext(), AddZhujiActivity.class);
                }
                startActivity(intent);
                break;
            /***************bbs菜单点击部分*********************/
            case R.id.pop_myfaq:
                String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
//                server = "192.168.2.15:9999";
                bbsMenuWindow.dismiss();
                intent = new Intent();
                intent.setClass(getApplicationContext(), CommonWebViewActivity.class);
                intent.putExtra("url", server + "/interaction/bbs/mybbs?t=0");
                startActivity(intent);
                break;
            case R.id.pop_myidea:
                server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
//                server = "192.168.2.15:9999";
                bbsMenuWindow.dismiss();
                intent = new Intent();
                intent.setClass(getApplicationContext(), CommonWebViewActivity.class);
                intent.putExtra("url", server + "/interaction/bbs/mybbs?t=1");
                startActivity(intent);
                break;
            case R.id.pop_myyugou:
                server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
//                server = "192.168.2.15:9999";
                bbsMenuWindow.dismiss();
                intent = new Intent();
                intent.setClass(getApplicationContext(), CommonWebViewActivity.class);
                intent.putExtra("url", server + "/interaction/bbs/myyugou");
                startActivity(intent);
                break;
            case R.id.pop_newfaq:
                server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
//                server = "192.168.2.15:9999";
                bbsMenuWindow.dismiss();
                intent = new Intent();
                intent.setClass(getApplicationContext(), CommonWebViewActivity.class);
                intent.putExtra("url", server + "/interaction/bbs/toadd?t=0");
                intent.putExtra("show_save", true);
                startActivity(intent);
                break;
            case R.id.pop_newidea:
                server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
//                server = "192.168.2.15:9999";
                bbsMenuWindow.dismiss();
                intent = new Intent();
                intent.setClass(getApplicationContext(), CommonWebViewActivity.class);
                intent.putExtra("url", server + "/interaction/bbs/toadd?t=1");
                intent.putExtra("show_save", true);
                startActivity(intent);
                break;
            case R.id.pop_newwjob:
                server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                long uid = dcsp.getLong(DataCenterSharedPreferences.Constant.LOGIN_APPID, 0);
                String code = dcsp.getString(DataCenterSharedPreferences.Constant.LOGIN_CODE, "");
                String v1 = "";
                String n = Util.randomString(12);
                String s = SecurityUtil.createSign(v1, uid, code, n);
                String param = "&uid=" + uid + "&n=" + n + "&s=" + s;
//                server = "192.168.2.15:9999";
                bbsMenuWindow.dismiss();
                intent = new Intent();
                intent.setClass(getApplicationContext(), CommonWebViewActivity.class);
                intent.putExtra("url", server + "/interaction/wjob/toadd?t=0&woid=0" + param);
                intent.putExtra("show_save", true);
                startActivity(intent);
                break;
            /***********************bbs菜单部分结束********************/
            default:
                Toast.makeText(DeviceMainActivity.this, getString(R.string.deviceslist_server_leftmenu_unknownbutton),
                        Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (!MainApplication.app.getAppGlobalConfig().isGridFragment()) {
            mainFragment.moreHubChange(zhujiList.get(position));
        } else {
            gridMainFragment.moreHubChange(zhujiList.get(position));
        }
        zjMenuWindow.dismiss();
    }

    @Override
    public void getData(Object a) {
        user_devices.setText(a.toString() + " " + getString(R.string.zhzj_main_slide_device));
    }

    private int updateCount;

    public class PushToken implements Runnable {
        private String token;
        private int type;

        public PushToken(String token, int type) {
            this.token = token;
            this.type = type;
        }

        @Override
        public void run() {
            JSONObject object = new JSONObject();
            object.put("token", token);
            object.put("t", type);

            String applicationArn = "arn:aws:sns:us-east-1:786203740403:app/GCM/CtrSmartHost";
            AmazonSNSClient snsClient = new AmazonSNSClient(AWSMobileClient.getInstance().getCredentials());

            CreatePlatformEndpointRequest endpointRequest = new CreatePlatformEndpointRequest();
            endpointRequest.setToken(token);
            endpointRequest.setPlatformApplicationArn(applicationArn);
            CreatePlatformEndpointResult result = snsClient.createPlatformEndpoint(endpointRequest);
            if (!StringUtils.isEmpty(result.getEndpointArn())){
                snsEndpointArn = result.getEndpointArn();
                updateCount = 3;
                updateEndpointArnToAws();
            }else{
                //重发
                updateCount++;
                Message msg = Message.obtain();
                msg.what = 10;
                msg.obj = object;
                mHandler.sendMessage(msg);
            }

            Log.i(TAG,"FCM token to SNS" + result.toString());
        }
    }

    /**
     * 更新推送用的arn
     */
    private void updateEndpointArnToAws(){
        AWSClients.getInstance().getmAWSAppSyncClient().query(ListCtrUserInfoTablesQuery.builder()
                .filter(TableCtrUserInfoTableFilterInput.builder()
                        .uid(TableStringFilterInput.builder().eq(AWSMobileClient.getInstance().getUsername()).build())
                        .build())
                .build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(queryRelatioinsCallback);
    }

    private GraphQLCall.Callback<ListCtrUserInfoTablesQuery.Data> queryRelatioinsCallback = new GraphQLCall.Callback<ListCtrUserInfoTablesQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListCtrUserInfoTablesQuery.Data> response) {
            Log.i("Results", response.data().listCtrUserInfoTables().items().toString());
            if (response.data().listCtrUserInfoTables().items().size() > 0){
                updateUserInfo(response.data().listCtrUserInfoTables().items().get(0));
            }else{
                createUserInfo();
            }
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("ERROR", e.toString());
        }
    };

    private void createUserInfo(){
        CreateCtrUserInfoTableInput createCtrUserInfoTableInput = CreateCtrUserInfoTableInput.builder()
                .uid(AWSMobileClient.getInstance().getUsername())
                .client("android")
                .snsarn(snsEndpointArn)
                .build();

        AWSClients.getInstance().getmAWSAppSyncClient().mutate(CreateCtrUserInfoTableMutation.builder().input(createCtrUserInfoTableInput).build())
                .enqueue(mutationCallback);
    }

    private GraphQLCall.Callback<CreateCtrUserInfoTableMutation.Data> mutationCallback = new GraphQLCall.Callback<CreateCtrUserInfoTableMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<CreateCtrUserInfoTableMutation.Data> response) {
            Log.i(TAG,"Results GraphQL Add:" + JSON.toJSONString(response));
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("Error","GraphQL Add Exception", e);
        }
    };

    private void updateUserInfo(ListCtrUserInfoTablesQuery.Item item){
        UpdateCtrUserInfoTableInput updateCtrUserInfoTableInput = UpdateCtrUserInfoTableInput.builder()
                .id(item.id())
                .uid(AWSMobileClient.getInstance().getUsername())
                .client("android")
                .snsarn(snsEndpointArn)
                .build();

        AWSClients.getInstance().getmAWSAppSyncClient().mutate(UpdateCtrUserInfoTableMutation.builder().input(updateCtrUserInfoTableInput).build())
                .enqueue(mutationUpdateCallback);
    }

    private GraphQLCall.Callback<UpdateCtrUserInfoTableMutation.Data> mutationUpdateCallback = new GraphQLCall.Callback<UpdateCtrUserInfoTableMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<UpdateCtrUserInfoTableMutation.Data> response) {
            if (response.hasErrors()){
                Log.e("Error","GraphQL update userinfo Exception"+response.errors().get(0).toString());
            }
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("Error","GraphQL update userinfo Exception", e);
        }
    };

    // logo图片的配置
    DisplayImageOptions options_userlogo = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.loading)
            .showImageOnFail(R.drawable.h0).cacheInMemory(true).cacheOnDisc(true)
            .bitmapConfig(Bitmap.Config.RGB_565).imageScaleType(ImageScaleType.EXACTLY_STRETCHED)// 设置图片以如何的编码方式显示
            .resetViewBeforeLoading(true)// 设置图片在下载前是否重置，复位
            .displayer(new RoundedBitmapDisplayer(40))// 是否设置为圆角，弧度为多少
            .displayer(new FadeInBitmapDisplayer(100))// 是否图片加载好后渐入的动画时间
            .build();

    public void setZhuji(ZhujiInfo zhuji) {
        if (zhuji != null) {
            this.zhuji = zhuji;
        }
    }

    public ZhujiInfo getZhuji() {
        return zhuji;
    }

    public void clearZhuji(){
        this.zhuji = null;
    }

    public void showFloatingButton() {

        if (Build.VERSION.SDK_INT >= 23) {
            if (Settings.canDrawOverlays(this)) {
                new DialogView(this, false, "悬浮框设置", "是否打开悬浮框？", "忽略", "打开", null, new DialogView.DialogViewItemListener() {
                    @Override
                    public void onItemListener(DialogView dialogView, View view, int index) {
                        if (index == 1) {
                            //有悬浮窗权限开启服务绑定 绑定权限
                            Intent intent = new Intent(DeviceMainActivity.this, FloatService.class);
                            startService(intent);
                        }
                        dialogView.dismiss();
                    }
                }).show();

            } else {
                new DialogView(this, false, "权限设置", "没有设置开启悬浮框权限，是否前往设置？", "忽略", "打开", null, new DialogView.DialogViewItemListener() {
                    @Override
                    public void onItemListener(DialogView dialogView, View view, int index) {
                        if (index == 1) {
                            //没有悬浮窗权限m,去开启悬浮窗权限
                            try {
                                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                                startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        dialogView.dismiss();
                    }
                });

            }
        } else {
            //默认有悬浮窗权限  但是 华为, 小米,oppo等手机会有自己的一套Android6.0以下  会有自己的一套悬浮窗权限管理 也需要做适配
            new DialogView(this, false, "悬浮框设置", "是否打开悬浮框？", "忽略", "打开", null, new DialogView.DialogViewItemListener() {
                @Override
                public void onItemListener(DialogView dialogView, View view, int index) {
                    if (index == 1) {
                        //有悬浮窗权限开启服务绑定 绑定权限
                        Intent intent = new Intent(DeviceMainActivity.this, FloatService.class);
                        startService(intent);
                    }
                    dialogView.dismiss();
                }
            }).show();
        }
        //按钮被点击
//        this.startService(new Intent(this, FloatService.class));
// new TableShowView(this).fun(); 如果只是在activity中启动
// 当activity跑去后台的时候[暂停态，或者销毁态] 我们设置的显示到桌面的view也会消失
// 所以这里采用的是启动一个服务，服务中创建我们需要显示到table上的view，并将其注册到windowManager上
//       finish();
    }

    public void logout(View v) {
        new AlertView(getString(R.string.activity_user_info_logout_tip),
                getString(R.string.activity_user_info_logout_mes), getString(R.string.cancel),
                new String[]{getString(R.string.sure)}, null, mContext, AlertView.Style.Alert,
                new com.smartism.znzk.view.alertview.OnItemClickListener() {

                    @Override
                    public void onItemClick(Object o, int position) {
                        if (position != -1) {
                            mContext.logout();
                        }
                    }
                }).show();
    }

    public void logout() {
        AWSMobileClient.getInstance().signOut();
        // 跳转到登录页面，并且需要清空activity栈
        Intent refreshContans = new Intent();
        refreshContans.setAction(Constants.Action.ACTIVITY_FINISH);
        sendBroadcast(refreshContans);
        Intent loginIntent = new Intent();
        loginIntent.setClass(this, CoreService.class);
        stopService(loginIntent);
        loginIntent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        loginIntent.setClass(this, LoginActivity.class);
        startActivity(loginIntent);
    }

    private AlertView mAlertViewExt;
    private InputMethodManager imm;

    public void showAddTel() {
        ViewGroup extView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.activity_add_mobile_dialog, null);
        final EditText setgsm_phone_edit = (EditText) extView.findViewById(R.id.setgsm_phone_edit);
        final EditText et_code = (EditText) extView.findViewById(R.id.et_code);
        setgsm_phone_edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                //输入框出来则往上移动
                boolean isOpen = imm.isActive();
                mAlertViewExt.setMarginBottom(isOpen && focus ? 120 : 0);
                System.out.println(isOpen);
            }
        });
        et_code.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                //输入框出来则往上移动
                boolean isOpen = imm.isActive();
                mAlertViewExt.setMarginBottom(isOpen && focus ? 120 : 0);
                System.out.println(isOpen);
            }
        });
        mAlertViewExt = new AlertView("提示", "", "取消", null, new String[]{"完成"}, this, AlertView.Style.Alert, new OnItemClickListener() {
            @Override
            public void onItemClick(Object o, int position) {
                imm.hideSoftInputFromWindow(setgsm_phone_edit.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(et_code.getWindowToken(), 0);
                mAlertViewExt.setMarginBottom(0);
                //判断是否是拓展窗口View，而且点击的是非取消按钮
                if (o == mAlertViewExt && position != AlertView.CANCELPOSITION) {
                    String name = setgsm_phone_edit.getText().toString();
                    if (name.isEmpty()) {
                        Toast.makeText(mthis, "啥都没填呢", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mthis, "hello," + name, Toast.LENGTH_SHORT).show();
                    }

                    return;
                }
            }
        });
        mAlertViewExt.addExtView(extView);
        mAlertViewExt.show();

    }

    public InputMethodManager getImm() {
        return imm;
    }

    public Fragment getMainShowFragment() {
        return mainShowFragment;
    }
}
