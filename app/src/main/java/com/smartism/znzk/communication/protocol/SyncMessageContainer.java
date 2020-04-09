package com.smartism.znzk.communication.protocol;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.smartism.znzk.communication.protocol.SyncMessage.CodeMenu;
import com.smartism.znzk.communication.protocol.SyncMessage.CommandMenu;
import com.smartism.znzk.util.LogUtil;


public class SyncMessageContainer {
	private static final String TAG = "SyncMessageContainer";
	private static volatile SyncMessageContainer _instance;
	
	private BlockingQueue<SyncMessage> sendQueue;
	
	private SyncMessageContainer() {
		LogUtil.i(TAG, "SyncMessageContainer singleton construct......");
		sendQueue = new LinkedBlockingQueue<SyncMessage>();
	}
	
	public static SyncMessageContainer getInstance() {
		if (_instance == null) {
			synchronized (SyncMessageContainer.class) {
				if (_instance == null) {
					_instance = new SyncMessageContainer();
				}
			}
		}
		return _instance;
	}
	
	public void produceSendMessage(SyncMessage message) {
		LogUtil.i(TAG, "SyncMessageContainer singleton send a message to serverqueue..command:"+message.getCommand());
		sendQueue.add(message);
	}
	
	public void sendMessageToServer(CommandMenu c,CodeMenu code,long did,byte[] b) {
		SyncMessage message1 = new SyncMessage();
		message1.setCommand(c.value());
		message1.setCode(code.value());
		message1.setDeviceid(did);
		message1.setSyncBytes(b==null?new byte[]{}:b);
		SyncMessageContainer.getInstance().produceSendMessage(message1);
	}
	
	public SyncMessage consumeSendMessage() throws InterruptedException {
		return sendQueue.take();
	}
}
