package com.smartism.znzk.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.smartism.znzk.R;

/*
* 自定义的波浪控件
* author 墨子
* */
public class CustomWaveView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private SurfaceHolder mHolder ;
    private Paint frontPaint;
    private int mTotalWidth,mTotalHeight;
    private float AMPLITUDE = 10F ;//振幅
    private float INIT_POSITION  ; //初始位置
    private float[] pointY ;
    private int mOneLineoffset,mTwoLineoffset;
    private int mBackgroundColor ,mWaveColor ;

    private Thread mTaskThread ;
    private boolean isDraw  =false ;

    public CustomWaveView(Context context) {
        super(context,null);
    }



    public CustomWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttr(context,attrs);
        initSetting();

    }

    private  void initAttr(Context context,AttributeSet attributeSet){
        if(attributeSet!=null){
            TypedArray typedArray =context.obtainStyledAttributes(attributeSet,R.styleable.CustomWaveView);
            mBackgroundColor = typedArray.getColor(R.styleable.CustomWaveView_wave_backgroundColor,context.getResources().getColor(R.color.zhzj_default));//背景
            mWaveColor = typedArray.getColor(R.styleable.CustomWaveView_wave_waveColor,context.getResources().getColor(R.color.white));//波浪颜色
            typedArray.recycle();//回收资源
        }else{
            mWaveColor = context.getResources().getColor(R.color.graysloae);
            mBackgroundColor = context.getResources().getColor(R.color.device_main_bg);
        }
    }

    private void initSetting(){
        mHolder = getHolder() ;
        mHolder.addCallback(this);


        frontPaint  = new Paint();
        frontPaint.setColor(mWaveColor);
        frontPaint.setAntiAlias(true);
        frontPaint.setDither(true);
        frontPaint.setStyle(Paint.Style.STROKE);
    }

    //绘制方法
    private  void drawWave(Canvas canvas){
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        //绘制背景
        canvas.drawColor(mBackgroundColor);
        canvas.translate(-mTotalWidth,0);
        for(int i=0;i<pointY.length;i++){
            canvas.drawLine(i+mTwoLineoffset,pointY[i],i+mTwoLineoffset,mTotalHeight,frontPaint);
            canvas.drawLine(i+mOneLineoffset,pointY[i],i+mOneLineoffset,mTotalHeight,frontPaint);
        }
        mOneLineoffset +=10 ;
        mTwoLineoffset +=6;

        if(mOneLineoffset>=mTotalWidth){
            mOneLineoffset=0;
        }

        if(mTwoLineoffset>=mTotalWidth){
            mTwoLineoffset = 0 ;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mTaskThread = new Thread(this);
        isDraw = true ;
        mTaskThread.start();//开启绘制任务
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //获取View的宽高
        mTotalWidth = width ;
        mTotalHeight =  height ;
        INIT_POSITION = mTotalHeight - 2*AMPLITUDE ;

        pointY = new float[mTotalWidth*2];
        //初始化点
        for(int i=0;i<mTotalWidth*2;i++){
            pointY[i] = (float) (AMPLITUDE* Math.sin(i*2* Math.PI/(mTotalWidth/2))+INIT_POSITION);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isDraw = false ;
    }

    @Override
    public void run() {
        while(isDraw){
            /*
            * 注意，当Surface被别的线程占用，或者为创建，或者已经销毁时，返回为null，注意判空，尤其是当
            * Surface销毁时，绘制任务还没能即使停止，所以判空一下。
            * */
            Canvas canvas =  mHolder.lockCanvas();//获取画布
            if(canvas==null){
                continue;
            }
            drawWave(canvas);//绘制内容
            mHolder.unlockCanvasAndPost(canvas);
        }
    }
}
