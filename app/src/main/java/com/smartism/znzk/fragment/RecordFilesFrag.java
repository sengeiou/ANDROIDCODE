package com.smartism.znzk.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.p2p.core.P2PHandler;
import com.p2p.core.P2PView;
import com.smartism.znzk.R;
import com.smartism.znzk.adapter.camera.RecordFileProvider;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.camera.P2PConnect;
import com.smartism.znzk.activity.camera.PlayBackActivity;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.domain.camera.RecordFile;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.util.camera.OnLoadMoreListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import me.drakeet.multitype.Items;
import me.drakeet.multitype.MultiTypeAdapter;

public class RecordFilesFrag extends BaseFragment implements OnClickListener , SwipeRefreshLayout.OnRefreshListener{
	private Context mContext;
	private Contact contact;
	private RecyclerView rcRecordfiles;
	private MultiTypeAdapter adapter;
	private Items items;
	private String deviceId;
	private String devicePwd;
	private boolean firstLoad = true;
	private int count = 0;
	private LinearLayoutManager mLayoutManager;
	private SwipeRefreshLayout swipeRefreshLayout;
	Date startDate = new Date(0);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mContext = getActivity();
//		P2PHandler.getInstance().reject();
	}

	public RecordFilesFrag() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.activity_recordfiles, container,
				false);
		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		contact = (Contact) getArguments().getSerializable("contact");
		deviceId = contact.contactId;
		devicePwd = P2PHandler.getInstance().EntryPassword(contact.contactPassword);
		rcRecordfiles = (RecyclerView) view.findViewById(R.id.rc_recordfiles);
		swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.frush_file);
		init();
		items = new Items();
		adapter = new MultiTypeAdapter(items);
		RecordFileProvider recordFileProvider = new RecordFileProvider();
		adapter.register(RecordFile.class, recordFileProvider);
		recordFileProvider.setOnItemClickListner(new RecordFileProvider.OnItemClickListner() {
			@Override
			public void onItemClick(int position, RecordFile recordFile) {
//				Intent intent = new Intent(getActivity(), PlayBackActivity.class);
//				Log.e("===============",deviceId+"---"+devicePwd+"---"+recordFile);
//				Bundle bundle = new Bundle();
//				bundle.putSerializable("file", recordFile);
//				intent.putExtra("recordFile", bundle);
//				intent.putExtra("deviceId", deviceId);
//				intent.putExtra("devicePwd", devicePwd);
//				intent.putExtra("finish",isFinish);
//				startActivity(intent);
				rfile = recordFile;
				P2PConnect.setCurrent_state(P2PConnect.P2P_STATE_CALLING);
				P2PConnect.setCurrent_call_id(contact.contactId);
				P2PHandler.getInstance().playbackConnect(contact.contactId,contact.contactId, contact.getContactPassword(),recordFile.getName(), recordFile.getPosition(),0, 0, 896, 896, 0,0,0);
//				Intent intentCall = new Intent();
//				intentCall.setClass(getActivity(),
//						PlayBackActivity.class);
//				intentCall.putExtra("type",
//						Constants.P2P_TYPE.P2P_TYPE_PLAYBACK);
//				intentCall.putExtra("fileName", recordFile.getName());
//				intentCall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				startActivity(intentCall);
				isFinish= false;
			}
		});
		lodingFile();
		initEvent();
	}
	boolean isFinish = true;
	private void init() {
//		txLoading.setVisibility(View.GONE);
		mLayoutManager = new LinearLayoutManager(mContext);
		rcRecordfiles.setLayoutManager(new LinearLayoutManager(mContext));
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		//加载更多
		rcRecordfiles.addOnScrollListener(new OnLoadMoreListener() {
			@Override
			public void onLoadMore() {
				//获取上一次获取的数据中最后一位
				RecordFile file = (RecordFile) items.get(OnLoadMoreListener.itemCount - 1);
				String lastTime = file.getName().substring(6, 22);
				lastTime = lastTime.replace("_", " ");
				Date nextEndTime = null;
				try {
					nextEndTime = sdf.parse(lastTime);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				//因获取到数据是按照时间从近到远顺序排序
				//所以加载更多时startDate不用变,改变nextEndTime即可
				P2PHandler.getInstance().getRecordFiles(deviceId, devicePwd, startDate, nextEndTime,MainApplication.GWELL_LOCALAREAIP);
			}
		});
		regFilter();
	}

	private void regFilter() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.P2P.RET_GET_PLAYBACK_FILES);
		filter.addAction(Constants.P2P.P2P_REJECT);
		filter.addAction(Constants.P2P.P2P_ACCEPT);
		filter.addAction(Constants.P2P.P2P_READY);
		mContext.registerReceiver(receiver, filter);
	}
	private RecordFile rfile;
	BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Constants.P2P.RET_GET_PLAYBACK_FILES)) {
				//获取到的录像文件名字
				// 该数组中的文件名按照录像时间从近到远开始排序 且 每次获取到的nams的长度不大于64
				String[] names = (String[]) intent.getCharSequenceArrayExtra("recordList");
				byte option = intent.getByteExtra("option1", (byte) -1);
				if (option == 82) {
					if (swipeRefreshLayout.isRefreshing())
						swipeRefreshLayout.setRefreshing(false);
				}
				if (names.length > 0) {
					if (swipeRefreshLayout.isRefreshing())
						swipeRefreshLayout.setRefreshing(false);
					updateAdapter(names);
				}
			}
			if (intent.getAction().equals(Constants.P2P.P2P_ACCEPT)) {
				int[] type = intent.getIntArrayExtra("type");
				P2PView.type = type[0];
				P2PView.scale = type[1];
				Log.e("dxsTest", "监控数据接收:" + deviceId);
				P2PHandler.getInstance().openAudioAndStartPlaying(2);//打开音频并准备播放，calllType与call时type一致
			} else if (intent.getAction().equals(Constants.P2P.P2P_READY)) {
				Intent intentCall = new Intent();
				intentCall.setClass(getActivity(),
						PlayBackActivity.class);
				intentCall.putExtra("type",
						Constants.P2P_TYPE.P2P_TYPE_PLAYBACK);
				intentCall.putExtra("fileName", rfile.getName());
				intentCall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intentCall);
			} else if (intent.getAction().equals(Constants.P2P.P2P_REJECT)) {
				P2PHandler.getInstance().reject();
			} else if (intent.getAction().equals(Constants.P2P.RET_P2PDISPLAY)) {
				Log.e("monitor", "RET_P2PDISPLAY");
//				hanHeaderView.setVisibility(View.GONE);
			}
		}
	};

	private void updateAdapter(String[] names) {
//		txLoading.setVisibility(View.GONE);
		//因加载更多时，之前的最后一个文件和加载后第一个文件重复，故仅第一次加载时加载第一个文件
		if (firstLoad) {
			items.add(new RecordFile(count, names[0]));
			firstLoad = false;
		}
		for (int i = 1; i < names.length; i++) {
			items.add(new RecordFile(++count, names[i]));
		}
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mContext.unregisterReceiver(receiver);
		Intent it = new Intent();
		it.setAction(Constants.Action.CONTROL_BACK);
		mContext.sendBroadcast(it);
		hander.removeMessages(0);
	}

	private Handler.Callback mCallback = new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			if (msg.what==0)
			{
				if (swipeRefreshLayout.isRefreshing())
					swipeRefreshLayout.setRefreshing(false);
//				Toast.makeText(mContext,mContext.getResources().getString(R.string.activity_recordfile_null),Toast.LENGTH_SHORT).show();
			}
			return false;
		}
	};
	private Handler hander = new WeakRefHandler(mCallback);

	private void lodingFile(){
		if (TextUtils.isEmpty(deviceId) || TextUtils.isEmpty(devicePwd)) {
			Toast.makeText(mContext, "99999", Toast.LENGTH_SHORT).show();
		} else {
//			txLoading.setVisibility(View.VISIBLE);
//			txLoading.setText(R.string.loading);
			devicePwd = P2PHandler.getInstance().EntryPassword(devicePwd);//经过转换后的设备密码
			Date endDate = new Date(System.currentTimeMillis());

			//获取录像列表
			P2PHandler.getInstance().getRecordFiles(deviceId, devicePwd, startDate, endDate,MainApplication.GWELL_LOCALAREAIP);
			rcRecordfiles.setAdapter(adapter);
			new Timer().schedule(new TimerTask() {
				@Override
				public void run() {
					if (items.size() == 0) {
						hander.sendEmptyMessage(0);
					}
				}
			}, 8000);
		}
	}
	@Override
	public void onClick(View v) {

	}

	@Override
	public void onStop() {
		super.onStop();
		hander.removeMessages(0);
	}

	private void initEvent() {
		swipeRefreshLayout.setOnRefreshListener(this);
	}
	@Override
	public void onRefresh() {
		items.clear();
		firstLoad = true;
		lodingFile();
	}
}
