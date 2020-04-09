package com.smartism.znzk.global;

import java.io.Serializable;

/**
 * Created by 王建 on 16/12/10.
 * app的全局配置bean，所有版本相关、功能相关的配置都需要配置在此bean中，此bean会在application初始化的时候进行初始化
 * 需要保持属性名称和assets中的AppGlobalConfig.json中配置的名称一样。
 */

public class AppGlobalConfig implements Serializable {

    private String version = "znzk";    //版本号(默认智能中控)
    private String versionPrefix = "FF00"; //版本主机前缀
    private String appid = "11141791"; //appid(默认智能中控)
    private String appSecret = "H44m3iw9z3JdYU58"; //appsecret
    private boolean isTc = false; //注册用户是否支持透传
    private boolean isDebug = false; // 是否是测试环境 需要配合地址使用

    private String miPushId; //小米推送id
    private String miPushKey; //小米推送key

    public String APPID = "";//摄像头appid
    public String APPToken = "";//摄像头token
    public String APPVersion = "";//摄像头version

    public String getXMAPPUUID() {
        return XMAPPUUID;
    }

    public void setXMAPPUUID(String XMAPPUUID) {
        this.XMAPPUUID = XMAPPUUID;
    }

    public String getXMAPPKey() {
        return XMAPPKey;
    }

    public void setXMAPPKey(String XMAPPKey) {
        this.XMAPPKey = XMAPPKey;
    }

    public String getXMAPPSecret() {
        return XMAPPSecret;
    }

    public void setXMAPPSecret(String XMAPPSecret) {
        this.XMAPPSecret = XMAPPSecret;
    }

    //雄迈摄像头
    public String XMAPPUUID="";
    public String XMAPPKey="";
    public String XMAPPSecret="";

    private boolean isRightMenu = false;//侧滑菜单是否在右边,右侧菜单功能的项目(1.伏索能源)
    private boolean isShowFrequency = true;  //是否显示遥控器频率 （1.智能中控显示）
    private boolean isFourKeyTelecontrol = false;  //是否是315 4键遥控器 （1.巨将）
    //右侧菜单
    private boolean isShowMostZhuji = true;  //是否显示多主机菜单
    private boolean isShowAddDevice = true;  //是否显示添加设备菜单
    private boolean isShowAddGroup = true;  //是否显示添加群组
    private boolean isShowNewAddGroup  ;  //新的添加群组方式,宏才
    private boolean isShowAddZhujiGroup = false;//是否支持主机添加群组
    private boolean isShowStudyFrequency = true;  //是否显示学习任意遥控器(巨将，智能中控)
    private boolean isShowVirtual = true;  //是否显示添加虚拟遥控器(巨将支持)

    public boolean isShowNewAddGroup() {
        return isShowNewAddGroup;
    }

    public void setShowNewAddGroup(boolean showAddNewGroup) {
        isShowNewAddGroup = showAddNewGroup;
    }

    private boolean isShowStudyProbe = true;  //是否显示学习任意探头(巨将，智能中控)
    private boolean isShowBatchControl = false;  //是否显示批量操作菜单
    private boolean isShowAddJWCamera = true;  //是否显示添加JW摄像头,也是否支持在主机中设置联动

    public boolean isShowAddCamera() {
        return isShowAddCamera;
    }

    public void setShowAddCamera(boolean showAddCamera) {
        isShowAddCamera = showAddCamera;
    }

    public boolean isShowAddXMCamera() {
        return isShowAddXMCamera;
    }

    public void setShowAddXMCamera(boolean showAddXMCamera) {
        isShowAddXMCamera = showAddXMCamera;
    }

