package com.smartism.znzk.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.macrovideo.sdk.custom.DeviceInfo;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.camera.ImageSeeActivity;
import com.smartism.znzk.adapter.recycleradapter.BaseRecyslerAdapter;
import com.smartism.znzk.adapter.recycleradapter.RecyclerItemBean;
import com.smartism.znzk.camera.adapter.ImageListAdapter;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.global.VList;
import com.smartism.znzk.util.camera.Utils;
import com.smartism.znzk.widget.NormalDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class UtilsFrag extends BaseFragment implements OnClickListener , BaseRecyslerAdapter.RecyclerItemClickListener,BaseRecyslerAdapter.RecyclerItemLongClickListener{
	private Context mContext;
	private RecyclerView recycler;
	private ImageListAdapter mAdapter;
	private int id;
	private String device;
	private DeviceInfo info;
	private Contact contact;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_utils, container, false);
		mContext = getActivity();
		id = getArguments().getInt("deviceid");
		device = getArguments().getString("device");
		contact = (Contact) getArguments().getSerializable("contact");
		info = VList.getInstance().findById(id);
		initComponent(view);
		return view;

	}

	public UtilsFrag() {
		super();
		// TODO Auto-generated constructor stub
	}
	List<RecyclerItemBean> pictrues;
	public void initComponent(View view) {
		initImage();
		recycler = (RecyclerView) view.findViewById(R.id.image_list);
		mAdapter = new ImageListAdapter(pictrues);
		mAdapter.setRecyclerItemClickListener(this);
		mAdapter.setRecyclerItemLongClickListener(this);
		//创建默认线性LinearLayoutManager
		GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 4, GridLayoutManager.VERTICAL, false);
		recycler.setLayoutManager(gridLayoutManager);  //设置布局管理器
		recycler.setItemAnimator(new DefaultItemAnimator()); //设置Item增加、移除动画
 		recycler.setAdapter(mAdapter);
	}
	public void  initImage(){
		if (pictrues == null) pictrues = new ArrayList<>();
		List<String> path = Utils.getScreenShotImagePath(contact.contactId, 1);
		pictrues.clear();
		if (path != null && !path.isEmpty()) {
			for (String str : path) {
				pictrues.add(new RecyclerItemBean(str,1));
			}
		}
		if (mAdapter!=null)mAdapter.notifyDataSetChanged();
	}


	private void showImageList() {
		//截图显示
		if (pictrues == null)
			pictrues = new ArrayList<>();
		pictrues.clear();
		List<String> paths = Utils.getScreenShotImagePath(contact.contactId, 1);
		if (!paths.isEmpty()) {
			for (String path : paths) {
				pictrues.add(new RecyclerItemBean(path, 0));
			}
		}
	}

	@Override
	public void onClick(View v) {

	}
	
	@Override
	public void onDestroy() {
		Intent it = new Intent();
		it.setAction(Constants.Action.CONTROL_BACK);
		mContext.sendBroadcast(it);
		super.onDestroy();
	}
	@Override
	public void onResume() {
		super.onResume();
	}


	@Override
	public void onRecycleItemClick(View view, int position) {
		String path = (String) pictrues.get(position).getT();
		Intent in = new Intent();
		in.setClass(mContext, ImageSeeActivity.class);
		in.putExtra("startactivity", 0);
		in.putExtra("path", path);
		in.putExtra("number", 0);
		in.putExtra("contact", contact);
		in.putExtra("connectType", Constants.ConnectType.P2PCONNECT);
		mContext.startActivity(in);
	}

	@Override
	public boolean onRecycleItemLongClick(View view, final int position) {
		final String path = (String) pictrues.get(position).getT();
		NormalDialog dialog = new NormalDialog(mContext,
				mContext.getResources().getString(R.string.delete),
				mContext.getResources().getString(R.string.confirm_delete),
				mContext.getResources().getString(R.string.delete),
				mContext.getResources().getString(R.string.cancel));
		dialog.setOnButtonOkListener(new NormalDialog.OnButtonOkListener() {

			@Override
			public void onClick() {
				// TODO Auto-generated method stub
				File file = new File(path);
				if (file.exists()) {
					file.delete();
					pictrues.remove(position);
					mAdapter.notifyItemRemoved(position);
				}
			}
		});
		dialog.showDialog();
		return true;
	}
}
