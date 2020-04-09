package com.smartism.znzk.activity.device.add;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.hiflying.smartlink.OnSmartLinkListener;
import com.hiflying.smartlink.SmartLinkedModule;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.LogUtil;
import com.smartism.znzk.util.Util;
import com.xlwtech.util.XlwDevice;
import com.xlwtech.util.XlwDevice.XlwDeviceListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Timer;
import java.util.TimerTask;

public class AddDeviceWifiActivity extends ActivityParentActivity implements OnSmartLinkListener{
	
	private static final String TAG = "AddDeviceWifiActivity";
	private long m_iTickSmartConfigStart = 0;
	protected EditText mSsidEditText;
	protected EditText mPasswordEditText;
	protected Button mStartButton;
//	protected SnifferSmartLinker mSnifferSmartLinker; //有人的配置 屏蔽掉
	protected boolean mIsConncting = false;
	protected ProgressDialog mWaitingDialog;
	private BroadcastReceiver mWifiChangedReceiver;
	private CheckBox     check_pass;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
//		mSnifferSmartLinker = SnifferSmartLinker.getInstence();
		
		
		mWaitingDialog = new ProgressDialog(this);
		mWaitingDialog.setCancelable(false);
//		mWaitingDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
//			
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//			}
//		});
		mWaitingDialog.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				if (mIsConncting) {
					m_iTickSmartConfigStart = 0;
					XlwDevice.getInstance().SmartConfigStop();
//					mSnifferSmartLinker.setOnSmartLinkListener(null);
//					mSnifferSmartLinker.stop();
					mIsConncting = false;
					Toast.makeText(AddDeviceWifiActivity.this, getString(R.string.canceled), Toast.LENGTH_SHORT).show();
				}
				
			}
		});
		
		setContentView(R.layout.activity_add_device_wifi);

		check_pass = (CheckBox) findViewById(R.id.check_pass);

		check_pass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				String psw = mPasswordEditText.getText().toString();

				if (isChecked) {
					mPasswordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
				} else {
					mPasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
				}
				mPasswordEditText.setSelection(psw.length());
			}
		});
		mSsidEditText = (EditText) findViewById(R.id.editText_hiflying_smartlinker_ssid);
		mPasswordEditText = (EditText) findViewById(R.id.editText_hiflying_smartlinker_password);
		mPasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
		mStartButton = (Button) findViewById(R.id.button_hiflying_smartlinker_start);
		mSsidEditText.setText(getSSid());

		mStartButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(AddDeviceWifiActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				if(!mIsConncting){
					mIsConncting = true;
					mWaitingDialog.setMessage(getString(R.string.hiflying_smartlinker_waiting));
					mWaitingDialog.show();
					//新力维的配置 20S超时好像没卵用
					if (XlwDevice.getInstance().SmartConfigStart(mSsidEditText.getText().toString().trim(), mPasswordEditText.getText().toString().trim(), 20000) == false)
	            	{
						//直接配置失败。要有什么提示呢？？？
						LogUtil.i(TAG, "配置新力维wifi返回false");
	            	}
					m_iTickSmartConfigStart = 1;
				}
			}
		});
		
		mWifiChangedReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				if (networkInfo != null && networkInfo.isConnected()) {
					mSsidEditText.setText(getSSid());
					mPasswordEditText.requestFocus();
					mStartButton.setEnabled(true);
				}else {
					mSsidEditText.setText(getString(R.string.hiflying_smartlinker_no_wifi_connectivity));
					mSsidEditText.requestFocus();
					mStartButton.setEnabled(false);
					if (mWaitingDialog.isShowing()) {
						mWaitingDialog.dismiss();
					}
				}
			}
		};
		registerReceiver(mWifiChangedReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		initXLWWifiListener();
		smartConfigGetTimer();
	}
	
	private void initXLWWifiListener(){
		//OUR OUI EUI-48Address Block 78-9C-E7 00-00-00 through FF-FF-FF
		// 78-9C-E7 is the first 3 byte of our mac address, it is register in IEEE
		XlwDevice.getInstance().SetXlwDeviceListener(new XlwDeviceListener()
		{
			@Override
			public boolean onSmartFound(final String mac,final String ip, String version, String capability, String ext)
			{
				m_iTickSmartConfigStart = 0;
				XlwDevice.getInstance().SmartConfigStop();		//如果需要配置多个设备，可以不调用stop，直到timeOut时间到了为止，系统自动停驶smart。			
				Log.e(TAG,String.format("onSmartFound(count=(%d): mac=%s, ip=%s, ver=%s, cap=%s, ext=%s, passed=%d", 
						1, mac, ip, version, capability, ext, System.currentTimeMillis()-m_iTickSmartConfigStart));		
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						mWaitingDialog.setMessage(getString(R.string.hiflying_smartlinker_new_module_found,mac, ip));
					}
				});
				JavaThreadPool.getInstance().excute(new TCPClient(AddDeviceWifiActivity.this,ip));
				return true;
			}
			@Override
			public boolean onSearchFound(String mac, String ip, String version, String capability, String ext) {
				Log.e(TAG,String.format("onSearchFound(), mac=%s, ip=%s, version=%s, capability=%s, ext=%s", mac, ip, version, capability, ext));			
				return true;
			}

			@Override
			public void onStatusChange(String mac, int status)
			{
				if(status == 11){
					Log.e(TAG,String.format("启动完成！不知道靠不靠谱(): mac=%s, status=%d", mac, status));
				}
				Log.e(TAG,String.format("onDeviceChange(): mac=%s, status=%d", mac, status));
			}

			@Override
			public void onReceive(String mac, byte[] data, int length) 
			{
				//已收到
				String rsp = new String(data, 0, length);
				Log.e(TAG,String.format("Main onReceive(): mac=%s, length=%d, rsp=%s", mac, length, rsp));
			}
			
			@Override
			public void onSendError(String mac, int sn, int err) 
			{
				if (err == XlwDevice.ERR_BUSY)					Log.e(TAG,String.format("onSendError(): mac=%s, sn=%d, send busy", mac, sn));
				else if (err == XlwDevice.ERR_TIMER_OUT)		Log.e(TAG,String.format("onSendError(): mac=%s, sn=%d, send time out", mac, sn));
				else if (err == XlwDevice.ERR_MAC_INVALID)		Log.e(TAG,String.format("onSendError(): mac=%s, sn=%d, device mac invalid", mac, sn));
				else if (err == XlwDevice.ERR_DEVICE_OFFLINE)	Log.e(TAG,String.format("onSendError(): mac=%s, sn=%d, device offline", mac, sn));
				else if (err == XlwDevice.ERR_IP_NOT_EXIST)		Log.e(TAG,String.format("onSendError(): mac=%s, sn=%d, device not in local network", mac, sn));
				else if (err == XlwDevice.ERR_MAC_NOT_EXIST)	Log.e(TAG,String.format("onSendError(): mac=%s, sn=%d, device not in local network", mac, sn));
				else											Log.e(TAG,String.format("onSendError(): mac=%s, sn=%d, err=%d", mac, sn, err));
				m_iTickSmartConfigStart = 0;
			}
		});
	}
	
	private Timer myTimer;    
    private void smartConfigGetTimer()
    {
    	myTimer = new Timer();
    	myTimer.schedule( 
				new TimerTask() 
				{
					@Override
					public void run() 
					{	
						if (m_iTickSmartConfigStart > 0)
						{
							Log.e(TAG, "获取配置结果，第几次:"+m_iTickSmartConfigStart);
							m_iTickSmartConfigStart ++;
							XlwDevice.getInstance().SmartConfigProgressGet();
							if (m_iTickSmartConfigStart == 60) { //60S超时
								m_iTickSmartConfigStart = 0;
								XlwDevice.getInstance().SmartConfigStop();
								onTimeOut();
//								try {  //屏蔽有人的配置
//									//有人的配置
//									mSnifferSmartLinker.setOnSmartLinkListener(AddDeviceWifiActivity.this);
//									mSnifferSmartLinker.start(getApplicationContext(), mPasswordEditText.getText().toString().trim(), 
//											mSsidEditText.getText().toString().trim());
//								} catch (Exception e) {
//									LogUtil.i(TAG, "配置有人wifi模块错误");
//								}
							}
						}
					}
				} , 0, 1000);
    }
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
//		mSnifferSmartLinker.stop();
//		mSnifferSmartLinker.setOnSmartLinkListener(null);
		try {
			unregisterReceiver(mWifiChangedReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
		XlwDevice.getInstance().SmartConfigStop(); //停止wifi模块配置
		if (myTimer!=null) {
			myTimer.cancel(); //退出新力维模块配置结果获取
			myTimer = null;
		}
		super.onDestroy();
	}


	@Override
	public void onLinked(final SmartLinkedModule module) {
		// TODO Auto-generated method stub
		
		Log.w(TAG, "onLinked");
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				mWaitingDialog.setMessage(getString(R.string.hiflying_smartlinker_new_module_found,module.getMac(), module.getModuleIP()));
//				Toast.makeText(getApplicationContext(), getString(R.string.hiflying_smartlinker_new_module_found, module.getMac(), module.getModuleIP()), 
//						Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	@Override
	public void onCompleted() {
		
		Log.w(TAG, "onCompleted");
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				mWaitingDialog.setMessage(getString(R.string.hiflying_smartlinker_waiting_set));
				JavaThreadPool.getInstance().excute(new UDPClient(AddDeviceWifiActivity.this));
				// TODO Auto-generated method stub
//				Toast.makeText(getApplicationContext(), getString(R.string.hiflying_smartlinker_completed), 
//						Toast.LENGTH_SHORT).show();
//				mWaitingDialog.dismiss();
				mIsConncting = false;
			}
		});
	}


	@Override
	public void onTimeOut() {
		
		Log.w(TAG, "onTimeOut");
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Toast.makeText(getApplicationContext(), getString(R.string.hiflying_smartlinker_timeout), 
						Toast.LENGTH_SHORT).show();
				mIsConncting = false;
				mWaitingDialog.dismiss();
			}
		});
	}	

	private String getSSid(){

		WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
		if(wm != null){
			WifiInfo wi = wm.getConnectionInfo();
			if(wi != null){
				String ssid = wi.getSSID();
				if(ssid.length()>2 && ssid.startsWith("\"") && ssid.endsWith("\"")){
					return ssid.substring(1,ssid.length()-1);
				}else{
					return ssid;
				}
			}
		}

		return "";
	}
	public void back(View v){
		finish();
	}
}
/**
 * 此client引用activity时，采用的是弱引用，有利于内存释放
 * @author wangjian
 *
 */
