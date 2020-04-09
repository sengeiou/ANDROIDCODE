package com.smartism.znzk.thread;

import com.smartism.znzk.global.FList;
import com.smartism.znzk.util.camera.Utils;

import android.content.Context;
import android.util.Log;

public class MainThread {
	static MainThread manager;
	boolean isRun;
	private String version;
	private int serVersion;
	private static final long SYSTEM_MSG_INTERVAL = 60 * 60 * 1000;
	long lastSysmsgTime;
	private Main main;
	private SearchUpdate update;
	Context context;
	private static boolean isOpenThread;

	public MainThread(Context context) {
		manager = this;
		this.context = context;
	}

	public static MainThread getInstance(Context context) {
		if(manager==null){
			manager=new MainThread(context);
		}
		return manager;

	}

	class Main extends Thread {
		@Override
		public void run() {
			isRun = true;
			Utils.sleepThread(3000);
			while (isRun) {
				if (isOpenThread == true) {
					Log.e("my", "updateOnlineState");
					try {
						Log.e("leleTest", "updateOnlineState");
						FList.getInstance().updateOnlineState();
						FList.getInstance().searchLocalDevice();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					Utils.sleepThread(20 * 1000);
				} else {
					Utils.sleepThread(10 * 1000);
				}

			}
		}
	};
	
	class SearchUpdate extends Thread {
		@Override
		public void run() {
			Utils.sleepThread(3000);
			while (isRun) {
				if (isOpenThread == true) {
					try {
						FList.getInstance().getCheckUpdate();
					} catch (Exception e) {
						e.printStackTrace();
					}
					Utils.sleepThread(4*60*60 * 1000);
				} else {
					Utils.sleepThread(10 * 1000);
				}
			}
		}
	};

	public void go() {

		if (null == main || !main.isAlive()) {
			main = new Main();
			main.start();
		}
		
		if (null == update || !update.isAlive()) {
			update = new SearchUpdate();
			update.start();
		}
	}

	public void kill() {
		isRun = false;
		main = null;
		update=null;
	}

	public static void setOpenThread(boolean isOpenThread) {
		MainThread.isOpenThread = isOpenThread;
	}
}
