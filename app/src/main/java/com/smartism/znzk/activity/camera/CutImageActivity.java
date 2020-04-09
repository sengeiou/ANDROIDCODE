package com.smartism.znzk.activity.camera;

import java.io.File;

import com.smartism.znzk.R;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.global.NpcCommon;
import com.smartism.znzk.util.camera.ImageUtils;
import com.smartism.znzk.util.camera.T;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;



public class CutImageActivity extends BaseActivity implements OnClickListener {
	public static final int TOUCH_EVENT_TYPE_ZOOM = 0;
	public static final int TOUCH_EVENT_TYPE_DRAG = 1;
	Context mContext;
	ImageView temp;
	ImageView layout_cut;
	RelativeLayout layout_bottom;
	ImageView header_img;
	int mWindowWidth, mWindowHeight;
	Button save;
	ImageView back_btn;
	int bottom_height;
	Bitmap mainBitmap;
	Bitmap tempBitmap;
	Bitmap saveBitmap;
	int width, height;
	int initX, initY;
	float scale;
	int maxWidth;
	int minWidth = 150;
	int type;
	Contact mContact;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cut_image);
		mContext = this;
		initCompoment();
		init();
		checkImageSize();
		updateHeader();
		mContact = (Contact) this.getIntent().getSerializableExtra("contact");
	}

	@SuppressLint("WrongViewCast")
	public void initCompoment() {
		temp = (ImageView) findViewById(R.id.temp);
		header_img = (ImageView) findViewById(R.id.header_img);
		save = (Button) findViewById(R.id.save);
		back_btn = (ImageView) findViewById(R.id.back_btn);
		layout_bottom = (RelativeLayout) findViewById(R.id.layout_bottom);
		layout_cut = (ImageView) findViewById(R.id.layout_cut);
		layout_cut.setOnTouchListener(onTouch);

		back_btn.setOnClickListener(this);
		save.setOnClickListener(this);
	}

	public void init() {
		try {

			mainBitmap = ImageUtils.getBitmap(new File(
					Constants.Image.USER_HEADER_PATH
							+ Constants.Image.USER_HEADER_TEMP_FILE_NAME),
					Constants.USER_HEADER_WIDTH_HEIGHT,
					Constants.USER_HEADER_WIDTH_HEIGHT);
			DisplayMetrics dm = new DisplayMetrics();
			dm = getResources().getDisplayMetrics();
			mWindowWidth = dm.widthPixels;
			mWindowHeight = dm.heightPixels;

			AbsoluteLayout.LayoutParams params1 = (AbsoluteLayout.LayoutParams) temp
					.getLayoutParams();
			LayoutParams params2 = (LayoutParams) layout_bottom
					.getLayoutParams();
			AbsoluteLayout.LayoutParams params3 = (AbsoluteLayout.LayoutParams) layout_cut
					.getLayoutParams();
			float x1 = mainBitmap.getWidth();
			float y1 = mainBitmap.getHeight();
			if (x1 > y1) {
				float x2 = mWindowWidth;
				float y2 = x2 * y1 / x1;

				if (y2 >= mWindowHeight) {

				} else {
					scale = x1 / x2;

					width = (int) x2;
					height = (int) y2;
					initX = 0;
					initY = (mWindowHeight - params2.height) / 2 - height / 2;
					params1.width = width;
					params1.height = height;

					params1.x = initX;
					params1.y = initY;

					temp.setLayoutParams(params1);
					temp.setImageBitmap(mainBitmap);

					params3.x = initX;
					params3.y = initY;
					params3.width = height;
					params3.height = height;
					maxWidth = height;
					layout_cut.setLayoutParams(params3);
					type = 0;
				}
			} else if (x1 < y1) {
				float y2 = mWindowHeight - params2.height;

				float x2 = x1 * y2 / y1;
				if (x2 >= mWindowWidth) {
					x2 = mWindowWidth;
					y2 = x2 * y1 / x1;
					scale = x1 / x2;

					width = (int) x2;
					height = (int) y2;
					initX = 0;
					initY = (mWindowHeight - params2.height) / 2 - height / 2;
					params1.width = width;
					params1.height = height;

					params1.x = initX;
					params1.y = initY;

					temp.setLayoutParams(params1);
					temp.setImageBitmap(mainBitmap);

					params3.x = initX;
					params3.y = initY;
					params3.width = width;
					params3.height = width;
					maxWidth = width;
					layout_cut.setLayoutParams(params3);
					type = 0;
				} else {
					scale = y1 / y2;

					width = (int) x2;
					height = (int) y2;
					initX = mWindowWidth / 2 - width / 2;
					initY = 0;
					params1.width = width;
					params1.height = height;
					params1.x = initX;
					params1.y = initY;

					temp.setLayoutParams(params1);
					temp.setImageBitmap(mainBitmap);

					params3.x = initX;
					params3.y = initY;
					params3.width = width;
					params3.height = width;
					maxWidth = width;
					layout_cut.setLayoutParams(params3);
					type = 1;
				}

			} else {
				float x2 = mWindowWidth;
				float y2 = mWindowWidth;

				scale = x1 / x2;

				width = (int) x2;
				height = (int) y2;

				initX = 0;
				initY = (mWindowHeight - params2.height) / 2 - height / 2;

				params1.width = width;
				params1.height = height;
				params1.x = initX;
				params1.y = initY;

				temp.setLayoutParams(params1);
				temp.setImageBitmap(mainBitmap);

				params3.x = initX;
				params3.y = initY;
				params3.width = width - 1;
				params3.height = width - 1;
				maxWidth = width - 1;
				layout_cut.setLayoutParams(params3);
				type = 2;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	OnTouchListener onTouch = new OnTouchListener() {
		boolean isActive = false;
		int mWidth, mHeight;
		float downWidth;
		float downHeight;
		int mode;
		float oldDist;
		float newDist;

		@Override
		public boolean onTouch(View view, MotionEvent event) {
			// TODO Auto-generated method stub
			// Log.e("my","ACTION_POINTER_DOWN:"+event.getAction());
			AbsoluteLayout.LayoutParams params = (AbsoluteLayout.LayoutParams) layout_cut
					.getLayoutParams();
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:

				mode = TOUCH_EVENT_TYPE_DRAG;
				mWidth = params.width;
				mHeight = params.height;
				downWidth = event.getRawX() - params.x;
				downHeight = event.getRawY() - params.y;
				isActive = true;
				break;
			case MotionEvent.ACTION_POINTER_2_DOWN:
				mode = TOUCH_EVENT_TYPE_ZOOM;
				oldDist = spacing(event);
				Log.e("my", "ACTION_POINTER_DOWN:" + oldDist);
				break;
			case MotionEvent.ACTION_MOVE:
				if (mode == TOUCH_EVENT_TYPE_ZOOM) {
					newDist = spacing(event);
					if (newDist == 0) {
						mode = TOUCH_EVENT_TYPE_DRAG;
						break;
					}
					updateCutLayout(newDist - oldDist);
					oldDist = newDist;
					// Log.e("my","ACTION_POINTER_DOWN:"+newDist);
				} else {
					int changeX = 0, changeY = 0;
					switch (type) {
					case 0:
						changeX = (int) (event.getRawX() - downWidth);
						if (changeX < initX) {
							changeX = initX;
						} else if (changeX > (width - mWidth)) {
							changeX = width - mWidth;
						}

						changeY = (int) (event.getRawY() - downHeight);
						if (changeY < initY) {
							changeY = initY;
						} else if (changeY > (height - mHeight + initY)) {
							changeY = height - mHeight + initY;
						}
						break;
					case 1:
						changeX = (int) (event.getRawX() - downWidth);
						if (changeX < initX) {
							changeX = initX;
						} else if (changeX > (width - mWidth + initX)) {
							changeX = width - mWidth + initX;
						}

						changeY = (int) (event.getRawY() - downHeight);
						if (changeY < initY) {
							changeY = initY;
						} else if (changeY > (height - mHeight)) {
							changeY = height - mHeight;
						}
						break;
					case 2:
						changeX = (int) (event.getRawX() - downWidth);
						if (changeX < initX) {
							changeX = initX;
						} else if (changeX > (width - mWidth)) {
							changeX = width - mWidth;
						}

						changeY = (int) (event.getRawY() - downHeight);
						if (changeY < initY) {
							changeY = initY;
						} else if (changeY > (height - mHeight + initY)) {
							changeY = height - mHeight + initY;
						}
						break;
					}
					params.x = changeX;
					params.y = changeY;
					layout_cut.setLayoutParams(params);
					updateHeader();
				}
				break;
			}
			return true;
		}

	};

	private float spacing(MotionEvent event) {
		try {
			float x = event.getX(0) - event.getX(1);
			float y = event.getY(0) - event.getY(1);
			return (float) Math.sqrt(x * x + y * y);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	void updateCutLayout(float value) {
		AbsoluteLayout.LayoutParams params = (AbsoluteLayout.LayoutParams) layout_cut
				.getLayoutParams();
		if ((params.width + value) > maxWidth) {
			params.width = maxWidth;
			params.height = maxWidth;
		} else if ((params.width + value) < minWidth) {
			params.width = minWidth;
			params.height = minWidth;
		} else {
			params.width += value;
			params.height += value;
		}

		layout_cut.setLayoutParams(params);
	}

	void checkImageSize() {
		tempBitmap = Bitmap.createBitmap(mainBitmap, 0, 0,
				(int) (minWidth * scale), (int) (minWidth * scale));
		if (tempBitmap.getWidth() < Constants.Image.USER_HEADER_MIN_SIZE) {
			setResult(0);
			T.showShort(mContext, R.string.image_size_too_small);
			finish();
		}
	}

	void updateHeader() {
		AbsoluteLayout.LayoutParams params = (AbsoluteLayout.LayoutParams) layout_cut
				.getLayoutParams();
		int x = 0, y = 0, width = 0, height = 0;
		switch (type) {
		case 0:
			x = (int) (params.x * scale);
			y = (int) ((params.y - initY) * scale);
			width = (int) (params.width * scale);
			height = (int) (params.height * scale);
			break;
		case 1:
			x = (int) ((params.x - initX) * scale);
			y = (int) (params.y * scale);
			width = (int) (params.width * scale);
			height = (int) (params.height * scale);
			Log.e("my", x + ":" + y + ":" + width + ":" + height);
			Log.e("my", mainBitmap.getWidth() + ":" + mainBitmap.getHeight());
			break;
		case 2:
			x = (int) (params.x * scale);
			y = (int) ((params.y - initY) * scale);
			width = (int) (params.width * scale);
			height = (int) (params.height * scale);

			break;
		}
		try {
			tempBitmap = Bitmap.createBitmap(mainBitmap, x, y, width, height);
			saveBitmap = Bitmap.createBitmap(tempBitmap, 0, 0,
					tempBitmap.getWidth(), tempBitmap.getHeight());
			Log.e("my", tempBitmap.getWidth() + "");
			Log.e("my", ImageUtils.getScaleRounded(tempBitmap.getWidth()) + "");
			tempBitmap = ImageUtils.roundCorners(tempBitmap,
					ImageUtils.getScaleRounded(tempBitmap.getWidth()));
			header_img.setImageBitmap(tempBitmap);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.save:
			this.setResult(1);
			ImageUtils.saveImg(saveBitmap, Constants.Image.USER_HEADER_PATH
					+ NpcCommon.mThreeNum + "/" + mContact.contactId + "/",
					Constants.Image.USER_HEADER_FILE_NAME);
			saveBitmap = ImageUtils.grayBitmap(saveBitmap);
			ImageUtils.saveImg(saveBitmap, Constants.Image.USER_HEADER_PATH
					+ NpcCommon.mThreeNum + "/" + mContact.contactId + "/",
					Constants.Image.USER_GRAY_HEADER_FILE_NAME);
			finish();
			break;
		case R.id.back_btn:
			finish();
			break;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (null != mainBitmap && !mainBitmap.isRecycled()) {
			mainBitmap.recycle();
		}

		if (null != tempBitmap && !tempBitmap.isRecycled()) {
			tempBitmap.recycle();
		}

		if (null != saveBitmap && !saveBitmap.isRecycled()) {
			saveBitmap.recycle();
		}
	}

	@Override
	public int getActivityInfo() {
		// TODO Auto-generated method stub
		return Constants.ActivityInfo.ACTIVITY_CUTIMAGEACTIVITY;
	}

	@Override
	protected int onPreFinshByLoginAnother() {
		return 0;
	}

}
