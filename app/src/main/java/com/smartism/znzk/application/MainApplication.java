package com.smartism.znzk.application;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.util.Log;
import android.util.Xml;

import com.alibaba.fastjson.JSON;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.client.UserStateListener;
import com.baidu.location.BDLocation;
import com.device.lib.CommConfig;
import com.hjq.toast.ToastUtils;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.p2p.core.P2PSpecial.P2PSpecial;
import com.smartism.znzk.activity.alert.AlertMessageActivity;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.global.AppGlobalConfig;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.listener.CrashHandler;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.LogUtil;
import com.smartism.znzk.xiongmai.lib.funsdk.support.FunSupport;
import com.umeng.commonsdk.UMConfigure;
import com.zhy.http.okhttp.OkHttpUtils;

import org.xmlpull.v1.XmlPullParser;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;


public class MainApplication extends MultiDexApplication {
    //摄像头语音
    static {
        CommConfig.NETWORK_URL = "http://api.ai-thinker.com:4567/v1/";
        CommConfig.ACCESS_SECRET = "e09045baa3736cdb6647acb51f463d8f";
        CommConfig.ACCESS_KEY = "046765cdf54855aefd74a87377eb4389";
        CommConfig.VENDOR_ID = "3zH4prqPRfDd5qPNrA2aV9";
        CommConfig.APPNAME = "AiSmart";
        CommConfig.MESSAGE_CODE_TIMEOUT = 300;
    }

    private HashMap<String, Intent> backNeedStartActivtyMap = new HashMap<>();
    public static MainApplication app;
    public static int GWELL_LOCALAREAIP = 0;
    private AppGlobalConfig appGlobalConfig;
    public static final String MAIN_SERVICE_START = "com.znwx.jiadianmao.service.CoreService";
    //    public static final String MAIN_SERVICE_START = Constants.PACKAGE_NAME
//            + "service.MAINSERVICE";
    public static final int NOTIFICATION_DOWN_ID = 0x53256562;
    public static final String LOGCAT = Constants.PACKAGE_NAME + "service.LOGCAT";
    public AlertMessageActivity alertMessageActivity;
    public int resendCount = 0; //心跳包已重发次数,收到回应设置为0，如果到达3次，断开连接重连。
    private int mCount = 0;//用于判断app处于前台还是后台
    private boolean isNeedRefreshMcenter = true; //默认是否需要自动刷新消息中心
    private boolean isNotice = true;
    private boolean isCNNet = false;//判断是否是中国网络
    private BDLocation location;//当前位置信息

    public boolean isNotice() {
        return isNotice;
    }

    public void setNotice(boolean notice) {
        isNotice = notice;
    }

    public boolean isNeedRefreshMcenter() {
        return isNeedRefreshMcenter;
    }

    public void setNeedRefreshMcenter(boolean needRefreshMcenter) {
        isNeedRefreshMcenter = needRefreshMcenter;
    }
    //	private boolean connectionIsOk = false;
    //http://jdm.smart-ism.com:9999/jdm/login

    //public static Boolean LockS=false;

    //    public final static String APPID="3d4f3cd96f07d302b291eca46ad6d174";
//    public final static String APPToken="09645d257535d8c6d77af2bd1a2ad3869881a810e27e90cb4f1019f1578708b8";
//    public final static String APPVersion="04.28.00.01";
    public static final String TAG = "AWS-CTR";

