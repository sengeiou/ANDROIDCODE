package com.smartism.znzk.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartism.znzk.R;
import com.smartism.znzk.domain.WeightUserInfo;

import java.util.List;

public class WeightUserAdapter extends BaseAdapter {
	private Context context;
	private List<WeightUserInfo> userInfos;
	private boolean isShowDelete;//根据这个变量来判断是否显示删除图标，true是显示，false是不显示
	private long mAccount;

	public long getmAccount() {
		return mAccount;
	}

	public void setmAccount(long mAccount) {
		this.mAccount = mAccount;
	}

	public WeightUserAdapter(Context context, List<WeightUserInfo> userInfos, long account) {
		this.context = context;
		this.userInfos = userInfos;
		this.mAccount=account;
	}

	public void setIsShowDelete(boolean isShowDelete) {
		this.isShowDelete = isShowDelete;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return userInfos.size();
	}

	@Override
	public Object getItem(int position) {
		return userInfos.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (null == convertView) {
			holder = new ViewHolder();
			convertView = View.inflate(context, R.layout.layout_user_list, null);
			holder.tv_name = (TextView) convertView.findViewById(R.id.item_txt);
			holder.iv_head = (ImageView) convertView.findViewById(R.id.item_img);
			holder.iv_del = (ImageView) convertView.findViewById(R.id.delete_markView);
			holder.iv_currentUser = (ImageView) convertView.findViewById(R.id.currentUser);
			convertView.setTag(holder);
		} else {

			holder = (ViewHolder) convertView.getTag();
		}
		holder.tv_name.setText(userInfos.get(position).getUserName());
		if (position == userInfos.size() - 1) {
			holder.iv_del.setVisibility(View.GONE);
			holder.iv_currentUser.setVisibility(View.GONE);
			holder.iv_head.setImageResource(R.drawable.role_add);
		} else {

			if (userInfos.get(position).getUserSex().equals("男")){
				holder.iv_head.setImageResource(R.drawable.weight_user_man_on);
			}else {
				holder.iv_head.setImageResource(R.drawable.weight_women_on);
			}

			for (int i =0;i<userInfos.size()-1;i++){
				if (getmAccount()==(userInfos.get(position).getUserId())){
					holder.iv_currentUser.setVisibility(View.VISIBLE);
					holder.iv_currentUser.setImageResource(R.drawable.zhuji_gprs);
				}else {
					holder.iv_currentUser.setVisibility(View.GONE);
				}
			}

//			if(isShowDelete){
//				holder.iv_currentUser.setVisibility(View.GONE);
//			}
			holder.iv_del.setVisibility(isShowDelete ? View.VISIBLE : View.GONE);//设置删除按钮是否显示
		}
		return convertView;
	}

	class ViewHolder {
		TextView tv_name;
		ImageView iv_head, iv_del,iv_currentUser;
	}
}