    private boolean isShowAddCamera = true ; //是否显示添加摄像头菜单
    private boolean isShowAddXMCamera = true ; //是否支持添加雄迈
    private boolean isShowAddVCamera = false;  //是否显示添加V380摄像头
    private boolean isShowTemporarySharing = true;  //是否显示临时分享
    private boolean isShowTime = true; //立乐斯屏蔽时间
    private boolean isShowWendu = true; //立乐斯屏蔽温度
    private boolean isShowShidu = true; //立乐斯屏蔽湿度
    private boolean isRotationFortification = false; //吉开达的三个按钮放在一起轮播(设防，在家，撤防)
    private boolean isShowFortification = true; //是否显示按钮(设防，在家，撤防)
    private boolean isBipcn = true; //是否显示绑定功能（巨匠）
    private boolean isAudioMenu = false; //是否显示自定义铃声设置功能（只有巨匠有）
    private boolean isNoticeCenter = false; //是否显示报警中心（支持：CHANNEL_SHUNANJU）
    private boolean isShowSecurity = false;//是否显示24小时防区（支持：CHANNEL_LILESI）
    private boolean isShowTabHostShop = false;//是否显示底部导航中的商城（支持：CHANNEL_JUJIANG）
    private boolean isShowTabHostInteraction = false;//是否显示底部导航中的互动（支持：CHANNEL_JUJIANG）
    private boolean isShowTabHostService = false;//是否显示底部导航中的服务（支持：CHANNEL_JUJIANG）
    private boolean isShowSecurityDevice = false;//是否显示防区设置（支持：CHANNEL_SHUNANJU）
    private boolean isShowABBQService = false;//所有服务TAB页面
    private boolean isShowMine = false;//智慧主机/安霸保全我的TAB页面
    private boolean isShowMessages = false;//是否支持消息中心TAB页签
    private boolean isShowShoTabMain = false;//是否支持TAB页签 - 商城版本 - 首页
    private boolean isShowShoTabFind = false;//是否支持TAB页签 - 商城版本 - 发现
    private boolean isShowShoTabMerchant = false;//是否支持TAB页签 - 商城版本 - 商家
    private boolean isShowShoTabMine = false;//是否支持TAB页签 - 商城版本 - 我的
    private boolean isPhone = false;//是否支持手机验证码功能（支持：CHANNEL_HTZN， CHANNEL_JKD）
    private boolean isShowRecode = false;//是否显示录像功能（支持：CHANNEL_HTZN， CHANNEL_JKD）
    private boolean isGridFragment = false; //设备列表是否显示gridview（支持：fsny）
    private boolean isAutomaticUpdates = false; //是否支持自动更新(目前每个都有，没写入json文件直接再这修改)
    private boolean isRepeatClickSence = false;//首页是否可重复点击场景
    private boolean isShowExperiHub = false;//是否显示体验主机
    private boolean isSupportGestures = false;//是否支持手势密码
    public String YaokanAppid = "14689138661283";
    private String YaoKanDeviceId = "";
    //设备详情界面
    private boolean isShowAFpanel = true; //是否显主机遥控器自定义面板
    //登录界面的第三方登录
    private boolean isShowThirdLogin = true; //是否显示第三方登录布局
    private boolean isShowFaceBook = true;
    private boolean isShowQQ = true;
    private boolean isShowWeiXin = true;
    private boolean isShowTwitter = true;
    private boolean isShowGoogle = false;
    //主机详情信息界面显示SIM 、电量、 联网 、 电源（立乐斯、jkd）
    private boolean isShowPhoneState = false;
    //侧滑菜单
    private boolean isShowScene = true; //是否显示智能场景选项（不显示：HTZN ）
    //添加主机界面
    private boolean isShowAddWifiZJ = true; //是否显示WIFI新购主机
    private boolean isShowAddLanZJ = true; //是否显示Lan新购主机
    private boolean isClickDeviceItem = false; //在主机离线的时候是否可以点击设备（支持：巨将）
    //首页底部菜单 此菜单已经去掉，修改为是否支持这两个功能。
    private boolean isShowCallAlarm = false;//是否支持电话报警(报警号码管理)
    private boolean isShowSmsAlarm = false;//是否支持短信报警
    private boolean isSuportVoice = false;//是否支持语音验证码
    private boolean isStartLogService = false;
    //是否显示欢迎页和引导页
    private boolean isShowWelcome;
    private boolean isShowScanCamera = false; //是否支持扫码添加摄像头
    private boolean isShowHeldWeb = false; //是否显示帮助页
    private boolean isSowSpeech = false; //是否显示语音
    private boolean isChildAccountAllow = false;
    // 摄像头设置菜单
    private boolean cameraTimeSetting = true; //时间设置
    private boolean cameraMediaSetting = true; //媒体设置
    private boolean cameraSnapshotSetting = true; //截图
    private boolean cameraSecuritySetting = true; //安全设置
    private boolean cameraRecordSetting = true; //录像设置
    private boolean cameraVedioFile = true; //录像列表
    private boolean cameraAlarmSetting = true; //报警设置
    private boolean cameraNetWorkSetting = true; //网络设置
    private boolean cameraUpDate = true; //更新固件
    private boolean isShowDevicesPermisson;//是否显示用户设备授权