    private void initP2P(MainApplication app) {
        P2PSpecial.getInstance().init(app, getAppGlobalConfig().getAPPID(), getAppGlobalConfig().getAPPToken(), getAppGlobalConfig().getAPPVersion());
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        LeakCanary.install(this);
        //讯飞语音初始化
//        SpeechUtility.createUtility(MainApplication.this, "appid=" + getResources().getString(R.string.app_id));
        app = this;
        ToastUtils.init(this);
        Resources res = super.getResources();
        Configuration configuration = new Configuration();

        configuration.setToDefaults();

        res.updateConfiguration(configuration, res.getDisplayMetrics());

        initAppGlobalConfig();
        //初始化雄迈摄像头SDK
        //集成雄迈摄像头
        FunSupport.getInstance().init(this);

        initP2P(this);
        CrashHandler.getInstance().init(this);

        initUMeng();//友盟初始化

//        CrashHandler.getInstance().initCrashHandler(app);

//        JPushInterface.stopPush(this);//默认停止极光推送，正的要使用时再调用init

//		Constants.PACKAGE_NAME = getPackageName()+".";
//		Thread.setDefaultUncaughtExceptionHandler(new CrashHandler());
        // 初始化数据库
        DatabaseOperator.getInstance(getApplicationContext());
        //创建默认的ImageLoader配置参数
//        ImageLoaderConfiguration configuration = ImageLoaderConfiguration
//                .createDefault(this);
        ImageLoaderConfiguration config = new ImageLoaderConfiguration
                .Builder(this)
                .memoryCacheExtraOptions(480, 800) // max width, max height，即保存的每个缓存文件的最大长宽
//        	    .discCacheExtraOptions(480, 800, CompressFormat.JPEG, 75, null) // Can slow ImageLoader, use it carefully (Better don't use it)/设置缓存的详细信息，最好不要设置这个
                .threadPoolSize(3)//线程池内加载的数量
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024)) // You can pass your own memory cache implementation/你可以通过自己的内存缓存实现
                .memoryCacheSize(2 * 1024 * 1024)
                .discCacheSize(100 * 1024 * 1024)
                .discCacheFileNameGenerator(new Md5FileNameGenerator())//将保存的时候的URI名称用MD5 加密
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .discCacheFileCount(100) //缓存的文件数量
//        	    .discCache(new UnlimitedDiscCache(cacheDir))//自定义缓存路径
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                .imageDownloader(new BaseImageDownloader(this, 10 * 1000, 30 * 1000)) // connectTimeout (5 s), readTimeout (30 s)超时时间
                .writeDebugLogs() // Remove for release app
                .build();//开始构建

        //Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
        // 初始化服务器地址
        DataCenterSharedPreferences dcsp = DataCenterSharedPreferences
                .getInstance(this, Constant.CONFIG);
        dcsp.putInt(Constant.READER_IDLE, 60).commit(); //60S未收到包则会断开连接
        //获取channel号
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            if (dcsp.getInt(Constant.APP_VERSIONCODE, 0) != packageInfo.versionCode) {
                dcsp.putBoolean(Constant.IS_FIRSTSTART, true).putInt(Constant.APP_VERSIONCODE, packageInfo.versionCode).commit();
            }
            LogUtil.isDebug = appGlobalConfig.isDebug();
            //debug.smart-ism.com 120.24.77.139
            //xjp 47.88.154.87
            //192.168.0.108
            if (LogUtil.isDebug) {
//				dcsp.putString(DataCenterSharedPreferences.Constant.SYNC_DATA_SERVERS,
//						"debug.smart-ism.com:7778")
//				.putString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS,
//						"debug.smart-ism.com:9999").putBoolean("isTC", getAppGlobalConfig().isTc()).commit();
//                dcsp.putString(Constant.SYNC_DATA_SERVERS,
//                        "dev.smart-ism.com:7778")
//                        .putString(Constant.HTTP_DATA_SERVERS,
//                                "http://192.168.0.103:9999").commit();
                dcsp.putString(Constant.SYNC_DATA_SERVERS,
                        "dev.smart-ism.com:7778")
                        .putString(Constant.HTTP_DATA_SERVERS,
                                "https://dev.smart-ism.com").commit();
            }
        } catch (NameNotFoundException e) {
            Log.e("jdm", "获取家电猫是否测试错误 ", e);
        }


        OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                .addInterceptor(new LoggerInterceptor("TAG"))
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                //其他配置
                .build();

        OkHttpUtils.initClient(okHttpClient);

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                mCount++;
                if (mCount == 1) {
                    //前台
//                    Log.e(TAG, "onActivityStopped: 进入前台啦~~~~~~~~~~~~~~~~~~~~~", null);
//                    KeyguardManager mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
//                    boolean flag = mKeyguardManager.isKeyguardLocked();
//                    if (flag){
//                        Log.e(TAG, "onActivityStopped: 进入前台啦~~~~~~发现上面还有手机密码，要显示手势滴~~~~~~~~~~~~~~~", null);
//                        DataCenterSharedPreferences dcsp = DataCenterSharedPreferences.getInstance(MainApplication.app,
//                                Constant.CONFIG);
//                        dcsp.putBoolean(DataCenterSharedPreferences.Constant.IS_LOOKS, true).commit();
//                    }
                }
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                mCount--;
                if (mCount == 0) {
                    //后台
//                    Log.e(TAG, "onActivityStopped: 进入后台啦！！！！！！！！！！！！！！！", null);
                    DataCenterSharedPreferences dcsp = DataCenterSharedPreferences.getInstance(MainApplication.app,
                            Constant.CONFIG);
                    dcsp.putBoolean(DataCenterSharedPreferences.Constant.IS_LOOKS, true).commit();
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
        //this.getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData.putString("", "");
    }
