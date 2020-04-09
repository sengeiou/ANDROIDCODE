package com.smartism.znzk.fragment;

import android.content.Context;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.p2p.core.P2PView;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.domain.camera.RecordFile;

public class PlayBackFrag extends BaseFragment {
	private Context mContext;
	private Contact contact;
	public P2PView pView;
	Button btnPalyback;
	TextView txText;
	RelativeLayout rlP2pview;

	private RecordFile recordFile;

//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		// TODO Auto-generated method stub
//		super.onCreate(savedInstanceState);
//		mContext = getActivity();
//		contact = (Contact) getArguments().getSerializable("contact");
//		recordFile = (RecordFile) getArguments().getSerializable("file");
//	}
//
//	public PlayBackFrag() {
//		super();
//		// TODO Auto-generated constructor stub
//	}
//
//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container,
//							 Bundle savedInstanceState) {
//		// TODO Auto-generated method stub
//
//		View view = inflater.inflate(R.layout.activity_play_back, container,
//				false);
//		btnPalyback = (Button) view.findViewById(R.id.btn_palyback);
//		rlP2pview = (RelativeLayout) view.findViewById(R.id.rl_p2pview);
//		initp2pView(view);
//		regFilter();
//		btnPalyback.setOnClickListener(this);
//		return view;
//	}
//
//	public void initp2pView(View view) {
//		pView = (P2PView) view.findViewById(R.id.pview);
//		//7是设备类型(技威定义的)
//		//LAYOUTTYPE_TOGGEDER 录像回放连接命令和P2P_ACCEPT、P2P_READY、P2P_REJECT等命令在同一界面
//		//LAYOUTTYPE_SEPARATION 录像回放连接命令和P2P_ACCEPT、P2P_READY、P2P_REJECT等命令不在同一界面
//		initP2PView(7, P2PView.LAYOUTTYPE_TOGGEDER);
//		pView.halfScreen();
//	}
//
//	@Override
//	protected void onCaptureScreenResult(boolean isSuccess, int prePoint) {
//
//	}
//
//	@Override
//	protected void onVideoPTS(long videoPTS) {
//
//	}
//
//	@Override
//	protected void onP2PViewSingleTap() {
//
//	}
//
//
//	public void regFilter() {
//		IntentFilter filter = new IntentFilter();
//		filter.addAction(Constants.P2P.P2P_REJECT);
//		filter.addAction(Constants.P2P.P2P_ACCEPT);
//		filter.addAction(Constants.P2P.P2P_READY);
//		registerReceiver(mReceiver, filter);
//	}
//
//	BroadcastReceiver mReceiver = new BroadcastReceiver() {
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			if (intent.getAction().equals(Constants.P2P.P2P_ACCEPT)) {
//				int[] type = intent.getIntArrayExtra("type");
//				P2PView.type = type[0];
//				P2PView.scale = type[1];
////                txText.append("\n 监控数据接收");
//				Log.e("dxsTest", "监控数据接收:" + deviceId);
//				P2PHandler.getInstance().openAudioAndStartPlaying(2);//打开音频并准备播放，calllType与call时type一致
//			} else if (intent.getAction().equals(Constants.P2P.P2P_READY)) {
////                txText.append("\n 监控准备,开始监控");
//				Log.e("dxsTest", "监控准备,开始监控" + deviceId);
//				pView.sendStartBrod();
//			} else if (intent.getAction().equals(Constants.P2P.P2P_REJECT)) {
////                txText.append("\n 监控挂断");
//			}
//		}
//	};
//
//
//	public void play() {
//		String filename = recordFile.getName();
//		//录像回放连接
//		P2PHandler.getInstance().playbackConnect(deviceId,
//				devicePwd, filename, recordFile.getPosition(), 0, 0, 896, 896, 0);
//	}
//
//	@Override
//	public int getActivityInfo() {
//		return 33;
//	}
//
//	@Override
//	protected void onGoBack() {
//
//	}
//
//	@Override
//	protected void onGoFront() {
//
//	}
//
//	@Override
//	protected void onExit() {
//
//	}
//
//	@Override
//	public void onDestroy() {
//		super.onDestroy();
//		unregisterReceiver(mReceiver);
//	}
//
//	@Override
//	public void onClick(View v) {
//		play();
//	}

}
