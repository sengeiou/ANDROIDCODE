package com.smartism.znzk.adapter.camera;

import com.smartism.znzk.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;



public class SelectorDialogAdapter extends BaseAdapter {
	String[] data;
	Context mContext;

	public SelectorDialogAdapter(Context context, String[] data) {
		this.mContext = context;
		this.data = data;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return data.length;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		View view = arg1;
		if (null == view) {
			view = LayoutInflater.from(mContext).inflate(
					R.layout.list_selector_dialog_item, null);
		}
		TextView name = (TextView) view.findViewById(R.id.name);
		name.setText(data[position]);
		return view;
	}

}
