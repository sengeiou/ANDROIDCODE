package com.smartism.znzk.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.smartism.znzk.R;
import com.smartism.znzk.activity.MainActivity;
import com.smartism.znzk.activity.device.DeviceMainActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.communication.service.CoreService;
import com.smartism.znzk.domain.MyNotificationInfo;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class NotificationUtil {
    public static String CHANNEL_ONGOING_ID = "c_o_id_ongoing";
    public static String CHANNEL_NOTIFICATION_ID = "c_n_id_default";
    public static String CHANNEL_MEDICATION_ID = "c_n_id_mediaction";//智能药箱通道id

    /**
     * 显示通知栏信息
     */
    @SuppressLint("InlinedApi")
    public static void showNotification(Context mContext, MyNotificationInfo nInfo) {

//        RemoteViews view = null;
//        if (isDarkNotificationBar(mContext)) {
//            view = new RemoteViews(nInfo.getContext().getPackageName(), R.layout.common_notification_white);
//        } else {
//            view = new RemoteViews(nInfo.getContext().getPackageName(), R.layout.common_notification_black);
//        }
//        view.setImageViewBitmap(R.id.noti_image, nInfo.getBigIcon());
//        view.setTextViewText(R.id.noti_title, nInfo.getContentTitle());
//        view.setTextViewText(R.id.noti_text, nInfo.getContentText());
          NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
          Intent mIntent = new Intent();
          //通过context是否是CoreService或者是否设置了device_id来区分通知是智慧主机或者普通通知
          if(nInfo.getDevice_id()!=0L){
              //跳转到DeviceMainActivity
              mIntent.setClass(mContext,DeviceMainActivity.class);
              mIntent.putExtra(DataCenterSharedPreferences.Constant.DEVICE_ID,nInfo.getDevice_id());
              mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
          }else{
              //点击智慧主机通知，启动应用
              mIntent.setAction(Intent.ACTION_MAIN);
              mIntent.addCategory(Intent.CATEGORY_LAUNCHER);
              mIntent.setClass(mContext,MainActivity.class);
          }
          PendingIntent mPendingIntent  = PendingIntent.getActivity(mContext,0,mIntent,PendingIntent.FLAG_UPDATE_CURRENT);


        /**
         * Oreo不用Priority了，用importance
         * IMPORTANCE_NONE 关闭通知
         * IMPORTANCE_MIN 开启通知，不会弹出，但没有提示音，状态栏中无显示
         * IMPORTANCE_LOW 开启通知，不会弹出，不发出提示音，状态栏中显示
         * IMPORTANCE_DEFAULT 开启通知，不会弹出，发出提示音，状态栏中显示
         * IMPORTANCE_HIGH 开启通知，会弹出，发出提示音，状态栏中显示
         * android 8.0新特性
         * channel已经在mainActivit中创建了，这里不创建也可以。
         */

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(nInfo.getContext());
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = null;
            //下面两组渠道在MainActivity中已经创建
            if (nInfo.isOngo()) {
                channel = new NotificationChannel(CHANNEL_ONGOING_ID, mContext.getString(R.string.notification_ongoing), NotificationManager.IMPORTANCE_LOW);
                channel.setShowBadge(false); //是否在久按桌面图标时显示此渠道的通知
                channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);//设置在锁屏界面上不显示这条通知 好像没效果
            } else {
                if(nInfo.getSpecial()==13){
                    channel = new NotificationChannel(CHANNEL_MEDICATION_ID,mContext.getString(R.string.notification_default),NotificationManager.IMPORTANCE_HIGH);
                    channel.setSound(Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.medicationtime),null);
                }else {
                    channel = new NotificationChannel(CHANNEL_NOTIFICATION_ID, mContext.getString(R.string.notification_default), NotificationManager.IMPORTANCE_HIGH);
                }
                channel.enableLights(true);////是否在桌面icon右上角展示小红点
                channel.setLightColor(Color.RED); //小红点颜色
                channel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知
                channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);//设置在锁屏界面上显示这条通知
            }
            mNotificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId(channel.getId());
        }