class UDPClient implements Runnable {
	DatagramSocket client = null;
	WeakReference<AddDeviceWifiActivity> context = null;
	SocketAddress addr = null;
	String serverAddr = "";
	public UDPClient(AddDeviceWifiActivity context) {
		try {
			String server = context.getDcsp().getString(Constant.SYNC_DATA_SERVERS, "139.196.38.110:7777");
			serverAddr = server.substring(0, server.indexOf(":"));
			//新建一个DatagramSocket
			client = new DatagramSocket();
			client.setSoTimeout(5 * 1000);
			this.context = new WeakReference<AddDeviceWifiActivity>(context);
			AddDeviceWifiActivity activity = this.context.get();
			if (activity!=null) {
				String ip = Util.getWIFILocalIpAdress(activity);
				addr = new InetSocketAddress(InetAddress.getByName(ip.substring(0, ip.lastIndexOf("."))+".255"),48899);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		try {
			//往服务端发送消息
			sendCommand(client, "HF-A11ASSISTHREAD",true);
			
			//发送+ok进入连接状态
			sendCommand(client, "+ok",false);
			//发送设置
			String recvStr = sendCommand(client, "AT+NETP=TCP,Client,7777,"+serverAddr+"\n",false);
			int count = 0;
			while ((recvStr==null || "".equals(recvStr) || !"+ok".equals(recvStr.substring(0, 3))) && count <= 10) {
				count ++ ;
				Thread.sleep(1000);
				//发送设置
				recvStr = sendCommand(client, "AT+NETP=TCP,Client,7777,"+serverAddr+"\n",true);
			}
			//发送断开
			sendCommand(client, "AT+Q\n",true);
			final AddDeviceWifiActivity activity = context.get();
			if (activity!=null) {
				activity.mHandler.post(new Runnable() {
					public void run() {
						activity.setResult(11);
						Toast.makeText(activity, activity.getString(R.string.hiflying_smartlinker_completed), 
								Toast.LENGTH_SHORT).show();
						activity.mWaitingDialog.dismiss();
						activity.finish();
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private String sendCommand(DatagramSocket client,String command,boolean receive) throws Exception{
		byte[] sendBuf = command.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendBuf, sendBuf.length,addr);
		client.send(sendPacket);
		if (receive) {
			//接受服务端传来的消息
			byte[] recvBuf = new byte[500];
			DatagramPacket recvPacket = new DatagramPacket(recvBuf, recvBuf.length);
			client.receive(recvPacket);
			String recvStr = new String(recvPacket.getData(), 0,
					recvPacket.getLength());
			System.out.println("wifi主机传来消息:" + recvStr);
			return recvStr;
		}
		return null;
	}
}
/**
 * 此client引用activity时，采用的是弱引用，有利于内存释放
 * @author 王建
 *
 */
class TCPClient implements Runnable {
	Socket client = null;
	WeakReference<AddDeviceWifiActivity> context = null;
	SocketAddress addr = null;
	String serverAddr = "";
	public TCPClient(AddDeviceWifiActivity context,String ip) {
		try {
			String server = context.getDcsp().getString(Constant.SYNC_DATA_SERVERS, "139.196.38.110:7777");
			serverAddr = server.substring(0, server.indexOf(":"));
			client =new Socket();
			client.setKeepAlive(true);
			this.context = new WeakReference<AddDeviceWifiActivity>(context);
			AddDeviceWifiActivity activity = this.context.get();
			addr = new InetSocketAddress(ip,25001);
			if (activity!=null) {
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		try {
			Thread.sleep(1000);
			//往服务端发送消息
			//发送设置  增加xopt=3可以免去saveconfig和reboot两个指令
			String recvStr = sendCommand(client, "xcmd_req:1:cmd=sockset,id=10,mode=2,lport=0,rhost="+serverAddr+",rport=7777,xopt=3\r\n",true);
			int count = 0;
			while ((recvStr==null || "".equals(recvStr) || !"success".equals(recvStr)/*!recvStr.startsWith("xcmd_rsp:1:ret=1")*/) && count <= 5) {
				count ++ ;
				Thread.sleep(2000);
				//发送设置
				recvStr = sendCommand(client, "xcmd_req:1:cmd=sockset,id=10,mode=2,lport=0,rhost="+serverAddr+",rport=7777,xopt=3\r\n",true);
			}
			final AddDeviceWifiActivity activity = context.get();
//			if("success".equals(sendCommand(client, "xcmd_req:2:cmd=saveconfig,\r\n", true))){
//				if("success".equals(sendCommand(client, "xcmd_req:3:cmd=reboot,\r\n", true))){
//					if (activity!=null) {
//						activity.mHandler.post(new Runnable() {
//							public void run() {
//								activity.mIsConncting = false;
//								activity.setResult(11);
//								Toast.makeText(activity, activity.getString(R.string.hiflying_smartlinker_completed), 
//										Toast.LENGTH_SHORT).show();
//								activity.mWaitingDialog.dismiss();
//								activity.finish();
//							}
//						});
//					}
//				}
//			}
			if (activity!=null && count <= 5) {
				activity.mHandler.post(new Runnable() {
					public void run() {
						activity.mIsConncting = false;
						activity.setResult(11);
						Toast.makeText(activity, activity.getString(R.string.hiflying_smartlinker_completed), 
								Toast.LENGTH_SHORT).show();
						activity.mWaitingDialog.dismiss();
						activity.finish();
					}
				});
			}
			if (activity!=null && count > 5) {
				activity.mHandler.post(new Runnable() {
					public void run() {
						activity.mIsConncting = false;
						Toast.makeText(activity, activity.getString(R.string.hiflying_smartlinker_failed), 
							Toast.LENGTH_SHORT).show();
						activity.mWaitingDialog.dismiss();
						}
				});
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if (client!=null) {
				try {
					client.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	private String sendCommand(Socket client,String command,boolean receive){
		try {
			if (!client.isConnected()) {
				client.connect(addr, 2000);
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
			OutputStream oStream = client.getOutputStream();
			oStream.write(command.getBytes());
			oStream.flush();
			if (receive) {
				//接受服务端传来的消息
				String remess=reader.readLine();  
				Log.e("wifi","wifi主机传来消息:" + remess);
				if (remess!=null && !"".equals(remess) && remess.contains("ret=1")) {
					return "success";
				}
				return null;
			}
			return "success";
		} catch (Exception e) {
			Log.e("发送", e.getLocalizedMessage());
		}
		return null;
	}
}