//	public boolean isConnectionIsOk() {
//		return connectionIsOk;
//	}
//	public void setConnectionIsOk(boolean connectionIsOk) {
//		this.connectionIsOk = connectionIsOk;
//	}

    private void initUMeng(){
        try {
            ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            String umengAppKey = applicationInfo.metaData.getString("UMENG_APPKEY");
            String umengChannel = applicationInfo.metaData.getString("UMENG_CHANNEL");
            UMConfigure.init(this,umengAppKey,umengChannel,UMConfigure.DEVICE_TYPE_PHONE,null);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public AppGlobalConfig getAppGlobalConfig() {
        return appGlobalConfig;
    }

/*    public String getVersionType() {
        return version_type;
    }*/

/*    public String getJdmVersionPrefix() {
        if (VersionType.CHANNEL_ZNZK.equals(getVersionType())) {
            return "FF00";
        } else if (VersionType.CHANNEL_LILESI.equals(getVersionType())) {
            return "FF01";
        } else if (VersionType.CHANNEL_JUJIANG.equals(getVersionType())) {
            return "FF02";
        } else if (VersionType.CHANNEL_JKD.equals(getVersionType())) {
            return "FF03";
        } else if (VersionType.CHANNEL_ZHISHANG.equals(getVersionType())) {
            return "FF04";
        } else if (VersionType.CHANNEL_ZHZJ.equals(getVersionType())) {
            return "FF05";
        } else if (VersionType.CHANNEL_UCTECH.equals(getVersionType())) {
            return "FF06";
        } else if (VersionType.CHANNEL_SHUNANJU.equals(getVersionType())) {
            return "FF07";
        } else if (VersionType.CHANNEL_HTZN.equals(getVersionType())) {
            return "FF08";
        } else if (VersionType.CHANNEL_SOLECOM.equals(getVersionType())) {
            return "FF09";
        }
        return "FF00";
    }*/

//    public String getJdmVersionByPrefix(String prefix) {
//        if ("FF00".equals(prefix)) {
//            return VersionType.CHANNEL_ZNZK;
//        } else if ("FF01".equals(prefix)) {
//            return VersionType.CHANNEL_LILESI;
//        } else if ("FF02".equals(prefix)) {
//            return VersionType.CHANNEL_JUJIANG;
//        } else if ("FF03".equals(prefix)) {
//            return VersionType.CHANNEL_JKD;
//        } else if ("FF04".equals(prefix)) {
//            return VersionType.CHANNEL_ZHISHANG;
//        } else if ("FF05".equals(prefix)) {
//            return VersionType.CHANNEL_ZHZJ;
//        } else if ("FF06".equals(prefix)) {
//            return VersionType.CHANNEL_UCTECH;
//        } else if ("FF07".equals(prefix)) {
//            return VersionType.CHANNEL_SHUNANJU;
//        } else if ("FF08".equals(prefix)) {
//            return VersionType.CHANNEL_HTZN;
//        } else if ("FF09".equals(prefix)) {
//            return VersionType.CHANNEL_SOLECOM;
//        }
//        return VersionType.CHANNEL_ZNZK;
//    }

    public AlertMessageActivity getAlertMessageActivity() {
        return alertMessageActivity;
    }

    public void setAlertMessageActivity(AlertMessageActivity alertMessageActivity) {
        this.alertMessageActivity = alertMessageActivity;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private void initAppGlobalConfig() {
        XmlPullParser parser = null;
        try {
            parser = Xml.newPullParser();
            parser.setInput(getAssets().open("AppGlobalConfig.xml"), "UTF-8");
            StringBuilder configBuilder = new StringBuilder("{");
            int event = parser.getEventType();//产生第一个事件
            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_DOCUMENT://判断当前事件是否是文档开始事件
                        break;
                    case XmlPullParser.START_TAG://判断当前事件是否是标签元素开始事件
                        if (!parser.getName().equals("Config")) {
                            if (!configBuilder.toString().equals("{")) {
                                configBuilder.append(",");
                            }
                            configBuilder.append("\"");
                            configBuilder.append(parser.getName());
                            configBuilder.append("\":\"");
                            configBuilder.append(parser.nextText());
                            configBuilder.append("\"");
                        }
                        break;
                    case XmlPullParser.END_TAG://判断当前事件是否是标签元素结束事件
                        break;
                }
                event = parser.next();//进入下一个元素并触发相应事件
            }//end while
            configBuilder.append("}");
            appGlobalConfig = JSON.parseObject(configBuilder.toString(), AppGlobalConfig.class);
        } catch (Exception e) {
            Log.e(TAG, "initAppGlobalConfig: 配置初始化失败！！！", e);
            appGlobalConfig = new AppGlobalConfig();
        }
    }

    public boolean isCNNet() {
        return isCNNet;
    }

    public void setCNNet(boolean CNNet) {
        isCNNet = CNNet;
    }

    public BDLocation getLocation() {
        return location;
    }

    public void setLocation(BDLocation location) {
        this.location = location;
    }

    public HashMap<String, Intent> getBackNeedStartActivtyMap() {
        return backNeedStartActivtyMap;
    }
}