//        mBuilder.setContent(view);
        mBuilder.setSmallIcon(nInfo.getIcon())
                .setLargeIcon(nInfo.getBigIcon())
                .setContentTitle(nInfo.getContentTitle())//设置通知栏标题
                .setContentText(nInfo.getContentText()) //设置通知栏显示内容
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)// 设置显示通知栏到锁屏页面
                .setContentIntent(mPendingIntent)
                // .setNumber(number)//显示数量
//                 .setTicker("测试内容")//通知首次出现在通知栏，带上升动画效果的
                .setWhen(System.currentTimeMillis())// 通知产生的时间，会在通知信息里显示
                .setPriority(Notification.FLAG_ONGOING_EVENT)// 设置该通知优先级
                // .setAutoCancel(true)//设置这个标志当用户单击面板就可以让通知将自动取消
                .setOngoing(nInfo.isOngo());// ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
        // .setDefaults(Notification.DEFAULT_ALL)//向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合：
        // Notification.DEFAULT_ALL Notification.DEFAULT_SOUND 添加声音 // requires
        // VIBRATE permission
        // .set(R.drawable.ic_newmessage);
        //创建大文本样式
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(nInfo.getContentTitle())
//                .setSummaryText("哈哈哈哈哈")
                .bigText(nInfo.getContentText());

        mBuilder.setStyle(bigTextStyle); //设置大文本样式
        if (!nInfo.isOngo()) {
            mBuilder.setAutoCancel(true).setPriority(Notification.PRIORITY_HIGH).setNumber(nInfo.getNr());// 显示数量
        }
        if (nInfo.getTicker() != null) {
            mBuilder.setTicker(nInfo.getTicker());
        }
        if (!nInfo.isSilence()) {
            if(MainApplication.app.getAppGlobalConfig().getVersion().equals(Actions.VersionType.CHANNEL_HTZN)) {
                mBuilder.setSound(Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.message));
            }else if(MainApplication.app.getAppGlobalConfig().getVersion().equals(Actions.VersionType.CHANNEL_ZHZJ)){
                if(nInfo.getSpecial()==13){
                    mBuilder.setSound(Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.medicationtime));
                }else{
                    mBuilder.setDefaults(Notification.DEFAULT_SOUND);
                }
            }else{
                mBuilder.setDefaults(Notification.DEFAULT_SOUND);
            }
        }
        Notification mNotification = mBuilder.build();
        if (nInfo.isVibrator()) {
            Vibrator vibrator = (Vibrator) nInfo.getContext().getSystemService(Service.VIBRATOR_SERVICE);
            vibrator.vibrate(1000);
        }
        if (StringUtils.isEmpty(nInfo.getTicker())) {
            mNotification.tickerText = nInfo.getTicker();
        }

        if (!nInfo.isOngo()) {
            try { // 小米MIUI6.0以及以上用此显示角标
                Field field = mNotification.getClass().getDeclaredField("extraNotification");
                Object extraNotification = field.get(mNotification);
                Method method = extraNotification.getClass().getDeclaredMethod("setMessageCount", int.class);
                method.invoke(extraNotification, nInfo.getNr());
            } catch (Exception e) {
                Log.w("NotificationUtil", "showNotification: 非小米手机或者非MIUI6.0以上，设置角标失败，此处只设置MINI6.0以上系统");
            }
            mNotificationManager.notify(nInfo.getId(), mNotification);
        } else {
            if (mContext instanceof CoreService) {
                ((CoreService) mContext).startForeground(nInfo.getId(), mNotification);
            }
        }
    }

    /**
     * 去掉通知栏信息 id 为 0 时 清掉所有通知
     */
    public static void cancelNotification(Context context, int id) {
        NotificationManager mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (id == 0) {
            mNotificationManager.cancelAll();
        } else {
            mNotificationManager.cancel(id);
        }
    }

    /**
     * 显示进行中...
     *
     * @param mContext
     */
    public static void showOngoingTips(Context mContext) {
//        MyNotificationInfo info = new MyNotificationInfo();
//        info.setBigIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher));
//        info.setContentTitle(
//                mContext.getString(R.string.app_name) + " " + mContext.getString(R.string.coreservice_isrunning));
//        info.setIcon(R.drawable.ic_launcher_small);
//        if (Actions.VersionType.CHANNEL_ANBABAOQUAN.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
//            info.setContentText(mContext.getString(R.string.coreservice_runningslogan_abbq));
//        }else{
//            info.setContentText(mContext.getString(R.string.coreservice_runningslogan));
//        }
//        info.setId(Constant.NOTIFICATIONID_ONGO);
//        info.setContext(mContext);
//        info.setOngo(true);
//        info.setSilence(true);
//        info.setVibrator(false); //静默 不震动
//        if (DataCenterSharedPreferences.getInstance(mContext, Constant.CONFIG).getBoolean(Constant.SHOW_ONGOING,
//                true)) {
//            //暂时屏蔽进行中通知
//            NotificationUtil.showNotification(mContext, info);
//        }
    }

    /**
     * 显示消息
     *
     * @param mContext
     */
    public static void showNotOngoingTips(Context mContext, MyNotificationInfo info) {
        info.setIcon(R.drawable.ic_newmessage);
        info.setOngo(false);
        info.setSilence(false);
        info.setTicker(info.getContentText());
        NotificationUtil.showNotification(mContext, info);
    }

    // 取消系统默认铃声，自定义铃声
    public static void showDefineBellTip(Context mContext, MyNotificationInfo info, long devId) {

        info.setDevice_id(devId);
        info.setIcon(R.drawable.ic_newmessage);
        info.setOngo(false);
        info.setSilence(Ring(mContext, devId));
        info.setTicker(info.getContentText());
        NotificationUtil.showNotification(mContext, info);
    }


    public static boolean Ring(Context context, long devId) {
        // 全局路径
        SharedPreferences sp = context.getSharedPreferences(Constant.SP_NAME, Context.MODE_MULTI_PROCESS);
        String default_path = sp.getString(Constant.PATH_NOTIFICATION, "");
        // 单个设备路径
        String ID = "" + devId;
        String devIdpath = sp.getString(ID + Constant.PATH_NOTIFICATION, "");
        MediaPlayer player = new MediaPlayer();

        String play_path = null;

        // 如果都没有设置
        if (!TextUtils.isEmpty(devIdpath)) {
            play_path = devIdpath;
        } else if (!TextUtils.isEmpty(default_path)) {
            play_path = default_path;
        }
        try {
            if (!TextUtils.isEmpty(play_path)) {
                player.setDataSource(play_path);
                player.prepare();
                player.start();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isDarkNotificationBar(Context context) {
        return !isColorSimilar(Color.BLACK, getNotivicationColor(context));
    }

    /**
     * 获取通知栏颜色,需要兼容5.0以上
     *
     * @param context
     * @return
     */
    private static int getNotivicationColor(Context context) {
        if (context instanceof AppCompatActivity) {
            return getNotivicationColorCompat(context);
        } else {
            return getNotivicationColorInternal(context);
        }
    }

    private static int getNotivicationColorCompat(Context context) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        Notification notification = mBuilder.build();
        int layoutId = notification.contentView.getLayoutId();
        ViewGroup notificationRoot = (ViewGroup) LayoutInflater.from(context).inflate(layoutId, null);
        TextView title = (TextView) notificationRoot.findViewById(android.R.id.title);
        if (title == null) { //如果厂商修改了title的ID
            final List<TextView> textViews = new ArrayList<>();
            iteratorView(notificationRoot, new Filter() {
                @Override
                public void filter(View view) {
                    if (view instanceof TextView) {
                        textViews.add((TextView) view);
                    }
                }
            });

            float minTextSize = 0;
            int index = 0;
            for (int i = 0; i < textViews.size(); i++) {
                float currentSize = textViews.get(i).getTextSize();
                if (currentSize > minTextSize) {
                    minTextSize = currentSize;
                    index = i;
                }
            }
            return textViews.get(index).getCurrentTextColor();
        } else {
            return title.getCurrentTextColor();
        }
    }

    private static int titleColor;

    private static int getNotivicationColorInternal(Context context) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setContentTitle("DUMMY_TITLE");
        Notification notification = mBuilder.build();
        int layoutId = notification.contentView.getLayoutId();
        ViewGroup notificationRoot = (ViewGroup) LayoutInflater.from(context).inflate(layoutId, null);
//		ViewGroup notificationRoot = (ViewGroup) notification.contentView.apply(context, null);
        TextView title = (TextView) notificationRoot.findViewById(android.R.id.title);
        if (title == null) { //如果厂商修改了title的ID
            iteratorView(notificationRoot, new Filter() {
                @Override
                public void filter(View view) {
                    if (view instanceof TextView) {
                        TextView textView = (TextView) view;
                        if ("DUMMY_TITLE".equals(textView.getText().toString())) {
                            titleColor = textView.getCurrentTextColor();
                        }
                    }
                }
            });
            return titleColor;
        } else {
            return title.getCurrentTextColor();
        }
    }

    private static void iteratorView(View view, Filter filter) {
        if (view == null || filter == null) {
            return;
        }
        filter.filter(view);
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0, j = viewGroup.getChildCount(); i < j; i++) {
                View child = viewGroup.getChildAt(i);
                iteratorView(child, filter);
            }
        }
    }

    private interface Filter {
        void filter(View view);
    }


    public static boolean isColorSimilar(int baseColor, int color) {
        int simpleBaseColor = baseColor | 0xff000000;
        int simpleColor = color | 0xff000000;
        int baseRed = Color.red(simpleBaseColor) - Color.red(simpleColor);
        int baseGreen = Color.green(simpleBaseColor) - Color.green(simpleColor);
        int baseBlue = Color.blue(simpleBaseColor) - Color.blue(simpleColor);
        double value = Math.sqrt(baseRed * baseRed + baseBlue * baseBlue + baseGreen * baseGreen);
        if (value < 180.0) {
            return true;
        }
        return false;
    }

    //设置智能药箱通知铃声
    public static void setSmartMedicineSound(MyNotificationInfo info , int special){
        if(info==null){
            return ;
        }

        if(special==13){
            //吃药提醒
            info.setSpecial(13);
            info.setSilence(false);
        }
    }

    //适配Android8.0 ，判断某一个id的通知渠道是否有处于活跃状体的通知,也就是说通知渠道与通知是一对多的关系,用户通过通知渠道控制通知的属性，是否震动、闪光等
    @TargetApi(Build.VERSION_CODES.O)
    private static boolean deleteNotificationChannelNotActive(NotificationManager notificationManager,String channelId){
        //获取所有的通道数
        List<NotificationChannel>  ncList = notificationManager.getNotificationChannels() ;
        if(ncList==null){
            return true;
        }
        for(NotificationChannel channel : ncList){
            if(channel==null){
                continue;
            }
            if(channelId.equals(channel.getId())){
                //判断是否有活跃的通知显示
                int num = getNotificationForChannelId(notificationManager,channelId);
                if(num==0){
                   // notificationManager.deleteNotificationChannel(channelId);
                    return true ;
                }
            }
        }

        return false ;
    }


    //适配Android8.0，获取某一个渠道的上处于活跃状态的通知数
    @TargetApi(Build.VERSION_CODES.O)
    private static int getNotificationForChannelId(NotificationManager notificationManager,String queryChannelId){
        if(notificationManager==null||TextUtils.isEmpty(queryChannelId)){
            return -1  ;
        }
        int numbers =  0 ;
        StatusBarNotification[] temp = notificationManager.getActiveNotifications() ;
        for(StatusBarNotification item:temp){
            Notification notification = item.getNotification() ;
            if(notification!=null&&notification.getChannelId().equals(queryChannelId)){
                numbers++;
            }
        }
        return numbers ;
    }
}
