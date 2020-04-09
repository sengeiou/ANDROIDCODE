package com.smartism.znzk.widget;

import com.smartism.znzk.R;

import android.content.Context;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;



public class MyInputDialog {
	private OnButtonOkListener onButtonOkListener;
	private OnButtonCancelListener onButtonCancelListener;
	String title_str, btn1_str, btn2_str;
	boolean isShow = false;
	Context context;
	EditText input1;

	public MyInputDialog(Context context) {
		this.context = context;

	}

	public void show(final View view) {
		view.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				return true;
			}

		});
		TextView title = (TextView) view.findViewById(R.id.title_text);
		TextView button1 = (TextView) view.findViewById(R.id.button1_text);
		TextView button2 = (TextView) view.findViewById(R.id.button2_text);
		input1 = (EditText) view.findViewById(R.id.input1);
		input1.setText("");
		input1.requestFocus();
		title.setText(title_str);
		button1.setText(btn1_str);
		button2.setText(btn2_str);
		button1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (null != onButtonOkListener) {
					onButtonOkListener.onClick();
				} else {
					hideDialog(view);
				}

			}
		});

		button2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (null == onButtonCancelListener) {
					hideDialog(view);
				} else {
					onButtonCancelListener.onClick();
				}
			}
		});
		showDialog(view);
	}

	public interface OnButtonOkListener {
		public void onClick();
	}

	public interface OnButtonCancelListener {
		public void onClick();
	}

	public void setOnButtonOkListener(OnButtonOkListener onButtonOkListener) {
		this.onButtonOkListener = onButtonOkListener;
	}

	public void setOnButtonCancelListener(
			OnButtonCancelListener onButtonCancelListener) {
		this.onButtonCancelListener = onButtonCancelListener;
	}

	public void setTitle(String title) {
		this.title_str = title;
	}

	public void setBtn1_str(String btn1_str) {
		this.btn1_str = btn1_str;
	}

	public void setBtn2_str(String btn2_str) {
		this.btn2_str = btn2_str;
	}

	private void showDialog(View v) {
		isShow = true;
		v.setVisibility(RelativeLayout.VISIBLE);
		LinearLayout dialog = (LinearLayout) v.findViewById(R.id.dialog_input);
		dialog.setVisibility(RelativeLayout.VISIBLE);
		Animation anim = AnimationUtils.loadAnimation(context, R.anim.scale_in);
		anim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation arg0) {
				// TODO Auto-generated method stub
				InputMethodManager m = (InputMethodManager) input1.getContext()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationStart(Animation arg0) {
				// TODO Auto-generated method stub

			}

		});
		dialog.startAnimation(anim);
	}

	private void hideDialog(final View v) {
		isShow = false;
		final LinearLayout dialog = (LinearLayout) v
				.findViewById(R.id.dialog_input);
		dialog.setVisibility(RelativeLayout.VISIBLE);
		Animation anim = AnimationUtils
				.loadAnimation(context, R.anim.scale_out);
		anim.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation arg0) {
				// TODO Auto-generated method stub
				dialog.setVisibility(RelativeLayout.GONE);
				v.setVisibility(RelativeLayout.GONE);
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationStart(Animation arg0) {
				// TODO Auto-generated method stub

			}

		});
		dialog.startAnimation(anim);
	}

	public void setInput1HintText(String hint) {
		input1.setHint(hint);
	}

	public void setInput1HintText(int rid) {
		input1.setHint(rid);
	}

	public String getInput1Text() {
		return input1.getText().toString();
	}

	public void setInput1Type_number() {
		input1.setInputType(InputType.TYPE_CLASS_NUMBER);
		input1.setTransformationMethod(PasswordTransformationMethod
				.getInstance());
	}

	public void hide(View view) {
		if (isShow) {
			hideDialog(view);
		}
	}

	public boolean isShowing() {
		return isShow;
	}
}
