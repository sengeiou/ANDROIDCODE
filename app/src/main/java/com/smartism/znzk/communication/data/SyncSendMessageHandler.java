package com.smartism.znzk.communication.data;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.smartism.znzk.R;
import com.smartism.znzk.communication.connector.SyncClientNettyConnector;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.LogUtil;

import java.io.Serializable;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

/**
 * 向服务器发送 TCP消息，常驻线程，不关闭
 * @author WJ
 */
public final class SyncSendMessageHandler{
	private static final String TAG = "SyncSendMessageHandler";

	private static SyncSendMessageHandler _instance;
	private Context context;
    private HandlerThread thread;

	private SyncSendMessageHandler(Context context) {
		this.context = context;
	}

	public synchronized static SyncSendMessageHandler getInstance(
			Context context) {
		if (_instance == null) {
			_instance = new SyncSendMessageHandler(context);
		}
		return _instance;
	}

	/**
	 * 启动同步消息处理线程
	 */
	public synchronized void start() {
		if(thread == null){
			thread = new HandlerThread();
			thread.setName("sync-message-send");
			JavaThreadPool.getInstance().excute(thread);
		}
		LogUtil.i(TAG, "----------------------Sync Send Message Handle Thread runned-------------------------");
	}

	/**
	 * 停止同步消息处理线程(私有方法 不提供关闭的能力)
	 */
	private void close() {
		LogUtil.i(TAG, "----------------------Sync Send Message Handle Thread close begin-------------------------");
        thread.stopThread();
        thread = null;
		LogUtil.i(TAG, "----------------------Sync Send Message Handle Thread close end-----------------------");
	}

	/**
	 *
	 * @author Administrator
	 *
	 */
	private class HandlerThread extends Thread {
		//每一个使用线程池的线程都需要增加一个私有变量表示自己是否需要继续运行。当为false时自动退出。因为线程池没有关闭某一个线程的方法
		private boolean isRun = true;

		private HandlerThread() {
			// UcApplication.getDbOperator().getWritableDatabase().beginTransaction();
		}

		@Override
		public void run() {
			LogUtil.i(TAG, "--- Sync Send Message Handle Thread run ---");
			while (isRun) {
				try {
					SyncMessage syncMsg = SyncMessageContainer.getInstance().consumeSendMessage();
					ChannelFuture future = SyncClientNettyConnector.getInstance().writeMessage(syncMsg);
					if (future!=null){
						final long command = syncMsg.getCommand();
						future.addListener(new ChannelFutureListener() {
							@Override
							public void operationComplete(ChannelFuture channelFuture) throws Exception {
								if (channelFuture.isSuccess() && channelFuture.isDone()) {
									Log.i("HandlerThread", "---send success---"+command);
								}else{
									Log.i("HandlerThread", "---send failed---"+command);
									if (command != SyncMessage.CommandMenu.rq_keepalive.value()
											&& command != SyncMessage.CommandMenu.rq_login.value()) {
										Intent intent = new Intent();
										intent.setAction(Actions.CONNECTION_FAILED_SENDFAILED);
										context.sendBroadcast(intent);
									}
								}
							}
						});
					}else{
						Log.i("HandlerThread", "---send failed---"+syncMsg.getCommand());
						if (syncMsg.getCommand() != SyncMessage.CommandMenu.rq_keepalive.value()
								&&  syncMsg.getCommand() != SyncMessage.CommandMenu.rq_login.value()){
							Intent intent = new Intent();
							intent.setAction(Actions.CONNECTION_FAILED_SENDFAILED);
							context.sendBroadcast(intent);
						}
					}
				} catch (InterruptedException e) {
					Log.w("HandlerThread", "---HandlerThread thread exception---",e);
				}
			}
			LogUtil.i(TAG, "--- Sync Send Message Handle Thread over ---");
		}

		/**
		 * 停止线程
		 */
		@SuppressWarnings("unused")
		private void stopThread() {
			isRun = false;
		}
	}
}