    public boolean isStartLogService() {
        return isStartLogService;
    }

    public void setStartLogService(boolean startLogService) {
        isStartLogService = startLogService;
    }

    public boolean isChildAccountAllow() {
        return isChildAccountAllow;
    }

    public void setChildAccountAllow(boolean childAccountAllow) {
        isChildAccountAllow = childAccountAllow;
    }

    public boolean isShowDevicesPermisson() {
        return isShowDevicesPermisson;
    }

    public void setShowDevicesPermisson(boolean showDevicesPermisson) {
        isShowDevicesPermisson = showDevicesPermisson;
    }

    public boolean isCameraTimeSetting() {
        return cameraTimeSetting;
    }

    public void setCameraTimeSetting(boolean cameraTimeSetting) {
        this.cameraTimeSetting = cameraTimeSetting;
    }

    public boolean isCameraMediaSetting() {
        return cameraMediaSetting;
    }

    public void setCameraMediaSetting(boolean cameraMediaSetting) {
        this.cameraMediaSetting = cameraMediaSetting;
    }

    public boolean isCameraSnapshotSetting() {
        return cameraSnapshotSetting;
    }

    public void setCameraSnapshotSetting(boolean cameraSnapshotSetting) {
        this.cameraSnapshotSetting = cameraSnapshotSetting;
    }

    public boolean isCameraSecuritySetting() {
        return cameraSecuritySetting;
    }

    public void setCameraSecuritySetting(boolean cameraSecuritySetting) {
        this.cameraSecuritySetting = cameraSecuritySetting;
    }


    public boolean isRepeatClickSence() {
        return isRepeatClickSence;
    }

    public void setRepeatClickSence(boolean repeatClickSence) {
        isRepeatClickSence = repeatClickSence;
    }

    public boolean isShowBatchControl() {
        return isShowBatchControl;
    }

    public void setShowBatchControl(boolean showBatchControl) {
        isShowBatchControl = showBatchControl;
    }

    public boolean isShowExperiHub() {
        return isShowExperiHub;
    }

    public void setShowExperiHub(boolean showExperiHub) {
        isShowExperiHub = showExperiHub;
    }


    public boolean isCameraRecordSetting() {
        return cameraRecordSetting;
    }

    public void setCameraRecordSetting(boolean cameraRecordSetting) {
        this.cameraRecordSetting = cameraRecordSetting;
    }

    public boolean isCameraVedioFile() {
        return cameraVedioFile;
    }

    public void setCameraVedioFile(boolean cameraVedioFile) {
        this.cameraVedioFile = cameraVedioFile;
    }

    public boolean isCameraAlarmSetting() {
        return cameraAlarmSetting;
    }

    public void setCameraAlarmSetting(boolean cameraAlarmSetting) {
        this.cameraAlarmSetting = cameraAlarmSetting;
    }

    public boolean isCameraNetWorkSetting() {
        return cameraNetWorkSetting;
    }

    public void setCameraNetWorkSetting(boolean cameraNetWorkSetting) {
        this.cameraNetWorkSetting = cameraNetWorkSetting;
    }

    public boolean isCameraUpDate() {
        return cameraUpDate;
    }

    public void setCameraUpDate(boolean cameraUpDate) {
        this.cameraUpDate = cameraUpDate;
    }

    public String getAPPID() {
        return APPID;
    }

    public void setAPPID(String APPID) {
        this.APPID = APPID;
    }

    public String getAPPToken() {
        return APPToken;
    }

    public void setAPPToken(String APPToken) {
        this.APPToken = APPToken;
    }

