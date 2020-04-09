package com.smartism.znzk.activity.device;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
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

public class AllEquipmentListActivity extends ActivityParentActivity {
	private ZhujiInfo zhuji;
	private ListView lv_all_equipment;
	private EquipmentAdapter mAdapter;

	public List<DeviceInfo> deviceInfos;
	private List<DeviceInfo> devices = new ArrayList<DeviceInfo>();
	private Set<DeviceInfo> selectDevId = new HashSet<DeviceInfo>();
	private List<DeviceInfo> deviceInfoses = new ArrayList<DeviceInfo>();
	int k = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_all_equipment_list);

		Intent intent = getIntent();
		deviceInfoses = (List<DeviceInfo>) intent.getSerializableExtra("deviceInfoses");
//		zhuji = DatabaseOperator.getInstance(AllEquipmentListActivity.this).queryDeviceZhuJiInfo(dcsp.getString(Constant.APP_MASTERID, ""));
		//替换
		zhuji = DatabaseOperator.getInstance(AllEquipmentListActivity.this).queryDeviceZhuJiInfo(ZhujiListFragment.getMasterId());
		if (zhuji!=null) {
			Cursor cursor = DatabaseOperator.getInstance(AllEquipmentListActivity.this).getWritableDatabase()
					.rawQuery("select * from DEVICE_STATUSINFO where zj_id = ? order by sort desc", new String[] {String.valueOf(zhuji.getId())});
			deviceInfos = new ArrayList<DeviceInfo>();
			if (cursor != null && cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					DeviceInfo info = DatabaseOperator.getInstance(getApplicationContext()).buildDeviceInfo(cursor);
					if (info.getControlType().equals(DeviceInfo.ControlTypeMenu.zhuji.value())
							||DeviceInfo.CaMenu.hongwaizhuanfaqi.value().equals(info.getCak())
							|| DeviceInfo.CakMenu.detection.value().equals(info.getCak())
							|| DeviceInfo.CakMenu.health.value().equals(info.getCak())
							|| DeviceInfo.CakMenu.surveillance.value().equals(info.getCak())
							|| DeviceInfo.CaMenu.zhujiControl.value().equals(info.getCa())
							|| DeviceInfo.CaMenu.zhinengsuo.value().equals(info.getCa())
							|| DeviceInfo.CaMenu.hongwaizhuanfaqi.value().equals(info.getCa())
							|| DeviceInfo.CaMenu.zhujifmq.value().equals(info.getCa())) {
						//健康采集，智能锁，主机, ,数据采集设备,主机遥控器设备,主机内置蜂鸣器设备 都不能应用于场景的联动控制
					} else {
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
				devices.add(deviceInfoses.get(i));
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
						devices.add(deviceInfo);
						selectDevId.add(deviceInfo);
					} else {
						selectDevId.remove(deviceInfo);
						devices.remove(deviceInfo);
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
		for (int i = 0; i < devices.size(); i++) {
			if (!selectDevId.contains(devices.get(i))) {
				devices.remove(i);
			}
		}
//		for(int j=0;j<devices.size();j++){
//			for(int k=devices.size()-1;k>j;k--){
//				if(devices.get(k).getId()==devices.get(j).getId()){
//					devices.remove(j);
//				}
//			}
//		}
		
		Log.e("设备", devices.toString());
		intent.putExtra("devices", (Serializable) devices);
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
