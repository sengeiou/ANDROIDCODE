package com.smartism.znzk.view;

import java.util.List;

import com.smartism.znzk.R;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

public class SceneTypeSpinnerPopWindow extends PopupWindow implements OnItemClickListener {

	private Context mContext;
	private ListView mListView;
	private NormalSpinerAdapter mAdapter;
	private IOnItemSelectedListener mItemSelectListener;

	public SceneTypeSpinnerPopWindow(Context context) {
		super(context);

		mContext = context;
		init();
	}

	public void setItemListener(IOnItemSelectedListener listener) {
		mItemSelectListener = listener;
	}

	private void init() {
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.activity_add_scene_typemenu, null);
		
		setContentView(view);
		//设置SelectPicPopupWindow弹出窗体的宽
		this.setWidth(LayoutParams.WRAP_CONTENT);
		//设置SelectPicPopupWindow弹出窗体的高
		this.setHeight(LayoutParams.WRAP_CONTENT);
		//设置SelectPicPopupWindow弹出窗体可点击
		this.setFocusable(true);
		//设置SelectPicPopupWindow弹出窗体动画效果
		//this.setAnimationStyle(R.style.Scene_add_typemenu_Animation);
		//实例化一个ColorDrawable颜色为白色
		ColorDrawable dw = new ColorDrawable(0x00000445);
		//设置SelectPicPopupWindow弹出窗体的背景
		this.setBackgroundDrawable(dw);

		mListView = (ListView) view.findViewById(R.id.addscene_typelist);

		mAdapter = new NormalSpinerAdapter(mContext);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
	}

	public void refreshData(List<String> list, int selIndex) {
		if (list != null && selIndex != -1) {
			mAdapter.refreshData(list, selIndex);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int pos, long arg3) {
		dismiss();
		if (mItemSelectListener != null) {
			mItemSelectListener.onItemClick(pos);
		}
	}
	
	public interface IOnItemSelectedListener {
		public void onItemClick(int position);
	}
	
	public class NormalSpinerAdapter extends BaseAdapter{
		private List<String> list;
		private int selIndex;
		private Context context;
		
		/**
		 * 视图内部类
		 * @author Administrator
		 *
		 */
		class DeviceInfoView {
			ImageView ioc;
			TextView name;
		}
		
		public NormalSpinerAdapter(Context context) {
			this.context = context;
		}
		
		public void refreshData(List<String> list, int selIndex){
			this.list = list;
			this.selIndex = selIndex;
			notifyDataSetChanged();
		}
		
		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public String getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			DeviceInfoView viewCache = null;
			if (view == null) {
				viewCache = new DeviceInfoView();
				view = LayoutInflater.from(context).inflate(R.layout.activity_add_scene_typeitem, null);
				viewCache.ioc = (ImageView) view.findViewById(R.id.scene_type_sel);
				viewCache.name = (TextView) view.findViewById(R.id.scene_type_name);
				view.setTag(viewCache);
			} else {
				viewCache = (DeviceInfoView) view.getTag();
			}
			if(position==selIndex){
				viewCache.ioc.setVisibility(View.VISIBLE);
			}else{
				viewCache.ioc.setVisibility(View.GONE);
			}
			viewCache.name.setText(getItem(position));
			return view;
		}
		
	}

}