    public String getAPPVersion() {
        return APPVersion;
    }

    public void setAPPVersion(String APPVersion) {
        this.APPVersion = APPVersion;
    }

    public boolean isSowSpeech() {
        return isSowSpeech;
    }

    public void setSowSpeech(boolean sowSpeech) {
        isSowSpeech = sowSpeech;
    }

    public boolean isShowHeldWeb() {
        return isShowHeldWeb;
    }

    public void setShowHeldWeb(boolean showHeldWeb) {
        isShowHeldWeb = showHeldWeb;
    }

    public boolean isShowScanCamera() {
        return isShowScanCamera;
    }

    public void setShowScanCamera(boolean showScanCamera) {
        isShowScanCamera = showScanCamera;
    }

    public boolean isClickDeviceItem() {
        return isClickDeviceItem;
    }

    public void setClickDeviceItem(boolean clickDeviceItem) {
        isClickDeviceItem = clickDeviceItem;
    }

    public boolean isShowGuidePage() {
        return isShowGuidePage;
    }

    public void setShowGuidePage(boolean showGuidePage) {
        isShowGuidePage = showGuidePage;
    }

    public boolean isShowWelcome() {
        return isShowWelcome;
    }

    public void setShowWelcome(boolean showWelcome) {
        isShowWelcome = showWelcome;
    }

    private boolean isShowGuidePage;


    public boolean isShowAddWifiZJ() {
        return isShowAddWifiZJ;
    }

    public void setShowAddWifiZJ(boolean showAddWifiZJ) {
        isShowAddWifiZJ = showAddWifiZJ;
    }

    public boolean isShowAddLanZJ() {
        return isShowAddLanZJ;
    }

    public void setShowAddLanZJ(boolean showAddLanZJ) {
        isShowAddLanZJ = showAddLanZJ;
    }

    public boolean isShowScene() {
        return isShowScene;
    }

    public void setShowScene(boolean showScene) {
        isShowScene = showScene;
    }

    public boolean isShowPhoneState() {
        return isShowPhoneState;
    }

    public void setShowPhoneState(boolean showPhoneState) {
        isShowPhoneState = showPhoneState;
    }

    public boolean isShowAFpanel() {
        return isShowAFpanel;
    }

    public void setShowAFpanel(boolean showAFpanel) {
        isShowAFpanel = showAFpanel;
    }

    public boolean isShowThirdLogin() {
        return isShowThirdLogin;
    }

    public void setShowThirdLogin(boolean showThirdLogin) {
        isShowThirdLogin = showThirdLogin;
    }

    public boolean isShowFaceBook() {
        return isShowFaceBook;
    }

    public void setShowFaceBook(boolean showFaceBook) {
        isShowFaceBook = showFaceBook;
    }

    public boolean isShowQQ() {
        return isShowQQ;
    }

    public void setShowQQ(boolean showQQ) {
        isShowQQ = showQQ;
    }

    public boolean isShowWeiXin() {
        return isShowWeiXin;
    }

    public void setShowWeiXin(boolean showWeiXin) {
        isShowWeiXin = showWeiXin;
    }

    public boolean isShowTwitter() {
        return isShowTwitter;
    }

    public void setShowTwitter(boolean showTwitter) {
        isShowTwitter = showTwitter;
    }

    public boolean isAutomaticUpdates() {
        return isAutomaticUpdates;
    }

    public void setAutomaticUpdates(boolean automaticUpdates) {
        isAutomaticUpdates = automaticUpdates;
    }

    public boolean isGridFragment() {
        return isGridFragment;
    }

    public void setGridFragment(boolean gridFragment) {
        isGridFragment = gridFragment;
    }

    public boolean isShowRecode() {
        return isShowRecode;
    }

    public void setShowRecode(boolean showRecode) {
        isShowRecode = showRecode;
    }

    public boolean isPhone() {
        return isPhone;
    }

    public void setPhone(boolean phone) {
        isPhone = phone;
    }

    public boolean isShowSecurityDevice() {
        return isShowSecurityDevice;
    }

    public void setShowSecurityDevice(boolean showSecurityDevice) {
        isShowSecurityDevice = showSecurityDevice;
    }

