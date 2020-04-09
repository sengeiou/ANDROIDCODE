package com.smartism.znzk.view.Speech;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.smartism.znzk.R;

import java.lang.reflect.Field;
import java.util.List;

public class MFloatingView extends View {
    private static final String TAG = "TableShowView";
    private OnFloatClickListent onFloatClickListent;

    public static final int STAT_READY_GENERAL = 1; // 表示已进入普通通话状态
    public static final int STAT_OPER_BUILD = 3; // 开始建立安全信道
    public static final int STAT_OPER_CHECK = 4; // 开始验证身份
    public static final int STAT_READY_SECURE = 5; // 表示进入加密通话状态
    public static final int STAT_OPER_DES = 6;

    private Context mContext;
    private WindowManager mWindowManager; // WindowManager
    private ActivityManager mActivityManager; // 根据当前Activity来处理控制界面的隐藏
    private List<ActivityManager.RunningTaskInfo> initTaskInfo;
    private OnCtrlViewTouchListener mCtrlViewTouchListener;
    private UpdateStatHander mUpdateStatusHandler; // 用于更新当前通话状态的Handler
    public View mCtrlView;
    public View mDetailView;

    long operDur; // 用于记录用户点击控件的时间，如果Touch控件的时间较短，就认为这Touch是一次点击

    /**
     * 记录系统状态栏的高度
     */
    private int statusBarHeight;
    /**
     * 记录当前手指位置在屏幕上的横坐标值
     */
    private float xInScreen;

    /**
     * 记录当前手指位置在屏幕上的纵坐标值
     */
    private float yInScreen;

    /**
     * 记录手指按下时在屏幕上的横坐标的值
     */
    private float xDownInScreen;

    /**
     * 记录手指按下时在屏幕上的纵坐标的值
     */
    private float yDownInScreen;

    /**
     * 记录手指按下时在小悬浮窗的View上的横坐标的值
     */
    private float xInView;
    /**
     * 记录手指按下时在小悬浮窗的View上的纵坐标的值
     */
    private float yInView;

    public boolean isShowFloating() {
        if (mDetailView != null) {
            return true;
        }
        return false;
    }

    public void setOnFloatClickListent(OnFloatClickListent onFloatClickListent) {
        this.onFloatClickListent = onFloatClickListent;
    }

