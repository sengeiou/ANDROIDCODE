package com.smartism.znzk.activity.camera;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hdl.udpsenderlib.UDPResult;
import com.jwkj.soundwave.ResultCallback;
import com.jwkj.soundwave.SoundWaveSender;
import com.jwkj.soundwave.bean.NearbyDevice;
import com.lib.FunSDK;
import com.lsemtmf.genersdk.tools.commen.ToastTools;
import com.mediatek.elian.ElianNative;
import com.smartism.znzk.R;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.global.FList;
import com.smartism.znzk.global.NpcCommon;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.ActivityTaskManager;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.util.camera.T;
import com.smartism.znzk.util.camera.UDPHelper;
import com.smartism.znzk.util.webviewimage.PermissionUtil;
import com.smartism.znzk.view.ImageDrawable;
import com.smartism.znzk.view.zbarscan.ScanCaptureActivity;
import com.smartism.znzk.xiongmai.lib.funsdk.support.FunSupport;
import com.smartism.znzk.xiongmai.lib.funsdk.support.OnFunDeviceWiFiConfigListener;
import com.smartism.znzk.xiongmai.lib.funsdk.support.models.FunDevice;
import com.smartism.znzk.xiongmai.lib.funsdk.support.utils.MyUtils;
import com.smartism.znzk.xiongmai.lib.funsdk.support.utils.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * wifi添加进度部分
 * @author 2016年08月05日 update by 王建
 *
 */

public class AddWaitActicity extends BaseActivity implements OnClickListener,OnFunDeviceWiFiConfigListener {
	final String TAG = AddWaitActicity.class.getSimpleName();
	private boolean isNeedSendWave = true;//是否需要发送声波，没有接到正确数据之前都需要发送哦
	WifiManager mWifiManager ;
	private ImageView ivBacke;
	private Context mContext;
	private TextView tv_title;
	private boolean isReceive = false;
	private String ssid, pwd;
	private int i;
	private int isCameraList;
	private Thread mThread = null;
	boolean mDone = true;
	public UDPHelper mHelper;
	byte type;
	int mLocalIp;
	ElianNative elain;
	private boolean isSendWifiStop = true;
	private boolean isTimerCancel = true;
	private boolean isNeedSendWifi = true;// 二维码页面返回时不需要发包
	private long TimeOut;
	private ImageDrawable imgAnim;

	BroadcastReceiver mWifiBroadcast  = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			boolean isSucess = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED,false);
			switch (action){
				case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
					List<ScanResult> list = mWifiManager.getScanResults();
					if(list!=null&&list.size()>0){
						ScanResult temp = null;
						Log.v(TAG,"周围Wifi个数:"+list.size());
						//扫描到了,判断用户输入的Wifi是否可用
						for(ScanResult scanResult:list){
							Log.v(TAG,scanResult.SSID);
							if(scanResult.SSID.equals(ssid)){
								temp = scanResult;
							}
						}
						startQuickSetting(temp);
					}
					break;
			}
		}
	};


	private Handler.Callback mCallback = new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what){
				//扫描Wifi
				case 83:
					//雄迈,接下来判断周围该Wifi信息
					boolean startSucess = mWifiManager.startScan();//开始扫描周围Wifi
					if(!startSucess){
						//由于某些原因扫描失败，比如权限
						Log.v(TAG,"Wifi扫描请求失败...");
					}else{
						Log.v(TAG,"发出扫描周围Wifi请求...");
					}
					break;
			}
			return false;
		}
	};
	private Handler mHandler = new WeakRefHandler(mCallback);

	//WifiManager.MulticastLock lock;
	static {
		System.loadLibrary("elianjni");
	}

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		Window win = getWindow();
		win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		setContentView(R.layout.activity_add_waite);

		mContext = this;
		//雄迈部分
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		//注册Wifi扫描广播接收器
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		registerReceiver(mWifiBroadcast,intentFilter);

		if(MainApplication.app.getAppGlobalConfig().isShowAddXMCamera()){
			//支持雄迈，才开启雄迈配网
			if(!PermissionUtil.isPermissionValid(this,Manifest.permission.ACCESS_COARSE_LOCATION)){
				Toast.makeText(this,"需要位置权限", Toast.LENGTH_LONG).show();
				ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},99);
			}else{
				Log.v(TAG,"具备访问位置的权限");
				mHandler.sendEmptyMessage(83);//发送扫描Wifi消息
			}
			FunSupport.getInstance().registerOnFunDeviceWiFiConfigListener(this);//注册Wifi配置成功与否监听器
		}

		//lock = manager.createMulticastLock("localWifi");
		ssid = getIntent().getStringExtra("ssidname");
		pwd = getIntent().getStringExtra("wifiPwd");
		type = getIntent().getByteExtra("type", (byte) -1);
		mLocalIp = getIntent().getIntExtra("LocalIp", -1);
		i = getIntent().getIntExtra("int", 0);
		isCameraList = getIntent().getIntExtra("isCameraList", 0);
		isNeedSendWifi = getIntent().getBooleanExtra("isNeedSendWifi", true);
		initUI();
		if (isNeedSendWifi) {
			TimeOut = 110 * 1000;
			//雷达设置wifi
			excuteTimer();
		} else {
			TimeOut = 60 * 1000;
			tv_title.setText(getResources().getString(
					R.string.qr_code_add_device));
		}