    public boolean isShowTabHostShop() {
        return isShowTabHostShop;
    }

    public void setShowTabHostShop(boolean showTabHostShop) {
        isShowTabHostShop = showTabHostShop;
    }

    public boolean isShowSecurity() {
        return isShowSecurity;
    }

    public void setShowSecurity(boolean showSecurity) {
        isShowSecurity = showSecurity;
    }

    public boolean isNoticeCenter() {
        return isNoticeCenter;
    }

    public void setNoticeCenter(boolean noticeCenter) {
        isNoticeCenter = noticeCenter;
    }

    public boolean isShowFortification() {
        return isShowFortification;
    }

    public boolean isAudioMenu() {
        return isAudioMenu;
    }

    public void setAudioMenu(boolean audioMenu) {
        isAudioMenu = audioMenu;
    }

    public void setShowFortification(boolean showFortification) {
        isShowFortification = showFortification;
    }

    public boolean isBipcn() {
        return isBipcn;
    }

    public void setBipcn(boolean bipcn) {
        isBipcn = bipcn;
    }

    public boolean isRotationFortification() {
        return isRotationFortification;
    }

    public void setRotationFortification(boolean rotationFortification) {
        isRotationFortification = rotationFortification;
    }

    public boolean isShowTabHostInteraction() {
        return isShowTabHostInteraction;
    }

    public void setShowTabHostInteraction(boolean showTabHostInteraction) {
        isShowTabHostInteraction = showTabHostInteraction;
    }

    public boolean isShowTabHostService() {
        return isShowTabHostService;
    }

    public void setShowTabHostService(boolean showTabHostService) {
        isShowTabHostService = showTabHostService;
    }

    public boolean isShowWendu() {
        return isShowWendu;
    }

    public void setShowWendu(boolean showWendu) {
        isShowWendu = showWendu;
    }

    public boolean isShowShidu() {
        return isShowShidu;
    }

    public void setShowShidu(boolean showShidu) {
        isShowShidu = showShidu;
    }

    public boolean isShowTime() {
        return isShowTime;
    }

    public void setShowTime(boolean showTime) {
        isShowTime = showTime;
    }

    public boolean isShowTemporarySharing() {
        return isShowTemporarySharing;
    }

    public void setShowTemporarySharing(boolean showTemporarySharing) {
        isShowTemporarySharing = showTemporarySharing;
    }

    public boolean isShowMostZhuji() {
        return isShowMostZhuji;
    }

    public void setShowMostZhuji(boolean showMostZhuji) {
        isShowMostZhuji = showMostZhuji;
    }

    public boolean isShowAddDevice() {
        return isShowAddDevice;
    }

    public void setShowAddDevice(boolean showAddDevice) {
        isShowAddDevice = showAddDevice;
    }

    public boolean isShowAddGroup() {
        return isShowAddGroup;
    }

    public void setShowAddGroup(boolean showAddGroup) {
        isShowAddGroup = showAddGroup;
    }

    public boolean isShowStudyFrequency() {
        return isShowStudyFrequency;
    }

    public void setShowStudyFrequency(boolean showStudyFrequency) {
        isShowStudyFrequency = showStudyFrequency;
    }

    public boolean isShowVirtual() {
        return isShowVirtual;
    }

    public void setShowVirtual(boolean showVirtual) {
        isShowVirtual = showVirtual;
    }

    public boolean isShowStudyProbe() {
        return isShowStudyProbe;
    }

    public void setShowStudyProbe(boolean showStudyProbe) {
        isShowStudyProbe = showStudyProbe;
    }

    public boolean isShowAddJWCamera() {
        return isShowAddJWCamera;
    }

    public void setShowAddJWCamera(boolean showAddJWCamera) {
        isShowAddJWCamera = showAddJWCamera;
    }

    public boolean isShowAddVCamera() {
        return isShowAddVCamera;
    }

    public void setShowAddVCamera(boolean showAddVCamera) {
        isShowAddVCamera = showAddVCamera;
    }

    public boolean isFourKeyTelecontrol() {
        return isFourKeyTelecontrol;
    }

