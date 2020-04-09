package com.smartism.znzk.activity.device;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TriggerEqipmentActivity extends ActivityParentActivity {
	private ZhujiInfo zhuji;
	private ListView lv_all_equipment;
	private EquipmentAdapter mAdapter;

	public List<DeviceInfo> deviceInfos;
	private List<DeviceInfo> triggerDeviceInfos = new ArrayList<DeviceInfo>();
	private Set<DeviceInfo> selectDevId = new HashSet<DeviceInfo>();
	private List<DeviceInfo> deviceInfoses = new ArrayList<DeviceInfo>();
	int k = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_all_equipment_list);

		Intent intent = getIntent();
		deviceInfoses = (List<DeviceInfo>) intent.getSerializableExtra("deviceInfoses");
//		zhuji = DatabaseOperator.getInstance(TriggerEqipmentActivity.this).queryDeviceZhuJiInfo(dcsp.getString(Constant.APP_MASTERID, ""));
		//替换
		zhuji = DatabaseOperator.getInstance(TriggerEqipmentActivity.this).queryDeviceZhuJiInfo(ZhujiListFragment.getMasterId());
		if (zhuji!=null) {
			Cursor cursor = DatabaseOperator.getInstance(TriggerEqipmentActivity.this).getWritableDatabase()
					.rawQuery("select * from DEVICE_STATUSINFO where zj_id = ? order by sort desc", new String[] {String.valueOf(zhuji.getId())});
			deviceInfos = new ArrayList<DeviceInfo>();
			if (cursor != null && cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					DeviceInfo info = DatabaseOperator.getInstance(getApplicationContext()).buildDeviceInfo(cursor);
//					if (DeviceInfo.CakMenu.zhuji.value().equals(info.getCak()) //主机
//							|| DeviceInfo.CakMenu.detection.value().equals(info.getCak()) //健康采集
//							|| DeviceInfo.CakMenu.surveillance.value().equals(info.getCak()) //监控
//							|| DeviceInfo.CaMenu.zhujiControl.value().equals(info.getCa()) //主机遥控器
//							|| DeviceInfo.CaMenu.zhinengsuo.value().equals(info.getCa())//智能锁
//							|| DeviceInfo.CaMenu.hongwaizhuanfaqi.value().equals(info.getCa())//红外转发
//							|| DeviceInfo.CaMenu.ipcamera.value().equals(info.getCa())//摄像头
//							|| DeviceInfo.CaMenu.wenshiduji.value().equals(info.getCa())//温湿度计
//							|| DeviceInfo.CakMenu.control.value().equals(info.getCa())//控制类
//							|| DeviceInfo.CaMenu.zhujifmq.value().equals(info.getCa())) { //内置蜂鸣器 等设备
//						//不加入智能场景
//					} else {
//						deviceInfos.add(info);
//					}
					if (DeviceInfo.CakMenu.security.value().equals(info.getCak())){
						//触发设备只显示安防类别
						deviceInfos.add(info);
					}
					
				}
				cursor.close();
			}
		}
		init();
	}

	private void init() {

		lv_all_equipment = (ListView) findViewById(R.id.lv_all_equipment);
		mAdapter = new EquipmentAdapter(this);
		lv_all_equipment.setAdapter(mAdapter);
		if (deviceInfoses != null && deviceInfoses.size() > 0) {
			for (int i = 0; i < deviceInfoses.size(); i++) {
				triggerDeviceInfos.add(deviceInfoses.get(i));
				selectDevId.add(deviceInfoses.get(i));
				// selectDevs.put(deviceInfoses.get(i).getId(),
				// deviceInfoses.get(i));
			}
		}

	}

	class EquipmentAdapter extends BaseAdapter {

		private Context context;

		public EquipmentAdapter(Context context) {
			this.context = context;

		}

		@Override
		public int getCount() {
			return deviceInfos.size();
		}

		@Override
		public Object getItem(int position) {
			return deviceInfos.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			ViewHolder holder = null;
			if (null == convertView) {
				holder = new ViewHolder();
				convertView = View.inflate(context, R.layout.all_equipment_list_item, null);
				holder.cb_equipment = (CheckBox) convertView.findViewById(R.id.cb_equipment);
				holder.tv_equipment = (TextView) convertView.findViewById(R.id.tv_equipment);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			// devices.clear();
			final DeviceInfo deviceInfo = deviceInfos.get(position);
			//holder.tv_equipment.setText(deviceInfo.getName() + deviceInfo.getType());
			boolean isNull= ((deviceInfo.getWhere()== null||"".equals(deviceInfo.getWhere())||"null".equals(deviceInfo.getWhere()))
							&&(deviceInfo.getType()== null||"".equals(deviceInfo.getType())));
			StringBuffer sb = new StringBuffer();
			sb.append(deviceInfo.getName());
			if (isNull){
				holder.tv_equipment.setText(sb.toString());
			}else {
				sb.append(" ");
				sb.append("( ");
				sb.append(((deviceInfo.getWhere()== null|| "null".equals(deviceInfo.getWhere()))? "" : deviceInfo.getWhere()+" "));
				sb.append((deviceInfo.getType()));
				sb.append(" )");
			}
			holder.tv_equipment.setText(sb.toString());
//			if(deviceInfo!=null&&deviceInfo.getType()!=null&&!deviceInfo.getType().equals("")){
//				   holder.tv_equipment.setText(deviceInfo.getName() +":"+deviceInfo.getType());
//				}else{
//					holder.tv_equipment.setText(deviceInfo.getName());
//				}
			if (selectDevId.contains(deviceInfo)) {
				holder.cb_equipment.setChecked(true);
			} else {
				holder.cb_equipment.setChecked(false);
			}

			// Log.e("TAG", deviceInfoses.toString());
			// Log.e("deviceInfo", deviceInfos.toString());
			holder.cb_equipment.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					CheckBox cb = (CheckBox) v;
					if (cb.isChecked()) {
						triggerDeviceInfos.add(deviceInfo);
						selectDevId.add(deviceInfo);

					} else {
						selectDevId.remove(deviceInfo);
						triggerDeviceInfos.remove(deviceInfo);
					}
				}
			});

			return convertView;
		}

		class ViewHolder {
			TextView tv_equipment;
			CheckBox cb_equipment;
		}

	}

	public void determine(View v) {
		// Toast.makeText(this, devices.size()+"", 1).show();

		Intent intent = new Intent();
		for (int i = 0; i < triggerDeviceInfos.size(); i++) {
			if (!selectDevId.contains(triggerDeviceInfos.get(i))) {
				
				triggerDeviceInfos.remove(i);
				i--;
				
			}
		}
//		for(int j=0;j<triggerDeviceInfos.size();j++){
//			for(int k=triggerDeviceInfos.size()-1;k>j;k--){
//				if(triggerDeviceInfos.get(k).getId()==triggerDeviceInfos.get(j).getId()){
//					triggerDeviceInfos.remove(j);
//				}
//			}
//		}
		intent.putExtra("triggerDeviceInfos", (Serializable) triggerDeviceInfos);
		setResult(RESULT_OK, intent);

		finish();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// EventBus.getDefault().unregister(this);
	}

	public void back(View v) {
		finish();
	}
}
