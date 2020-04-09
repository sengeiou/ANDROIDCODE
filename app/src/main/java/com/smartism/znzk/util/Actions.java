package com.smartism.znzk.util;

public interface Actions {
    //广播名称命名规范：发送广播主体-发生了什么事 例如：收到服务器发过来的设备信息  消息-初始化完成， 消息-新消息
    public static final String ACCETP_ONEDEVICE_MESSAGE = "ACCETP_ONEDEVICE_MESSAGE";//收到一个需要更新数据库并刷新页面的回复包
    public static final String ACCETP_ONEWEIGHT_MESSAGE = "ACCETP_ONEWEIGHT_MESSAGE";//收到一个需要更新数据库并刷新页面的体重秤主动推送包
    public static final String ACCETP_ONEXYJ_MESSAGE = "ACCETP_ONEXYJ_MESSAGE";//收到一个需要更新数据库并刷新页面的血压计主动推送包
    public static final String ACCETP_REFERSH_USERDATA = "ACCETP_REFERSH_USERDATA";//刷新成员数据
    public static final String ACCETP_WEIGHT_WEIGHTDATA = "ACCETP_WEIGHT_WEIGHTDATA";//修改目标体重
    public static final String ACCETP_WEIGHT_UPDATEDATA = "ACCETP_WEIGHT_UPDATEDATA";//历史数据修改或删除
    public static final String ACCETP_WEIGHT_UPDATEUNIT = "ACCETP_WEIGHT_UPDATEUINT";//单位切换
    public static final String CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";// 网络状态广播
    public static final String CONNECTIVITY_KEPLIVE = "android.net.conn.CONNECTIVITY_KEPLIVE";// 网络状态检查和心跳广播 
    public static final String CONNECTION_ING = "znwx.net.conn.CONNECTION_ING";// 服务器连接中
    public static final String CONNECTION_SUCCESS = "znwx.net.conn.CONNECTION_SUCCESS";// 服务器连接成功 (登录成功和心跳检测时都会触发此广播，页面接收显示断开连接提示)
    public static final String CONNECTION_FAILED = "znwx.net.conn.CONNECTION_FAILED";// 服务器连接失败
    public static final String CONNECTION_NONET = "znwx.net.conn.CONNECTION_NONET";// 无网络
    public static final String CONNECTION_FAILED_SENDFAILED = "znwx.net.conn.CONNECTION_FAILED_SENDFAILED";// 服务器未连接，发送失败 请发送指令的地方监听 请发送指令的地方监听 心跳和登录不返回
    public static final String SHOW_SERVER_MESSAGE = "znwx.net.conn.SHOW_SERVER_MESSAGE";// 显示服务器返回的信息

    public static final String CONTROL_BACK_MESSAGE = "znwx.control.result.CONTROL_BACK_MESSAGE";//控制返回

    public static final String REFRESH_DEVICES_LIST = "znwx.refresh.devices.list123";//设备列表有变更，需要刷新列表页面(重新加载数据，设置主机是否显示等)
    public static final String SEND_SEARCHZHUJICOMMAND = "znwx.init.device.send.searchcommand";//未发现主机,发送刷新108重新获取主机
    public static final String ZHUJI_CHECKUPDATE = "znwx.zhuji.checkupdate";//主机检查更新广播
    public static final String ZHUJI_UPDATE = "znwx.zhuji.update";//主机更新
    public static final String APP_KICKOFF = "znwx.app.kickoff";//被踢下线提醒
    public static final String APP_RECONNECTION = "znwx.app.reconnection";//重新连接服务器
    public static final String APP_KICKOFF_SESSIONFAILURE = "znwx.app.kickoff.sessionfailure";//被踢下线提醒 登录失效
    public static final String APP_KICKOFF_OUTOFDAY = "znwx.app.kickoff.outofday";//被踢下线提醒 登录已经过期
//    public static final String ACCETP_QWQYLIQUID = "ACCETP_QWQYLIQUID";//驱蚊器液量
    public static final String ACCETP_REFRESH_MEDICINE_INFO = "ACCETP_REFRESH_MEDICINE_INFO";//刷新
    public static final String ACCETP_MAIN_SHOW_SCENCE = "ACCETP_MAIN_SHOW_SCENCE";//首页显示场景
    public static final String UPDATE_USER_LOGO = "UPDATE_USER_LOGO";//首页更新头像


    public static final String ADD_NEW_ZHUJI = "add_new_zhuji";//首页更新头像

    public static final String MQTT_TOPIC_THINGNAME = "MQTT_TOPIC_THINGNAME";//主题中的thingName参数
    public static final String MQTT_UPDATE_ACCEPTED = "MQTT_UPDATE_ACCEPTED";//收到设备的更新包
    public static final String MQTT_UPDATE_ACCEPTED_DATA_JSON = "MQTT_UPDATE_ACCEPTED_DATA";//数据参数
    public static final String MQTT_GET_ACCEPTED = "MQTT_GET_ACCEPTED";//收到设备的初始化包
    public static final String MQTT_GET_ACCEPTED_DATA_JSON = "MQTT_GET_ACCEPTED_DATA";//数据参数
    public static final String MQTT_GET_REJECTED = "MQTT_GET_REJECTED";//收到设备的初始化失败包 ,会将错误信息解析出来
    public static final String MQTT_GET_REJECTED_DATA_JSON = "MQTT_GET_REJECTED_DATA";//数据参数

