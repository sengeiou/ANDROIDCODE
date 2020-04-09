package com.smartism.znzk.widget;


import com.smartism.znzk.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class HeaderTextView extends TextView{
	private String text1;
	private String text2;
	private Paint mPaint;
	private Rect textRect=new Rect();
	
	public HeaderTextView(Context context,String text,String text2) {
		super(context);
		this.text1=text;
		this.text2=text2;
	}
	/*public HeaderTextView(Context context) {
		super(context);
		//initUI(context);
	}*/

	/*private void initUI(Context context) {
		View view=LayoutInflater.from(context).inflate(R.layout.header_view_contactfrag,null);
        
	}*/
	public HeaderTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		
		super.onLayout(changed, left, top, right, bottom);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		mPaint=getPaint();
		mPaint.setColor(Color.BLACK);
		float w=getMeasuredWidth();
		float h=getMeasuredHeight();
		float textWidth1=0;
		if(text1!=null&&text1.length()>0){
			textWidth1=getPaint().measureText(text1);
		}
		float textWidth2=0;
		if(text2!=null&&text2.length()>0){
			textWidth2=getPaint().measureText(text2);
		}
		mPaint.getTextBounds(text1, 0, text1.length(), textRect);
		int baseline1=(int) (h-70);
		canvas.drawText(text1, (w-textWidth1)/2, baseline1, getPaint());
		
		mPaint.getTextBounds(text2, 0, text2.length(), textRect);
		int baseline2=(int) (h-20);
		canvas.drawText(text2, (w-textWidth2)/2, baseline2, getPaint());
		super.onDraw(canvas);
	}

}
