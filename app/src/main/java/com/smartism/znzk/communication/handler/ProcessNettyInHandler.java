package com.smartism.znzk.communication.handler;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.smartism.znzk.communication.connector.SyncClientNettyConnector;
import com.smartism.znzk.communication.data.SyncDataDispatcher;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.LogUtil;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 *
 * Netty TCP client handler class
 *
 * 处理连接、收到数据等业务逻辑
 *
 * 2016年10月15日  by 王建
 */
public class ProcessNettyInHandler extends ChannelInboundHandlerAdapter {
	private static final String TAG = "NettyInBoundHandler";
	@SuppressWarnings("unused")
	private Context context;

	public ProcessNettyInHandler(Context context) {
		this.context = context;
	}

	/**
	 * 收到服务器的包
	 * @param ctx
	 * @param message
	 * @throws Exception
     */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
		super.channelRead(ctx,message);
		if (ctx == SyncClientNettyConnector.getInstance().getCtx()) {
			SyncMessage syncMessage = (SyncMessage) message;
			SyncDataDispatcher.getInstance(context).dispatch(syncMessage);
		}else{
			Log.i(TAG,"TCP收到不是当前连接的包。。。有时候会有问题。。看到此日志需要检查是否符合逻辑");
			LogUtil.e(context,"war","收到非当前TCP连接的包 - 可能是APP创建了多个连接需要定位问题");
		}
	}

	/**
	 * 连接建立成功
	 * @param ctx
	 * @throws Exception
     */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		SyncClientNettyConnector.getInstance().setCtx(ctx);
		SyncClientNettyConnector.getInstance().login();
	}

	/**
	 * 连接出现异常 会主动调用channelInactive方法
	 * @param ctx
	 * @param cause
	 * @throws Exception
     */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		Log.e(TAG,"连接异常",cause);
		super.exceptionCaught(ctx,cause);
	}

	/**
	 * 连接已断开
	 * @param ctx
	 * @throws Exception
     */
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		if (ctx == SyncClientNettyConnector.getInstance().getCtx()){
			context.sendBroadcast(new Intent(Actions.CONNECTION_ING));
			SyncClientNettyConnector.getInstance().setCtx(null);
		}
	}
}