    public void setFourKeyTelecontrol(boolean fourKeyTelecontrol) {
        isFourKeyTelecontrol = fourKeyTelecontrol;
    }

    public boolean isShowFrequency() {
        return isShowFrequency;
    }

    public void setShowFrequency(boolean showFrequency) {
        isShowFrequency = showFrequency;
    }

    public String getVersionPrefix() {
        return versionPrefix;
    }

    public void setVersionPrefix(String versionPrefix) {
        this.versionPrefix = versionPrefix;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isRightMenu() {
        return isRightMenu;
    }

    public void setRightMenu(boolean rightMenu) {
        isRightMenu = rightMenu;
    }

    public String getMiPushId() {
        return miPushId;
    }

    public void setMiPushId(String miPushId) {
        this.miPushId = miPushId;
    }

    public String getMiPushKey() {
        return miPushKey;
    }

    public void setMiPushKey(String miPushKey) {
        this.miPushKey = miPushKey;
    }

    public boolean isTc() {
        return isTc;
    }

    public void setTc(boolean tc) {
        isTc = tc;
    }

    public boolean isDebug() {
        return isDebug;
    }

    public void setDebug(boolean debug) {
        isDebug = debug;
    }

    public String getYaokanAppid() {
        return YaokanAppid;
    }

    public void setYaokanAppid(String yaokanAppid) {
        YaokanAppid = yaokanAppid;
    }

    public String getYaoKanDeviceId() {
        return YaoKanDeviceId;
    }

    public void setYaoKanDeviceId(String yaoKanDeviceId) {
        YaoKanDeviceId = yaoKanDeviceId;
    }

    public boolean isShowCallAlarm() {
        return isShowCallAlarm;
    }

    public void setShowCallAlarm(boolean showCallAlarm) {
        isShowCallAlarm = showCallAlarm;
    }

    public boolean isShowSmsAlarm() {
        return isShowSmsAlarm;
    }

    public void setShowSmsAlarm(boolean showSmsAlarm) {
        isShowSmsAlarm = showSmsAlarm;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public boolean isShowMessages() {
        return isShowMessages;
    }

    public void setShowMessages(boolean showMessages) {
        isShowMessages = showMessages;
    }

    public boolean isSupportGestures() {
        return isSupportGestures;
    }

    public void setSupportGestures(boolean supportGestures) {
        isSupportGestures = supportGestures;
    }

    public boolean isShowAddZhujiGroup() {
        return isShowAddZhujiGroup;
    }

    public void setShowAddZhujiGroup(boolean showAddZhujiGroup) {
        isShowAddZhujiGroup = showAddZhujiGroup;
    }

    public boolean isShowABBQService() {
        return isShowABBQService;
    }

    public void setShowABBQService(boolean showABBQService) {
        isShowABBQService = showABBQService;
    }

    public boolean isShowMine() {
        return isShowMine;
    }

    public void setShowMine(boolean showMine) {
        isShowMine = showMine;
    }

    public boolean isShowShoTabMain() {
        return isShowShoTabMain;
    }

    public void setShowShoTabMain(boolean showShoTabMain) {
        isShowShoTabMain = showShoTabMain;
    }

    public boolean isShowShoTabFind() {
        return isShowShoTabFind;
    }

    public void setShowShoTabFind(boolean showShoTabFind) {
        isShowShoTabFind = showShoTabFind;
    }

    public boolean isShowShoTabMerchant() {
        return isShowShoTabMerchant;
    }

    public void setShowShoTabMerchant(boolean showShoTabMerchant) {
        isShowShoTabMerchant = showShoTabMerchant;
    }

    public boolean isShowShoTabMine() {
        return isShowShoTabMine;
    }

    public void setShowShoTabMine(boolean showShoTabMine) {
        isShowShoTabMine = showShoTabMine;
    }

    public boolean isSuportVoice() {
        return isSuportVoice;
    }

    public void setSuportVoice(boolean suportVoice) {
        isSuportVoice = suportVoice;
    }

    public boolean isShowGoogle() {
        return isShowGoogle;
    }

    public void setShowGoogle(boolean showGoogle) {
        isShowGoogle = showGoogle;
    }
}
