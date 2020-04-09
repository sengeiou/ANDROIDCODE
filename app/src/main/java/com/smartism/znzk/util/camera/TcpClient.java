package com.smartism.znzk.util.camera;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import com.smartism.znzk.global.Constants;
import com.smartism.znzk.application.MainApplication;



import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class TcpClient {
	Socket socket = null;
	int port = 10086;
	InetAddress ipAddress;
	byte[] data;
	private Handler mHandler;
	public static final int SEARCH_AP_DEVICE = 0x66;

	public TcpClient(byte[] data) {
		// TODO Auto-generated constructor stub
		this.data = data;
	}

	public void setIpdreess(InetAddress ipAddress) {
		this.ipAddress = ipAddress;
	}

	public void setCallBack(Handler handler) {
		this.mHandler = handler;
	}

	public void createClient() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					socket = new Socket(ipAddress, port);
					if (socket.isConnected()) {
						startListen();
						OutputStream os = socket.getOutputStream();
						os.write(data);
						os.flush();
					} else {
						int isconnect = 1;
						while (isconnect == 1) {
							try {
								socket = new Socket(ipAddress, port);
								Thread.sleep(100);
								if (socket.isConnected()) {
									startListen();
									OutputStream os = socket.getOutputStream();
									os.write(data);
									os.flush();
									isconnect = 0;
								}
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
		
	}

	public void startListen() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				OutputStream outputStream;
				try {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					InputStream in = socket.getInputStream();
					byte[] buffer = new byte[272];
					in.read(buffer);
					String s = "";
					for (int i = 0; i < buffer.length; i++) {
						s = s + buffer[i] + " ";
					}
					if (buffer.length < 0) {
						return;
					}
					if (buffer[0] == 3) {
						int result = bytesToInt(buffer, 4);
						Log.e("receivedata", "result=" + result);
						Intent i = new Intent();
						i.setAction(Constants.Action.SET_AP_DEVICE_WIFI_PWD);
						i.putExtra("result", result);
						MainApplication.app.sendBroadcast(i);
					} else if (buffer[0] == 1 && buffer[4] == 1
							&& buffer.length >= 20) {
						int id = bytesToInt(buffer, 16);
						int ip = bytesToInt(buffer, 12);
						if (null != mHandler) {
							Message msg = new Message();
							msg.what = SEARCH_AP_DEVICE;
							Bundle bundler = new Bundle();
							bundler.putString("contactId", String.valueOf(id));
							msg.setData(bundler);
							mHandler.sendMessage(msg);
						}
					}
					socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}).start();
	}

	public static int bytesToInt(byte[] src, int offset) {
		int value;
		value = (int) ((src[offset] & 0xFF) | ((src[offset + 1] & 0xFF) << 8)
				| ((src[offset + 2] & 0xFF) << 16) | ((src[offset + 3] & 0xFF) << 24));
		return value;
	}
}
