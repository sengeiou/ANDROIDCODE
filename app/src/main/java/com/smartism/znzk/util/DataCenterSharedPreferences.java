package com.smartism.znzk.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class DataCenterSharedPreferences {
    private SharedPreferences sp;

    private DataCenterSharedPreferences(Context context, String spName) {
        sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
    }

    private DataCenterSharedPreferences(Context context, String spName, int mode) {
        sp = context.getSharedPreferences(spName, mode);
    }

    public synchronized static DataCenterSharedPreferences getInstance(Context context, String spName) {
        return new DataCenterSharedPreferences(context.getApplicationContext(), spName);
    }

    public Editor getEditor() {
        return sp.edit();
    }

    public Editor putString(String key, String value) {
        return getEditor().putString(key, value);
    }
    public void putStringAndCommit(String key, String value) {
        getEditor().putString(key, value).commit();
    }

    public Editor putBoolean(String key, boolean b) {
        return getEditor().putBoolean(key, b);
    }

    public Editor putLong(String key, long value) {
        return getEditor().putLong(key, value);
    }

    public Editor putInt(String key, int value) {
        return getEditor().putInt(key, value);
    }

    public String getString(String key, String defValue) {
        return sp.getString(key, defValue);
    }

    public boolean getBoolean(String key, boolean defValue) {
        return sp.getBoolean(key, defValue);
    }

    public long getLong(String key, long defValue) {
        return sp.getLong(key, defValue);
    }

    public int getInt(String key, int defValue) {
        return sp.getInt(key, defValue);
    }


    public Editor remove(String key) {
        return getEditor().remove(key);
    }

    public static class Constant {
        // 配置文件名称
        public static final String CONFIG = "config";
        public static final String WEATHER_INFO = "weather_info";

        //雄迈相关
        public static final String XM_CONFIG = "xiongmai_config"; //雄迈摄像头xm文件名
        public static  final String ALARM_PUSH_STATUS = "subscription_message" ; //是否订阅了报警消息,前面需要加上摄像头序列号，整体作为标识
        public static final String SECURITY_SETTING_PWD = "camera_pwd"; //摄像头密码


        // shareprefence在activity写入的数据，工具类在service中取不出数据（Mode权限不够）单独建一个
        public static final String SP_NAME = "bell_name";
        // 通知栏通知ID
        public static final int NOTIFICATIONID_ONGO = 998888;
        public static final int NOTIFICATIONID = 998889;
        // session消息通道读取空闲时长
        public static final String READER_IDLE = "READER_IDLE";
        // 当前系统的版本号
        public static final String APP_VERSIONCODE = "version_code";
        // 配置文件中存储的服务器地址
        public static final String SYNC_DATA_SERVERS = "sync_data_services";
        // 配置文件中存储的服务器web请求地址
        public static final String HTTP_DATA_SERVERS = "http_data_services";
        // 首次打开
        public static final String IS_FIRSTSTART = "is_firststart";
        // 是否登录
        public static final String IS_LOGIN = "is_login";
        public static final String LOGIN_ACCOUNT = "login_account";
        // 存放boolean值，判断保存密码按钮是否选中
        public static final String LOGIN_PWD_REMEMBER = "login_remember_pwd";
        // 存放string值，保存密码存放的密码值
        public static final String LOGIN_PWD_ORIGINAL = "login_pwd_original";
        // 存放string值，保存输入的账号
        public static final String LOGIN_ACCOUNT_ORIGINAL = "login_account_original";
        // 密码 md5 加密后的值
        public static final String LOGIN_PWD = "login_pwd";
        public static final String LOGIN_APPID = "login_appid";
        public static final String LOGIN_APPNAME = "login_appname";
        public static final String LOGIN_ROLE = "login_role";
        public static final String LOGIN_LOGO = "login_logo";
        public static final String LOGIN_LOGO_LOCAL = "login_logo_local";
        public static final String LOGIN_CODE = "login_code";
        public static final String ACCOUNT = "account";

        //短信验证码验证登录 0、是正常登录， 1、是验证登录
        public static final String LOGIN_PHONESMS = "login_phonesms";

        //记录获取验证码时候的时间戳
        public static final String CODE_START_TIME = "code_start_time";

        /**
         * 当前登录账号选择查看的主机masterID
         */
        public static final String APP_MASTERID = "app_masterid";
//		public static final String APP_NEW_ADDMASTERID = "app_new_add_masterid";

        public static final String SHOWNOTIFICATION_PREFIX = "shownotification_prefix";
        // 设置选项 - 是否显示主机
        public static final String SHOW_ZHUJI = "show_zhuji";
        // 设置选项 - 是否显示图标
        public static final String SHOW_ONGOING = "show_ongoing";
        // 设置选项 - 温度显示单位
        public static final String SHOW_TEMPERATURE_UNIT = "show_temperature_unit";
        // 设置选项 - 设备列表排序方式
        public static final String SHOW_DLISTSORT = "show_dlistsort";
        //体重单位
        public static final String WEIGHT_UNIT = "WEIGHT_UNIT";
        public static final String WEIGHT_UNIT_KG = "gJin";
        public static final String WEIGHT_UNIT_LB = "bang";
        //成员ID
        public static final String USER_ID = "user_id";
        public static final String KEY_TCP = "znwx1234SERV4567";
        public static final String KEY_HTTP = "jjm_2345SEV_9857";

        // caputre扫描请求码
        public static final String CAPUTRE_REQUESTCOE = "caputre_requestcoe";
        public static final int CAPUTRE_ADDDEVICE = 1;
        public static final int CAPUTRE_ADDZHUJI = 2;
        public static final int CAPUTRE_ADDRESULT = 3344;

        // 用户角色。普通用户和公司管理员 服务点管理员  服务人员
        public static final String ROLE_NORMAL = "app_role_normal";
        public static final String ROLE_USERADMIN = "app_role_useradmin";
        public static final String ROLE_SUPERADMIN = "app_role_superadmin";
        public static final String ROLE_ASADMIN = "app_role_asadmin";
        public static final String ROLE_ASSERVICE = "app_role_asservice";

        // 当前场景
        public static final String SCENE_NOW_CF = "0"; // 撤防
        public static final String SCENE_NOW_SF = "-1"; // 设防 -2为取消场景
        public static final String SCENE_NOW_HOME = "-3"; // 在家
        public static String scene;

        // tcp连接超时时间
        public static final int CONN_TIMEOUT = 5000;

        // 按钮操作效果
        public static final String BTN_CONTROLSTYLE = "btn_control_style";

        // 当前程序是否设置九宫格锁
        public static final String IS_APP_GENSTURE = "is_app_gensture";

        // 是否接收主机上线下线通知
        public static final String IS_SERVER_STATUS_NOTIFY = "is_server_status_notify";

        // 手势密码以字符串形式保存
        public static final String CODE_GENSTURE = "code_gensture";

        // 判断activity是否需要加锁
        public static final String IS_LOOKS = "is_lock";

        // 巨将判断学习探头和遥控器是否打开
        public static final String IS_SUPORT_STU = "is_support_stu";

        // 选择的警告声音在列表中的序号
        public static final String ORDER_ALARM_SONG = "order_alarm_songs";

        // 选择的短信提示声音在列表中的序号
        public static final String ORDER_NOTIFICATION_SONG = "order_notification_songs";

        // 用户选择的全局警告声音的路径
        public static final String PATH_ALARM_SONG = "path_alarm_song";

        // 用户选择的全局短信提示声音的路径
        public static final String PATH_NOTIFICATION = "path_notification_song";

        // 强力模式alarm警告音
        public static final String TYPE_RING_ALARM = "ring_alarm";

        // 短信模式notification提示音
        public static final String TYPE_RING_NOTIFICATION = "ring_notification";

        //手势密码输错次数
        public static final String COUNT_ERRORLOCKPATTERN = "count_errorlockpattern";

        //输错手势密码倒计时开始时间点
        public static final String DATE_OF_LOCKPATTERNENTERERROR = "date_of_lockpatternentererror";

        //遥控器空调指令库
        public static final String YAOKAN_CODE_LIB = "yaokan_code_lib";

        //风扇指令库
//		public static final String YAOKAN_FAN_CODE_LIB="yaokan_fan_code_lib";
//
//		public static final String YAOKAN_TV_CODE_LIB="yaokan_tv_code_lib";
//
//		public static final String YAOKAN_TVBOX_CODE_LIB="yaokan_tvbox_code_lib";

        //保存温度
        public static final String YAOKAN_LOCAL_TEMPRATURE = "yaokan_local_temprature";

        //保存启动app时间
        public static final String START_APP_TIME = "start_app_time";

        //保存是否设置了报警中心号码
        public static final String ALARM_CENTER_ADD = "alarm_center_add";

        //当前应用的版本，用于实现报警号码是否更新
        public static final String ALARM_VERSIONCODE  = "alarm_versioncode";


        //扫码添加
        public static final String SCAN_CONTEXT = "ju_jiang_x-house";

        //保存启动app时间
        public static final String START_APP_FIRST = "start_app_first";

        //启动首页传入DID的参数
        public static final String DEVICE_ID = "did";

        //单主机跳转
        public static final String IS_TURN = "is_turn";


        public static final String NOTICE_ADD_MOBILE="notice_add_mobile";//提醒添加手机号，7天一次

        public static final String LOCALE_GCODE="locale_gcode";//本地手机国家代码

    }
}
