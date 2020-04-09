package com.smartism.znzk.widget.addBar;

import java.util.ArrayList;

import com.smartism.znzk.R;
import com.smartism.znzk.util.camera.Utils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;



public class AddBar extends RelativeLayout {
	private int item_count = 0;
	ArrayList<View> items = new ArrayList<View>();
	Context mContext;
	LinearLayout parent;
	LayoutParams parent_params;
	OnItemChangeListener onItemChangeListener;
	OnLeftIconClickListener onLeftIconClickListener;
	OnItemClickListener onItemClickListener;
	int max_count = 0;
	boolean isVisiableArrow = true;

	public AddBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;

		// TODO Auto-generated constructor stub
	}

	Handler handler = new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			// TODO Auto-generated method stub
			int length = msg.arg1;
			if (null != parent_params) {
				parent_params.height += length;
				parent.setLayoutParams(parent_params);
			}
			return false;
		}
	});

	public void updateItem(int position, String content) {
		try {
			View view = items.get(position);
			if (null != view) {
				TextView alarmId_text = (TextView) view
						.findViewById(R.id.alarmId_text);
				alarmId_text.setText(content);
			}
		} catch (Exception e) {
			Log.e("my", "update view error");
		}

	}

	public void addItem(String id) {
		parent = (LinearLayout) this.findViewById(R.id.add_bar_parent);
		parent_params = (LayoutParams) parent.getLayoutParams();

		View view = LayoutInflater.from(mContext).inflate(
				R.layout.list_add_bar_item, null);

		final RelativeLayout item = (RelativeLayout) view
				.findViewById(R.id.add_bar_item);
		ImageView arrow = (ImageView) item.findViewById(R.id.arrow);
		if (!isVisiableArrow) {
			arrow.setVisibility(RelativeLayout.INVISIBLE);
		}
		TextView alarmId_text = (TextView) view.findViewById(R.id.alarmId_text);
		if (!id.equals("")) {
			alarmId_text.setText(id);
		} else {
			alarmId_text.setText(R.string.unbound);
		}

		final LayoutParams params = (LayoutParams) item
				.getLayoutParams();
		if (item_count > 0) {
			View last_view = items.get(item_count - 1);
			RelativeLayout last_item = (RelativeLayout) last_view
					.findViewById(R.id.add_bar_item);
			last_item.setBackgroundResource(R.drawable.tiao_bg_center);
		}

		items.add(view);
		parent.addView(view, item_count);
		item_count++;
		if (null != onItemChangeListener) {
			onItemChangeListener.onChange(item_count);
		}
		int total = params.height;
		changeParent(total / 10, total % 10);
		UpdateItemListener();
	}

	private synchronized void changeParent(final int count, final int remainder) {
		new Thread() {
			public void run() {
				int n = count;
				while (n >= 0) {
					n--;
					Message msg = new Message();
					if (n == 0) {
						msg.arg1 = remainder;
					} else {
						msg.arg1 = 10;
					}
					handler.sendMessage(msg);
					Utils.sleepThread(20);
				}
			}
		}.start();
	}

	public void removeItem(int position) {
		parent = (LinearLayout) this.findViewById(R.id.add_bar_parent);
		parent_params = (LayoutParams) parent.getLayoutParams();
		View view = items.get(position);

		RelativeLayout item = (RelativeLayout) view
				.findViewById(R.id.add_bar_item);
		LayoutParams params = (LayoutParams) item
				.getLayoutParams();
		parent_params.height -= params.height;
		parent.removeViewAt(position);
		items.remove(position);
		item_count--;
		if (null != onItemChangeListener) {
			onItemChangeListener.onChange(item_count);
		}
		if (item_count > 0) {
			RelativeLayout last_item = (RelativeLayout) items.get(
					item_count - 1).findViewById(R.id.add_bar_item);
			last_item.setBackgroundResource(R.drawable.tiao_bg_center);
		}
		UpdateItemListener();
	}

	public void removeAll() {
		while (item_count > 0) {
			removeItem(item_count - 1);
		}
	}

	private void UpdateItemListener() {
		for (int i = 0; i < items.size(); i++) {
			final int position = i;
			View view = items.get(i);
			final RelativeLayout item = (RelativeLayout) view
					.findViewById(R.id.add_bar_item);
			final ImageView left_icon = (ImageView) view
					.findViewById(R.id.delete_item);

			if (null != onItemClickListener) {
				item.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						onItemClickListener.onClick(item, position);
					}

				});
			}

			if (null != onLeftIconClickListener) {
				left_icon.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						onLeftIconClickListener.onClick(left_icon, position);
					}

				});
			}
		}
	}

	public void setOnItemChangeListener(
			OnItemChangeListener onItemChangeListener) {
		this.onItemChangeListener = onItemChangeListener;
	}

	public void setOnLeftIconClickListener(
			OnLeftIconClickListener onLeftIconClickListener) {
		this.onLeftIconClickListener = onLeftIconClickListener;
	}

	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {

		this.onItemClickListener = onItemClickListener;
	}

	public int getItemCount() {
		return this.item_count;
	}

	public int getMax_count() {
		return max_count;
	}

	public void setMax_count(int max_count) {
		this.max_count = max_count;
	}

	public void setArrowVisiable(boolean bool) {
		this.isVisiableArrow = bool;
	}
}