    public MFloatingView(Context context) {
        super(context);
        mContext = context;
        mWindowManager = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);
        mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);

    }

    //悬浮窗的移动操作
    public void moveFloatingButton(float endX, float endY) {

        int mOldOffsetX = mLayoutParams.x; // 偏移量
        int mOldOffsetY = mLayoutParams.y; // 偏移量
        mLayoutParams.x += (int) endX; // 偏移量
        mLayoutParams.y += (int) endY; // 偏移量
        mWindowManager.updateViewLayout(mCtrlView, mLayoutParams);
    }

    public WindowManager.LayoutParams mLayoutParams; //悬浮框的状态管理
    public ImageView statImg;

    public void showFloatingBtn() {
        // 设置载入view WindowManager参数
        initTaskInfo = mActivityManager.getRunningTasks(1);
        mLayoutParams = showCtrlView(mWindowManager);
        mCtrlViewTouchListener = new OnCtrlViewTouchListener(mWindowManager, mLayoutParams);
        // mCtrlViewLayoutParams = showCtrlView(mWindowManager);
        mCtrlView.setOnTouchListener(mCtrlViewTouchListener);
        mUpdateStatusHandler = new UpdateStatHander();
        removeCtrlViewByTopActivityChag();
        statImg = (ImageView) mCtrlView.findViewById(R.id.img_stat);

    }

    public void setIconBackGround(int resouseId) {
        statImg.setBackgroundResource(resouseId);
        // 获取AnimationDrawable对象
        AnimationDrawable animationDrawable = (AnimationDrawable) statImg.getBackground();
        // 动画是否正在运行
        if (animationDrawable.isRunning()) {
            //停止动画播放
            animationDrawable.stop();
        } else {
            //开始或者继续动画播放
            animationDrawable.start();
        }
    }

    private class OnCtrlViewTouchListener implements OnTouchListener {
        private static final long MAX_MILLI_TREAT_AS_CLICK = 100;  //当用户触控控制按钮的时间小于该常量毫秒时，就算控制按钮的位置发生了变化，也认为这是一次点击事件

        private WindowManager mWindowManager;
        private WindowManager.LayoutParams mLayoutParams;
        // 触屏监听
        float mLastX, mLastY;

        int mOldOffsetX, mOldOffsetY;
        int mRecordFlag = 0; // 用于重新记录CtrlView位置的标志
        long mTouchDur;  //记录用户触控控制按钮的时间

        boolean hasShowedDetail = false;

        public OnCtrlViewTouchListener(WindowManager windowManager,
                                       WindowManager.LayoutParams layoutParams) {
            mWindowManager = windowManager;
            mLayoutParams = layoutParams;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // 手指按下时记录必要数据,纵坐标的值都需要减去状态栏高度
                    xInView = event.getX();
                    yInView = event.getY();
                    xDownInScreen = event.getRawX();
                    yDownInScreen = event.getRawY() - getStatusBarHeight();
                    xInScreen = event.getRawX();
                    yInScreen = event.getRawY() - getStatusBarHeight();
                    mTouchDur = System.currentTimeMillis();
                    break;
                case MotionEvent.ACTION_MOVE:
                    xInScreen = event.getRawX();
                    yInScreen = event.getRawY() - getStatusBarHeight();
                    // 手指移动的时候更新小悬浮窗的位置
                    updateViewPosition();
                    break;
                case MotionEvent.ACTION_UP:
                    // 如果手指离开屏幕时，xDownInScreen和xInScreen相等，且yDownInScreen和yInScreen相等，则视为触发了单击事件。
                    mTouchDur =  System.currentTimeMillis() - mTouchDur;
                    if (mTouchDur < MAX_MILLI_TREAT_AS_CLICK || (xDownInScreen == xInScreen && yDownInScreen == yInScreen)) {
//						openBigWindow();
                        onFloatClickListent.onFloatClick();
                    }else {
                        mRecordFlag = 0;
                    }
                    break;
                default:
                    break;
            }
            return true;
        }

        /**
         * 更新小悬浮窗在屏幕中的位置。
         */
        private void updateViewPosition() {
            mLayoutParams.x = (int) (xInScreen - xInView);
            mLayoutParams.y = (int) (yInScreen - yInView);
            mWindowManager.updateViewLayout(mCtrlView, mLayoutParams);
        }

        /**
         * 用于获取状态栏的高度。
         *
         * @return 返回状态栏高度的像素值。
         */
        private int getStatusBarHeight() {
            if (statusBarHeight == 0) {
                try {
                    Class<?> c = Class.forName("com.android.internal.R$dimen");
                    Object o = c.newInstance();
                    Field field = c.getField("status_bar_height");
                    int x = (Integer) field.get(o);
                    statusBarHeight = getResources().getDimensionPixelSize(x);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return statusBarHeight;
        }



        /**
         * 该方法用于向更新通话状态的Handler发送消息
         *
         * @param handler
         * @param status  所发送的状态信息
         * @param seconds 几秒后向handler发送消息
         */
        private void sendUpdateMsg(Handler handler, int status, int seconds) {
            final Message msg = Message.obtain(handler);
            msg.what = status;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    msg.sendToTarget();
                }
            }, seconds * 1000);
        }
    }

    /**
     * 用于显示控制按钮，该控制按钮可以移动。当点击一次之后会弹出另一个浮动界面，本例中是详情界面
     *
     * @param windowManager 用于控制通话状态标示出现的初始位置、大小以及属性
     * @return 会返回所创建的通话状态标示的WindowManager.LayoutParams型对象，该对象会在移动过程中修改。
     */
    private WindowManager.LayoutParams showCtrlView(WindowManager windowManager) {
        mCtrlView = LayoutInflater.from(mContext).inflate(R.layout.view_floatting_window,
                null);
        mCtrlView.setBackgroundColor(Color.TRANSPARENT);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.type = 2003; // type是关键，这里的2002表示系统级窗口，你也可以试试2003。
        layoutParams.flags = 40;// 这句设置桌面可控
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        layoutParams.x = 60;
        layoutParams.y = 80;
        layoutParams.width = 80;
        layoutParams.height = 80;
        layoutParams.format = -3; // 透明

        windowManager.addView(mCtrlView, layoutParams);// 这句是重点
        // 给WindowManager中丢入刚才设置的值
        // 只有addview后才能显示到页面上去。
        // 注册到WindowManager win是要刚才随便载入的layout，
        // wmParams是刚才设置的WindowManager参数集
        // 效果是将win注册到WindowManager中并且它的参数是wmParams中设置饿
        return layoutParams;
    }

    private class UpdateStatHander extends Handler {

        @Override
        public void handleMessage(Message msg) {

        }
    }

    /**
     * 该方法会在第5、10、15秒检测topActivity是否是发生了变化，如果发生了变化就移除浮动控制按钮
     */
    private void removeCtrlViewByTopActivityChag() {
        for (int i = 0; i < 5; i++) {

            mUpdateStatusHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    String currPackName = mActivityManager.getRunningTasks(1).get(0).topActivity.getPackageName(); //得到正运行的App包名
                    String initPackName = initTaskInfo.get(0).topActivity.getPackageName(); //得到显示浮动控制按钮时的App包名
                    Log.w(TAG, "oldPackageName: " + initPackName
                            + "  currPackageName: " + currPackName);
                    if (!currPackName.equals(initPackName)) {
                        if (mDetailView != null) {
                            mWindowManager.removeView(mDetailView);
                            mDetailView = null;
                        }
                        if (mCtrlView != null) {
                            mWindowManager.removeView(mCtrlView);
                            mCtrlView = null;
                        }
                    }
                }
            }, 3 * (i + 1) * 1000);
        }
    }

    public interface OnFloatClickListent {

        public void onFloatClick();
    }
}