package com.smartism.znzk.xiongmai.widget;

import android.content.Context;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.os.Handler;

import com.lib.EPTZCMD;
import com.smartism.znzk.xiongmai.lib.funsdk.support.FunSupport;
import com.smartism.znzk.xiongmai.lib.funsdk.support.models.FunDevice;
import com.smartism.znzk.xiongmai.lib.funsdk.support.widget.FunVideoView;

/*
* 雄迈摄像头转动布局，包含了播放视频控件
* 2018/10/11/16点49分
* */
public class XMFramLayout extends FrameLayout{

    private  final int LEFT=19,RIGHT=95,UP=12,DOWM=21;//标识是左滑还是右滑上滑啥的
    final String TAG  = XMFramLayout.class.getSimpleName();
    public FunVideoView getmFunVideoView() {
        return mFunVideoView;
    }
    FunVideoView mFunVideoView ;
    FunDevice mFunDevice ;
    Handler outHandler ; //解决点击事件时的一些处理
    //控制转动咱自己实现
    Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                //魔法数字
                case 1:
                    mHandler.removeMessages(1);
                    //如果没有在播放
                    if(!mFunVideoView.isPlaying()){
                        break;
                    }
                    //停止摄像头转动,不太好
                    if(msg.arg1==LEFT){
                        FunSupport.getInstance().requestDevicePTZControl(mFunDevice, EPTZCMD.PAN_LEFT,true,mFunDevice.CurrChannel);
                    }else if(msg.arg1==RIGHT){
                        FunSupport.getInstance().requestDevicePTZControl(mFunDevice, EPTZCMD.PAN_RIGHT,true,mFunDevice.CurrChannel);
                    }else if(msg.arg1==UP){
                        FunSupport.getInstance().requestDevicePTZControl(mFunDevice, EPTZCMD.TILT_UP,true,mFunDevice.CurrChannel);
                    }else if(msg.arg1 == DOWM){
                        FunSupport.getInstance().requestDevicePTZControl(mFunDevice, EPTZCMD.TILT_DOWN,true,mFunDevice.CurrChannel);
                    }
                    break;
            }
            return true;
        }
    };
    Handler mHandler=new Handler(mCallback);
    public XMFramLayout(@NonNull Context context,@NonNull FunDevice funDevice,@NonNull Handler outHandler) {
        super(context);
        this.outHandler = outHandler ;
        mFunDevice = funDevice ;
        initView();
    }

    private void initView(){
        mFunVideoView =new FunVideoView(getContext());
        mFunVideoView.setLayoutParams(generateDefaultLayoutParams());
        setLayoutParams(generateDefaultLayoutParams());
        addView(mFunVideoView);
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    int touchSlop ; //默认的最小滑动距离
    float downX,downY,upX,upY;//保存按下和抬起的坐标
    long lastTime = 0L;//计时用的

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                downX = ev.getX();
                downY = ev.getY();
                lastTime = System.currentTimeMillis();
                Log.v(TAG,"DOWN事件");
                break;
            case MotionEvent.ACTION_UP:
                Log.v(TAG,"UP事件");
                upX = ev.getX();
                upY = ev.getY();
                long currentTime =0L;
                currentTime = System.currentTimeMillis();
                long result = currentTime - lastTime;
                Log.v(TAG,"currentTime-lastTime:"+result);
                //抬起的时间和按下的时间小于500毫秒时，转动摄像头
                if(result<500) {
                    Message message = Message.obtain();
                    message.what = 1;
                    //大于默认的最小滑动距离时，进行摄像头转动
                    if ((upX - downX > touchSlop) || (upY - downY > touchSlop)) {
                        if (upY > downY) {
                            //大致下滑
                            double angleDown = Math.abs(Math.atan((upX - downX) / (upY - downY)));
                            if (angleDown > 4 / Math.PI) {
                                if (upX > downX) {
                                    Log.v(TAG, "右下滑");
                                    FunSupport.getInstance().requestDevicePTZControl(mFunDevice, EPTZCMD.PAN_RIGHT, false, mFunDevice.CurrChannel);
                                    message.arg1 = RIGHT;
                                } else {
                                    Log.v(TAG, "左下滑");
                                    FunSupport.getInstance().requestDevicePTZControl(mFunDevice, EPTZCMD.PAN_LEFT, false, mFunDevice.CurrChannel);
                                    message.arg1 = LEFT;
                                }
                            } else {
                                Log.v(TAG, "正宗下滑");
                                FunSupport.getInstance().requestDevicePTZControl(mFunDevice, EPTZCMD.TILT_DOWN, false, mFunDevice.CurrChannel);
                                message.arg1 = DOWM;
                            }
                        } else {
                            //大致上滑
                            double angleUp = Math.abs(Math.atan((upY - downY) / (upX - downX)));
                            if (angleUp < 4 / Math.PI) {
                                //右上滑或者左上滑
                                if (upX > downX) {
                                    //右上滑
                                    Log.v(TAG, "右上滑");
                                    message.arg1 = RIGHT;
                                    FunSupport.getInstance().requestDevicePTZControl(mFunDevice, EPTZCMD.PAN_RIGHT, false, mFunDevice.CurrChannel);
                                } else {
                                    //左上滑
                                    Log.v(TAG, "左上滑");
                                    FunSupport.getInstance().requestDevicePTZControl(mFunDevice, EPTZCMD.PAN_LEFT, false, mFunDevice.CurrChannel);
                                    message.arg1 = LEFT;
                                }
                            } else {
                                //正宗上滑
                                Log.v(TAG, "正宗上滑");
                                FunSupport.getInstance().requestDevicePTZControl(mFunDevice, EPTZCMD.TILT_UP, false, mFunDevice.CurrChannel);
                                message.arg1 = UP;
                            }
                        }
                        mHandler.sendMessageDelayed(message, 200);
                    } else {
                        //当作是点击,认为是点击事件
                        outHandler.sendEmptyMessage(99);
                    }
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler = null ;
    }
}