    public static final String WEATHER_GET = "WEATHER_GET";//天气 请求刷新
    public static final String WEATHER_GET_RESULT = "WEATHER_GET_RESULT";//天气 获取结果

    //设置广播
    public static final String SET_BROADCAST_CHANGEONGOING = "znwx.app.set.changeongoing";//设置广播修改是否显示后台运行图标

    //微信支付回调广播
    public static final String WXPAY_ONRESP = "znwx.app.wxpay.onreqsp";//收到微信支付非成功的回调
    public static final String WXPAY_SUCCESS = "znwx.app.wxpay.success";//收到微信支付成功的回调


    //控制指令返回
    public static final String RP_CONTROL = "znwx.app.command.rp_control";//控制指令返回
    //配对提示返回信息
    public static final String RP_PDBYHAND = "znwx.app.command_rp_pdByHand";  //手动配对指令返回
    public static final String RP_PDBYAUTO = "znwx.app.command_rp_pdByAuto";  //自动配对指令返回
    //配对提示返回信息 这都是配对通用的。
    public static final String PEIDUI_ACTIONS = "znwx.insert.peidui.actions";  //配对返回

    public static final String PEIDUI_FAILED = "znwx.insert.peidui.failed";  //配对失败
    public static final String PEIDUI_FAILED_TIMEOUT = "znwx.insert.peidui.failed.timeout";  //配对失败 超时
    public static final String PEIDUI_MODEN_EXIT = "znwx.insert.peidui.exit.peiduimoden";  //退出配对模式失败

    //配对返回广播
    public static final String STUDY_ACTIONS = "znwx.insert.study.actions"; //学习返回


    public static final String FINISH_YK_EXIT = "yk.finish.activity"; //学习返回

    //主机搜索提示广播
    public static final String SEARCH_ZHUJI_RESPONSE = "znwx.search.zhuji.response";  //搜索主机回应广播
    //触发设置设备正常状态广播
    public static final String DEVICE_STATUS_NORMAL = "znwx.device.status.normal";  //触发设置设备为正常状态广播
    //工厂模式反馈
    public static final String ZHUJI_FACTORY = "znwx.zhuji.factory";  //工厂模式反馈，带上code指令表示进入和退出反馈
    //开发者透传
    public static final String DEVE_TC = "znwx.app.deve.tc";
    //接收到华为push
    String ACCETP_HUAWEIPUSH_MESSAGE = "huawei_push_message";
    //MIpush
    String ACCETP_MIPUSH_MESSAGE = "xiaomi_push_message";
    String ACCETP_JIGUANGPUSH_MESSAGE = "jiguang_push_message";

    String USER_LOGO="user_logo";

    //版本列表
    public interface VersionType {
        public static final String CHANNEL_UCTECH = "uctech";
        public static final String CHANNEL_ZNZK = "znzk";
        public static final String CHANNEL_JUJIANG = "jujiang";
        public static final String CHANNEL_LILESI = "lilesi";
        public static final String CHANNEL_JKD = "jkd";
        public static final String CHANNEL_ZHISHANG = "zhishang";
        public static final String CHANNEL_ZHZJ = "zhzj";
        public static final String CHANNEL_SHUNANJU = "shunanju";
        public static final String CHANNEL_HTZN = "htzn";
        public static final String CHANNEL_FSNY = "fsny";
        public static final String CHANNEL_DARUI = "daruianfang";
        public static final String CHANNEL_AIERFUDE = "aierfude";
        public static final String CHANNEL_WOAIJIA = "woaijia";
        public static final String CHANNEL_AIHUIER = "aihuier";
        public static final String CHANNEL_RUNLONG = "runlong";
        public static final String CHANNEL_CHUANGAN = "chuangan";
        public static final String CHANNEL_ZHILIDE = "zhilide";
        public static final String CHANNEL_QYJUNHONG = "qyjunhong";
        public static final String CHANNEL_SZJIAJIAAN = "szjiajiaan";
        public static final String CHANNEL_ANBABAOQUAN = "anbabaoquan";
        public static final String CHANNEL_DVIOT = "dviot";
        public static final String CHANNEL_HONGTAI = "hongtai";
        public static final String CHANNEL_UHOME = "uhome";
        public static final String CHANNEL_WANGDUODUO = "wangduoduo";
        public static final String CHANNEL_ZHICHENG = "zhicheng";
        public static final String CHANNEL_HZYCZN = "hzyczn";
        public static final String CHANNEL_HCTZ= "hctz";
        public static final String CHANNEL_WOFEE="wofea";
        public static final String CHANNEL_JAOLH="jaolih";
        public static final String CHANNEL_DITAIXING="ditaixing";
        public static final String CHANNEL_HUALI = "huali";
        public static final String CHANNEL_YIXUNGE = "yixunge";
        public static final String CHANNEL_MDS = "mds";
    }
}
