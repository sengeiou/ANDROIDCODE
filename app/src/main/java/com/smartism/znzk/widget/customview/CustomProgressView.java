package com.smartism.znzk.widget.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import com.smartism.znzk.R;

/*
*这个进度条两种外观，以5.0为界
*  author :墨子
*  2018 11 10 16点18分
*扩展性极差
* */
public  class CustomProgressView extends  View{

    Paint mBGPaint,mContentPaint ;
    float mStrokeWidth,mRadiu;
    int width ,height ;
    int mContentColor  ;
    int mInitAngle  = 0 ; //旋转的初始角度
    int sweepAngle = 60;
    float left,right,top,bottom ;
    int mBackgroundColor ;

    public CustomProgressView(Context context) {
        super(context);
        init(context,null);
    }

    public CustomProgressView(Context context,AttributeSet attributeSet){
        super(context,attributeSet);
        init(context,attributeSet);
    }

    void init(Context context,AttributeSet set){
        if(set!=null){
            TypedArray array = context.obtainStyledAttributes(set,R.styleable.CustomProgressView);
            mBackgroundColor = array.getColor(R.styleable.CustomProgressView_cpv_background, Color.parseColor("#7f000000"));
            array.recycle();
        }
        mStrokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,2,context.getResources().getDisplayMetrics());
        mRadiu = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,12,context.getResources().getDisplayMetrics());
        mContentColor = context.getResources().getColor(R.color.zhzj_default);//默认颜色
        //背景初始化，半透明背景
        setBackgroundDrawable(new ColorDrawable(mBackgroundColor));
        //进行事件拦截，当然有很多种方式都可以实现事件处理功能
        setClickable(true);
        //设置宽高
        ViewGroup.LayoutParams  lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        setLayoutParams(lp);

        mBGPaint = new Paint();
        mBGPaint.setAntiAlias(true);
        mBGPaint.setDither(true);
        mBGPaint.setColor(context.getResources().getColor(R.color.device_main_bg));
        mBGPaint.setStyle(Paint.Style.STROKE);
        mBGPaint.setStrokeWidth(mStrokeWidth);

        mContentPaint = new Paint();
        mContentPaint.setAntiAlias(true);
        mContentPaint.setDither(true);
        mContentPaint.setColor(mContentColor);
        mContentPaint.setStyle(Paint.Style.STROKE);
        mContentPaint.setStrokeWidth(mStrokeWidth);


    }


    public void setProgressColor(int color){
        mContentColor = color ;
        postInvalidate();
    }

    boolean daoTui ;
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制背景
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            canvas.drawCircle(width/2,height/2,mRadiu,mBGPaint);
            canvas.drawArc(left,top,right,bottom,mInitAngle,sweepAngle,false,mContentPaint);
            if(sweepAngle>=120){
                daoTui = true ;
            }else if(sweepAngle<=30){
                daoTui = false ;
            }
            if(!daoTui){
                sweepAngle++;
            }else{
                sweepAngle--;
            }
        }else{
            canvas.drawCircle(width/2,height/2,mRadiu,mBGPaint);
            canvas.rotate(mInitAngle,width/2,height);
            canvas.restore();
        }

        if(mInitAngle>=360){
            mInitAngle =0 ;
        }
        mInitAngle+=7;
        invalidate();
    }

    //适应内容大小50dp
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if(heightMode==MeasureSpec.AT_MOST&&widthMode ==MeasureSpec.AT_MOST){
            width = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,50f,getResources().getDisplayMetrics())+0.5f);
            height = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,50f,getResources().getDisplayMetrics())+0.5f);
        }else if(heightMode==MeasureSpec.AT_MOST){
            height = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,50f,getResources().getDisplayMetrics())+0.5f);
        }else if(widthMode==MeasureSpec.AT_MOST){
            width = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,50f,getResources().getDisplayMetrics())+0.5f);
        }
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(width,widthMode);
        heightMeasureSpec  = MeasureSpec.makeMeasureSpec(height,heightMode);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h ;
        left = width/2-mRadiu ;
        top = height/2-mRadiu ;
        right = width/2+mRadiu ;
        bottom = height/2+mRadiu;
        //由于SweepGradient用到宽高，所以在这里进行创建
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.LOLLIPOP){
            int[] colors  = new int[]{getResources().getColor(R.color.colorPurplelitle),getResources().getColor(R.color.colorPurplelight),
                    getResources().getColor(R.color.mediumpurple)} ;
            Shader gradient = new SweepGradient(width/2,height/2,colors,null);
            mBGPaint.setShader(gradient);
        }else{
            mBGPaint.setColor(getResources().getColor(R.color.graysloae));
        }
    }
}
