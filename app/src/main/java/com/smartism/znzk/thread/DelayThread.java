package com.smartism.znzk.thread;

public class DelayThread extends Thread {
	int delayTime;
	OnRunListener onRunListener;

	public DelayThread(int delayTime, OnRunListener onRunListener) {
		this.delayTime = delayTime;
		this.onRunListener = onRunListener;
	}

	public void run() {
		try {
			Thread.sleep(delayTime);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		onRunListener.run();
	}

	public static interface OnRunListener {
		public void run();
	}
}
