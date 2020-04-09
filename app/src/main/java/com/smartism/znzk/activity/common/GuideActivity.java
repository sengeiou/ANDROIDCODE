package com.smartism.znzk.activity.common;

import java.util.ArrayList;

import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.WeakRefHandler;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * 多图引导页
 *
 */
public class GuideActivity extends ActivityParentActivity implements OnClickListener,OnPageChangeListener{
	// 定义ViewPager对象
	private ViewPager viewPager;

	// 定义ViewPager适配器
	private ViewPagerAdapter vpAdapter;

	// 定义一个ArrayList来存放View
	private ArrayList<View> views;

	// 引导图片资源
//	private static final int[] pics = { R.drawable.guide_1, R.drawable.guide_2,
//			R.drawable.guide_3 };
	private static final int[] pics = {};

	// 底部小点的图片
	private ImageView[] points;

	// 记录当前选中位置
	private int currentIndex;
	
	private Button startBtn;
	
	private DisplayMetrics metric = new DisplayMetrics();

	private Handler.Callback mCallback = new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
				case 1:
					startBtn.setVisibility(View.VISIBLE);
					break;

				default:
					break;
			}
			return false;
		}
	};
	private Handler defaultHandler = new WeakRefHandler(mCallback);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_guide);

		initView();

		initData();
	}

	/**
	 * 初始化组件
	 */
	private void initView() {
		// 实例化ArrayList对象
		views = new ArrayList<View>();

		// 实例化ViewPager
		viewPager = (ViewPager) findViewById(R.id.viewpager);

		// 实例化ViewPager适配器
		vpAdapter = new ViewPagerAdapter(views);
		
		startBtn = (Button) findViewById(R.id.startBtn);
		
		getWindowManager().getDefaultDisplay().getMetrics(metric);
	}

	/**
	 * 初始化数据
	 */
	private void initData() {
		// 定义一个布局并设置参数
		LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);

		// 初始化引导图片列表
		for (int i = 0; i < pics.length; i++) {
			ImageView iv = new ImageView(this);
			iv.setLayoutParams(mParams);
//			iv.setImageBitmap(ImageUtil.readBitMap(this, pics[i]));
			iv.setImageDrawable(new BitmapDrawable(getResources(), getResources().openRawResource(pics[i])));
//			iv.setImageResource(pics[i]);
			views.add(iv);
		}

		// 设置数据
		viewPager.setAdapter(vpAdapter);
		// 设置监听
		viewPager.setOnPageChangeListener(this);

		// 初始化底部小点
		initPoint();
	}

	/**
	 * 初始化底部小点
	 */
	private void initPoint() {
		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.ll);

		points = new ImageView[pics.length];

		// 循环取得小点图片
		for (int i = 0; i < pics.length; i++) {
			// 得到一个LinearLayout下面的每一个子元素
			points[i] = (ImageView) linearLayout.getChildAt(i);
			// 默认都设为灰色
			points[i].setEnabled(true);
			// 给每个小点设置监听
			points[i].setOnClickListener(this);
			// 设置位置tag，方便取出与当前位置对应
			points[i].setTag(i);
		}

		// 设置当面默认的位置
		currentIndex = 0;
		// 设置为白色，即选中状态
		points[currentIndex].setEnabled(false);
	}

	/**
	 * 当滑动状态改变时调用
	 */
	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	/**
	 * 当当前页面被滑动时调用
	 */

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	/**
	 * 当新的页面被选中时调用
	 */

	@Override
	public void onPageSelected(int position) {
		// 设置底部小点选中状态
		setCurDot(position);
	}

	/**
	 * 通过点击事件来切换当前的页面
	 */
	@Override
	public void onClick(View v) {
		int position = (Integer) v.getTag();
		setCurView(position);
		setCurDot(position);
	}

	/**
	 * 设置当前页面的位置
	 */
	private void setCurView(int position) {
		if (position < 0 || position >= pics.length) {
			return;
		}
		viewPager.setCurrentItem(position);
	}

	/**
	 * 设置当前的小点的位置
	 */
	private void setCurDot(int positon) {
		if (positon < 0 || positon > pics.length - 1 || currentIndex == positon) {
			return;
		}
		points[positon].setEnabled(false);
		points[currentIndex].setEnabled(true);

		currentIndex = positon;
		//让跳转按钮不出来
//		if (currentIndex == points.length-1) {
//			defaultHandler.sendEmptyMessageDelayed(1, 2000);
//		}else{
//			defaultHandler.removeMessages(1);
//			startBtn.setVisibility(View.GONE);
//		}
	}

	/**
	 * 相应按钮点击事件
	 */
	public void startbutton(View v) {
		Intent intent = new Intent();
//		if (dcsp.getBoolean(Constant.IS_LOGIN, false)) {
//			intent.setClass(getApplication(), DevicesListActivity.class);
//		} else {
//			intent.setClass(getApplication(), LoginActivity.class);
//		}
		dcsp.putBoolean(Constant.IS_FIRSTSTART, false).commit();
		startActivity(intent);
		this.finish();
	}

	class ViewPagerAdapter extends PagerAdapter {

		// 界面列表
		private ArrayList<View> views;

		public ViewPagerAdapter(ArrayList<View> views) {
			this.views = views;
		}

		/**
		 * 获得当前界面数
		 */
		@Override
		public int getCount() {
			if (views != null) {
				return views.size();
			}
			return 0;
		}

		/**
		 * 初始化position位置的界面
		 */
		@Override
		public Object instantiateItem(View view, int position) {

			((ViewPager) view).addView(views.get(position), 0);

			return views.get(position);
		}

		/**
		 * 判断是否由对象生成界面
		 */
		@Override
		public boolean isViewFromObject(View view, Object arg1) {
			return (view == arg1);
		}

		/**
		 * 销毁position位置的界面
		 */
		@Override
		public void destroyItem(View view, int position, Object arg2) {
			((ViewPager) view).removeView(views.get(position));
		}
	}
	public void back(View v){
		finish();
	}
	
	@Override
	protected void onDestroy() {
		defaultHandler.removeCallbacksAndMessages(null);
		defaultHandler = null;
		super.onDestroy();
	}
}
