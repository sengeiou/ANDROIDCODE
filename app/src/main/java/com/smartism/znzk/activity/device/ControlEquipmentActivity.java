package com.smartism.znzk.activity.device;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.DeviceInfo;

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


public class ControlEquipmentActivity extends ActivityParentActivity {

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

		deviceInfoses = (List<DeviceInfo>) intent
				.getSerializableExtra("deviceInfoses");
		Cursor cursor = DatabaseOperator
				.getInstance(ControlEquipmentActivity.this)
				.getWritableDatabase()
				.rawQuery("select * from DEVICE_STATUSINFO order by sort desc",
						new String[] {});
		deviceInfos = new ArrayList<DeviceInfo>();
		if (cursor != null && cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				DeviceInfo info = DatabaseOperator.getInstance(
						getApplicationContext()).buildDeviceInfo(cursor);
				if (info.getCak().equals("control") && !"502R".equals(info.getType())&&!info.getCak().equals("surveillance")) {
					deviceInfos.add(info);
				}

			}
			cursor.close();
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
		public View getView(final int position, View convertView,
				ViewGroup parent) {

			ViewHolder holder = null;
			if (null == convertView) {
				holder = new ViewHolder();
				convertView = View.inflate(context,
						R.layout.all_equipment_list_item, null);
				holder.cb_equipment = (CheckBox) convertView
						.findViewById(R.id.cb_equipment);
				holder.tv_equipment = (TextView) convertView
						.findViewById(R.id.tv_equipment);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			// devices.clear();
			final DeviceInfo deviceInfo = deviceInfos.get(position);
			/*holder.tv_equipment.setText(deviceInfo.getName()
					+ deviceInfo.getType());
*/
			if(deviceInfo!=null&&deviceInfo.getType()!=null&&!deviceInfo.getType().equals("")){
				   holder.tv_equipment.setText(deviceInfo.getName() +":"+deviceInfo.getType());
				}else{
					holder.tv_equipment.setText(deviceInfo.getName());
				}
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
						// Toast.makeText(ControlEquipmentActivity.this,
						// deviceInfo.getCak()+"", 1).show();

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
				i--;
				
			}
		}
//		for(int j=0;j<devices.size();j++){
//			for(int k=devices.size()-1;k>j;k--){
//				if(devices.get(k).getId()==devices.get(j).getId()){
//					devices.remove(j);
//				}
//			}
//		}
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
