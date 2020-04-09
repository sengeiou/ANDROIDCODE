package com.smartism.znzk.activity.alert;

import java.io.File;
import java.util.LinkedList;

import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

public class AudioSettingActivity extends ActivityParentActivity {

	// 所有系统提示声音文件名
	private LinkedList<String> list_audio;
	private ListView lv_audio_setting;
	private MyAdapter adapter;
	private MediaPlayer player;
	private String mode;

	private String devId;


	String path_select;

	private SharedPreferences sp;

	// String alarm="/system/media/audio/alarms/Creamy.ogg";
	// String notification="/system/media/audio/notifications/Bongo.ogg";

	String folder_alarm = "/system/media/audio/alarms/";
	String folder_notification = "/system/media/audio/notifications/";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sp = getApplicationContext().getSharedPreferences(Constant.SP_NAME,
				Context.MODE_MULTI_PROCESS);
		initView();
		initValue();
		initEvent();
	}

	private void initView() {
		setContentView(R.layout.activity_alarm_audio_setting);
		lv_audio_setting = (ListView) findViewById(R.id.lv_audio_setting);
	}

	private void initValue() {

		mode = getIntent().getStringExtra("mode");
		// Log.e("aaa", "进来AudioSettingActivity " + mode);
		devId = getIntent().getStringExtra("devId");
		list_audio = new LinkedList<String>();

		// 从单个设备的设置按钮进入此activity
		if (devId != null) {

			if (mode.contentEquals("device")) {
				File[] files = new File(folder_alarm).listFiles();
				for (int i = 0; i < files.length; i++) {
					File file = files[i];
					list_audio.add(file.getName());
				}
			} else if (mode.contentEquals("message")) {
				File[] files = new File(folder_notification).listFiles();
				for (int i = 0; i < files.length; i++) {
					File file = files[i];
					list_audio.add(file.getName());
				}
			}
			adapter = new MyAdapter();
			lv_audio_setting.setAdapter(adapter);
		} else {

			if (mode.contentEquals("device")) {
				File[] files = new File(folder_alarm).listFiles();
				for (int i = 0; i < files.length; i++) {
					File file = files[i];
					list_audio.add(file.getName());
				}
			} else if (mode.contentEquals("message")) {
				File[] files = new File(folder_notification).listFiles();
				for (int i = 0; i < files.length; i++) {
					File file = files[i];
					list_audio.add(file.getName());
				}
			}
			adapter = new MyAdapter();
			lv_audio_setting.setAdapter(adapter);
		}
	}

	private void initEvent() {

		lv_audio_setting.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// 单个设备设置按钮进入此activity
				if (devId != null) {
					if (mode.contentEquals("device")) {
						dcsp.putInt(devId + Constant.ORDER_ALARM_SONG, arg2).commit();
					}
					// 短信提示音
					else if (mode.contentEquals("message")) {
						dcsp.putInt(devId + Constant.ORDER_NOTIFICATION_SONG, arg2).commit();
					}
					// 设备警告音
					if (mode.contentEquals("device")) {
						// 如果第一条则保存空值 表示默认
						if (arg2 == 0) {
							sp.edit().remove(devId  + Constant.PATH_ALARM_SONG).commit();
						} else {
							path_select = folder_alarm + "/" + list_audio.get(arg2);
							sp.edit()
									.putString(devId + Constant.PATH_ALARM_SONG, path_select)
									.commit();
						}
					}
					// 短信提示音
					else if (mode.contentEquals("message")) {
						if (arg2 == 0) {
							sp.edit().remove(devId+Constant.PATH_NOTIFICATION).commit();
						} else {
							path_select = folder_notification + "/" + list_audio.get(arg2);
							sp.edit().putString(devId +  Constant.PATH_NOTIFICATION,
									path_select).commit();
						}
					}
					// 从设置界面进入此activity======================================================================================================
				} else {
					// 设备警告音
					if (mode.contentEquals("device")) {
						dcsp.putInt(Constant.ORDER_ALARM_SONG, arg2).commit();
					}
					// 短信提示音
					else if (mode.contentEquals("message")) {
						dcsp.putInt(Constant.ORDER_NOTIFICATION_SONG, arg2).commit();
					}

					// 设备警告音
					if (mode.contentEquals("device")) {
						if (arg2 == 0) {
							sp.edit().remove(Constant.PATH_ALARM_SONG).commit();
						} else {
							path_select = folder_alarm + "/" + list_audio.get(arg2);
							sp.edit().putString(Constant.PATH_ALARM_SONG, path_select).commit();
						}

					}
					// 短信提示音
					else if (mode.contentEquals("message")) {

						if (arg2 == 0) {
							sp.edit().remove(Constant.PATH_NOTIFICATION).commit();
						} else {
							path_select = folder_notification + "/" + list_audio.get(arg2);
							sp.edit().putString(Constant.PATH_NOTIFICATION, path_select).commit();
						}
					}
				}
				adapter.notifyDataSetChanged();
				if (arg2 == 0) {
					if (player != null) {
						player.stop();
					}
				} else {
					if (player == null) {
						player = new MediaPlayer();
					} else {
						player.reset();
					}
					try {
						player.setDataSource(path_select);
						player.prepare();
						player.start();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	private class MyAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			if (list_audio == null) {
				return 0;
			}
			return list_audio.size();
		}

		@Override
		public Object getItem(int position) {
			if (list_audio == null || list_audio.size() <= position) {
				return null;
			}
			return list_audio.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			viewHolder holder;
			if (convertView == null) {
				holder = new viewHolder();
				convertView = View.inflate(getApplicationContext(), R.layout.item_audio_setting, null);
				holder.SongName = (TextView) convertView.findViewById(R.id.tv_notification_item);
				holder.selectBtn = (RadioButton) convertView.findViewById(R.id.rb_notification_item);
				convertView.setTag(holder);
			} else {
				holder = (viewHolder) convertView.getTag();
			}
			holder.SongName.setText(getName(list_audio.get(position)));

			// 单个设备铃声显示
			if (devId != null) {
				if (mode.contentEquals("device")) {
					holder.selectBtn.setChecked(dcsp.getInt(devId + Constant.ORDER_ALARM_SONG, 0) == position);
				} else if (mode.contentEquals("message")) {
					holder.selectBtn.setChecked(dcsp.getInt(devId + Constant.ORDER_NOTIFICATION_SONG, 0) == position);
				}
			} else {
				// 全局铃声设置
				// 设备警告音
				if (mode.contentEquals("device")) {
					holder.selectBtn.setChecked(dcsp.getInt(Constant.ORDER_ALARM_SONG, 0) == position);
				}
				// 短信提示音
				else if (mode.contentEquals("message")) {
					holder.selectBtn.setChecked(dcsp.getInt(Constant.ORDER_NOTIFICATION_SONG, 0) == position);
				}
			}

			if (position == 0) {
				TextView SongName = (TextView) convertView.findViewById(R.id.tv_notification_item);
				SongName.setText(getString(R.string.follow_the_system));
			}

			return convertView;
		}
	}

	public class viewHolder {
		public TextView SongName;
		public RadioButton selectBtn;
	}

	// 去掉文件名后缀
	public String getName(String nameWithFix) {
		int dot = nameWithFix.lastIndexOf('.');
		return nameWithFix.substring(0, dot);
	}

	@Override
	protected void onDestroy() {
		if (player != null) {
			player.release();
		}
		super.onDestroy();
	}

	public void back(View v) {
		finish();
	}

}
