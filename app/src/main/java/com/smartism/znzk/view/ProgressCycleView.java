package com.smartism.znzk.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.smartism.znzk.R;

public class ProgressCycleView extends View {

	Paint mPaint;
	int progress;
	int start_degree = -90;

	public ProgressCycleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		InitResources(context, attrs);
	}

	int background_color;
	int progress_color;
	float layout_width_float;
	int textColor_int;
	float textSize_float;
	int max;
	Drawable thumb_double;
	int thumbSize_int;

	private void InitResources(Context context, AttributeSet attrs) {
		TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.jdm);

		background_color = mTypedArray.getColor(R.styleable.jdm_backgroundjdm, 0xFF87cfe8);
		progress_color = mTypedArray.getColor(R.styleable.jdm_progressDrawable, 0xFFabd800);
		layout_width_float = mTypedArray.getDimension(R.styleable.jdm_jdmlayout_width, 7);
		textColor_int = mTypedArray.getColor(R.styleable.jdm_textColor, 0xFFada1de);
		textSize_float = mTypedArray.getDimension(R.styleable.jdm_textSize, 50);
		max = mTypedArray.getInt(R.styleable.jdm_max, 100);
		progress = mTypedArray.getInt(R.styleable.jdm_jdmprogress, 20);
		thumb_double = mTypedArray.getDrawable(R.styleable.jdm_thumb);
		thumbSize_int = mTypedArray.getInt(R.styleable.jdm_thumbSize, 30);
		mTypedArray.recycle();

		if (thumb_double == null) {
			Bitmap bitmap = Bitmap.createBitmap(thumbSize_int, thumbSize_int, Bitmap.Config.ARGB_8888);
			// 图片画片
			Canvas cas = new Canvas(bitmap);
			Paint paint = new Paint();
			paint.setStyle(Style.FILL);
			paint.setColor(0xFF68ba32);
			int center = thumbSize_int / 2;
			int radius = center - 4;
			cas.drawCircle(center, center, radius, paint);
			thumb_double = new BitmapDrawable(getResources(), bitmap);
		}

		mPaint = new Paint();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		drawProgressView(canvas);
	}

	private void drawProgressView(Canvas canvas) {
		InitOval(canvas);
		drawBackground(canvas);
		drawProgress(canvas);
		drawProgressText(canvas);
	}

	RectF oval;

	private void InitOval(Canvas canvas) {
		int center = getWidth() / 2;
		int radius = (int) (center  - thumbSize_int / 2);
		// 画布中央减去半径就是左上角
		int left_top = center - radius;
		// 画布中央加上半径就是右下角
		int right_bottom = center + radius;

		if (oval == null) {
			oval = new RectF(left_top, left_top, right_bottom, right_bottom);
		}
	}

	/**
	 * 绘制进度文字
	 * 
	 * @param canvas
	 */
	private void drawProgressText(Canvas canvas) {

		mPaint.setTextSize(textSize_float);
		mPaint.setColor(textColor_int);
		mPaint.setStrokeWidth(2);
		mPaint.setStyle(Style.FILL);
		int progressint = (int) (((float)this.progress / this.max) * 100);
		String progresstext =  progressint + "%";
		float text_left = (getWidth() - mPaint.measureText(progresstext)) / 2;
		FontMetrics fontMetrics = mPaint.getFontMetrics();
		// 绘制文字是底部对齐
		float text_top = (float) ((getHeight() / 2 + Math.ceil(fontMetrics.descent - fontMetrics.ascent) / 2));
		canvas.drawText(progresstext, text_left, text_top, mPaint);
	}

	private void drawProgress(Canvas canvas) {
		// 设置进度
		mPaint.setColor(progress_color);
		mPaint.setStrokeWidth(layout_width_float);
		float seek = 0;
		if (max > 0) {
			seek = (float) progress / max * 360;
		}
		canvas.drawArc(oval, start_degree, seek, false, mPaint);

		canvas.save();
		int center = getWidth() / 2;
		int radius = (int) (center - thumbSize_int / 2);

		double cycle_round = (seek + start_degree) * Math.PI / 180;
		float x = (float) (Math.cos(cycle_round) * (radius) + center - thumbSize_int / 2);
		float y = (float) (Math.sin(cycle_round) * (radius) + center - thumbSize_int / 2);
		thumb_double.setBounds(0, 0, thumbSize_int, thumbSize_int);
		canvas.translate(x, y);
		thumb_double.draw(canvas);
		canvas.restore();
	}

	private void drawBackground(Canvas canvas) {

		mPaint.setStrokeWidth(layout_width_float);
		mPaint.setStyle(Style.STROKE);
		mPaint.setAntiAlias(true);
		// 设置背景
		mPaint.setColor(background_color);
		canvas.drawArc(oval, start_degree, 360, false, mPaint);

	}

	/**
	 * 
	 * 
	 * @param progress
	 */
	public synchronized void setProgress(int progress) {
		if (progress > max) {
			progress = max;
		}
		this.progress = progress;
		postInvalidate();
	}

	public int getProgress() {
		return this.progress;
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (thumb_double != null) {
			thumb_double.setCallback(null);
			thumb_double = null;
		}
	}

	public int getMax() {
		return max;
	}

	public synchronized void setMax(int max) {
		if (progress > max) {
			this.max = progress;
		}
		this.max = max;
		postInvalidate();
	}
}