//        lock.acquire();
		mHelper = new UDPHelper(9988);
		//设置回调,监听设置wifi的情况
		listen();
		//设置定时器在规定时间内没有回应时调用
		mHandler.postDelayed(mrunnable, TimeOut);
		//设置监听回调
		mHelper.StartListen();
		sendSoundWave();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode){
			case 99:
				if(grantResults[0]==PackageManager.PERMISSION_DENIED){
					Log.v(TAG,"没有同意访问位置权限");
					ToastTools.short_Toast(this,"您拒绝了访问位置的权限，设备配置Wifi可能会失败");
					if(!PermissionUtil.isPermissionValid(this,Manifest.permission.ACCESS_COARSE_LOCATION)){
						Toast.makeText(this,"需要位置权限", Toast.LENGTH_LONG).show();
						ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},99);
					}
				}else{
					mHandler.sendEmptyMessage(83);//发送扫描Wifi消息
				}
				break;
		}
	}

	//雄迈对Wifi处理
	private void startQuickSetting(ScanResult scanResult){
		try {
			if ( null == scanResult ) {
				ToastTools.short_Toast(this,"当前Wifi信息有误1");
				Log.v(TAG,"Wifi扫描结果为空,没有扫描到周围该Wifi信息");
				return;
			}
			WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
			DhcpInfo wifiDhcp = mWifiManager.getDhcpInfo();

			if ( null == wifiInfo ) {
				ToastTools.short_Toast(this,"当前Wifi信息有误2");
				return;
			}

			String ssid = wifiInfo.getSSID().replace("\"", "");
			if ( StringUtils.isStringNULL(ssid) ) {
				ToastTools.short_Toast(this,"当前Wifi信息有误3");
				return;
			}

			int pwdType = MyUtils.getEncrypPasswordType(scanResult.capabilities);
			String wifiPwd = pwd;

			if ( pwdType != 0 && StringUtils.isStringNULL(wifiPwd) ) {
				// 需要密码
				ToastTools.short_Toast(this,"当前Wifi信息有误4");
				return;
			}

			StringBuffer data = new StringBuffer();
			data.append("S:").append(ssid).append("P:").append(wifiPwd).append("T:").append(pwdType);

			String submask;
			if (wifiDhcp.netmask == 0) {
				submask = "255.255.255.0";
			} else {
				submask = MyUtils.formatIpAddress(wifiDhcp.netmask);
			}

			String mac = wifiInfo.getMacAddress();
			StringBuffer info = new StringBuffer();
			info.append("gateway:").append(MyUtils.formatIpAddress(wifiDhcp.gateway)).append(" ip:")
					.append(MyUtils.formatIpAddress(wifiDhcp.ipAddress)).append(" submask:").append(submask)
					.append(" dns1:").append(MyUtils.formatIpAddress(wifiDhcp.dns1)).append(" dns2:")
					.append(MyUtils.formatIpAddress(wifiDhcp.dns2)).append(" mac:").append(mac)
					.append(" ");

			FunSupport.getInstance().startWiFiQuickConfig(ssid,
					data.toString(), info.toString(),
					MyUtils.formatIpAddress(wifiDhcp.gateway),
					pwdType, 0, mac, -1);

//            FunWifiPassword.getInstance().saveWifiPassword(ssid, wifiPwd);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Timer mTimer;
	private int time;

	private void excuteTimer() {
		mTimer = new Timer();
		TimerTask mTask = new TimerTask() {
			@Override
			public void run() {
				if (time < 3) {
					sendWifiHandler.sendEmptyMessage(0);
				} else {
					sendWifiHandler.sendEmptyMessage(1);
				}
			}
		};
		mTimer.schedule(mTask, 500, 30 * 1000);
		isTimerCancel = false;
	}

	private void cancleTimer() {
		if (mTimer != null) {
			mTimer.cancel();
			isTimerCancel = true;
		}

	}

	private Handler sendWifiHandler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message arg0) {
			switch (arg0.what) {
				case 0:
					time++;
					sendWifi();
					Log.i("dxsnewTimer", "第" + time + "次发包时间:" + getTime());
					break;
				case 1:
					cancleTimer();
					Log.i("dxsnewTimer", "第" + time + "次停止计时器时间:" + getTime());
					break;
				case 2:
					stopSendWifi();
					Log.i("dxsnewTimer", "第" + time + "次停止发包时间:" + getTime());
					break;

				default:
					break;
			}
			return false;
		}
	});

	/**
	 * 发包 20秒后停止
	 */
	private void sendWifi() {
		if (elain == null) {
			elain = new ElianNative();
		}
		if (null != ssid && !"".equals(ssid)) {
			elain.InitSmartConnection(null, 1, 1);
			//发送信息链接摄像头
			elain.StartSmartConnection(ssid, pwd, "", type);
			Log.e("wifi_mesg", "ssidname=" + ssid + "--" + "wifipwd=" + pwd
					+ "--" + "type=" + type);
			isSendWifiStop = false;
		}
		sendWifiHandler.postDelayed(stopRunnable, 20 * 1000);
	}

	public Runnable stopRunnable = new Runnable() {
		@Override
		public void run() {
			sendWifiHandler.sendEmptyMessage(2);
		}
	};

	/**
	 * 停止发包
	 */
	private void stopSendWifi() {
		if (elain != null) {
			elain.StopSmartConnection();
			isSendWifiStop = true;
		}

	}

	private void initUI() {
		imgAnim = (ImageDrawable) findViewById(R.id.wait_anim);
		ivBacke = (ImageView) findViewById(R.id.back_btn);
		tv_title = (TextView) findViewById(R.id.title);
		ivBacke.setOnClickListener(this);

		AnimationDrawable animationDrawable = (AnimationDrawable) imgAnim.getBackground();
		animationDrawable.start();
	}

	void listen() {
		mHelper.setCallBack(new Handler(new Handler.Callback(){
								@Override
								public boolean handleMessage(Message msg) {
									switch (msg.what) {
										case UDPHelper.HANDLER_MESSAGE_BIND_ERROR:
											T.showShort(mContext, R.string.port_is_occupied);
											break;
										case UDPHelper.HANDLER_MESSAGE_RECEIVE_MSG:
											isReceive = true;
											if (isNeedSendWave) {
												synchronized (AddWaitActicity.this) {
													if (isNeedSendWave) {
														Bundle bundle = msg.getData();
														String contactId = bundle.getString("contactId");
														//判断摄像头是否有密码
														String frag = bundle.getString("frag");
														String ipFlag = bundle.getString("ipFlag");
														connectSucce(frag,ipFlag,contactId);
													}
												}
											}
											isNeedSendWave = false;
											break;
									}
									cancleTimer();
									return false;
								}
							})
		);
	}
	/*
	flag:设备是否自带密码
	ipFlag:设备的IP地址
	contactId:设备的序列号
	*/
	public void connectSucce(String frag, String ipFlag, String contactId) {
		Log.v(TAG,"声波接收到的ID:"+contactId);
		isNeedSendWave = false;
		mHelper.StopListen();
		SoundWaveSender.getInstance().stopSend();
		T.showShort(mContext, R.string.set_wifi_success);
		//获取摄像头的信息
		Intent it = new Intent();
		it.setAction(Constants.Action.RADAR_SET_WIFI_SUCCESS);
		sendBroadcast(it);
		FList flist = FList.getInstance();

		flist.updateOnlineState();
		flist.searchLocalDevice();


		Contact saveContact = new Contact();
		saveContact.contactId = contactId;
		saveContact.activeUser = NpcCommon.mThreeNum;

		Intent add_device = new Intent();
		if (MainApplication.app.getAppGlobalConfig().getVersion().equals(Actions.VersionType.CHANNEL_JUJIANG)) {
			add_device.setClass(mContext.getApplicationContext(), ScanCaptureActivity.class);
		} else {
			add_device.setClass(mContext, AddContactNextActivity.class);
			add_device.putExtra("isMainList",getIntent().getBooleanExtra("isMainList",false));
		}
		add_device.putExtra("isScan", true);
		add_device.putExtra("contact", saveContact);
		add_device.putExtra("int", i);
		add_device.putExtra("isCameraList", isCameraList);
		if (Integer.parseInt(frag) == Constants.DeviceFlag.UNSET_PASSWORD) {
			add_device.putExtra("isCreatePassword", true);
		} else {
			add_device.putExtra("isCreatePassword", false);
		}
		add_device.putExtra("isfactory", true);
		add_device.putExtra("ipFlag", ipFlag);
		startActivity(add_device);
		ActivityTaskManager.getActivityTaskManager().finishAllActivity();
		finish();
	}

	/**
	 * 开始发送声波
	 */

	private void sendSoundWave() {
		SoundWaveSender.getInstance()
				.with(this)
				.setWifiSet(ssid, pwd)
				.send(resultCallback);
	}

	ResultCallback resultCallback = new ResultCallback() {
		@Override
		public void onNext(UDPResult udpResult) {
			NearbyDevice device = NearbyDevice.getDeviceInfoByByteArray(udpResult.getResultData());
			device.setIp(udpResult.getIp());

			SoundWaveSender.getInstance().stopSend();//收到数据之后，需要发送
			isReceive = true;
			if (isNeedSendWave) {
				synchronized (AddWaitActicity.this) {
					if (isNeedSendWave) {
						NearbyDevice nearbyDevice = NearbyDevice.getDeviceInfoByByteArray(udpResult.getResultData());
						String contactId = String.valueOf(nearbyDevice.getDeviceId());
						//判断摄像头是否有密码
						String frag = String.valueOf(nearbyDevice.getPwdFlag());
						String ipFlag = String.valueOf(nearbyDevice.getIp());
						connectSucce(frag,ipFlag,contactId);
					}
				}
			}
			isNeedSendWave = false;
		}

		@Override
		public void onError(Throwable throwable) {
			super.onError(throwable);
			SoundWaveSender.getInstance().stopSend();//出错了就要停止任务，然后重启发送
		}

		/**
		 * 当声波停止的时候
		 */
		@Override
		public void onStopSend() {
			if (isNeedSendWave) {//是否需要继续发送声波
				sendSoundWave();
			} else {//结束了就需要将发送器关闭
				SoundWaveSender.getInstance().stopSend();
			}
		}
	};
	public Runnable mrunnable = new Runnable() {

		@Override
		public void run() {
			if (!isReceive) {
				if (isNeedSendWifi) {
					T.showShort(mContext, R.string.set_wifi_failed);
					Intent it = new Intent();
					it.setAction(Constants.Action.RADAR_SET_WIFI_FAILED);
					sendBroadcast(it);
					// 跳转
					ActivityTaskManager.getActivityTaskManager().finishAllActivity();
					finish();
				} else {
					T.showShort(mContext, R.string.set_wifi_failed);
					finish();
				}

			}
		}
	};

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
			case R.id.back_btn:
				isNeedSendWave = false;
				finish();
				break;
			default:
				break;
		}
	}


	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mHandler.removeCallbacks(mrunnable);
		sendWifiHandler.removeCallbacks(stopRunnable);
		isNeedSendWave = false;
		mHelper.StopListen();
		SoundWaveSender.getInstance().stopSend();
		if (!isTimerCancel) {
			cancleTimer();
		}
		//解注册广播接收器
		if(mWifiBroadcast!=null){
			unregisterReceiver(mWifiBroadcast);
		}
		if(MainApplication.app.getAppGlobalConfig().isShowAddXMCamera()){
			FunSDK.DevStopWifiConfig();
			FunSupport.getInstance().removeOnFunDeviceWiFiConfigListener(this);
		}
	}

	@Override
	public int getActivityInfo() {
		// TODO Auto-generated method stub
		return Constants.ActivityInfo.ACTIVITY_ADDWAITACTIVITY;
	}

	@Override
	protected int onPreFinshByLoginAnother() {
		return 0;
	}

	private String getTime() {
		String time = new SimpleDateFormat("HH-mm-ss").format(new Date());
		return time;
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (!isSendWifiStop) {
			stopSendWifi();
		}
	}

	@Override
	public void onDeviceWiFiConfigSetted(FunDevice funDevice) {
		if ( null != funDevice ) {
			//配置Wifi成功
			ToastTools.short_Toast(this,"给设备配置Wifi成功");
			FunSDK.DevStopAPConfig();//关闭快速Wifi配置
			Contact saveContact = new Contact();
			saveContact.contactId = funDevice.getDevSn();
			Intent add_device =new Intent(this,AddContactActivity.class);
			add_device.putExtra("isMainList",getIntent().getBooleanExtra("isMainList",false));
			add_device.putExtra("isScan", true);
			add_device.putExtra("contact", saveContact);
			add_device.putExtra("int", 9);//雄迈标识9
			add_device.putExtra("isCameraList", isCameraList);
			add_device.putExtra("isfactory", true);
			startActivity(add_device);
			ActivityTaskManager.getActivityTaskManager().finishAllActivity();
			finish();
		}else{
			Log.v(TAG,"雄迈配置Wifi失败");
		}
	}
}
